#!/bin/sh
APP_BASE_NAME=`basename "$0"`
DIRNAME=`dirname "$0"`
if [ -z "$DIRNAME" ]; then
  DIRNAME="."
fi

# Attempt to use JAVA_HOME
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    if [ -x "/opt/homebrew/opt/openjdk@17/bin/java" ]; then
        JAVACMD="/opt/homebrew/opt/openjdk@17/bin/java"
    else
        JAVACMD="java"
    fi
fi

exec "$JAVACMD" "-Dorg.gradle.appname=$APP_BASE_NAME" -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
