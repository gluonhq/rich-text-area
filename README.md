ChatApp
====

The Gluon Chat App that runs with Gluon Mobile, OpenJFX, OpenJDK 11 and GraalVM

Prerequisites
-------------

You need a valid subscription to Gluon CloudLink. You can get it [here](http://gluonhq.com/products/cloudlink/buy/), and 
there is a 30-day free trial. Sign up and get a valid account on Gluon CloudLink and a link to download the Gluon CloudLink 
Dashboard. 

Install and open the Dashboard, and sign in using the Gluon account credentials provided above. Go to the App Management view, and you will 
find a pair of key/secret tokens. Save the file `gluoncloudlink_config.json` under your project 
`src/main/resources/` folder. The content of the file is a JSON object with the key and secret that will grant access
to Gluon CloudLink:

```json
{
  "gluonCredentials": {
    "applicationKey": "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
    "applicationSecret": "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
  }
}
```

Instructions
------------

* Set `JAVA_HOME` to a JDK 11+
* Install all sub-projects in local maven repository:
```
mvn clean install
```
* Execute the application:
```
mvn javafx:run -pl App
```

Instructions for Native Image
------------

* Download the following version of GraalVM and unpack it like you would any other JDK. (e.g. in `/opt`):

  * [GraalVM for Linux](https://download2.gluonhq.com/substrate/graalvm/graalvm-svm-linux-20.1.0-ea+25.zip)
  * [GraalVM for Mac](https://download2.gluonhq.com/substrate/graalvm/graalvm-svm-darwin-20.1.0-ea+25.zip)

* Configure the runtime environment. Set `GRAALVM_HOME` environment variable to the GraalVM installation directory:
```
export GRAALVM_HOME=path-to-graalvm-directory
```

### Desktop

* Native build the application:
```
mvn client:build -pl App
```
* Once the build is successful, the native image be executed by:
```
mvn client:run -pl App
```

### iOS

* Native build the application:
```
mvn -Pios client:build -pl App
```
* Once the build is successful, the native image be installed on a connected iOS device by:
```
mvn -Pios client:run -pl App
```