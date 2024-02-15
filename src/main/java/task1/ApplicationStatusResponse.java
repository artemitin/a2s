package task1;

import java.time.Duration;

public sealed interface ApplicationStatusResponse {
    record Failure(Duration lastRequestTime, int retriesCount) implements ApplicationStatusResponse {
        @Override
        public String toString() {
            return "Failure{" +
                    "lastRequestTime=" + lastRequestTime +
                    ", retriesCount=" + retriesCount +
                    '}';
        }
    }

    record Success(String id, String status) implements ApplicationStatusResponse {
        @Override
        public String toString() {
            return "Success{" +
                    "id='" + id + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
