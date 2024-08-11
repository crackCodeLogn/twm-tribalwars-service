APP_NAME="twm-tribalwars-service"
JAVA_PARAM="-Xmx1g"

BIN_PATH=$TWM_HOME_PARENT/TWM/$APP_NAME/bin     #TWM-HOME-PARENT :: exported in .bashrc
cd $BIN_PATH/../target/
JAR_NAME=`ls *jar`
JAR_PATH=$BIN_PATH/../target/$JAR_NAME
JAVA_PATH=$HOME/.jdks/jdk17/bin/java

echo "Starting '$APP_NAME' with java param: '$JAVA_PARAM', at '$JAR_PATH'"
$JAVA_PATH $JAVA_PARAM -jar $JAR_PATH
