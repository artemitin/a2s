package task2;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;


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
public abstract class HandlerImpl2 implements Handler {

    private final Client client;

    // можно настраивать кол-во потоков для максимального throughput
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public HandlerImpl2(Client client) {
        this.client = client;
    }

    @Override
    public void performOperation() {
        Event event = client.readData();
        List<Address> recipients = event.recipients();

        for (Address address : recipients) {
            scheduler.schedule(() -> sendWithRetry(address, event.payload()), 0, TimeUnit.MILLISECONDS);
        }
    }

    private void sendWithRetry(Address recipient, Payload payload) {
        Result result = client.sendData(recipient, payload);
        if (result == Result.REJECTED) {
            scheduler.schedule(() -> sendWithRetry(recipient, payload),
                    timeout().get(ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
        }
    }
}

