#!/bin/bash
CLASSPATH=$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):target/test-classes:target/classes
java -Dglass.platform=Headless -Dprism.order=sw -cp $CLASSPATH com.gluonhq.richtextarea.PerformanceTests
