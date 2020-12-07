package com.vv.personal.twm.tribalwars.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 07/12/20
 */
@Configuration
public class HealthConfig {

    @Value("${timeout.ping:7}")
    private int pingTimeout;

    public int getPingTimeout() {
        return pingTimeout;
    }
}
