package task1;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandlerTest {

    @Test
    public void test() {
        Client client = new ClientImpl();
        Handler h = new HandlerImpl(client);
        ApplicationStatusResponse abc = h.performOperation("abc");
        assertEquals("Success{id='abc', status='abc'}", abc.toString());

    }

    private static class ClientImpl implements Client {

        @Override
        public Response getApplicationStatus1(String id) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(100));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Response.Success("abc", "111");
        }

        @Override
        public Response getApplicationStatus2(String id) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Response.Failure(null);
        }
    }
}
