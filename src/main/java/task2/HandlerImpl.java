package task2;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * У вас есть метод `readData` для получения порции непрерывных данных.
 * Данные нужно сразу отослать всем потребителям при помощи `sendData`.
 * <p>
 * Ваша задача написать метод `performOperation`,
 * который будет производить такую рассылку с максимальной пропускной способностью.
 * <p>
 * Технические детали
 * 1. Аргументы для `sendData` нужно брать из значения, возвращаемого `readData`
 * 2. Каждый адресат из списка `Event.recipients` должен получить данные `payload`
 * 3. Во время отправки данные могут быть:
 * * `Result.ACCEPTED` - приняты потребителем, операция отправки данных адресату `dest` считается завершённой
 * * `Result.REJECTED` - отклонены, операцию отправки следует повторить после задержки `timeout()`
 * 4. Метод `performOperation` должен обладать высокой пропускной способностью: события внутри `readData` **могут накапливаться**
 * <p>
 * В качестве ответа пришлите полную реализацию интерфейса `Handler` (со списком импортов).
 * В поле для ответа прикрепите ссылку на репозиторий с вашим решением.
 */
public abstract class HandlerImpl implements Handler {

    private static final int N_PRODUCERS = 1;
    private static final int N_CONSUMERS = 1;
    private static final int N_SENDERS = 5;

    private final Client client;

    //размер буфера для примера
    private final BlockingQueue<Event> buffer = new LinkedBlockingDeque<>(1000);
    // можно настраивать кол-во потоков для максимального throughput
    private final ExecutorService producers = Executors.newFixedThreadPool(N_PRODUCERS);
    private final ExecutorService consumers = Executors.newFixedThreadPool(N_CONSUMERS);
    private final ExecutorService senders = Executors.newFixedThreadPool(N_SENDERS);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public void performOperation() {
        for (int i = 0; i < N_PRODUCERS; i++) {
            producers.submit(this::produce);
        }

        for (int i = 0; i < N_CONSUMERS; i++) {
            consumers.submit(this::consume);
        }
    }

    // загружаем events в буфер
    private void produce() {
        Event event;
        while ((event = client.readData()) != null) {
            try {
                buffer.put(event);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void consume() {
        // осталось избежать вечного цикла
        while (true) {
            try {
                // загружаем из буфера
                Event event = buffer.take();
                List<Future<ResponseWrapper>> futures = event.recipients().stream()
                        .map(address -> senders.submit(
                                () -> new ResponseWrapper(client.sendData(address, event.payload()), address)))
                        .toList();

                // фильтруем неотправленные
                List<Address> retryAddresses = futures.stream()
                        .map(f -> {
                            try {
                                return f.get(10, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }).filter(r -> r.result() == Result.REJECTED)
                        .map(ResponseWrapper::address)
                        .toList();

                // запускаем ретрай
                Event retry = new Event(retryAddresses, event.payload());
                scheduler.schedule(() -> {
                    try {
                        buffer.put(retry);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, timeout().get(ChronoUnit.MILLIS), TimeUnit.of(ChronoUnit.MILLIS));

            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

record ResponseWrapper(Result result, Address address) {
}
