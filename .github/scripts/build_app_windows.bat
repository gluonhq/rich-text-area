@ECHO OFF

rem ------ ENVIRONMENT --------------------------------------------------------

rem See https://github.com/dlemmermann/JPackageScriptFX

rem The script depends on various environment variables to exist in order to
rem run properly:
rem
rem PROJECT_VERSION=1.1.0-SNAPSHOT
rem APP_VERSION=1.0.0

set JAVA_VERSION=17
set APP_NAME=ChatApp
set MAIN_JAR="App-%PROJECT_VERSION%.jar"
set LAUNCHER_CLASS=com.gluonhq.chat.AppLauncher
set MAIN_CLASS=com/gluonhq/chat/GluonChat
set ICON_PATH=App\src\main\resources\ChatAppLogo.ico

rem Set desired installer type: "app-image" "msi" "exe".
set INSTALLER_TYPE=msi

rem ------ SETUP DIRECTORIES AND FILES ----------------------------------------
rem Remove previously generated java runtime and installers. Copy all required
rem jar files into the input/libs folder.

IF EXIST App\target\java-runtime rmdir /S /Q  App\target\java-runtime
IF EXIST App\target\installer rmdir /S /Q App\target\installer

xcopy /S /Q App\target\libs\* App\target\installer\input\libs\
copy App\target\%MAIN_JAR% App\target\installer\input\libs\

rem ------ REQUIRED MODULES ---------------------------------------------------
rem Use jlink to detect all modules that are required to run the application.
rem Starting point for the jdep analysis is the set of jars being used by the
rem application.

echo detecting required modules

"%JAVA_HOME%\bin\jdeps" ^
  -q ^
  --multi-release %JAVA_VERSION% ^
  --ignore-missing-deps ^
  --class-path "App\target\installer\input\libs\*" ^
  --print-module-deps App\target\classes\%MAIN_CLASS%.class > temp.txt

set /p detected_modules=<temp.txt

echo detected modules: %detected_modules%

rem ------ MANUAL MODULES -----------------------------------------------------
rem jdk.crypto.ec has to be added manually bound via --bind-services or
rem otherwise HTTPS does not work.
rem
rem See: https://bugs.openjdk.java.net/browse/JDK-8221674
rem
rem In addition we need jdk.localedata if the application is localized.
rem This can be reduced to the actually needed locales via a jlink parameter,
rem e.g., --include-locales=en,de.
rem
rem Do not forget the leading ','!

set manual_modules=,jdk.crypto.ec,jdk.localedata
echo manual modules: %manual_modules%

rem ------ RUNTIME IMAGE ------------------------------------------------------
rem Use the jlink tool to create a runtime image for our application. We are
rem doing this in a separate step instead of letting jlink do the work as part
rem of the jpackage tool. This approach allows for finer configuration and also
rem works with dependencies that are not fully modularized, yet.

echo creating java runtime image

call "%JAVA_HOME%\bin\jlink" ^
  --strip-native-commands ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2 ^
  --strip-debug ^
  --add-modules %detected_modules%%manual_modules% ^
  --include-locales=en ^
  --output App/target/java-runtime

rem ------ PACKAGING ----------------------------------------------------------
rem In the end we will find the package inside the target/installer directory.

echo Creating installer

call "%JAVA_HOME%\bin\jpackage" ^
  --type %INSTALLER_TYPE% ^
  --dest App/target/installer ^
  --input App/target/installer/input/libs ^
  --name %APP_NAME% ^
  --main-class %LAUNCHER_CLASS% ^
  --main-jar %MAIN_JAR% ^
  --java-options -Xmx2048m ^
  --runtime-image App/target/java-runtime ^
  --icon %ICON_PATH% ^
  --app-version %APP_VERSION% ^
  --vendor "Gluon" ^
  --copyright "Copyright © 2021 Gluon" ^
  --win-dir-chooser ^
  --win-shortcut ^
  --win-per-user-install ^
  --win-menu
