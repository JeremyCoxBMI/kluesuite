#bash script settings
jar=kluesuite-1.0-SNAPSHOT.jar
#java 1.8 or later
java18=java

if [[ $# -ne 5]]; then
    echo "     Create a small piece of KLUE database, using 8GB memory"
    echo "     Chunks to be created in parallel or serial to decrease real time used"
    echo "     Parallel processing decreases real time by factor of (number chunks)*(log (number of chunks))"
    echo "     however, number of chunks should not exceed your ability to run concurrently)
    echo "     Serial processing as chunks saves real time by making smaller chunks by factor of log (number chunks)"
    echo ""
    echo "     You entered incorrect number of parameters"
    echo ""
    echo "     1 : (path +) prefix name of final KLUE databases to create"
    echo "     2 : fastA file for entire database"
    echo "     3 : chunk number to process from [0, num chunks), i.e. 0-indexed"
    echo "     4 : number of chunks to process"
    echo "     5 : number megabytes memory to use (default is 8000 or 8GB)"
    echo ""
    echo "     you may set custom paths for java or the jar file by editting the script"
    echo "     WARNING: as Java memory usage is not standardized, you may need to adjust the calculation in the script if you crash"

    #TODO example
    exit 0
fi

ARG5=${5:-7800}

count=`grep -P "^>" $2 | wc -l`
class=org.cchmc.kluesuite.mainprograms.databasebuilders.BuildPieces

let memory=$5-500
let heapSize=$memory/80  #integer truncation.  what I want. yeah

memory=-Xmx"$memory"M

$java18 $memory -cp $jar $class $1 $2 $heapSize $3 $4 $count

