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

    public void initiateReadingAllVillasForWorld() {
        //SEQUENCE WISE
        driver.loadUrl(TW_MAIN);
        sleeper(3);
        driver.getDriver().findElement(By.id(ID_USER)).sendKeys(sso.getUser());
        driver.getDriver().findElement(By.id(ID_PASSWORD)).sendKeys(sso.getCred());
        LOGGER.info("Firing Login process now!");
        try {
            driver.getDriver().findElement(By.className(CLASS_BTN_LOGIN)).click();
        } catch (Exception e) {
            LOGGER.error("Failed to login. ", e);
        }
        sleeper(3);
        driver.loadUrl(String.format(TW_MAIN_PLAY_WORLDS, worldType, worldNumber));
        sleeper(2);
        driver.loadUrl(String.format(TW_INTRO_SCREEN, worldType, worldNumber));
        sleeper(5);
        driver.getDriver().findElements(By.linkText("Log out")).get(0).click();
        driver.shutDownDriver();
    }

    public void sleeper(long sleepTimeSeconds) {
        try {
            LOGGER.info("Sleeping for {} seconds", sleepTimeSeconds);
            Thread.sleep(sleepTimeSeconds * 1000);
        } catch (InterruptedException e) {
            LOGGER.warn("Sleep for {}s interrupted. ", sleepTimeSeconds, e);
        }
    }
}
