#!/usr/bin/env bash

source ./config.sh

export ACISPARK_JAR=$ACISPARK_PATH/target/scala-2.11/alchemist-assembly-0.5.jar

echo $ACISPARK_JAR

CURR_DIR=$PWD

if [ "$SYSTEM" == "MacOS" ]
then
	# spark-shell --jars $ACISPARK_JAR
	spark-submit --master local[3] --class alchemist.AlchemistSession $ACISPARK_JAR
	
elif [ "$SYSTEM" == "Cori" ]
then
	spark-shell --jars $ACISPARK_JAR
	
elif [ "$SYSTEM" == "<your system here>" ]
then
	spark-shell --jars $ACISPARK_JAR
fi
