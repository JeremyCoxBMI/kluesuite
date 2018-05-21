#bash script settings
jar=kluesuite-1.0-SNAPSHOT.jar
#java 1.8 or later
java18=java

if [[ $# -ne 4 ]]; then
    echo "     KLAT uses a KLUE database to calculate alignments"
    echo ""
    echo "     You entered incorrect number of parameters"
    echo ""
    echo "     1 : (path +) prefix name of KLUE databases"
    echo "     2 : fastA file of queries"
    echo "     3 : minimum fast KLAT score (minimum number of matches)"
    echo "     4 : output file (blast6 plus CIGAR string, possibly '.bl6+' extension"
    echo ""
    echo "     you may set custom paths for java or the jar file by editing the script"
    echo "     you may change memory usage by editting the script"


    exit 0
fi


#TODO add KLATsettings file, KLUEsettings file


#prefix=$1
#fastA=$2
#minFKLAT=$3
#outF=$4


class=org.cchmc.kluesuite.mainprograms.KlatProgram2

$java18 -cp $jar $class $1 $2 $3 > $4
