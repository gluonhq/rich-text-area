#!/bin/bash
CLASSPATH=$(mvn -Ptest dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q):target/test-classes:target/classes
java -Dglass.platform=Headless -Dprism.order=sw -cp $CLASSPATH com.gluonhq.richtextarea.PerformanceTests
