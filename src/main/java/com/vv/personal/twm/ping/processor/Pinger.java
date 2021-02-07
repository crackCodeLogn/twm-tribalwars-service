package com.vv.personal.twm.ping.processor;

import com.vv.personal.twm.ping.feign.HealthFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Vivek
 * @since 05/02/21
 */
public class Pinger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pinger.class);
    private final ExecutorService pingChecker;
    private final int pingTimeoutSeconds;
    private final int pingRetryCount;
    private final int pingRetryTimeoutSeconds;

    public Pinger(int pingTimeoutSeconds, int pingRetryCount, int pingRetryTimeoutSeconds) {
        this.pingTimeoutSeconds = pingTimeoutSeconds;
        this.pingRetryCount = pingRetryCount;
        this.pingRetryTimeoutSeconds = pingRetryTimeoutSeconds;

        this.pingChecker = Executors.newSingleThreadExecutor();
    }

    public boolean allEndPointsActive(HealthFeign... healthFeigns) {
        //check for end-points of rendering service and mongo-service
        int retry = 0;
        while (++retry <= pingRetryCount) {
            LOGGER.info("Attempting allEndPointsActive test sequence: {}", retry);
            AtomicBoolean allPingsPass = new AtomicBoolean(true);
            for (HealthFeign healthFeign : healthFeigns)
                if (!pingResult(createPingTask(healthFeign))) {
                    allPingsPass.set(false);
                    break;
                }
            if (allPingsPass.get()) return true;
            try {
                Thread.sleep(pingRetryTimeoutSeconds * 1000L);
            } catch (InterruptedException e) {
                LOGGER.error("Pinger interrupted whilst sleeping. ", e);
            }
        }
        return false;
    }

    private Callable<String> createPingTask(HealthFeign healthFeign) {
        return healthFeign::ping;
    }

    private boolean pingResult(Callable<String> pingTask) {
        Future<String> pingResultFuture = pingChecker.submit(pingTask);
        try {
            String pingResult = pingResultFuture.get(pingTimeoutSeconds, TimeUnit.SECONDS);
            LOGGER.info("Obtained '{}' as ping result for {}", pingResult, pingResult);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.warn("Timed out waiting on ping, task: {}", pingTask);
        }
        return false;
    }

    public void destroyExecutor() {
        if (!pingChecker.isShutdown())
            pingChecker.shutdown();
    }
}
