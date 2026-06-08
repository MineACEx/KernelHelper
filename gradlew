#!/bin/sh
APP_HOME=$( cd "${0%/*}" > /dev/null && pwd -P ) || exit
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
JAVACMD=${JAVA_HOME:+$JAVA_HOME/bin/java}
JAVACMD=${JAVACMD:-java}
exec "$JAVACMD" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"