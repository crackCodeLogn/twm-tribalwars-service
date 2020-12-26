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

    public static final String ID_USER = "user";
    public static final String ID_PASSWORD = "password";
    public static final String CLASS_BTN_LOGIN = "btn-login";

    public static final String EMPTY_STR = "";

    public enum SCREENS {
        TRAIN, WALL, SNOB, FARM
    }
}
