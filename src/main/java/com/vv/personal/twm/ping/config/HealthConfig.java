package com.vv.personal.twm.ping.config;

import com.vv.personal.twm.ping.processor.Pinger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Vivek
 * @since 07/12/20
 */
@Configuration
public class HealthConfig {

    @Value("${ping.timeout:7}")
    private int pingTimeout;

    @Value("${ping.retry.count:5}")
    private int pingRetryCount;

    @Value("${ping.retry.timeout:3}")
    private int pingRetryTimeout;

    @Scope("prototype")
    @Bean(value = "Pinger", destroyMethod = "destroyExecutor")
    public Pinger pinger() {
        return new Pinger(pingTimeout, pingRetryCount, pingRetryTimeout);
    }
}
