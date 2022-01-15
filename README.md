## twm-tribalwars-service

## Tech stack

1. Java 11
2. Spring-Boot
3. Selenium + Firefox gecko driver
4. Swagger
5. Guava, Gson, Protobuf - the Google family

## Requirements

1. Download gecko driver - the firefox engine - from here: https://github.com/mozilla/geckodriver/releases
2. Set a parameter - ```TWM_HOME_PARENT``` at your system level, which would point to the location of folder one level
   above the TWM folder (where all the twm family members reside).
3. In the code, the default location it'll look for is: ```/etc/WEBDRIVER/FIREFOX/geckodriver```. Thus store the
   downloaded driver at said location or pass location of customized in the input by
   passing ```-Dlocation.driver.gecko=<ur-loc>```
4. Pass your tribalwars login credentials in similar file.

## Starting up locally

1. Build and fire up the Eureka server from https://github.com/crackCodeLogn/twm-eureka-service/
2. Build and fire up the Rendering server from https://github.com/crackCodeLogn/twm-rendering-service/
3. Build and fire up the MongoDB server if you require db access. Then fire up the
   microservice: https://github.com/crackCodeLogn/twm-mongo-service/
4. Build and fire up the CockroachDB server if you require db access. Then fire up the
   mircroservice: https://github.com/crackCodeLogn/twm-tribalwars-crdb-service
5. Finally, build and fire up the twm-tribalwars-service jar.
6. All firing up is via the ```bin/<service-name>.sh``` starter scripts situated in each of the projects.

## Shut down locally

1. Ctrl+c the window where twm-tribalwars-service was started.
2. Ctrl+c the window where twm-mongo-service was started.
3. Ctrl+c the window where twm-rendering-service was started.
3. Ctrl+c the window where twm-tribalwars-crdb-service was started.
4. Finally shut down the Eureka server by Ctrl+c the window where twm-eureka-service was started.