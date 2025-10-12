package http;

import java.time.Instant;

public class RequestLimiter {
    private Instant nextRequestAllowed;
    private int delayMillis = 1000;

    public RequestLimiter(int maxRequestsPerSecond) {
        this.nextRequestAllowed = Instant.now();
        setMaxRequestsPerSecond(maxRequestsPerSecond);
    }

    public void setMaxRequestsPerSecond(int maxRequestsPerSecond) {
        this.delayMillis = 1000 / maxRequestsPerSecond;
    }

    public void waitForNextRequest() {
        if (nextRequestAllowed.isAfter(Instant.now())) {
            try {
                Thread.sleep(nextRequestAllowed.toEpochMilli() - Instant.now().toEpochMilli());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        updateNextRequestAllowed();
    }

    public void updateNextRequestAllowed() {
        nextRequestAllowed = Instant.now().plusMillis(delayMillis);
    }

    public void updateNextRequestAllowed(int secondsToWait) {
        nextRequestAllowed = Instant.now().plusSeconds(secondsToWait);
    }
}
