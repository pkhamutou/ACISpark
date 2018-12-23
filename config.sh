#!/bin/bash

# Configuration file for building Alchemist-Spark Interface

if [ "$SYSTEM" = "" ]; then
  export SYSTEM="MacOS"                # Options: MacOS, Cori, <add your own>
fi

if [ "$SYSTEM" = "MacOS" ]
then
	export ACISPARK_PATH=$HOME/Projects/ACISpark
	
elif [ "$SYSTEM" = "Linux" ]
then
	export ACISPARK_PATH=$PWD
	
elif [ "$SYSTEM" = "Cori" ]
then
	export ACISPARK_PATH=$SCRATCH/Projects/ACISpark
	
elif [ "$SYSTEM" = "<your system here>" ]
then
	export ACISPARK_PATH=$SCRATCH/Projects/ACISpark
fi

echo $ACISPARK_PATH
