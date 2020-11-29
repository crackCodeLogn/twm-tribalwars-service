package com.vv.personal.twm.tribalwars.automation.config;

import com.google.gson.Gson;
import com.vv.personal.twm.tribalwars.automation.driver.AutomationDriver;
import com.vv.personal.twm.tribalwars.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Vivek
 * @since 29/11/20
 */
@Configuration
public class TribalWarsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TribalWarsConfiguration.class);

    @Value("${location.driver.gecko:/etc/WEBDRIVER/FIREFOX/geckodriver}")
    private String geckoDriverLocation;

    @Value("${tw.sso.location:/etc/TW/sso}")
    private String ssoLocation;

    @Lazy
    @Bean(destroyMethod = "shutDownDriver")
    public AutomationDriver driver() {
        return new AutomationDriver(geckoDriverLocation);
    }

    @Bean
    public Sso sso() {
        String ssoJson = FileUtil.readFileFromLocation(ssoLocation);
        Gson gson = new Gson();
        Sso sso = new Sso();
        try {
            sso = gson.fromJson(ssoJson, Sso.class);
        } catch (Exception e) {
            LOGGER.error("Failed to read data from {} for sso. ", ssoLocation, e);
        }
        return sso;
    }
}
