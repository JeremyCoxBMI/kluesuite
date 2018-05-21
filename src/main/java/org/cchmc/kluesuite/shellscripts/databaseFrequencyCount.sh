#bash script settings
jar=kluesuite-1.0-SNAPSHOT.jar
#java 1.8 or later
java18=java

if [[ $# -ne 2 ]]; then
    echo "     Counts the number of occurrences of locations of each k-mer found in database"
    echo ""
    echo "     You entered incorrect number of parameters"
    echo ""
    echo "     1 : (path +) prefix name of KLUE databases (same as when using buildDatabasePiece.sh)"
    echo "     2 : output text file"
    echo ""
    echo "     you may set custom paths for java or the jar file by editting the script"

    exit 0
fi

class=org.cchmc.kluesuite.mainprograms.DatabaseFrequencyCount

$java18 -cp $jar $class $1 $2