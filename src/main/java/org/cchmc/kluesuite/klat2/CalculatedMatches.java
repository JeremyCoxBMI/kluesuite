package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klue.DNAcodes;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.SuperString;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * Created by jwc on 10/05/2017.
 *
 * For *gaps* _between_ long seeds (of adjacency) in SuperSeeds
 *
 * DESIGN DECISIONS
 *  - coordinates used will be the real coordinates of the alignment S/W table
 *      - so we just adjust by subtracting box start coordinates
 *  - sub table score always starts at 0, then the values are adjusted by previous
 *      - thus, a table calculation is not necessary if previous score were to change
 *
 */
public class CalculatedMatches extends Box2 {


    public InitType mytype;
    ArrayList<TableEntry2[]> table;

    /**
     * Note these rows represent the rows and column sequence letters
     * (the whole deal, not over range inclusive to inclusive)
     * (thus, only references are duplicated, not partial arrays)
     */
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
        cumulativeActualFastKlatScore = -1;
        cumulativeSmithWatermanScore = -1;
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
            this.ecol = seed.start ;//WANT OVERLAP
            this.erow = seed.queryStart;  //WANT OVERLAP
            this.scol=0;    //no matter where it starts, we include the whole thing
            this.srow=0;    //no matter where it starts, we include the whole thing
            cumulativeActualFastKlatScore = 0;
            cumulativeSmithWatermanScore = 0;
        }

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
     * Calculates the SW and FastKLAT score min and max
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

        //TODO check formulas
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
        if (table == null)
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

        constructCalculateMiniSWTable(previousSW, previousFastKlat);
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

    public static Integer getScore(ArrayList<TableEntry2[]> table, int r, int c) throws DataFormatException {

        if (r <0 || c < 0)
            //this sub-table always starts at 0; no negative indexes
            return 0;

        if (table == null){
            throw new DataFormatException("CalculatedMatches::getScore called with null ArrayList");
        }

//        if (r==4 && c == 4)
//            c++;

        TableEntry2 t = table.get(r)[c];
        if (t == null) {  //unintialized, intentional when row or column was skipped with FIRST_ROW_COL_CALCULATED=1
            return null;
        }
        return t.score;

    }

    public static Integer getFastKlatScore(ArrayList<TableEntry2[]> table, int r, int c){
        if (r < 0 || c < 0) {
            return 0;
        }else {
            TableEntry2 t = table.get(r)[c];
            if (t != null)
                return t.fastScore;
            else
                // score is relative to table  if uninitialized, this means we are in leftCap
                // thus we can end without reaching upper left corner
                // therefore, the fastKlatScore is 0
                return 0;
        }
    }

    /**
     * Constructs a subtable of smith-waterman alignment table and fills it in
     * This is the main function of this class
     * Here, the SW alignment is calculated
     *
     * @param previousSW
     * @param previousFastKlat
     */
    private void constructCalculateMiniSWTable(int previousSW, int previousFastKlat) {
        // **************************
        // construct table
        // **************************

        // **************************
        // major change over original: first row and column are not empty by definition !!!
        // however, if not leftCap, they will be empty because first position is a MATCH
        // **************************

        if (table != null){
            System.err.println("WARNING\t\tCAclulatedMAtches::constructCalculateMiniSWTable called when already constructed");
        }

        //first row and column include matching kmer end, so
        int num_table_rows = erow - srow + 1;  //INCLUSIVE INCLUSIVE
        int num_table_cols = ecol - scol + 1;

        table = new ArrayList<TableEntry2[]>(num_table_rows);


        //Programmer's note:
        // table is now 0-indexed relative to itself,
        // which translates to row = k+srow and col = j+scol coordinates
        // this consideration is irrelevant to this table, but relevant for get() functions

        for (int k = 0; k < num_table_rows; k++) {
            TableEntry2[] row = new TableEntry2[num_table_cols];
            // writeBestMove(..) now constructs the TableEntry2 in each "cell"
            table.add(row);       //initializes to move = DIAGONAL, score = 0
        }

        // to control filling in columns differently due to mytype AND adjacency on diagonal
        // is true IF allowed to be a match close to ends
        boolean leftCap = false;
        boolean rightCap = false;
        int LAST_ROW_CALCULATE = num_table_rows - 2;
        int LAST_COL_CALCULATE = num_table_cols - 2;
        int FIRST_ROW_COL_CALCULATE = 1;

        if (mytype == InitType.LEFT) {
            leftCap = true;
            FIRST_ROW_COL_CALCULATE = 0;
        } else if (mytype == InitType.RIGHT) {
            LAST_COL_CALCULATE += 1;
            LAST_ROW_CALCULATE += 1;
            rightCap = true;
        }

//        if (FIRST_ROW_COL_CALCULATE == 1){
//            table.get(0)[0] = new TableEntry2(0,0,0);//start blank slate
//        }

        table.get(0)[0] = new TableEntry2(0,0,0);//start blank slate

        // over table, move diagonally down, calculating row then column starting at the diagonal square position
        // do not calculate last row/col if perfect match is next

        int k,j;
        for (k = FIRST_ROW_COL_CALCULATE; k <= LAST_ROW_CALCULATE; k++) {
            // calculate row k and column k first

            for ( j = (k==0) ? 1 : k;  //skip cell 0,0
                  j <= LAST_COL_CALCULATE; j++) {
                //calculate row k, column j ; MOVING ACROSS ROW LEFT TO RIGHT
                table.get(k)[j] = writeBestMove(k,j,LAST_ROW_CALCULATE,LAST_COL_CALCULATE, leftCap,rightCap);
            }   // end for j

            // moving diagonally down right through array;
            // this row was at index k just calculated
            // this column at index k needs to be calculated
            //We must have first row and first column to calculate second row and second column (DOWN and RIGHT operations)

            //column remains fixed; going down
            int c = k;  //strictly unnecessary, used for clarity
            int r = k + 1;
            if (r <= LAST_ROW_CALCULATE) {
                for (; r <= LAST_ROW_CALCULATE; r++) {  // position k,k already calculated by row k calculations
                    table.get(r)[c] = writeBestMove(r, c, LAST_ROW_CALCULATE, LAST_COL_CALCULATE, leftCap, rightCap);
                } // end for j
            }

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
            int maxScore = Integer.MIN_VALUE;
            int maxFast = Integer.MIN_VALUE;
            Integer sw, fk;

            // QUESTION: will all fastKlat and SW corresponding scores match?
            // I think not
            // SW(MATCH)  == SW(2 MATCH, 2 GAPS)

            try {
                //scan last column
                for (k = 0; k <= LAST_ROW_CALCULATE; k++) {
                    sw = getScore(table, k, LAST_COL_CALCULATE);
                    fk = getFastKlatScore(table, k, LAST_COL_CALCULATE);

                    //short circuit if null returned
                    if (sw != null && sw >= maxScore)
                        maxScore = sw;
                    if (fk != null && fk >= maxFast)
                        maxFast = fk;

                }
                //scan last row
                for (j = 0; j <= LAST_COL_CALCULATE; j++) {
                    sw = getScore(table, LAST_ROW_CALCULATE, j);
                    fk = getFastKlatScore(table, LAST_ROW_CALCULATE, j);
                    if (sw != null && fk != null && sw >= maxScore && fk >= maxFast) {
                        maxScore = sw;
                        maxFast = fk;
                    }
                }
            } catch (DataFormatException e) {
                e.printStackTrace();
                System.exit(1);
            }
            cumulativeActualFastKlatScore = maxFast + previousFastKlat;
            cumulativeSmithWatermanScore = maxScore + previousSW;
        } else {
            //not rightcap, no scanning needed
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

    public String printTable(){
        int nr = table.size();
        int nc = table.get(0).length;
        return printTable(nr, nc, rows, cols);
    }


    public String printTable(int num_table_rows, int num_table_cols, String rows, String columns){
        int mySize = 8;
        SuperString ss = new SuperString();
        ss.add(justifyLeft("",mySize));
        System.out.println("DEBUG ROWS "+rows.substring(srow,erow+1));
        System.out.println("DEBUG COLS "+columns.substring(scol,ecol+1));

        for (int j=0; j <  num_table_cols; j++) {
            ss.add(justifyLeft(columns.charAt(j+scol),mySize));
        }
        ss.add("\n");
        String stringo = "";  //use string to create entry to justify
        for (int k=0; k < num_table_rows; k++){
            ss.add(justifyLeft(rows.charAt(k+srow),mySize));

            for (int j=0; j < num_table_cols; j++) {
                TableEntry2 t = table.get(k)[j];
                if (t == null){
                    stringo = "o __"; //use empty symbol, '__' for uninitialized
                } else if (t.move == TableEntry2.DIAGONAL) {
                    stringo = "* ";
                    stringo += Integer.toString(t.score);
                } else if (t.move == TableEntry2.DOWN) {
                    stringo = "^ ";
                    stringo += Integer.toString(t.score);
                } else if (t.move == TableEntry2.RIGHT) {
                    stringo = "< ";
                    stringo += Integer.toString(t.score);
                } else if (t.move == TableEntry2.EMPTY) {
                    stringo = "o ";
                    stringo += Integer.toString(t.score);
                } else {
                    stringo = "problem";
                }
//                stringo += Integer.toString(table.get(k)[j].score);
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

    /**
     *
     * @param k     row coordinate
     * @param j     col coordinate
     * @param LAST_ROW_CALCULATE    from construct table; determines proper edge
     * @param LAST_COL_CALCULATE    from construct table; determines proper edge
     * @param leftCap   is first column & first row calculated (left most sub-table)
     * @param rightCap  is last column &  last  row calculated (right most sub-table)
     * @return
     */
    private TableEntry2 writeBestMove(int k, int j, int LAST_ROW_CALCULATE, int LAST_COL_CALCULATE, boolean leftCap, boolean rightCap){
        //calculate row k, column j next
        boolean match = false;

        //TODO fix calculations

        //pre-existing values
        //return null if non-existant
        Integer diagonal = null;
        Integer down = null;
        Integer right = null;
        try {
            diagonal = getScore(table, k - 1, j - 1);
            down = getScore(table, k - 1, j);
            right = getScore(table, k, j - 1);
        } catch (DataFormatException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //convert from relative 0 coordinates to actual query/reference String coordinates
        if (diagonal != null) {
            if (DNAcodes.equals(cols.charAt(j + scol), rows.charAt(k + srow))) {
                //they match
                match = true;

                //ends of strings cannot be a match
                if ((!leftCap && (k == 0) && (j == 0))
                        ||
                        (!rightCap && (k == LAST_ROW_CALCULATE) && (j == LAST_COL_CALCULATE))
                        ) {
                    diagonal += SmithWatermanTruncated2.MISMATCH;
                } else {
                    diagonal += SmithWatermanTruncated2.MATCH;
                }
            } else {
                diagonal += SmithWatermanTruncated2.MISMATCH;
            }
        } else {
            //illegal move; path must connect to top left
            diagonal = Integer.MIN_VALUE;  //will not win
        }
        if (down != null) {
            down += SmithWatermanTruncated2.GAP;
        } else {
            //illegal move; path must connect to top left
            down = Integer.MIN_VALUE;  //will not win
        }
        if (right != null) {
            right += SmithWatermanTruncated2.GAP;
        } else {
            //illegal move; path must connect to top left
            right = Integer.MIN_VALUE;  //will not win
        }

        return writeBestMove(k, j, match, diagonal, down, right);
    }


    /**
     * records the best move for the position indicated by coordinate (r,c) to the tableEntry
     * constructs the tableEntry2, using branching logic to determine which is best
     *
     * @param r
     * @param c
     * @param match
     * @param diagonal
     * @param down
     * @param right
     */
    private TableEntry2 writeBestMove(int r, int c, boolean match, int diagonal, int down, int right) {
        //DEFAULT MOVE IS DOWN
        //IN CASE OF TIE, use diagonal move for score -- could be a match
        int move, score, fastScore;
//        if (right == -3)
//            "DEBUG".equals("HERE");
        if (diagonal == down && down == right) {
            //three way tie
            move = TableEntry2.TIE;
            score = diagonal;


            int tmp = getFastKlatScore(table, r - 1, c - 1);  //+1 for match
            if (match){
                fastScore = tmp+1;
            } else {
                int x = Math.max(getFastKlatScore(table,r-1,c),getFastKlatScore(table,r,c-1));
                fastScore = Math.max(x, tmp);
            }

//            int tmp = fastScore = Math.max(
//                    getFastKlatScore(table, r, c - 1),  //right
//                    getFastKlatScore(table, r - 1, c)  //down
//            );
//
//            if (match)
//                fastScore = Math.max(
//
//                        tmp
//                );
//            else
//                fastScore = Math.max(
//                        getFastKlatScore(table, r - 1, c - 1),
//                        tmp
//                );
        } else if (diagonal > down && diagonal > right) {
            move = TableEntry2.DIAGONAL;
            score = diagonal;
            fastScore = getFastKlatScore(table, r - 1, c - 1);
            if (match)
                fastScore += 1;
        } else {
            //then down or right is winner
            if (right == down) {
                move = TableEntry2.DOWN;
                score = right;
                fastScore = getFastKlatScore(table, r, c - 1);
            } else if (right > down) {
                move = TableEntry2.RIGHT;
                score = right;
                fastScore = getFastKlatScore(table, r, c - 1);
            } else {  // right < down
                move = TableEntry2.DOWN;
                score = down;
                fastScore = getFastKlatScore(table, r - 1, c);
            }
        }

        return new TableEntry2(move, score, fastScore);
    }

}
