package task1;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Быстрая проверка задачи, на полноценные тесты Junit не хватило времени
 */
public class Test {
    public static void main(String[] args) {
        Client client = new ClientImpl();
        Handler h = new HandlerImpl(client);
        ApplicationStatusResponse abc = h.performOperation("abc");
        System.out.println(abc);

    }

    private static class ClientImpl implements Client {

        @Override
        public Response getApplicationStatus1(String id) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(300));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Response.Success("abc", "111");
        }

        @Override
        public Response getApplicationStatus2(String id) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(200));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Response.Failure(null);
        }
    }
}
