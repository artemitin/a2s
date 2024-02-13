package a2s1;

import java.time.Duration;

public class Task {
}

public sealed interface Response {
    record Success(String applicationStatus, String applicationId) implements Response {}
    record RetryAfter(Duration delay) implements Response {}
    record Failure(Throwable ex) implements Response {}
}

public sealed interface ApplicationStatusResponse {
    record Failure(@Nullable Duration lastRequestTime, int retriesCount) implements ApplicationStatusResponse {}
    record Success(String id, String status) implements ApplicationStatusResponse {}
}

public interface Handler {
    ApplicationStatusResponse performOperation(String id);
}

public interface Client {
    //блокирующий вызов сервиса 1 для получения статуса заявки
    Response getApplicationStatus1(String id);

    //блокирующий вызов сервиса 2 для получения статуса заявки
    Response getApplicationStatus2(String id);

}