package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.PartialAlignment1;
import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klat.SmithWaterman;
import org.cchmc.kluesuite.klat.TableEntry;
import org.cchmc.kluesuite.klue.DNAcodes;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.masterklue.KLATsettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;

/**
 * Created by jwc on 8/30/17.
 *
 * This Smith-Waterman uses a faster algorithm for parsing the best path, based on having known perfect matches
 * from SuperSeed class
 *
 * Change: does not build the alignment strings
 *
 * SPECIAL NOTE:  does not process the GAP character from FastA format
 *
 * This plays roll of helper class to AlignmentKLAT1 and PartialAlignment1.
 * All alignment statistics (as a separation of concerns) are in PartialAlignment1
 *
 * We can of course fill in the rectangles between known diagonals.
 * Is forcing alignment to use the exact match series mathematically sound?
 *  Possibly, we will have to identify where the best score falls on the last row of/col of a rectangle
 *
 * First concern should be paring down the alignments to do.
 *
 * WARNING IF   MISMATCH != GAP, this will not work.
 *
 *
 * DEPRECATED : also not completely functional
 */


    //needed at this juncture?
public class SmithWatermanTruncated implements SmithWaterman, Comparable<SmithWatermanTruncated>, Comparator<SmithWatermanTruncated> {

    //according to wikipedia https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm#Example//
//    static int MATCH = 2;
//    static int MISMATCH = -1;
//    static int GAP = -1;

    static int MATCH = KLATsettings.SMITH_WATERMAN_MATCH;        //default 2
    static int MISMATCH = KLATsettings.SMITH_WATERMAN_MISMATCH;    //default -1
    static int GAP = KLATsettings.SMITH_WATERMAN_GAP;                //default -1

    /**
     * True if rows represent query.  TRUE : This is the standard layout of tradition
     */
    boolean queryISrows;

    /**
     * Values of query and reference
     * Note queryISrows indicates which is which
     * Note this is 0-index, but corresponding table coordinate is 1-indexed for same characters,
     * with 0 being all zero starting position
     */
    final String columns, rows;

    /**
     * Table representing the S-W calculation
     */
    ArrayList<TableEntry[]> table;

    private int kid;

    /**
     * the 0-indexed index referring the first letter in reference sequence used in the alignment
     */
    private int refStart;


    /**
     * During buildDiagonalRectangles, determines if a known alignment (know Diagonal seed) is first
     * or if a known alignment gap (rectangle) is first
     */
    boolean seedFirst;

    /**
     *  List of all diagonal adjacent seed streaks known to match by the seeds
     */
    ArrayList<Diagonal> knownDiagonals;

    /**
     * represent the range of the unaligned regions as rectangles from one position to another
     * note if this touches end of sequences (0 or max cooridinate), it has more max_score
     * THE ends of the adjacent seeds are INCLUDED in the rectangle coordinates, as this rectangle
     * will be  filled in completely in the table
     */
    ArrayList<Diagonal> rectangles;

    /**
     * Contains seeds calculated by parent Alignment Class, passed in during constructor
     */
    SuperSeed superSeed;

    /**
     *  this represents the maximum possible fast klat score that can be achieved by the alignment
     */
    public int maxPossibleFastKlatScore;

    /**
     *   the final calculated fastKlatScore
     */
    public int fastKlatScore;

    /**
     * Constructor
     *
     * Distinction between sequences makes no difference to algorithm
     *
     * @param query     the query for local alignment
     * @param refseq    the reference sequence for local alignment
     */
    public SmithWatermanTruncated(String query, String refseq, SuperSeed superSeed){
        this.kid = superSeed.myKid;
        this.refStart = superSeed.start;
        this.superSeed = superSeed;
        if (refseq.length() > query.length()) {
            columns = refseq;
            rows = query;
            queryISrows = true;
        } else {
            columns = query;
            rows = refseq;
            queryISrows = false;
        }


        fastKlatScore = Integer.MIN_VALUE;
        table = buildEmptyTable(columns, rows);

        //build arrays representing sub-tables aligned and unaligned sub-tables
        buildKnownDiagonalsAndRectangles();
        //viewTable();
        calculateTable();
    }

    private void viewTable(ArrayList<TableEntry[]> table) {
    }

    /**
     * Build known diagonals representing the known perfect matches
     * Build rectangles representing gaps between known alignments by SuperSeed
     * Calculates maximum alignment score
     *
     */
    private void buildKnownDiagonalsAndRectangles() {

        //Issue #76   TODO: is conversion to row/column coordinates accurate?

        maxPossibleFastKlatScore = 0;
        Diagonal d;

        rectangles = new ArrayList<>(superSeed.numSeeds() + 1);
        knownDiagonals = new ArrayList<>(superSeed.numSeeds());

        //is the first object a seed? (usually a rectangle!)
        seedFirst = true;  //will be proven false and set

        int refStop;  //INCLUSIVE
        int qStop;
        if (queryISrows){
            refStop = refStart + columns.length();
            qStop = rows.length();
        } else {
            refStop = refStart + rows.length();
            qStop = columns.length();
        }

        Iterator<Seed> it = superSeed.iterator();
        Seed curr = null, prev = null;

        if (it.hasNext()) {
            curr = it.next();
        } else {
            return;
        }

        //PROCESS CAP SEQUENCE BEFORE FIRST SEED, if it exists
        //convert to faster comparison
        //!(curr.start == refStart || curr.queryStart == 0)
        if (curr.start != refStart && curr.queryStart != 0) {
            seedFirst = false;
            d = leftRectangle(curr);
            rectangles.add(d);
            maxPossibleFastKlatScore += maxPossibleGapFastKlatScore(d, true);
        }

        //PROCESS FIRST SEED
        knownDiagonals.add(seedToDiagonal(curr));
        maxPossibleFastKlatScore += curr.hits;  //maxPossibleGapFastKlateScore(d, true);

        prev=curr;
        //PROCESS SEEDS AND GAPS
        //First seed and cap (if it exists) processed
        while (it.hasNext()){
            curr = it.next();

            //write between gaps
            d = processGapToDiagonal(curr, prev);
            rectangles.add(d);
            maxPossibleFastKlatScore += maxPossibleGapFastKlatScore(d, false);

            //write new diagonal
            knownDiagonals.add(seedToDiagonal(curr));
            maxPossibleFastKlatScore += curr.hits;  //maxPossibleGapFastKlateScore(d, true);

            prev = curr;
        }

        //PROCESS LAST GAP, IFF EXISTS
        //if last seed has gap afterwards

        //if neither reaches the end
        if (!(prev.end + Kmer31.KMER_SIZE_MINUS_ONE  == refStop
                ||
                prev.queryEnd +Kmer31.KMER_SIZE_MINUS_ONE == qStop)) {

            //write between gaps

            d = rightRectangle(prev);
            rectangles.add(d);
            maxPossibleFastKlatScore += maxPossibleGapFastKlatScore(d, true);
        }//end if

    } //end function buildDiagonalRectangle

    /**
     * calculates the rectangle to right of last known diagonal
     * @param prev
     * @return
     */
    private Diagonal rightRectangle(Seed prev) {
        //RightRectangle has to extend 1 past the end of the table, so that we can calculate last row and column
        //because the last column and row is skipped by miniAlign

        Diagonal result = seedToDiagonal(prev);
        if (queryISrows){
            //(int srow, int scol, int erow, int ecol )
            result = new Diagonal(result.erow, result.ecol, rows.length()+1, columns.length()+1);
        } else {
            result = new Diagonal(result.erow, result.ecol, columns.length()+1, rows.length()+1);
        }
        return result;
    }

    /**
     * Calualates the rectangle to the left of the first knownDiagonal
     * @param next
     * @return
     */
    private Diagonal leftRectangle(Seed next) {
        Diagonal result = seedToDiagonal(next);
        //(int srow, int scol, int erow, int ecol ){
        result = new Diagonal(0, 0, result.srow, result.scol);
//        if (queryISrows){
//            result = new Diagonal(0, result.srow, 0, result.scol);
//        } else {
//            result = new Diagonal(0, next.start, 0, next.queryStart);
//        }
        return result;
    }


    /**
     * based on leftmost seed, determines how far to extend the reference lookup
     * proabaly less than the max_whisker
     *
     * @param curr
     * @return
     */
    private Diagonal leftCap(Seed curr) {
        Diagonal d;

        //it seems that there is a maximum distance to go for alignment, need not extend too gratuitously
        // let x = min (rows, cols)
        // max of x matches
        // minimum score is x mismatches
        //so how many spaces can we go before it is gratuitious?
        //  maximum x + y
        //   MATCH*x + MISMATCH y > MISMATCH x
        //   MISMATCH y > (MISMATCH - MATCH) x
        //    -y > -3x
        //    y < 3x
        //    x+y < 4x


        int x = curr.queryEnd - curr.queryStart;
        int y = curr.end - curr.start;
        int max_distance = Math.min(x,y) * 4;
        int rowStart, colStart;
        if (queryISrows) {
            colStart = Math.max(1, curr.start - refStart - max_distance);
            rowStart = Math.max(1, curr.queryStart - max_distance);
        } else {
            rowStart = Math.max(1, curr.start - refStart - max_distance);
            colStart = Math.max(1, curr.queryStart - max_distance);
        }

        return new Diagonal(rowStart, colStart, curr.queryStart, curr.start);
    }

    /**
     * based on right most seed, how far should the lookup extend in the reference sequence
     * @param curr
     * @return
     */
    private Diagonal rightCap(Seed curr) {
        Diagonal d;

        int x = curr.queryEnd - curr.queryStart;
        int y = curr.end - curr.start;
        int max_distance = Math.min(x,y) * 4;
        int rowEnd, colEnd;
        if (queryISrows) {
            colEnd = Math.min(columns.length(), curr.end - refStart + max_distance -1 );
            rowEnd = Math.min(rows.length(),    curr.queryEnd + max_distance - 1);
        } else {
            rowEnd = Math.max(1, curr.end - refStart + max_distance -1);
            colEnd = Math.max(1, curr.queryEnd + max_distance - 1);
        }

        return new Diagonal(curr.queryEnd, curr.end, rowEnd, colEnd);
    }


    private Diagonal seedToDiagonal(Seed curr) {
        Diagonal d;
        if (queryISrows) {
            d = new Diagonal(curr.queryStart + 1, curr.start + 1 - refStart,
                    curr.queryEnd + Kmer31.KMER_SIZE_MINUS_ONE, curr.end - refStart+Kmer31.KMER_SIZE_MINUS_ONE);
        } else {
            d = new Diagonal(curr.start + 1 - refStart, curr.queryStart + 1,
                    curr.end - refStart + Kmer31.KMER_SIZE_MINUS_ONE, curr.queryEnd + Kmer31.KMER_SIZE_MINUS_ONE);
                    //0 index to 1-index (+1), exclusive end to inclusive end (-1)
        }
        return d;
    }


    /**
     * This method takes two seeds and creates the gap diagonal between them
     * @param curr
     * @param prev
     * @return
     */
    private Diagonal processGapToDiagonal(Seed curr, Seed prev) {
        Diagonal d;

//        if(prev == null){
//            d = new Diagonal(1,1,curr.queryStart+1, curr.start+1 );
//
//        }

        if (queryISrows) {
            d =new Diagonal(

                    prev.queryEnd +Kmer31.KMER_SIZE_MINUS_ONE, prev.end - refStart + Kmer31.KMER_SIZE_MINUS_ONE,  //exclusive to INCLUSIVE: -1; 0 index to 1 index: +1
                    curr.queryStart + 1, curr.start - refStart + 1

            );
        } else {
            d = new Diagonal(
                    prev.end - refStart+Kmer31.KMER_SIZE_MINUS_ONE , prev.queryEnd +Kmer31.KMER_SIZE_MINUS_ONE,
                    curr.start + 1 - refStart, curr.queryStart + 1
            );
        }
        return d;
    }


    /**
     * maximum possible number of matches from an unaligned region (i.e. Fast Klat score)
     * @param d     Diagonal representing the region, where both ends INCLUSIVE to seed start/end
     * @param cap   TRUE if between nothing and one seed, FALSE if between two seeds
     * @return
     */
    private int maxPossibleGapFastKlatScore(Diagonal d, boolean cap) {
        int m = d.ecol - d.scol + 1;  //both INCLUSIVE
        int n = d.erow - d.srow + 1;

        if (cap){
            return Math.min(m-2,n-1);
        } else {
            return Math.min(m-4,n-1);
        }
        //S-W score
        //return MATCH * Math.min(m-4,n-1)+MISMATCH * Math.max(m-n-1,2);
    }

    /**
     * Builds a complete Smith-Waterman calculation table.
     * @param cols
     * @param rows
     * @return
             */
     private ArrayList<TableEntry[]> buildEmptyTable(String cols, String rows) {

        //we must have an extra row and column to start alignment of all zeroes
        int num_table_rows = rows.length() + 1;
        int num_table_cols = cols.length() + 1;
        //cols is already forced to be the longest
        ArrayList<TableEntry[]> result = new ArrayList<TableEntry[]>(rows.length()+1);

        //
        for (int k = 0; k < num_table_rows; k++) {
            TableEntry[] row = new TableEntry[num_table_cols];
            for (int j = 0; j < num_table_cols; j++){
                row[j] = new TableEntry();
            }
            result.add(row);       //initializes to move = DIAGONAL, score = 0
        }


        //Initialize first column/row (all zeroes)
        //+1 because first row and first column is "blanks" full of zeroes

        //constructor sets scores to 0
        //first row except corner set to RIGHT


        for (int j = 1; j < num_table_cols; j++) {
            result.get(0)[j].move = TableEntry.RIGHT;
        }

        //first column except corner set to DOWN
        for (int k = 1; k < num_table_rows; k++) {
            result.get(k)[0].move = TableEntry.DOWN;
        }

        //Upper left corner can not place a move.  So it is EMPTY
        result.get(0)[0].move = TableEntry.EMPTY;

        //superseed will be used in buildKnownDiagonalsAndRectangles();

        return result;
    }


    /**
     * Key change to SmithWaterManAdvanced -- uses SuperSeeds and only fills table in limited amount
     * Algorithm is serial, because we parallelize alignments.
     *
     * MUST BUILD TABLE ENTRIES AS WE GO
     */
    private void calculateTable() {
        int m = columns.length();
        int n = rows.length();

        int diag = 0;
        int rect = 0;


        while (diag < knownDiagonals.size() || rect < rectangles.size()){
            if (rect < diag) { //rectangle goes
                miniAlign(rectangles.get(rect));
                rect++;
            } else {
                slideDownSeed(knownDiagonals.get(diag));
                diag++;
            }
        } //end while


//        if (seedFirst) {
//            slideDownSeed(knownDiagonals.get(diag));
//            diag++;
//
//
//            while (diag < knownDiagonals.size() || rect < rectangles.size()){
//                if (rect < diag) { //rectangle goes
//                    miniAlign(rectangles.get(rect));
//                    rect++;
//                } else {
//                    slideDownSeed(knownDiagonals.get(diag));
//                    diag++;
//                }
//            } //end while
//        } else {
//            miniAlign(rectangles.get(rect));
//            rect++;
//
//            while (diag < knownDiagonals.size() || rect < rectangles.size()){
//                if (diag < rect) { //rectangle goes
//                    slideDownSeed(knownDiagonals.get(diag));
//                    diag++;
//                } else {
//                    miniAlign(rectangles.get(rect));
//                    rect++;
//                }
//            } //end while
//        } // end else
    } // end calculateTable()

    private void miniAlign(Diagonal d) {

        //Not that WE DO NOT PROCESS last row and last column

        int diagonal, down, right;  //score if said move is made

        //BUILD TABLE
        int offset = 0;
        int k=0,j=0;

        for (k = d.srow; k < d.erow; offset++,k++) {   //d.erow inclusive, but not processed
            // offset allows j to start higher and higher each loop

            //calculate row, stepping right one space each time (table fills diagonally)
            for (j = d.scol+offset; j < d.ecol; j++) {  //d.ecol inclusive, but not processed

                //first row, first column skipped
                //same as     !(  k==d.srow  &&  j==d.scol  )
                //this way can be short circuited
                if (k != d.srow || j != d.scol){
                    //calculate row k, column j next
                    if (DNAcodes.equals(columns.charAt(j - 1), rows.charAt(k - 1))) {
                        //they match
                        diagonal = MATCH + table.get(k - 1)[j - 1].score;
                    } else {
                        diagonal = MISMATCH + table.get(k - 1)[j - 1].score;
                    }
                    down = GAP + table.get(k - 1)[j].score;
                    right = GAP + table.get(k)[j - 1].score;

                    if (diagonal >= down && diagonal >= right) {
                        table.get(k)[j].move = TableEntry.DIAGONAL;
                        table.get(k)[j].score = diagonal;
                    } else {
                        if (right >= down) {
                            table.get(k)[j].move = TableEntry.RIGHT;
                            table.get(k)[j].score = right;
                        } else {
                            table.get(k)[j].move = TableEntry.DOWN;
                            table.get(k)[j].score = down;
                        }
                    }
                }

            }   // end for j


            //calculate leftmost column, stepping down one step each time (table fills diagonally)
            for (j = d.scol+offset+1; j < d.ecol; j++) {    //d.ecol inclusive, last column not processed
                                   // +1 skips the row already calculated
                if (DNAcodes.equals(columns.charAt(k - 1), rows.charAt(j - 1))) {
                    //they match
                    diagonal = MATCH + table.get(j - 1)[k - 1].score;
                } else {
                    diagonal = MISMATCH + table.get(j - 1)[k - 1].score;
                }
                down = GAP + table.get(j - 1)[k].score;
                right = GAP + table.get(j)[k - 1].score;

                if (diagonal >= down && diagonal >= right) {
                    table.get(j)[k].move = TableEntry.DIAGONAL;
                    table.get(j)[k].score = diagonal;
                } else {
//                    if (right == down) {
//                        table.get(j)[k].move = TableEntry.TIE;
//                        table.get(j)[k].score = right;
//                    } else if (right > down) {
//                        table.get(j)[k].move = TableEntry.RIGHT;
//                        table.get(j)[k].score = right;
//                    } else {
//                        table.get(j)[k].move = TableEntry.DOWN;
//                        table.get(j)[k].score = down;
//                    }

                    if (right >= down) {
                        table.get(j)[k].move = TableEntry.RIGHT;
                        table.get(j)[k].score = right;
                    } else {
                        table.get(j)[k].move = TableEntry.DOWN;
                        table.get(j)[k].score = down;
                    }

                }
            } // end for j

        } // end for k


        //If at the end of table, don't write off the table
        if (d.erow != rows.length() && d.ecol != columns.length()) {

            //move back to last position
            k--;
            j--;

            //Special consideration for last cell (MUST BE a MISMATCH or GAP, _NO_ matches allowed)
            diagonal = MISMATCH + table.get(j - 1)[k - 1].score;
            down = GAP + table.get(j - 1)[k].score;
            right = GAP + table.get(j)[k - 1].score;

            if (diagonal >= down && diagonal >= right) {
                table.get(j)[k].move = TableEntry.DIAGONAL;
                table.get(j)[k].score = diagonal;
            } else {
                if (right >= down) {
                    table.get(j)[k].move = TableEntry.RIGHT;
                    table.get(j)[k].score = right;
                } else {
                    table.get(j)[k].move = TableEntry.DOWN;
                    table.get(j)[k].score = down;
                }
            }
        }
    }  //end     private void miniAlign(Diagonal d)

    /**
     * follows the diagonal, writing matches the whole way down, incrementing score as we go
     *
     * @param d
     */
    private void slideDownSeed(Diagonal d) {

        int r = d.srow;
        int c = d.scol;
        //int count = d.erow - r + 1;
        //if (count != d.ecol - c +1){
        if ( (d.erow - d.srow) != (d.ecol - d.scol)){
            try{
                throw new DataFormatException("diagonal distance is not same across columns and rows");
            } catch (DataFormatException e){
                e.printStackTrace();
                exit(0);
            }

        }

        int last_score = table.get(r-1)[c-1].score;
        for (; r <= d.erow && c <= d.ecol; r++,c++){
            int s = table.get(r-1)[c-1].score;
            last_score += MATCH;
            table.get(r)[c].score = last_score;

//            Assert.assertTrue(DNAcodes.equals(columns.charAt(r+x-1), rows.charAt(c+x- 1)));
//            table.get(r+x)[c+x].move = TableEntry.DIAGONAL;
//            table.get(r+x)[c+x].score = MATCH + s;

            //DEPRECATED TO-DO once debugged, this debugging test can be removed for speed   ISSUE #77
            if (DNAcodes.equals(columns.charAt(c-1), rows.charAt(r-1))) {  //0 indexed rows/columns  vs r, c 1-indexed
                table.get(r)[c].move = TableEntry.DIAGONAL;

            } else {
                try{
                    throw new DataFormatException("diagonal guaranteed match is actually  mismatch :: col "+columns.charAt(c-1)+" vs row "+rows.charAt(r-1));
                } catch (DataFormatException e){
                    e.printStackTrace();
                    exit(0);
                }
            }

        } //end for

        "DEBUG".equals("BREAK LINE");
    } //end slideDownSeed


//    @Override
//    public void setMatchValue(int k) {
//        MATCH = k;
//    }
//
//    @Override
//    public void setMisMatchValue(int k) {
//        MISMATCH = k;
//    }
//
//    @Override
//    public void setGapValue(int k) {
//        GAP = k;
//    }

    @Override
    public ArrayList<PartialAlignment1> bestAlignments() {
        return null;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

    public void setRefStart(int refStart) {
        this.refStart = refStart;
    }


    //Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
    @Override
    public int compareTo(SmithWatermanTruncated o) {
        return maxPossibleFastKlatScore - o.maxPossibleFastKlatScore;
    }

    @Override
    public int compare(SmithWatermanTruncated o1, SmithWatermanTruncated o2) {
        return o1.compareTo(o2);
    }


//    /**
//     * A rectangle defines a region that is not aligned, INCLUSIVE TO INCLUSIVE of the two seeds on either end.
//     * It has a maximum S-W score based on
//     *
//     * @param rectangle
//     * @return
//     */
//    public int maxScore(Diagonal rectangle){
//        int m = rectangle.ecol - rectangle.scol +1;  //INCLUSIVE
//        int n = rectangle.erow - rectangle.srow +1;  //INCLUSIVE
//
//        int result = Math.min(m,n)*MATCH-2*(MATCH-GAP);
//
//        //alignment caps have 1 less forbidden match
//        if (rectangle.srow == 1 || rectangle.erow == rows.length() ||
//                rectangle.scol == 1 || rectangle.ecol == columns.length()) {  //characters are 1-indexed
//            result += (MATCH-GAP);
//        }
//        return result;
//    }


//    private void viewTable(ArrayList<TableEntry[]> table) {
//        return viewTable()
//    }

    /**
     * Tab delimited
     * @return
     */
    public SuperString viewTable(){
        SuperString result = new SuperString("\t");
        for (int k = 0; k < columns.length(); k++) {
            result.add(columns.charAt(k));
            result.add("  \t ");
        }
        result.add('\n');

        for (int k = 0; k < rows.length() + 1; k++) {  //rare case where we print every row

            if (k == 0){
                result.add(" :\t ");
            }
            else{
                result.add(rows.charAt(k - 1));
                result.add( ":\t ");
            }

            for (int j = 0; j < columns.length() + 1; j++) {
                if (table.get(k)[j].move == TableEntry.DIAGONAL) {
                    result.add('*');
                } else if (table.get(k)[j].move == TableEntry.DOWN) {
                    result.add('^');
                } else if (table.get(k)[j].move == TableEntry.RIGHT) {
                    result.add('<');
                }

                if (table.get(k)[j].score < 0){
                    result.add(Integer.toString(table.get(k)[j].score));
                    result.add(" \t ");
                }
                else {
                    result.add(' ');
                    result.add(Integer.toString(table.get(k)[j].score));
                    result.add(" \t ");
                }
            }
            result.add('\n');        }
        return result;
    }

    public SuperString viewTable(Diagonal d){
        SuperString result = new SuperString("| | ");

        //We write off the edge of the table for the rightmost rectangle
        if (d.ecol > columns.length())  d.ecol = columns.length();
        if (d.erow > rows.length())  d.erow = rows.length();

        //First Line
        for (int k = d.scol; k < d.ecol; k++) {
            result.add(' ');
            result.add(columns.charAt(k));
            result.add("   | ");
        }
        result.add('\n');

        //data lines
        for (int k = d.srow; k < d.erow; k++) {  //rare case where we print every row

            if (k == 0){
                result.add(" |  ");
            }
            else{
                result.add(rows.charAt(k - 1));
                result.add( "|  ");
            }

            for (int j = d.scol; j <= d.ecol; j++) {
                if (table.get(k)[j].move == TableEntry.DIAGONAL) {
                    result.add('*');
                } else if (table.get(k)[j].move == TableEntry.DOWN) {
                    result.add('^');
                } else if (table.get(k)[j].move == TableEntry.RIGHT) {
                    result.add('<');
                }

                if (table.get(k)[j].score < 0){
                    result.add(Integer.toString(table.get(k)[j].score));
                    result.add(" | ");
                }
                else {
                    result.add(' ');
                    result.add(Integer.toString(table.get(k)[j].score));
                    result.add(" | ");
                }
            }
            result.add('\n');        }
        return result;
    }


    public int getFastKlatScore(){

        //if not calculated, calculate
        if (fastKlatScore == Integer.MIN_VALUE){
            int rnum = rows.length();
            int cnum = columns.length();

            for (int r=0; r < rnum -1; r++) {  //skip last row, corner will be considered in column
                fastKlatScore = Math.max(table.get(r)[cnum-1].score,fastKlatScore);
            }
            for (int c=0; c < cnum; c++) {  //skip last row, corner will be considered in column
                fastKlatScore = Math.max(table.get(rnum-1)[c].score,fastKlatScore);
            }
        }
        return fastKlatScore;

    }



}
