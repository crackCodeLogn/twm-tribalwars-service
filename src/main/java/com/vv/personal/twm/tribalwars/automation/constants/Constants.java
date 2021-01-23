package com.vv.personal.twm.tribalwars.automation.constants;

/**
 * @author Vivek
 * @since 29/11/20
 */
public class Constants {

    public static final String GECKO_WEBDRIVER = "webdriver.gecko.driver";

    public static final String TW_MAIN = "https://tribalwars.net";
    public static final String TW_MAIN_PLAY_WORLDS = "https://tribalwars.net/en-dk/page/play/en%s%d";
    public static final String TW_SCREEN = "https://en%s%d.tribalwars.net/game.php?village=%s&screen=%s";
    public static final String TW_INTRO_SCREEN = "https://en%s%d.tribalwars.net/game.php?screen=overview_villages";
    public static final String TW_REPORT_SCREEN_MODE = "https://en%s%d.tribalwars.net/game.php?screen=%s&mode=%s";
    public static final String TW_URL_WORLD_BASE = "https://en%s%d.tribalwars.net";

    public static final String ID_USER = "user";
    public static final String ID_PASSWORD = "password";
    public static final String CLASS_BTN_LOGIN = "btn-login";

    public static final String EMPTY_STR = "";

    public enum SCREENS_TO_EXTRACT_AUTOMATED_INFO {
        TRAIN, WALL, SNOB, FARM
    }
}
