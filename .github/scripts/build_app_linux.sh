#!/bin/bash

# See https://github.com/dlemmermann/JPackageScriptFX

# ------ ENVIRONMENT --------------------------------------------------------
# The script depends on various environment variables to exist in order to
# run properly:
#
# PROJECT_VERSION=1.1.0-SNAPSHOT
# APP_VERSION=1.0.0

JAVA_VERSION=17
APP_NAME=WaveApp
MAIN_JAR="App-$PROJECT_VERSION.jar"
LAUNCHER_CLASS=com.gluonhq.chat.AppLauncher
MAIN_CLASS=com/gluonhq/chat/GluonChat
ICON_PATH=App/src/main/resources/ChatAppLogo.png

echo "java home: $JAVA_HOME"
echo "project version: $PROJECT_VERSION"
echo "app version: $APP_VERSION"
echo "main JAR file: $MAIN_JAR"

# ------ SETUP DIRECTORIES AND FILES ----------------------------------------
# Remove previously generated java runtime and installers. Copy all required
# jar files into the input/libs folder.

rm -rfd App/target/java-runtime/
rm -rfd App/target/installer/

mkdir -p App/target/installer/input/libs/

cp App/target/libs/* App/target/installer/input/libs/
cp App/target/${MAIN_JAR} App/target/installer/input/libs/

# ------ REQUIRED MODULES ---------------------------------------------------
# Use jlink to detect all modules that are required to run the application.
# Starting point for the jdep analysis is the set of jars being used by the
# application.

echo "detecting required modules"
detected_modules=`$JAVA_HOME/bin/jdeps \
  -q \
  --multi-release ${JAVA_VERSION} \
  --ignore-missing-deps \
  --print-module-deps \
  --class-path "App/target/installer/input/libs/*" \
    App/target/classes/${MAIN_CLASS}.class`
echo "detected modules: ${detected_modules}"


# ------ MANUAL MODULES -----------------------------------------------------
# jdk.crypto.ec has to be added manually bound via --bind-services or
# otherwise HTTPS does not work.
#
# See: https://bugs.openjdk.java.net/browse/JDK-8221674
#
# In addition we need jdk.localedata if the application is localized.
# This can be reduced to the actually needed locales via a jlink parameter,
# e.g., --include-locales=en,de.
#
# Don't forget the leading ','!

manual_modules=,jdk.crypto.ec,jdk.localedata
echo "manual modules: ${manual_modules}"

# ------ RUNTIME IMAGE ------------------------------------------------------
# Use the jlink tool to create a runtime image for our application. We are
# doing this in a separate step instead of letting jlink do the work as part
# of the jpackage tool. This approach allows for finer configuration and also
# works with dependencies that are not fully modularized, yet.

echo "creating java runtime image"
$JAVA_HOME/bin/jlink \
  --strip-native-commands \
  --no-header-files \
  --no-man-pages  \
  --compress=2  \
  --strip-debug \
  --add-modules "${detected_modules}${manual_modules}" \
  --include-locales=en \
  --output App/target/java-runtime

# ------ PACKAGING ----------------------------------------------------------
# In the end we will find the package inside the target/installer directory.

echo "Creating installer of type $INSTALLER_TYPE"

$JAVA_HOME/bin/jpackage \
--dest App/target/installer \
--input App/target/installer/input/libs \
--name ${APP_NAME} \
--main-class ${LAUNCHER_CLASS} \
--main-jar ${MAIN_JAR} \
--java-options -Xmx2048m \
--runtime-image App/target/java-runtime \
--icon ${ICON_PATH} \
--app-version ${APP_VERSION} \
--vendor "Gluon" \
--copyright "Copyright © 2021 Gluon" \
"$@"
