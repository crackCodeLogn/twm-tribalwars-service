APP_NAME="twm-tribalwars-service"
APP_VERSION="2.0-SNAPSHOT"
JAVA_PARAM="-Xmx1g"

BIN_PATH=$TWM_HOME_PARENT/TWM/$APP_NAME/bin     #TWM-HOME-PARENT :: exported in .bashrc
JAR_PATH=$BIN_PATH/../target/$APP_NAME-$APP_VERSION.jar
JAVA_PATH=$HOME/.jdks/jdk17/bin/java

echo "Starting '$APP_NAME' with java param: '$JAVA_PARAM', at '$JAR_PATH'"
$JAVA_PATH $JAVA_PARAM -jar $JAR_PATH
