Gluon Wave Application
====

This repository contains the Gluon Wave Application, which is using the 
Signal protocol with Java and JavaFX.

Where can I test it?
--------------------

We highly recommend to read this README first. Gluon Wave is a technical
project, in an experimental phase. We hope it can serve many people and
help achieve the goals of the Signal Foundation. At this moment though,
you should not expect top-quality from this project.
If you're not scared by that, and want to go to the download page 
immediately, here you go: 
https://github.com/gluonhq/wave-app/releases/tag/v1.0.4

About Signal
------------

[Signal](https:/signal.org) is an encryption tool enabling end-to-end
encryption. Messages sent via the Signal Protocol are encrypted by the
sender, and decrypted by the recipient, using a combination of clever
techniques including [Extended Triple Diffie-Hellman](https://signal.org/docs/specifications/x3dh/)
and [Double Ratchet](https://signal.org/docs/specifications/doubleratchet/).

The Signal protocol and its implementations are open-source and free to
use (as long as the GPL license is respected). Signal respects users privacy
and is not showing ads. Signal is a non-profit organisation, and accepts
[donations](https://signal.org/donate).

Signal on mobile and desktop
----------------------------

In order to use Signal, you need to install it on your phone first.
Go to https://signal.org/download/ and follow the links for Android or
iOS. Once you are using Signal on your phone, you can _pair_ other
devices, e.g. your desktop or laptop, and use Signal on those devices as
well -- using the same account as the one you use on your phone.
Currently, there is an electron-based desktop application that you can
use on your system. The Wave Application in the repository you're 
currently looking at is a Java and JavaFX based alternative for this electron
based application.

Signal and Java
---------------

We didn't have to start from scratch when writing this application.
There is a Java implementation of the Signal protocol that is (or was) used by
the Android client. That implementation served as the basis for the Java
API's we needed for the Wave Application. We made a number of changes though,
since we don't have to worry about Android restrictions. We rather use
the latest Java, as developed in the [OpenJDK](https://openjdk.java.net).
We forked the Signal repositories and updated them to Java 17.
The access to the Signal Protocol is defined in the Gluon Equation
project, which can be found at https://github.com/gluonhq/equation.git .

Wave App
--------

The application in this repository uses the libraries described in the
previous section, and creates a JavaFX user interface around them.

Running the Wave App
====================

You can run Gluon Wave in 3 ways:

* Download and run the native executables for Windows, Mac or Linux
* Download and run the jpackaged installers for Windows, Mac or Linux
* Build the code from this repository and run it.

Download and run
----------------

The first 2 options don't require a JVM at runtime. 
You can get the latest development versions and instructions from
https://github.com/gluonhq/wave-app/releases/tag/v1.0.4

*Keep in mind that this is a developer project, and you should not
use this for critical/production purposes*

The first option leverages
the Java packager that is part of the JDK distributions.
The second option leverages [GraalVM native-image](https://graalvm.org).

Building and running with the Java packager-based build
----------------------------------------------------------

* Set `JAVA_HOME` to a JDK 11+
* Install all sub-projects in local maven repository:
```
mvn clean install
```
* Execute the application:
```
cd App && mvn gluonfx:run
```

Building and running with the GraalVM Native Image-based build
-----------------------------------------------------------------

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

What to do now?
===============

Issues
------

There are a bunch of known and unknown issues, which you can report in
the issue tracker of this repository.

Missing functionality
---------------------
* No support for groups
* No support for attachments
* No support for stickers

... but that is just a matter of time.

Known issues:
-------------
Below is a list of frequently asked questions / issues one might face during running ChatApp from source:

### No Device Found (in the mobile app)

This error comes when the app is started, but you waited too long to scan the QR code

### Network Error (in the mobile app)

This error normally occurs due to "Rate Limit Exceeded", which means that scanning was tried too often.
You need to allow it to cool down and try again after 1 or 2 minutes.

### Scanning done but no contact list shown

If all goes well, after the QR code is scanned, you should see your contact list in a few
seconds. Sometimes, the contact sync requests isn't received. 
Current workaround is to close the application and start it again.
If that doesn't work, remove all your linked devices from mobile app, remove `~/.signalfx` directory and restart the application.

I want to contribute!
=====================
We're excited you're reading until here! We recommend that you fork this repository, and change
whatever you want. You can work on a different theme, by modifying the CSS files. You can
create a different layout, by modifying the FXML files. Or you can modify the flow and logic,
by modifying the Java files.

