package com.vv.personal.twm.tribalwars.automation.engine;

import com.vv.personal.twm.tribalwars.automation.config.Sso;
import com.vv.personal.twm.tribalwars.automation.driver.AutomationDriver;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vv.personal.twm.tribalwars.automation.constants.Constants.*;

/**
 * @author Vivek
 * @since 29/11/20
 */
public class Engine {
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);

    private final AutomationDriver driver;
    private final Sso sso;
    private final String worldType;
    private final int worldNumber;


    public Engine(AutomationDriver driver, Sso sso, String worldType, int worldNumber) {
        this.driver = driver;
        this.sso = sso;
        this.worldType = worldType;
        this.worldNumber = worldNumber;
    }

    public String extractOverviewDetailsForWorld() {
        //SEQUENCE WISE
        if (loginSequence()) {
            final String overviewSource = driver.getDriver().getPageSource();
            //logoutSequence();
            return overviewSource;
        }
        return EMPTY_STR;
    }

    public boolean loginSequence() {
        try {
            driver.loadUrl(TW_MAIN);
            sleeper(1.5);
            driver.getDriver().findElement(By.id(ID_USER)).sendKeys(sso.getUser());
            driver.getDriver().findElement(By.id(ID_PASSWORD)).sendKeys(sso.getCred());
            LOGGER.info("Firing Login process now!");
            try {
                driver.getDriver().findElement(By.className(CLASS_BTN_LOGIN)).click();
            } catch (Exception e) {
                LOGGER.error("Failed to login. ", e);
            }
            sleeper(25);
            sleeper(1.5);
            driver.loadUrl(String.format(TW_MAIN_PLAY_WORLDS, worldType, worldNumber));
            sleeper(1.5);
            driver.loadUrl(String.format(TW_INTRO_SCREEN, worldType, worldNumber));
            sleeper(.5);
            return true;
        } catch (Exception e) {
            LOGGER.error("Login sequence failed! ", e);
        }
        return false;
    }

    public boolean logoutSequence() {
        try {
            driver.getDriver().findElements(By.linkText("Log out")).get(0).click();

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to have a clean log-out. ", e);
        }
        return false;
    }

    public Object executeJsScript(String script) {
        return driver.executeJsScript(script);
    }

    public void sleeper(double sleepTimeSeconds) {
        try {
            LOGGER.info("Sleeping for {} seconds", sleepTimeSeconds);
            Thread.sleep((long) (sleepTimeSeconds * 1000));
        } catch (InterruptedException e) {
            LOGGER.warn("Sleep for {}s interrupted. ", sleepTimeSeconds, e);
        }
    }

    public void destroyDriver() {
        driver.shutDownDriver();
    }

    public AutomationDriver getDriver() {
        return driver;
    }
}
