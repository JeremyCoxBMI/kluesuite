#!/usr/bin/env bash

#bash script settings
jar=kluesuite-1.0-SNAPSHOT.jar
#java 1.8 or later
java18=java

if [[ $# -ne 4 ]]; then
    echo "Create a small piece of KLUE database, using 8GB memory"
    echo "Chunks to be created in parallel or serial to decrease real time used"
    echo "Parallel processing decreases real time by factor of (number chunks)*(log (number of chunks)"
    echo "Serial processing as chunks saves real time by making smaller chunks by factor of log (number chunks)"
    echo ""
    echo "You entered incorrect number of parameters"
    echo ""
    echo "1 : (path +) prefix name of final KLUE databases to create"
    echo "2 : fastA file for entire database"
    echo "3 : chunk number to process from [0, num chunks), i.e. 0-indexed"
    echo "4 : number of chunks total being processed"
    echo ""
    echo "you may set custom paths for java or the jar file by editting the script"
    echo "you may change memory usage by editting the script"
    exit 0
fi

count=`grep -P "^>" $2 | wc -l`
class=org.cchmc.kluesuite.mainprograms.databasebuilders.BuildPieces

#to increase memory usage, memory required is  ~= 0.08 GB * heapSize + 0.5 GB
heapSize=85
memory="-Xmx7500M"


$java18 $memory -cp $jar $class $1 $2 $heapSize $3 $4 $count

