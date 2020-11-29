package com.vv.personal.twm.tribalwars.automation.driver;

import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.vv.personal.twm.tribalwars.automation.constants.Constants.GECKO_WEBDRIVER;

/**
 * @author Vivek
 * @since 29/11/20
 */
public class AutomationDriver {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomationDriver.class);
    private final WebDriver driver;

    public AutomationDriver(String webDriverLocation) {
        System.setProperty(GECKO_WEBDRIVER, webDriverLocation);
        this.driver = new FirefoxDriver();
        LOGGER.info("Driver started now, source being => {}", webDriverLocation);
    }

    public void loadUrl(String urlToHit) {
        LOGGER.info("Going to hit URL => {}", urlToHit);
        StopWatch timer = new StopWatch();
        timer.start();

        driver.get(urlToHit);

        timer.stop();
        LOGGER.info("URL loaded. Time taken: {}s", timer.getTime(TimeUnit.SECONDS));
    }

    public void shutDownDriver() {
        driver.quit();
        LOGGER.info("Driver has been shutdown now!");
    }

    public WebDriver getDriver() {
        return driver;
    }
}
