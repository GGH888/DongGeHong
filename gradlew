#!/bin/sh
set -e
JAVA=java
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec $JAVA -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
