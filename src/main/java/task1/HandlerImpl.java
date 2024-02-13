package task1;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class HandlerImpl implements Handler {
    private final Client client;
    private final AtomicInteger retriesCount = new AtomicInteger(0);

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        CompletableFuture<ApplicationStatusResponse> clientResponse = CompletableFuture.anyOf(
                        CompletableFuture.supplyAsync(() -> getResponseWrapper(() -> client.getApplicationStatus1(id))),
                        CompletableFuture.supplyAsync(() -> getResponseWrapper(() -> client.getApplicationStatus2(id)))
                )
                .orTimeout(15, TimeUnit.SECONDS)
                .exceptionally(t -> CompletableFuture.completedStage(
                        new ApplicationStatusResponse.Failure(Duration.of(15, ChronoUnit.SECONDS), retriesCount.incrementAndGet())))
                .thenApply(response -> {
                    ResponseWrapper wrapper = (ResponseWrapper) response;
                    if (wrapper.response() instanceof Response.Success success) {
                        return new ApplicationStatusResponse.Success(id, success.applicationStatus());
                    } else {
                        return new ApplicationStatusResponse.Failure(wrapper.duration(), retriesCount.incrementAndGet());
                    }
                });
        return clientResponse.join();
    }

    private ResponseWrapper getResponseWrapper(Supplier<Response> supplier) {
        long start = System.currentTimeMillis();
        Response response = supplier.get();
        Duration duration = Duration.of(System.currentTimeMillis() - start, ChronoUnit.MILLIS);
        return new ResponseWrapper(response, duration);
    }
}

record ResponseWrapper(Response response, Duration duration) {
}
