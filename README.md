Wave App
====

The Wave App runs with Gluon Mobile, OpenJFX, OpenJDK 11 and GraalVM

Instructions
------------

* Set `JAVA_HOME` to a JDK 11+
* Install all sub-projects in local maven repository:
```
mvn clean install
```
* Execute the application:
```
cd App && mvn gluonfx:run
```

Instructions for Native Image
------------

* Download the latest version of [GraalVM from Gluon](https://github.com/gluonhq/graal/releases/latest) and unpack it like you would any other JDK.

* Set `GRAALVM_HOME` environment variable to the GraalVM installation directory:
```
export GRAALVM_HOME=path-to-graalvm-directory
```

* Native build the application:
```
mvn gluonfx:build -pl App
```
* Once the build is successful, the native image be executed by:
```
mvn gluonfx:nativerun -pl App
```

## FAQs

Below is a list of frequently asked questions / issues one might face during running ChatApp from source:

### No Device Found

This error comes when the app is started, but we waited too long to scan the QR code

### Network Error

This error normally occurs due to "Rate Limit Exceeded", which means that scanning was tried too often.
We need to allow it to cool down and try again after 1 or 2 minutes.

### Scanning done but no contact list shown

Current workaround is to remove all your linked devices from mobile app, remove `~/.signalfx` directory and re-scan.