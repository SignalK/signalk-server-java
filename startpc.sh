#!/bin/bash

# start script for signalk-server-java

JAR="signalk-server-java-0.0.1-SNAPSHOT-jar-with-dependencies.jar"

#temporary until linux-arm.jar is in purejavacom.jar
export LD_LIBRARY_PATH=$SIGNALK_HOME/jna

#start server

#NOTE: you may need to explicitly set your JAVA_HOME for your environment
#
#JAVA_HOME=/home/pi/jdk1.8.0
#JAVA_HOME=/home/robert/java/jdk1.7.0_07
#export JAVA_HOME

JAVA=java
if [ -n "$JAVA_HOME" ]; then
	JAVA=$JAVA_HOME/bin/java
fi

EXT="-Djava.util.Arrays.useLegacyMergeSort=true"
MEM="-Xmx32m"

LOG4J=-Dlog4j.configuration=file://./conf/log4j.properties

$JAVA $EXT $LOG4J $MEM -jar target/$JAR
