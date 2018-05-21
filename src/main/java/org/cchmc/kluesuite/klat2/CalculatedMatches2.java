package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klat.TableEntry;
import org.cchmc.kluesuite.klue.DNAcodes;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.SuperString;

import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * Created by jwc on 4/11/18.
 *
 * Intended to replace CalculatedMatches class
 * Uses new kind of less pointers used table
 *
 */
public class CalculatedMatches2 extends  Box2 {

    public MiniatureSwTable table;
    public InitType mytype;
//    ArrayList<TableEntry2[]> table;

    /**
     * Note these rows represent the rows and column sequence letters
     * (the whole deal, not over range inclusive to inclusive)
     * (thus, only references are duplicated, not partial arrays)
     */
    String rows;
    String cols;

    //global, passed between functions

    //private int FIRST_ROW_COL_CALCULATED;

    public CalculatedMatches2(InitType mytype, int srow, int scol, int erow, int ecol,
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


    public CalculatedMatches2(Seed seed, InitType mytype,
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
    public CalculatedMatches2(Seed prev, Seed curr,
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

        int d1, g1, x1, p1, SWscore1;
        int d2, g2, x2, p2, SWscore2;
        //diagonal moves, gap moves, forced mismatches, potential matches
        if (mytype == InitType.RIGHT || mytype == InitType.LEFT){

            d1 = n - 1;
            d2 = n-1;
            if (m == n) {
                x1 = 1; //square cannot use offset diagonal
                g1 = 0;
            } else {
                x1 = 0;
                g1 = 1;
            }
            x2 = 1;
            g2 = 0;
            p1 = d1 - x1;
            p2 = d2 - x2;

        } else {
            //middle type; two corners

            d1 = n - 2;
            d2 = n - 3;
            g1 = m - n;
            g2 = m - n + 2;
            x2 = 0;

            if (m == 3 && n == 3) {
                x1 = 1;
            } else {
                x1 = 2;
            }
            p1 = d1 - x1;
            p2 = d2;
        }
        //best score
        SWscore1 = p1 * SmithWatermanTruncated3.MATCH +
                   x1 *  SmithWatermanTruncated3.MISMATCH +
                    g1 * SmithWatermanTruncated3.GAP;
        SWscore2 = p2 * SmithWatermanTruncated3.MATCH +
                x2 *  SmithWatermanTruncated3.MISMATCH +
                g2 * SmithWatermanTruncated3.GAP;

        cumulativeMaximumSWscore += Math.max(SWscore1,SWscore2);

        //worst score
        SWscore1 =
                d1 *  SmithWatermanTruncated3.MISMATCH +
                g1 * SmithWatermanTruncated3.GAP;
        SWscore2 =
                d2 *  SmithWatermanTruncated3.MISMATCH +
                g2 * SmithWatermanTruncated3.GAP;
        cumulativeMinimumSWscore += Math.min(SWscore1,SWscore2);

        cumulativeMaximumFastKlatScore += Math.max(p1,p2);
        // adding 0 is pointless
        // cumulativeMinimumFastKlatScore += 0;

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

        return cumulativeActualFastKlatScore;
    }

    public static Integer getScore(MiniatureSwTable table, int r, int c, int first_row_col){

        //uninitialized, return null
        //uninitialized occurs if we are reading first row or column when not allowed
        if (r < first_row_col || c < first_row_col)
            return null;

        return table.getScore(r, c);

    }

    public static Integer getFastKlatScore(MiniatureSwTable table, int r, int c, int first_row_col){
        if (r < first_row_col || c < first_row_col)
            return null;

        return table.getFastScore(r,c);
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
            System.err.println("WARNING\t\tCAclulatedMAtches2::constructCalculateMiniSWTable called when already constructed");
        }

        //first row and column include matching kmer end, so
        int num_table_rows = erow - srow + 1;  //INCLUSIVE INCLUSIVE
        int num_table_cols = ecol - scol + 1;

        //Programmer's note:
        // table is now 0-indexed relative to itself,
        // which translates to row = k+srow and col = j+scol coordinates
        // this consideration is irrelevant to this table, but relevant for get() functions


        // to control filling in columns differently due to mytype AND adjacency on diagonal
        // is true IF allowed to be a match close to ends
        boolean leftCap = false;
        boolean rightCap = false;
        int LAST_ROW_CALCULATE = num_table_rows - 2;
        int LAST_COL_CALCULATE = num_table_cols - 2;

        //bug always claculate first row
        int FIRST_ROW_COL_CALCULATE = 0;

//        if (mytype == InitType.LEFT) {
//            leftCap = true;
//            FIRST_ROW_COL_CALCULATE = 0;
//
//        } else
        if (mytype == InitType.RIGHT) {
            LAST_COL_CALCULATE += 1;
            LAST_ROW_CALCULATE += 1;
            rightCap = true;
        }

        table = new MiniatureSwTable(num_table_rows, num_table_cols, FIRST_ROW_COL_CALCULATE);

        //start blank slate
        table.setEntry(0,0,TableEntry2.DIAGONAL,0,0);

        // over table, move diagonally down, calculating row then column starting at the diagonal square position
        // do not calculate last row/col if perfect match is next

        int k,j;
        for (k = FIRST_ROW_COL_CALCULATE; k <= LAST_ROW_CALCULATE; k++) {
            // calculate row k and column k first


            //BUG: we do calculate first row and column == in sparseTable, first is not the blank start
//            for ( j = (k==0) ? 1 : k;  //skip cell 0,0
//                  j <= LAST_COL_CALCULATE; j++) {
                for ( j = k; j <= LAST_COL_CALCULATE; j++) {
                //calculate row k, column j ; MOVING ACROSS ROW LEFT TO RIGHT

                boolean match = DNAcodes.equals(cols.charAt(j + scol), rows.charAt(k + srow));
                table.writeBestMove(k,j,LAST_ROW_CALCULATE,LAST_COL_CALCULATE, match, leftCap,rightCap);
            }   // end for j

            // moving diagonally down right through array;
            // this row was at index k just calculated
            // this column at index k needs to be calculated
            //We must have first row and first column to calculate second row and second column (DOWN and RIGHT operations)

            //column remains fixed; going down
            int c = k;  //strictly unnecessary, used for clarity
            int r = k + 1;
            for (; r <= LAST_ROW_CALCULATE; r++) {  // position k,k already calculated by row k calculations
                boolean match = DNAcodes.equals(cols.charAt(c + scol), rows.charAt(r + srow));
                table.writeBestMove(r,c,LAST_ROW_CALCULATE,LAST_COL_CALCULATE, match, leftCap,rightCap);
            } // end for r


        } // end for k


//        String DEBUG1 = "BREAK.POINT";


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


            //scan last column
            for (k = 0; k <= LAST_ROW_CALCULATE; k++) {
                sw = table.getScore(k, LAST_COL_CALCULATE);
                fk = table.getFastScore(k,LAST_COL_CALCULATE);

                //short circuit if null returned
                if (sw != null && sw >= maxScore)
                    maxScore = sw;
                if (fk != null && fk >= maxFast)
                    maxFast = fk;

            }
            //scan last row
            for (j = 0; j <= LAST_COL_CALCULATE; j++) {
                sw = table.getScore(LAST_ROW_CALCULATE, j);
                fk = table.getFastScore(LAST_ROW_CALCULATE, j);
//                if (sw != null && fk != null && sw >= maxScore && fk >= maxFast) {
//                    maxScore = sw;
//                    maxFast = fk;
//                }
                //short circuit if null returned
                if (sw != null && sw >= maxScore)
                    maxScore = sw;
                if (fk != null && fk >= maxFast)
                    maxFast = fk;
            }

            cumulativeActualFastKlatScore = maxFast + previousFastKlat;
            cumulativeSmithWatermanScore = maxScore + previousSW;
        } else {
            //not rightcap, no scanning needed
            cumulativeActualFastKlatScore = previousFastKlat + table.getFastScore(LAST_ROW_CALCULATE,LAST_COL_CALCULATE);
            cumulativeSmithWatermanScore = previousSW + table.getScore(LAST_ROW_CALCULATE, LAST_COL_CALCULATE);
        }

//        String DEBUG2 = "BREAK.POINT";
        //return cumulativeActualFastKlatScore;
    }

    private void traceBackLeft(int lastRow, int lastCol) {
        //int offset = -1;
        int r = lastRow;
        int c = lastCol;
        recurseBackLeft(r, c);
    }

    /**
     * TODO  is this needed? reading off the table edge will always make these entries -1 as lowest
     * @param r
     * @param c
     * @return
     */
    private int recurseBackLeft(int r, int c){

        if (r==0 && c == 0){
            return 0;
        }
        else if ( (r == 0 || c == 0)) {  //STOP
            return - 1 - table.getScore(r,c); //offset

        } else {
            int move = table.getMove(r,c);
            int offset;
            if (move == TableEntry2.DIAGONAL){
                offset = recurseBackLeft(r-1, c-1);
            } else if (move == TableEntry2.RIGHT) {
                offset = recurseBackLeft( r, c - 1);
            } else {
                offset = recurseBackLeft(r-1, c);
            }

            table.setScore(r,c,table.getScore(r,c)+offset);//adjust score

            //If this is always 0, then as I suspect, this function is unnecessary

            return offset;
            //IGNORE TIE PARAMETER -- could be mistaked for error handling
        }
    }


//    /**
//     * DEPRECATED
//     * @param r
//     * @param c
//     * @return
//     */
//    public TableEntry2 getTableEntry(int r, int c) {
//        int r2 = r - srow;  //to 0index of mini table
//        int c2 = c - scol;
//        return table.get(r2)[c2];
//    }

    public String printTable(){
        int nr = erow - srow + 1;  //INCLUSIVE INCLUSIVE
        int nc = ecol - scol + 1;
        return printTable(nr, nc, rows, cols);
    }

    public String printTableFK(){
        int nr = erow - srow + 1;  //INCLUSIVE INCLUSIVE
        int nc = ecol - scol + 1;
        return printTableFK(nr, nc, rows, cols);
    }


    public String printTable(int num_table_rows, int num_table_cols, String rows, String columns){
        int mySize = 8;
        SuperString ss = new SuperString();
        ss.add(justifyLeft("",mySize));
//        System.out.println("DEBUG ROWS "+rows.substring(srow,erow+1));
//        System.out.println("DEBUG COLS "+columns.substring(scol,ecol+1));

        for (int j=0; j <  num_table_cols; j++) {
            ss.add(justifyLeft(columns.charAt(j+scol),mySize));
        }
        ss.add("\n");
        String stringo = "";  //use string to create entry to justify
        for (int k=0; k < num_table_rows; k++){
            ss.add(justifyLeft(rows.charAt(k+srow),mySize));

            for (int j=0; j < num_table_cols; j++) {
                Integer move = table.getMove(k, j);
                Integer score = table.getScore(k, j);
                //Integer fastScore = table.getFastScore(k,j);

                if (move == null){
                    stringo = "o __"; //use empty symbol, '__' for uninitialized
                } else if (move == TableEntry2.DIAGONAL) {
                    stringo = "* ";
                    stringo += Integer.toString(score);
                } else if (move == TableEntry2.DOWN) {
                    stringo = "^ ";
                    stringo += Integer.toString(score);
                } else if (move == TableEntry2.RIGHT) {
                    stringo = "< ";
                    stringo += Integer.toString(score);
                } else if (move == TableEntry2.EMPTY) {
                    stringo = "o ";
                    stringo += Integer.toString(score);
                } else {
                    stringo = "problem";
                }

                ss.add(justifyLeft(stringo, mySize));

            }
            ss.add("\n");
        }

        return ss.toString();
    }

    public String printTableFK(int num_table_rows, int num_table_cols, String rows, String columns){
        int mySize = 8;
        SuperString ss = new SuperString();
        ss.add(justifyLeft("",mySize));
//        System.out.println("DEBUG ROWS "+rows.substring(srow,erow+1));
//        System.out.println("DEBUG COLS "+columns.substring(scol,ecol+1));

        for (int j=0; j <  num_table_cols; j++) {
            ss.add(justifyLeft(columns.charAt(j+scol),mySize));
        }
        ss.add("\n");
        String stringo = "";  //use string to create entry to justify
        for (int k=0; k < num_table_rows; k++){
            ss.add(justifyLeft(rows.charAt(k+srow),mySize));

            for (int j=0; j < num_table_cols; j++) {
                Integer move = table.getMove(k, j);
                Integer score = table.getFastScore(k, j);


                if (move == null){
                    stringo = "o __"; //use empty symbol, '__' for uninitialized
                } else if (move == TableEntry2.DIAGONAL) {
                    stringo = "* ";
                    stringo += Integer.toString(score);
                } else if (move == TableEntry2.DOWN) {
                    stringo = "^ ";
                    stringo += Integer.toString(score);
                } else if (move == TableEntry2.RIGHT) {
                    stringo = "< ";
                    stringo += Integer.toString(score);
                } else if (move == TableEntry2.EMPTY) {
                    stringo = "o ";
                    stringo += Integer.toString(score);
                } else {
                    stringo = "problem";
                }

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

    @Override
    public ArrayList<PairRC> getWinnerAlignmentCoordinates(){
        return table.getWinners(srow, scol);
    }

    public int getScoreAt(int r, int c) {
        return table.getScore(r, c);
    }

    public int getMoveAt(int r, int c) {
        return table.getMove(r,c);
    }

//    public TableEntry getTableEntry(int r, int c) {
//        table.get
//        return null;
//    }
}
