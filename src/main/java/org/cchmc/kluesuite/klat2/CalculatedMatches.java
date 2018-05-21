package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klue.DNAcodes;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.SuperString;

import java.util.ArrayList;

/**
 * Created by jwc on 10/5/17.
 *
 * For *gaps* between seeds
 */
public class CalculatedMatches extends Box2 {


    public InitType mytype;
    ArrayList<TableEntry2[]> table;

    //Note these rows represent the rows and column sequence letters over range inclusive to inclusive
    String rows;
    String cols;



    public CalculatedMatches(InitType mytype, int srow, int scol, int erow, int ecol,
                             int previousMinFastKlatScore, int previousMaxFastKlatScore,
                             int prevSWmin, int prevSWmax,
                             String rows, String cols) {
        super(srow, scol, erow, ecol);
        this.mytype = InitType.MID;
        this.type = BoxType.CALCULATED;
        //SENTINEL value
        cumulativeActualFastKlatScore = -1;
        cumulativeSmithWatermanScore = -1;
        cumulativeMinimumFastKlatScore = previousMinFastKlatScore;
        cumulativeMaximumFastKlatScore = previousMaxFastKlatScore;
        cumulativeMinimumSWscore = prevSWmin;
        cumulativeMaximumSWscore = prevSWmax;
        table = null;
        this.rows = rows;
        this.cols = cols;
        calculateScores();
//        if (this.mytype == InitType.LEFT) {
//            //add coordinate buffer to left for larger table in left; requires blank first row and column
//            this.rows = " " + this.rows;
//            this.cols = " " + this.cols;
//        }
    }

    /**
     *      Constructor for unaligned regions at the beginning or end of alignment
     * @param seed      Seed to left or right as indicated in mytype
     * @param mytype    Direction of the constructed region from seed  (Seed(previous) then Region) is InitType.RIGHT, [Region then Seed(next)] is InitType.LEFT
     * @param prevMinimumFastKlat
     * @param prevMaximumFastKlat
     * @param queryStop     NOT NEEDED for left
     * @param stop          NOT NEEDED for right
     */


    public CalculatedMatches(Seed seed, InitType mytype,
                             int prevMinimumFastKlat, int prevMaximumFastKlat,
                             int prevSWmin, int prevSWmax,
                             int queryStop, int stop, int refStart,
                             String rows, String cols) {
        super();
        this.mytype = mytype;
        this.type = BoxType.CALCULATED;
        if (mytype == InitType.MID){
            System.err.println("MIDDLE type calculated match region must use different constructor.  Wrong information passed.");
        }
        if (mytype == InitType.RIGHT) {
            //seed here is actually previous --> but one name above in function call
            this.scol = seed.end + Kmer31.KMER_SIZE_MINUS_TWO; // EXCLUSIVE TO INCLUSIVE (-1), WANT OVERLAP (SHIFT 30)
            this.srow = seed.queryEnd + Kmer31.KMER_SIZE_MINUS_TWO;  //EXCLUSIVE TO INCLUSIVE (-1), WANT OVERLAP (SHIFT 30)
            this.ecol=stop  - 1;  //EXCLUSIVE to INCLUSIVE, WANT OVERLAP
            this.erow=queryStop - 1;    //EXCLUSIVE to INCLUSIVE, WANT OVERLAP
        } else if (mytype == InitType.LEFT) {
            //seed here is actually next --> but one name above in function call
            this.ecol = seed.start;//WANT OVERLAP
            this.erow = seed.queryStart;  //WANT OVERLAP
            this.scol=0;    //no matter where it starts, we include the whole thing
            this.srow=0;    //no matter where it starts, we include the whole thing
        }
        cumulativeActualFastKlatScore = -1;
        cumulativeSmithWatermanScore = -1;
        cumulativeMinimumFastKlatScore = prevMinimumFastKlat;
        cumulativeMaximumFastKlatScore = prevMaximumFastKlat;
        cumulativeMinimumSWscore = prevSWmin;
        cumulativeMaximumSWscore = prevSWmax;
        table = null;
        this.rows = rows;
        this.cols = cols;
        calculateScores();
    }

    /**
     *      * COnstructor for between two seeds only
     * @param prev
     * @param curr
     * @param prevMinimumFastKlat
     * @param prevMaximumFastKlat
     * @param prevSWmin
     * @param prevSWmax
     */
    public CalculatedMatches(Seed prev, Seed curr,
                             int prevMinimumFastKlat, int prevMaximumFastKlat,
                             int prevSWmin, int prevSWmax,
                             String rows, String cols) {

        super();
        this.srow =prev.queryEnd +Kmer31.KMER_SIZE_MINUS_TWO;  //exclusive to INCLUSIVE: -1; extend 30
        this.scol = prev.end + Kmer31.KMER_SIZE_MINUS_TWO; //- refStart ;  //exclusive to INCLUSIVE: -1; extend 30
        this.erow = curr.queryStart;   //INCLUSIVE to INCLUSIVE
        this.ecol = curr.start; // - refStart; //INCLUSIVE to INCLUSIVE
        this.mytype = InitType.MID;
        this.type = BoxType.CALCULATED;

        cumulativeActualFastKlatScore = -1;
        cumulativeSmithWatermanScore = -1;
        cumulativeMinimumFastKlatScore = prevMinimumFastKlat;
        cumulativeMaximumFastKlatScore = prevMaximumFastKlat;
        cumulativeMinimumSWscore = prevSWmin;
        cumulativeMaximumSWscore = prevSWmax;
        table = null;
        this.rows = rows;
        this.cols = cols;
        calculateScores();

    }

    /**
     * REQUIREMENT:
     * GAP >= abs(MISMATCH), MISMATCH == GAP
     */
    public void calculateScores() {
        int m = (ecol - scol);
        int n = (erow - srow);
        //while length(refSeq) >= length(querySeq), there could be a long deletion, where num rows > num cols
        if (m > n) {
            int t = m;
            m = n;
            n = t;
        }

        int maxMatches = 0;
        int minMismatches = 0;  //minMismatches given maximum score
        int maxMismatches = 0;  //maxMismatches given optimal score
        //int minMatches = 0;  //always 0

        if (mytype == InitType.RIGHT || mytype == InitType.LEFT){
            maxMatches = Math.min(m - 2, n);
            minMismatches = Math.max(1, (m - maxMatches - 1));
        } else {
            if (n == 3){
                maxMatches = m - 3;
                if (m==3)
                    maxMismatches = 1;
                else
                    maxMismatches = m + n - 4;
            } else {
                maxMatches = Math.min(m - 4, n - 1);
                maxMismatches = m + n - 4;
            }
            minMismatches = Math.max(2, (m - maxMatches));
        }  //end if not InitType.MID

        cumulativeMaximumSWscore += maxMatches * SmithWatermanTruncated2.MATCH + minMismatches*SmithWatermanTruncated2.GAP;
        //cumulativeMinimumSWscore += minMatches * SmithWatermanTruncated2.MATCH + maxMismatches*SmithWatermanTruncated2.GAP; // minMatches always 0
        cumulativeMinimumSWscore += maxMismatches*SmithWatermanTruncated2.GAP;
        cumulativeMaximumFastKlatScore += maxMatches - minMismatches;
        cumulativeMinimumFastKlatScore -= maxMismatches*SmithWatermanTruncated2.GAP;
    }//end calculateScores()



    @Override
    public boolean isCalculated(){
        return table != null;
    }

    @Override
    public void calculateScores(int prevSWscore, int prevFastKlatScore) {
        buildTableAndCalculateAlignment(prevSWscore, prevFastKlatScore);
    }

    /**
     * Returns the cumulative FastKlatScore?  WHAT?
     * @param previousSW
     * @return
     */
    public int buildTableAndCalculateAlignment(int previousSW, int previousFastKlat) {

        int num_table_rows = erow-srow+1;  //INCLUSIVE INCLUSIVE
        int num_table_cols = ecol-scol+1;

        constructTable(previousSW, previousFastKlat);
//TODO
//        if (mytype == InitType.LEFT) {
//            constructLeftTable(previousSW, previousFastKlat);
//        } else if (mytype == InitType.RIGHT) {
//            constructRightTable(previousSW, previousFastKlat);
//        } else if (mytype == InitType.MID) {
//            constructMidTable(previousSW, previousFastKlat);
//        }

        return cumulativeActualFastKlatScore;
    }

//    private void constructRightTable(int previousSW, int previousFastKlat) {
//    }
//
//    private void constructLeftTable(int previousSW, int previousFastKlat) {
//    }
//
//    private void constructMidTable(int previousSW, int previousFastKlat) {
//    }

    public static int getScore(ArrayList<TableEntry2[]> table, int r, int c){
        if (r <0 || c < 0)
            return 0;
        else
            return table.get(r)[c].score;

    }

    public static int getFastKlatScore(ArrayList<TableEntry2[]> table, int r, int c){
        if (r < 0 || c < 0)
            return 0;
        else
            return table.get(r)[c].fastScore;

    }

    private void constructTable(int previousSW, int previousFastKlat) {
        // **************************
        // construct table
        // **************************

        //first row and column include matching kmer end, so
        int num_table_rows = erow - srow + 1;  //INCLUSIVE INCLUSIVE
        int num_table_cols = ecol - scol + 1;

        table = new ArrayList<TableEntry2[]>(num_table_rows);

        for (int k = 0; k < num_table_rows; k++) {
            TableEntry2[] row = new TableEntry2[num_table_cols];
            for (int j = 0; j < num_table_cols; j++) {
                row[j] = new TableEntry2();
            }
            table.add(row);       //initializes to move = DIAGONAL, score = 0
        }

        //to control filling in columns differently due to mytype AND adjacency on diagonal
        // is true IF allowed to be a match close to ends
        boolean leftCap = false;
        boolean rightCap = false;
        int LAST_ROW_CALCULATE = num_table_rows - 2;
        int LAST_COL_CALCULATE = num_table_cols - 2;

        if (mytype == InitType.LEFT) {
            leftCap = true;
        } else if (mytype == InitType.RIGHT) {
            LAST_COL_CALCULATE += 1;
            LAST_ROW_CALCULATE += 1;
            rightCap = true;
        }

        //DEBUG
//        printTable(num_table_rows, num_table_cols, rows, cols);

        //table is now 0-indexed, which translates to k+srow and j+scol coordinates
        //this consideration is irrelevant to this table

        for (int k = 0; k < num_table_rows; k++) {
            TableEntry2[] row = new TableEntry2[num_table_cols];
            for (int j = 0; j < num_table_cols; j++) {
                row[j] = new TableEntry2();
            }
            table.add(row);       //initializes to move = DIAGONAL, score = 0
        }


        //Initialize first column/row (all zeroes)

        //constructor sets scores to 0
        //first row except corner set to RIGHT

        //TO DO

//        for (int j = 1; j < num_table_cols; j++) {
//            table.get(0)[j].move = TableEntry.RIGHT;
//        }
//
//        //first column except corner set to DOWN
//        for (int k = 1; k < num_table_rows; k++) {
//            table.get(k)[0].move = TableEntry.DOWN;
//        }


        //Upper left corner can not place a move.  So it is EMPTY; unless a left cap
        //leftCap handled by special treatment of first box (which is only place behavior changes)
        if (leftCap) {
            if (rows.charAt(0) == cols.charAt(0)) {
                table.get(0)[0].move = TableEntry2.DIAGONAL;
                table.get(0)[0].score = SmithWatermanTruncated2.MATCH;
                table.get(0)[0].fastScore = 1;
            } else {
                table.get(0)[0].move = TableEntry2.DIAGONAL;
                table.get(0)[0].score = SmithWatermanTruncated2.MISMATCH;
                table.get(0)[0].fastScore = 0;
            }
        } else {
            table.get(0)[0].move = TableEntry2.EMPTY;
        }

        //Need columns (text) and rows(text)


        //LAST_ROW_CALCULATE extended for
        int diagonal, down, right;
        int z = 0;
        boolean match = false;
        for (int k = srow; k <= LAST_ROW_CALCULATE; k++, z++) {
            // calculate row k and column k first
            for (int j = scol + z; j <= LAST_COL_CALCULATE; j++) {
                //calculate row k, column j next
                match = false;
                if (DNAcodes.equals(cols.charAt(j - scol), rows.charAt(k - srow))) {
                    //they match
                    match = true;
                    //ends of strings cannot be a match
                    if ((k != erow - 1 && j != ecol - 1) || (k != srow + 1 && j != scol + 1)) {  //TODO CHECK
                        diagonal = SmithWatermanTruncated2.MISMATCH + getScore(table, k - 1 - srow, j - 1 - scol);
                    } else {
                        diagonal = SmithWatermanTruncated2.MATCH + getScore(table, k - 1 - srow, j - 1 - scol);
                    }
                } else {
                    diagonal = SmithWatermanTruncated2.MISMATCH + getScore(table, k - 1 - srow, j - 1 - scol);
                }
                down = SmithWatermanTruncated2.GAP + getScore(table, k - 1 - srow, j - scol);
                right = SmithWatermanTruncated2.GAP + getScore(table, k - srow, j - 1 - scol);

                if (diagonal > down && diagonal > right) {
                    table.get(k - srow)[j - scol].move = TableEntry2.DIAGONAL;
                    table.get(k - srow)[j - scol].score = diagonal;
                    table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow - 1, j - scol - 1);
                    if (match)
                        table.get(k - srow)[j - scol].fastScore += 1;
                } else {
                    if (right == down) {
                        //table.get(k-srow)[j-scol].move = TableEntry2.TIE;
                        table.get(k - srow)[j - scol].move = TableEntry2.DOWN;
                        table.get(k - srow)[j - scol].score = right;
                        table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow, j - scol - 1);
                    } else if (right > down) {
                        table.get(k - srow)[j - scol].move = TableEntry2.RIGHT;
                        table.get(k - srow)[j - scol].score = right;
                        table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow, j - scol - 1);
                    } else {
                        table.get(k - srow)[j - scol].move = TableEntry2.DOWN;
                        table.get(k - srow)[j - scol].score = down;
                        table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow - 1, j - scol);
                    }
                }
            }   // end for j

            // Necessary Code
            // Do not understand the magic in how it works
            // I think this processes the column to the left?
            //This processes the the leftmost column
            //WE must have first row and first column to calculate second row and second column (DOWN and RIGHT operations)
            int j = scol + z;
            for (int x = srow + z + 1; x <= LAST_ROW_CALCULATE; x++) {
                match = false;
                if (DNAcodes.equals(cols.charAt(j - scol), rows.charAt(x - srow))) {
                    //they match
                    diagonal = SmithWatermanTruncated2.MATCH + getScore(table, x - srow - 1, j - scol - 1);
                    match = true;
                } else {
                    diagonal = SmithWatermanTruncated2.MISMATCH + getScore(table, x - srow - 1, j - scol - 1);
                }
                down = SmithWatermanTruncated2.GAP + getScore(table, x - srow, j - scol - 1);
                right = SmithWatermanTruncated2.GAP + getScore(table, x - srow - 1, j - scol);

                if (diagonal >= down && diagonal >= right) {
                    table.get(x - srow)[j - scol].move = TableEntry2.DIAGONAL;
                    table.get(x - srow)[j - scol].score = diagonal;
                    table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow - 1, j - scol - 1);
                    if (match)
                        table.get(k - srow)[j - scol].fastScore += 1;
                } else {
                    if (right == down) {
                        //table.get(x-srow)[j-scol].move = TableEntry2.TIE;
                        table.get(x - srow)[j - scol].move = TableEntry2.DOWN;
                        table.get(x - srow)[j - scol].score = right;
                        table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow, j - scol - 1);
                    } else if (right > down) {
                        table.get(x - srow)[j - scol].move = TableEntry2.RIGHT;
                        table.get(x - srow)[j - scol].score = right;
                        table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow - 1, j - scol);
                    } else {
                        table.get(x - srow)[j - scol].move = TableEntry2.DOWN;
                        table.get(x - srow)[j - scol].score = down;
                        table.get(k - srow)[j - scol].fastScore = getFastKlatScore(table, k - srow, j - scol - 1);
                    }
                }
            } // end for j

        } // end for k

        String DEBUG1 = "BREAK.POINT";


        //LEFT CAP TRACEBACK to account for LEFT unaligned portion (ending in first row or column prematurely, adjust score)
        //NEED TO TRACE BACK AND INCREASE SCORE
        //SUSPECT unnecessary because the ends will all be -1, because adjacent to lines of 0's existing in next row/column (off the table)

//        if (leftCap) {
//            traceBackLeft(table, rows.length() - 1, cols.length() - 1);
//        }

        if (rightCap) {
            //scan for all possibilities (boring)
            int lastCol = cols.length()-1;
            int lastRow = rows.length()-1;


            int maxScore = Integer.MIN_VALUE;
            int maxFast = Integer.MIN_VALUE;
            int sw, fk;

            // QUESTION: will all fastKlat and SW corresponding scores match?
            // I think not
            // SW(MATCH)  == SW(2 MATCH, 2 GAPS)

            for (int k=0; k <= lastRow; k++){
                sw = getScore(table, k, lastCol);
                fk = getFastKlatScore(table,k,lastCol);
                if (sw >= maxScore && fk >= maxFast){
                    maxScore = sw;
                    maxFast = fk;
                }
            }
            for (int j=0; j <= lastCol; j++){
                sw = getScore(table, lastRow,j);
                fk = getFastKlatScore(table,lastRow,j);
                if (sw >= maxScore && fk >= maxFast){
                    maxScore = sw;
                    maxFast = fk;
                }
            }

            cumulativeActualFastKlatScore = maxFast;
            cumulativeSmithWatermanScore = maxScore;

        } else {
            cumulativeActualFastKlatScore = previousFastKlat + table.get(LAST_ROW_CALCULATE)[LAST_COL_CALCULATE].fastScore;
            cumulativeSmithWatermanScore = previousSW + table.get(LAST_ROW_CALCULATE)[LAST_COL_CALCULATE].score;
        }

        String DEBUG2 = "BREAK.POINT";
        //return cumulativeActualFastKlatScore;
    }

    private void traceBackLeft(ArrayList<TableEntry2[]> table, int lastRow, int lastCol) {
        //int offset = -1;
        int r = lastRow;
        int c = lastCol;
        recurseBackLeft(table, r, c);
    }

    /**
     * TODO  is this needed? reading off the table edge will always make these entries -1 as lowest
     * @param table
     * @param r
     * @param c
     * @return
     */
    private int recurseBackLeft(ArrayList<TableEntry2[]> table, int r, int c){

        if (r==0 && c == 0){
            return 0;
        }
        else if ( (r == 0 || c == 0)) {  //STOP
            return - 1 - table.get(r)[c].score; //offset
        } else {
            TableEntry2 t = table.get(r)[c];
            int offset;
            if (t.move == TableEntry2.DIAGONAL){
                offset = recurseBackLeft(table, r-1, c-1);
            } else if (t.move == TableEntry2.RIGHT) {
                offset = recurseBackLeft(table, r, c - 1);
            } else {
                offset = recurseBackLeft(table, r-1, c);
            }
            t.score += offset; //adjust score
            //If this is always 0, then as I suspect, this function is unnecessary

            return offset;
            //IGNORE TIE PARAMETER -- could be mistaked for error handling
        }
    }


    /**
     * DEPRECATED
     * @param r
     * @param c
     * @return
     */
    public TableEntry2 getTableEntry(int r, int c) {
        int r2 = r - srow;  //to 0index of mini table
        int c2 = c - scol;
        return table.get(r2)[c2];
    }


//    /**
//     * Not needed?
//     * @return
//     */
//    private int calculateSWMaxScore(InitType type) {
//        int m = (ecol-scol);
//        int n = (erow-ecol);
//        if (m>n){
//            int t = m;
//            m = n;
//            n = t;
//        }
//
//        if (type == InitType.RIGHT || type == InitType.LEFT){
//            return SmithWatermanTruncated.MATCH*(n-1)+(m-n)*SmithWatermanTruncated.GAP;   //straight diagonal
//        }
//
//
//        // m >= n >= 3
//        // because includes the ends that are perfect matches, and must have a mismatch between
//        // special case
//        if (n == 3 && m == 3)
//            return SmithWatermanTruncated.MISMATCH;
//
//        //if GAP != MISMATCH, who knows, doesn't work
//        return SmithWatermanTruncated.MATCH*(Math.min(m-2,n-1)) + Math.max(2,(m-n-1)*SmithWatermanTruncated.GAP);
//    }
//
//    private int calculateMaximumFastKlatScore(InitType type) {
//        int m = (ecol-scol);
//        int n = (erow-ecol);
//        if (m>n){
//            int t = m;
//            m = n;
//            n = t;
//        }
//
//        if (type == InitType.RIGHT || type == InitType.LEFT){
//            return (n-1) - (m - n);   //straight diagonal minus any gaps necessary
//            //return 2*n - m -1;
//        }
//
//        // m >= n >= 3
//        // because includes the ends that are perfect matches, and must have a mismatch between
//        // special case
//        if (n == 3 && m == 3)
//            return -1;
//
//        //if GAP != MISMATCH, who knows, doesn't work
//        return (Math.min(m-2,n-1)) - Math.max(2,(m-n-1));
//    }
//
//    private int calculateMinimumFastKlatScore(InitType type) {
//        int m = (ecol-scol);
//        int n = (erow-ecol);
////        int result;
//        if (m>n){
//            int t = m;
//            m = n;
//            n = t;
//        }
//        return (n-1) - (m-n);
//    }
//
//
//    /**
//     * minimum private score for this area requiring calculation
//     * assumes InitType == MID
//     *
//     * NOT NEEDED?
//     * @return
//     */
//    private int calculateSWMinimumScore() {
//        int m = (ecol-scol);
//        int n = (erow-ecol);
////        int result;
//        if (m>n){
//            int t = m;
//            m = n;
//            n = t;
//        }
////        //entire diagonal of mismatches
////        result = (n-1) * SmithWatermanTruncated2.MISMATCH;
////        //any leftover gaps
////        result += (m-n)*SmithWatermanTruncated.GAP;
////        return result;
//        return (n-1) * SmithWatermanTruncated2.MISMATCH + (m-n)*SmithWatermanTruncated.GAP;
//    }
//
//    /**
//     * minimum private score for this area requiring calculation, given it is at right end
//     * @return
//     */
//    private int right() {
//        return SmithWatermanTruncated2.MISMATCH + calculateSWMinimumScore();
//    }
//
//    /**
//     * minimum private score for this area requiring calculation, given it is at left end
//     * @return
//     */
//    private int left() {
//        //the normally not included end may be a mismatch or gap, but basically the diagonal of possible mismatches is one longer
//
//        ecol=scol+1;
//        erow=srow+1;
//        scol = 0;
//        srow = 0;
//
//        return SmithWatermanTruncated2.MISMATCH + calculateSWMinimumScore();
//    }

    public String printTable(int num_table_rows, int num_table_cols, String rows, String columns){
        int mySize = 8;
        SuperString ss = new SuperString();
        ss.add(justifyLeft("",mySize));
        for (int j=1; j <= num_table_cols; j++) {
            ss.add(justifyLeft(columns.charAt(j-1),mySize));
        }
        ss.add("\n");
        String stringo = "";  //use string to create entry to justify
        for (int k=0; k < num_table_rows; k++){
            ss.add(justifyLeft(rows.charAt(k),mySize));

            for (int j=0; j < num_table_cols; j++) {
                if (table.get(k)[j].move == TableEntry2.DIAGONAL) {
                    stringo = "* ";
                } else if (table.get(k)[j].move == TableEntry2.DOWN) {
                    stringo = "^ ";
                } else if (table.get(k)[j].move == TableEntry2.RIGHT) {
                    stringo = "< ";
                } else {
                    stringo = "X ";
                }
                stringo += Integer.toString(table.get(k)[j].score);
                ss.add(justifyLeft(stringo, mySize));

            }
            ss.add("\n");
        }

        return ss.toString();
    }


    private String justifyLeft(char c, int size){
        SuperString ss = new SuperString();
        ss.add(c);
        for( int k=0; k < (size-1);k++){
                ss.add(" ");
            }

        return ss.toString();
    }

    private String justifyLeft(String s, int size){
        SuperString ss = new SuperString();
        ss.add(s);
        int l = s.length();
        if (l >= size ){
            ss.add(s.substring(0,l-1));
        } else {
            for( int k=0; k < (size-l); k++){
                ss.add(" ");
            }
        }
        return ss.toString();
    }
}
