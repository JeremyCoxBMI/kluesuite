#bash script settings
jar=kluesuite-1.0-SNAPSHOT.jar
#java 1.8 or later
java18=java

if [[ $# -ne 2 ]]; then
    echo "     Combines previously calculated database pieces into single KLUE database"
    echo "     This step is required even if you made 1 piece database, in order to build database in optimal order"
    echo ""
    echo "     You entered incorrect number of parameters"
    echo ""
    echo "     1 : (path +) prefix name of KLUE databases (same as when using buildDatabasePiece.sh)"
    echo "     2 : number of database pieces"
    echo ""
    echo "     you may set custom paths for java or the jar file by editing the script"
    exit 0
fi

class=org.cchmc.kluesuite.mainprograms.databasebuilders.CombinePieces

$java18 -cp $jar $class $1 $2