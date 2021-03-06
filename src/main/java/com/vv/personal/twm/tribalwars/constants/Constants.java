package com.vv.personal.twm.tribalwars.constants;

/**
 * @author Vivek
 * @since 23/01/21
 */
public class Constants {

    public static final Integer ZERO_INT = 0;
    public static final Long NA_LONG = -1L;

    public enum SCREEN_TYPE {
        FARM, REPORT, SNOB, MARKET
    }

    public static final String DISTANCE_WITH_COMPARING_VILLA_KEY = "comp-dist";
    public static final String COMPARING_VILLA_COORD = "comp-loc";

    //FORMATTERS
    public static final String HEROKU_SWAGGER_UI_URL = "https://%s/swagger-ui/index.html";
    public static final String SWAGGER_UI_URL = "http://%s:%s/swagger-ui/index.html";
    public static final String HEROKU_HOST_URL = "https://%s";
    public static final String HOST_URL = "http://%s:%s";

    public static final String LOCALHOST = "localhost";
    public static final String LOCAL_SPRING_HOST = "local.server.host";
    public static final String LOCAL_SPRING_PORT = "local.server.port";
    public static final String SPRING_APPLICATION_HEROKU = "spring.application.heroku";
}
