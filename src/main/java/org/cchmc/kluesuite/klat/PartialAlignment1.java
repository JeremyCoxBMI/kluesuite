package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.mutation;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * This is a helper class to SmithWatermanOriginal  but mostly for SmithWatermanAdvanced.
 * Each instantiation calculates and tracks the statistics for a single alignment possibility.
 *
 * 2016-08-05   v1.6    Making a major upgrade to track multiple alignment statistics
 *
 * 2017/03/30  NEED to addAndTrim fast KLAT score to scoring system
 * 2017/05/25  NEED to addAndTrim CIGAR string
 * 2017-08-01  Updating to store Variants Called
 */
public class PartialAlignment1 {

    /**
     * For process gaps method 2, stores the positions of gaps and which string they belong to
     * ISSUE #69
     */
    ArrayList<Integer> gaps2positions = null;


    /**
     * For process gaps method 2, stores the reference positions of gaps
     */
    ArrayList<Integer> gaps2refPositions = null;

    /**
     * For process gaps method 2, stores the query positions of gaps
     */
    ArrayList<Integer> gaps2queryPositions = null;

    /**
     * For process gaps method 2, stores TRUE if the gap is in query strand
     * ISSUE #69
     */
    ArrayList<Boolean> gapsQuery = null;

    /**
     * Calculating the String for the alignment is a waste of time for Smith-Waterman Advanced
     */
    public static boolean DO_NOT_CALCULATE_ALIGNMENT_STRINGS = true;

    /**
     * Save the alignment string for variant calling
     */
    private String columns = null;

    /**
     * Save the alignment string for variant calling
     */
    private String rows = null;

    //track key data pieces for Variant calling
    private int refStart = 0;
    private int kid = 0;


    public int getNumMatches() {
        return numMatches;
    }

    public String getCIGARString() {
        return CIGARString;
    }

    public int getNumAligned() {
        return numAligned;
    }

    public int getLength() {
        return length;
    }

    public int getMismatch() {
        return mismatch;
    }

    public double getEvalue() {
        return evalue;
    }

    public int getBitscore() {
        return bitscore;
    }

    public ArrayList<Variant> getMismatchesCalled() {
        return mismatchesCalled;
    }

    public int getFastKLATscore() {
        return fastKLATscore;
    }

    public int numMatches;

    /**
     * AlignmentKLAT1 string in human readable form; may be query or reference, see queryASleft
     */
    String top;

    /**
     * AlignmentKLAT1 string in human readable form; may be query or reference, see queryASleft
     */
    String left;

    int score;

    // These are altschul parameters for calculating Evalue, bitscore
    private static double K;
    private static double lambda;
    private static double H;

    static {
        K      = KLATsettings.EVALUE_K;
        lambda = KLATsettings.EVALUE_LAMBDA;
        H      = KLATsettings.EVALUE_H;  //not used
    }

    /**
     * All variants called
     */
    ArrayList<Variant> called;

    private boolean hasVariantsCalled  = false;


    /**
     *
     * Sam File CIGAR string
     */
    public String CIGARString;

    /**
     * it is possible that the query will be stored as longer sequence
     * true means query is contained in string "left", otherwise in "top"
     */
    boolean queryASleft;

    /**
     * possibly a duplication of the length parameter.
     * This counts the total length of matches, mismatches, and gaps in the alignment.
     * or, then length of the laignment.  Note this can be more or less than the query length, because there may be unaligned edges.
     */
    public int numAligned;


    /**
     * BlastN format6 reporting term
     * percent of identical matches
     *
     * Percent Identity = (Matches x 100)/Length of aligned region (with gaps)
     */
    public float pident;

    /**
     * BlastN format6 reporting term
     * this is alignment length
     * todo: judges ruling on what length means?
     *
     * IN SAM FILE, tlen is reference alignment length
     * IN BLAST, unknown -- need to test
     */
    public int length;


    /**
     * BlastN format6 reporting term
     *
     */
    public int mismatch;

    /**
     * Total number of gaps in alignment
     */
    public int gapopen;

    /**
     * BlastN format6 reporting term
     * Position in query where alignment starts, INCLUSIVE
     */
    public int qstart;

    /**
     * BlastN format6 reporting term
     * Position in query where alignment ends, INCLUSIVE
     */
    public int qend;

    /**
     * BlastN format6 reporting term
     * Position in reference sequence where alignment begins, INCLUSIVE
     * NOTE this is 0-indexed, as we do not know the offset of the sequence submitted
     * Furthermore, if it is reversed, that complicates matters
     */
    public int sstart;

    /**
     * BlastN format6 reporting term
     * Position in reference sequence where alignment ends, INCLUSIVE
     * NOTE this is 0-indexed, as we do not know the offset of the sequence submitted
     * Furthermore, if it is reversed, that complicates matters
     */
    public int send;

    /**
     * BlastN format6 reporting term
     */
    public double evalue;

    /**
     * BlastN format6 reporting term
     */
    public int bitscore;

    ArrayList<AlignmentGap> gaps;

    /**
     * Optional data element, describes SNPs that could be cause of mismatches
     */
    public ArrayList<Variant> mismatchesCalled;


    //intermediate global variables to pass between functions

    /**
     * intermediate global variable
     * while processing alignment output, has a gap been found?
     */
    private boolean inGap;

    /**
     * intermediate global variable
     * for current gap, is it to the left String (true) or top (false)
     */
    private boolean gapISleft;

    /**
     * intermediate global variable
     * first index where gap occurs
     */
    private int gapStart;

    /**
     * intermediate global variable
     * last index where gap observed
     */
    private int gapEnd;

    /**
     * intermediate global variable
     * this is set to last gap position as gap iterates
     */
    private int gapPos;

    /**
     * Fast Klat Score for the AlignmentKLAT1
     */
    public int fastKLATscore = 0;


//    /**
//     * Input argument
//     */
//    boolean queryISrows = false;

    /**
     * for SmithWatermanOriginal backwards compatability
     *
     * Builds an alignment based on precalculated table and position (row, col) corresponding to an alignment in that table
     *
     * @param table
     * @param columns
     * @param rows
     * @param row
     * @param col
     */
    public PartialAlignment1(ArrayList<TableEntry[]> table, String columns, String rows, int row, int col) {
        mismatchesCalled = new ArrayList<>();
        this.columns = columns;
        this.rows = rows;

        called = new ArrayList<Variant>();
        score = table.get(row)[col].score;

        if (DO_NOT_CALCULATE_ALIGNMENT_STRINGS){
            top= "(top  not calculated)";
            left="(left not calculated)";
        } else {
            buildPath(table, row, col);
        }
    }

    /**
     * builds top and left Strings (optional)
     *
     * @param table
     * @param row
     * @param col
     */
    private void buildPath(ArrayList<TableEntry[]> table, int row, int col) {
        SuperString topTemp = new SuperString();
        SuperString leftTemp = new SuperString();

        int n = columns.length();
        int m = rows.length();

        if (row < m) {
            for (int k = 0; k < m - row; k++) {
                topTemp.addAndTrim(".");
                leftTemp.add(rows.charAt(m - 1 - k));
            }
        }

        if (col < n) {
            for (int k = 0; k < n - col; k++) {
                topTemp.add(columns.charAt(n-1-k));
                leftTemp.addAndTrim(".");
            }
        }

        while (row > 0 || col > 0) {
            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
                //Traceback to DIAGONALLY   UP and LEFT
                col--;
                row--;
                topTemp.add(columns.charAt(col));
                leftTemp.add(rows.charAt(row));

            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
                //Traceback LEFT
                col--;
                if (row == 0) {
                    leftTemp.addAndTrim(".");
                }
                else {
                    leftTemp.addAndTrim("_");
                }

                topTemp.add(columns.charAt(col));

            } else if (table.get(row)[col].move == TableEntry.DOWN) {
                //Traceback UP
                row--;
                if (col == 0){
                    topTemp.add('.');
                }
                else{
                    topTemp.add('_');
                }

                leftTemp.add(rows.charAt(row));
            }
            int DEBUG=2;
        }



        top = topTemp.toReverseString();
        left = leftTemp.toReverseString();
    }

    public float getPercentIdentity(){
        return pident;
    }


    public PartialAlignment1(ArrayList<TableEntry[]> table, String columns, String rows, int row, int col, boolean queryISrows, int kid, int refStart) {

        this(table, columns, rows, row, col, queryISrows);
        this.kid = kid;
        this.refStart = refStart;
    }


    /**
     * Builds an alignment based on precalculated table and position (row, col) corresponding to an alignment in that table
     *
     * @param table
     * @param columns
     * @param rows
     * @param row
     * @param col
     * @param queryISrows   query may or may not be the rows, to output correctly, this must be known
     */
    public PartialAlignment1(ArrayList<TableEntry[]> table, String columns, String rows, int row, int col, boolean queryISrows) {
        mismatchesCalled = new ArrayList<>();
        queryASleft = queryISrows;
        this.columns = columns;
        this.rows = rows;
//        String rows, columns;
//        if (query.length() > refSeq.length()){
//            rows = refSeq;
//            columns = query;
//            queryASleft = false;
//        } else {
//            rows = refSeq;
//            columns = query;
//            queryASleft = true;
//        }

        //Mark as uninitialized
        qstart = -1;
        qend = -1;
        sstart = -1;
        send = -1;
        inGap = false;
        gapISleft = false;
        gapStart = -1;
        gapEnd = -1;
        gapPos = -1;
        mismatch = 0;

//        mismatchesCalled = new ArrayList<>();
        gaps = new ArrayList<AlignmentGap>();
        called = new ArrayList<Variant>();

        score = table.get(row)[col].score;
        if (DO_NOT_CALCULATE_ALIGNMENT_STRINGS) {
            top = "(top  not calculated)";
            left = "(left not calculated)";

            //buildPathAdvanced(table, row, col);
            buildPathAdvancedNoString(table, row, col);
        } else {

            buildPathAdvanced(table, row, col);
        }
    }


    private void buildPathAdvancedNoString(ArrayList<TableEntry[]> table, int row, int col) {
            //buildPathAdvanced(table, row, col);
        int n = columns.length();
        int m = rows.length();
        numMatches = 0;
        numAligned = 0;


        //BY DEFINTION,  !( row < m && col < n), both cannot be true
        //execute one block, other block, or neither

        //skip first rows that are not aligned
        if (row < m) {
            if (queryASleft) qstart = row-1;
            else sstart = row-1;
        }

        //skip first columns that are not aligned
        if (col < n) {
            if (queryASleft) sstart = col-1;
            else qstart = col-1;
        }

        inGap = false;

        int alignPositionReverse = 0;
        while (row > 0 || col > 0) {
            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
                //Traceback to DIAGONALLY   UP and LEFT

                //case 1: A match!
                if (table.get(row)[col].score > table.get(row - 1)[col - 1].score){
                    numMatches++;
                }
                //case 2: A mismatch!
                else{
                    mismatch++;
                    //subtract 1 because of extra blank column
                    if (queryASleft) {
                        processMismatches(rows.substring(row-1, row), columns.substring(col-1, col), col-1);
                    } else {
                        processMismatches( columns.substring(col-1, col), rows.substring(row-1, row), row-1);
                    }
                }

                col--;
                row--;


                //CASE 1 & CASE 2: Match or mismatch, numAligned is incremented
                //numAligned counts the total number of matches, mismatches, and gaps in alignment
                //here, match or mismatch
                numAligned++;

            //case 3: A gap / insertion or deletion (per queryASleft)
            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
                //Traceback LEFT


                if (row != 0) {   //row 0 does not count towards alignment; these are trailing or leading sequences
                    numAligned++;
                    processGap2(TableEntry.RIGHT, row, col, alignPositionReverse);
                } else {
                    break;
                }
                col--;

            // case 4: A gap / insertion or deletion (per queryASleft)
            } else if (table.get(row)[col].move == TableEntry.DOWN) {
                //Traceback UP

                if (col != 0) { //col 0 does not count towards alignment; these are trailing or leading sequences
                    numAligned++;
                    processGap2(TableEntry.DOWN, row, col, alignPositionReverse);
                } else {
                    break;
                }
                row--;
            }

            if (qend == -1 && (row == 0 || col == 0)) {
                if (queryASleft) {
                    qend = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    send = col;
                } else {
                    qend = col;
                    send = row;
                }
            }

            if (qstart == -1) {
                if (queryASleft) {
                    if (row < m) {
                        qstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                } else {
                    if (col < n) {
                        qstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                }
            }

            if (sstart == -1) {
                if (queryASleft) {
                    if (col < n) {
                        sstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                } else {
                    if (row < m) {
                        sstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                }
            }

            alignPositionReverse++;
        } // end while (trace path through table)

        buildGaps2();


        //reverse all gap position coordinate
        //they were written in reverse, because alignment crawler is right to left, length is unknown
        //gap coordinates easily reversed, as AlignmentKLAT1 is built in reverse, BUT gap offset will need to be processed in CALL VARIANTs?
        //probably should not change here --> unless we write a callVariants2 to match
        for (AlignmentGap x : gaps){
            //            x.pos = numAligned - x.pos - 1;
            x.pos = numAligned - x.pos - x.length+1;  //gap start is now gap end, so gap end becomes gap start

//            AlignmentGap2 z = (AlignmentGap2) x;
            //row and column stored correctly as refPosition or qPosition, not offset for length
            //THEY ARE NOW!!!

//            if (z.gapISquery) {
//                z.qPos = z.qPos - z.length + 1;
//            }else{
//                z.refPos = z.refPos - z.length + 1;
//            }


        }

        //COMBINED SNP calls into a chain
        Collections.sort(mismatchesCalled);
        //eliminate adjacent calls
        for (int k=1; k< mismatchesCalled.size(); k++){
            Variant a = mismatchesCalled.get(k-1);
            Variant b = mismatchesCalled.get(k);
            //both are SNPS
            if(a.start+a.length == b.start){
                a.length += b.length;
                a.insertSequence += b.insertSequence;
                String[] splitsA = a.detailedName.split("\\||/");
                String[] splitsB = b.detailedName.split("\\||/");
                a.detailedName = splitsA[0]+"|"+(splitsA[1]+splitsB[1]+"/"+splitsA[2]+splitsB[2]).replace("|","")+"|";
                a.name = a.name;
                //a.name
                mismatchesCalled.remove(b);
                k--;
            }
        }

        //sorted in descending order, want ascending to parse gaps.
        Collections.reverse(gaps);

        //Because I am foolish, I have start and end reversed:
        int temp;
        temp = qstart;
        qstart = qend;
        qend = temp;
        temp = sstart;
        sstart = send;
        send = temp;



        pident = ((float)numMatches) / ((float)numAligned);

        //see https://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
        //let S = smith-waterman score
        evalue = K * m * n * Math.exp(-1*score*lambda);
        Double b = (lambda*score - Math.log(K)) / Math.log(2.0);
        bitscore = b.intValue();

        fastKLATscore = numMatches + Kmer31.KMER_SIZE_MINUS_ONE;

        gapopen = gaps.size();

        length = numAligned;

    }

    private void processMismatches(String seq, String ref, int refCoor) {
        Variant v;
        //TODO are these strings called in correct order?
        v = Variant.buildSNP(refCoor + refStart,kid,seq,
                "unknown",(refCoor+refStart)+"|"+ref+"/"+seq+"|");
        mismatchesCalled.add(v);
    }

    private void buildPathAdvanced(ArrayList<TableEntry[]> table, int row, int col){
        SuperString topTemp = new SuperString();
        SuperString leftTemp = new SuperString();

        int n = columns.length();
        int m = rows.length();
        numMatches = 0;
        numAligned = 0;
        top = "";
        left = "";

        if (row < m) {
            for (int k = 0; k < m - row; k++) {
//                top += '.';
//                left += rows.charAt(m - 1 - k);

                topTemp.addAndTrim(".");
                leftTemp.add(rows.charAt(m - 1 - k));
            }

            if (queryASleft) qstart = row-1;
            else sstart = row-1;
        }

        if (col < n) {
            for (int k = 0; k < n - col; k++) {
//                left += '.';
//                top += columns.charAt(n - 1 - k);

                leftTemp.addAndTrim(".");
                topTemp.add(columns.charAt(n - 1 - k));
            }
            if (queryASleft) sstart = col-1;
            else qstart = col-1;
        }

        inGap = false;

        int alignPositionReverse = 0;
        while (row > 0 || col > 0) {
            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
                //Traceback to DIAGONALLY   UP and LEFT

//                char debug1 = rows.charAt(row - 1);
//                char debug2 = columns.charAt(col - 1);
//                int debug3 = table.get(row)[col].score;
//                int debug4 = table.get(row - 1)[col - 1].score;

                //case 1: A match!
                if (table.get(row)[col].score > table.get(row - 1)[col - 1].score){
                    numMatches++;
                }
                //case 2: A mismatch!
                else{
                    mismatch++;
                    //subtract 1 because of extra blank column
                    if (queryASleft) {
                        processMismatches(rows.substring(row-1, row), columns.substring(col-1, col), col-1);
                    } else {
                        processMismatches( columns.substring(col-1, col), rows.substring(row-1, row), row-1);
                    }
                }

                col--;
                row--;
//                top += columns.charAt(col);
//                left += rows.charAt(row);
                topTemp.add(columns.charAt(col));
                leftTemp.add(rows.charAt(row));

              //CASE 1 & CASE 2: Match or mismatch, numAligned is incremented
                //numAligned counts the total number of matches, mismatches, and gaps in alignment
                //here, match or mismatch
                numAligned++;

            //case 3: A gap / insertion or deletion (per queryASleft)
            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
                //Traceback LEFT

                col--;
                if (row == 0) {
//                    left += '.';
                    leftTemp.addAndTrim(".");
                } else {
//                    left += "_";
                    leftTemp.addAndTrim("_");
                    //Case 3: top is a letter
//                    if (queryASleft){
//                        //query length increases
//                        numAligned++;
//                    }
                    numAligned++;

                    processGap(TableEntry.RIGHT, row, col, alignPositionReverse);
                }
//                top += columns.charAt(col); //col was decremented before we right char; so the 1 offset (first entry is blank letter) is included
                topTemp.add(columns.charAt(col));

                //if at end, write last gap to table :: toggle direction triggers a write
                //if (row == 0 && col == 0) processGap(TableEntry.DOWN, row, col);

            // case 4: A gap / insertion or deletion (per queryASleft)
            } else if (table.get(row)[col].move == TableEntry.DOWN) {
                //Traceback UP
                row--;
                if (col == 0) {
                    //top += '.';

                    topTemp.addAndTrim(".");
                } else {
//                    top += "_";
                    topTemp.addAndTrim("_");
                    //Case 4: left is a letter
                    //if (!queryASleft){
                        //query length increases
                     //   numAligned++;
                    //}
                    numAligned++;
                    processGap(TableEntry.DOWN, row, col, alignPositionReverse);
                }
//                left += rows.charAt(row);
                leftTemp.add(rows.charAt(row));


            }

            if (qend == -1 && (row == 0 || col == 0)) {
                if (queryASleft) {
                    qend = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    send = col;
                } else {
                    qend = col;
                    send = row;
                }
            }

            if (qstart == -1) {
                if (queryASleft) {
                    if (row < m) {
                        qstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                } else {
                    if (col < n) {
                        qstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                }
            }

            if (sstart == -1) {
                if (queryASleft) {
                    if (col < n) {
                        sstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                } else {
                    if (row < m) {
                        sstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
                    }
                }
            }

            alignPositionReverse++;
        } // end while (trace path through table)

        if (row == 0 && col == 0 && inGap){
            //if at end, and a gap was found, write last gap to table
            if (gapISleft)  gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),!queryASleft)); //if query is left sequence, then gap is query
            else            gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),queryASleft)); //if query is left sequence, then gap is NOT query
        }


        //reverse all gap position coordinate

        for (AlignmentGap x : gaps){
            x.pos = numAligned - x.pos - 1;
        }



        //Because I am foolish, I have start and end reversed:
        int temp;
        temp = qstart;
        qstart = qend;
        qend = temp;
        temp = sstart;
        sstart = send;
        send = temp;


        pident = ((float)numMatches) / ((float)numAligned);

        //System.err.println("num match: "+numMatches+" numAligned "+numAligned+" pident "+pident);

//        top = new StringBuilder(top).reverse().toString();
//        left = new StringBuilder(left).reverse().toString();
        top = topTemp.toReverseString();
        left = leftTemp.toReverseString();

        //see https://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
        //let S = smith-waterman score
        evalue = K * m * n * Math.exp(-1*score*lambda);
        Double b = (lambda*score - Math.log(K)) / Math.log(2.0);
        bitscore = b.intValue();

        fastKLATscore = numMatches; // + Kmer31.KMER_SIZE_MINUS_ONE;

        gapopen = gaps.size();

        length = numAligned;

    } // end constructor


    /**
     * Alternative way to track gaps, more quickly.
     * Does not require calculation when running.
     * Paired with buildGaps2 for after the fact
     *
     * @param direction whether the gap is RIGHT or DOWN movement
     * @param row   coordinate in table
     * @param col   coordinate in table
     * @param pos    gap position (reversed index of alignment)
     */
    private void processGap2(int direction, int row, int col, int pos) {
        if (gaps2positions == null){
            gaps2positions = new ArrayList<>();
            gaps2refPositions = new ArrayList<>();
            gaps2queryPositions = new ArrayList<>();
            gapsQuery = new ArrayList<>();
        }
        gaps2positions.add(pos);
        if (queryASleft){
            gaps2refPositions.add(col-1);
            gaps2queryPositions.add(row-1);
            //moving right puts a gap into reference
            if (direction == TableEntry.RIGHT){
                //right is skipping across columns, row stays same
                //row = query
                gapsQuery.add(true);
            } else {
                gapsQuery.add(false);
            }
        } else {
            gaps2refPositions.add(row-1);
            gaps2queryPositions.add(col-1);
            //moving right puts a gap into query
            if (direction == TableEntry.RIGHT){
                gapsQuery.add(false);
            } else {
                gapsQuery.add(true);
            }
        }
    }

    /**
     *
     */
    private void buildGaps2(){

        if (gaps2positions == null || gaps2positions.size() == 0){
            return;
        }

        //singleton caught in the after loop final write
//        else if (gaps2positions.size() == 1){
//            //singleton does not require loop
//            gaps.addAndTrim(new AlignmentGap(gaps2positions.get(0),1,gapsQuery.get(0)) );
//        }

        //write gaps to gaps object as they are found
        int gapStart=0;
//        boolean queryStart = gapsQuery.get(0);
        int k;
        for (k=1; k< gaps2positions.size();k++){
            boolean continueOn = (gapsQuery.get(k) == gapsQuery.get(gapStart)
                    && (gaps2positions.get(k) - gaps2positions.get(gapStart)) == (k-gapStart) );


            if (!continueOn){

                //subtraction reversed: coordinates reversed
                addGap2(gapStart,
                            gaps2refPositions.get(gapStart)  -gaps2refPositions.get(k-1),
                        gaps2queryPositions.get(gapStart)-gaps2queryPositions.get(k-1));

                gapStart = k;
            }

        }
        //write final
        addGap2(gapStart,
                  gaps2refPositions.get(gapStart)  -gaps2refPositions.get(k-1),
                gaps2queryPositions.get(gapStart)-gaps2queryPositions.get(k-1));


    }

    /**
     * adds a gap to list
     * takes offset for for reference and query
     * Note that one of these two should be 0
     * DELETION: refOffset >0, queryOffset = 0
     * INSERT:   queryOffset >0, refOffset = 0
     * @param gapStart
     * @param refOffset
     * @param queryOffset
     */
    private void addGap2(int gapStart, int refOffset, int queryOffset) {
        int length = Math.max(refOffset, queryOffset)+1;  //OFFSET is INCLUSIVE --> INCLUSIVE
        gaps.add(new AlignmentGap2(gaps2positions.get(gapStart),
                gaps2refPositions.get(gapStart)-refOffset,
                gaps2queryPositions.get(gapStart)-queryOffset,
                length,
                gapsQuery.get(gapStart)));
    }


    /**
     * helper function
     * @param direction whether the gap is RIGHT or DOWN movement
     * @param row   coordinate in table
     * @param col   coordinate in table
     * @param posRev    gap position
     */
    private void processGap(int direction, int row, int col, int posRev) {
        if (direction == TableEntry.RIGHT){
            //newGapIsLeft = false;
            if (  inGap && gapISleft == true  ){
                //gaps switching sides
                //write previous gap AND start new gap

                //gap Start/End are reversed because parsing backwards
                gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),!queryASleft)); //if query is left sequence, then gap is query

                gapISleft = false;
                gapStart = col;
                gapEnd = col;
                gapPos = posRev;
            } else if (inGap) {
                if (col == gapEnd-1) {
                    //continue gap along top
                    gapEnd = col;
                } else {
                    //write previous gap AND start new gap
                    gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),queryASleft)); //if query is left sequence, then gap is NOT query
                    gapISleft = false;
                    gapStart = col;
                    gapEnd = col;
                    gapPos = posRev;
                }
            } else {
                //new gap
                inGap = true;
                gapISleft = false;
                gapStart = col;
                gapEnd = col;
                gapPos = posRev;
            }
        } else if ( direction == TableEntry.DOWN ) {
            //newGapIsLeft = true;
            if (inGap && gapISleft == false){
                //gaps switching sides
                //write previous gap AND start new gap

                //gap Start/End are reversed because parsing backwards
                gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),queryASleft)); //if query is left sequence, then gap is NOT query

                gapISleft = true;
                gapStart = row;
                gapEnd = row;
                gapPos = posRev;
            } else if (inGap) {
                if (row == gapEnd-1) {
                    //continue gap along left
                    gapEnd = row;
                } else {
                    //write previous gap AND start new gap
                    gaps.add( new AlignmentGap(gapPos,(gapStart-gapEnd+1),!queryASleft)); //if query is left sequence, then gap is query
                    gapISleft = true;
                    gapStart = row;
                    gapEnd = row;
                    gapPos = posRev;
                }
            } else {
                //new gap
                inGap = true;
                gapISleft = true;
                gapStart = row;
                gapEnd = row;
                gapPos = posRev;
            }
        }
    }

    public String toString(){
        return "SCORE: "+score+" of "+numAligned*SmithWatermanAdvanced.MATCH+" %id "+ pident *100+"\n"+top + "\n" + left;
    }

    /**
     * Full output of alignment statistics
     * @return
     */
    public String toFullReport(){
        String result = "";
        if (queryASleft) {
            result += "RefSeq " + top + "\n";
            result += " Query " + left + "\n";
        } else {
            result += "RefSeq " + left + "\n";
            result += " Query " + top + "\n";
        }
        result += "pident   "+pident*100+"\n";
        result += "length   "+length+"\n";
        result += "mismatch "+mismatch+"\n";
        result += "gapopen  "+gapopen+"\n";
        for(AlignmentGap x : gaps){
            result += x.toString()+"\n";
        }
        result += "qstart   "+qstart+"\n";
        result += "qend     "+qend+"\n";
        result += "sstart   "+sstart+"\n";
        result += "send     "+send+"\n";
        result += "evalue   "+evalue+"\n";
        result += "bitscore "+bitscore+"\n";
        result += "fast KLAT score "+fastKLATscore+"\n";
        return result;
    }


    public ArrayList<Variant> callVariants(){

        if (DO_NOT_CALCULATE_ALIGNMENT_STRINGS){
            return callVariants2();
        }

        if (hasVariantsCalled){
            return called;
        }

        //what about coordinate offset from multiple variants?   not sure
        //the coordinates in reference string remain absolute and are used for the reference
        //deletions and inserts should still occur at these absolute coordinates

        for (int k=0; k < gaps.size()-1; k++){
            AlignmentGap x = gaps.get(k);
            AlignmentGap y = gaps.get(k+1);
            if (y.pos < x.pos){
                AlignmentGap t = x;
                x=y;
                y=t;
            }

            int stop = x.pos+x.length();
            int nextStart = y.pos;
            boolean gapISquery = (x.gapISquery == y.gapISquery);
            if (stop == nextStart && gapISquery){
                gaps.set(k,new AlignmentGap(x.pos, x.length+y.length,x.gapISquery));
                gaps.remove(k+1);
                k--;
                System.out.println("  WARNING :: gaps had to be combined for this alignment");
            }
        }
        gapopen = gaps.size();

        for (AlignmentGap g : gaps){

            //g.pos is relative to the string they refer to (not reference coordinates)
            if (g.gapISquery){
                //DELETION
                if (queryASleft) {
                    called.add(Variant.buildDELETION(g.pos+refStart+1, kid, g.length,
                            "<"+columns.substring(g.pos,g.pos+g.length())+"/->", "unknown:"+(g.pos+refStart+1)));
                } else {

                    called.add(Variant.buildDELETION(g.pos+refStart+1, kid, g.length,
                            "<"+rows.substring(g.pos,g.pos+g.length())+"/->", "unknown:"+(g.pos+refStart+1)));
                }
            } else {
                //INSERTION
                if (queryASleft){

                    String insert = columns.substring(g.pos,g.pos+g.length());
                    called.add(Variant.buildINSERTION(g.pos+refStart+1,kid,insert,
                            "<-/"+insert+">", "unknown:"+(g.pos+refStart-g.length+1)));
                } else {
                    String insert = rows.substring(g.pos,g.pos+g.length());
                    called.add(Variant.buildINSERTION(g.pos+refStart+1,kid,insert,
                            "<-/"+insert+">", "unknown:"+(g.pos+refStart-g.length+1)));  //Not sure why -g.length is needed
                }
            }
        }

        called.addAll(mismatchesCalled);

        hasVariantsCalled = true;
        return called;
    }


    /**
     * Returns variants calculcated, so that position is relative to the start of the reference sequence
     * Part of the processGaps2 upgrade / no AlignmentKLAT1 String calculations  ISSUE #69  ISSUE #71  ISSUE #30 ISSUE #44
     * @return
     */
    public ArrayList<Variant> callVariants2(){

        //TODO CIGAR STRINGS

        if (hasVariantsCalled){
            return called;
        }

        //what about coordinate offset from multiple variants?   not sure
        //the coordinates in reference string remain absolute and are used for the reference
        //deletions and inserts should still occur at these absolute coordinates

        //mismatch coordinates are already absolute in reference coordinates


//        for (int k=0; k < gaps.size()-1; k++){
//
//
//
//
//            AlignmentGap x = gaps.get(k);
//            AlignmentGap y = gaps.get(k+1);
//            if (y.pos < x.pos){
//                AlignmentGap t = x;
//                x=y;
//                y=t;
//            }
//
//            int end = x.pos+x.length();
//            int nextStart = y.pos;
//            boolean gapISquery = (x.gapISquery == y.gapISquery);
//            if (end == nextStart && gapISquery){
//                gaps.set(k,new AlignmentGap(x.pos, x.length+y.length,x.gapISquery));
//                gaps.remove(k+1);
//                k--;
//                System.out.println("  WARNING :: gaps had to be combined for this alignment");
//            }
//        }
//        gapopen = gaps.size();

//        int offset = 0;


        for ( AlignmentGap x : gaps){
            AlignmentGap2 g = (AlignmentGap2) x;
            //g.pos is relative to the alignment they refer to, presorted by position
            // (not reference coordinates)

            String before;
            String after;

            if (g.gapISquery){

                //DELETION
                //Want sequence from reference
                if (queryASleft) {
                    before = columns.substring(g.refPos, g.refPos+g.length);
                    after="-";
                } else {
                    before = rows.substring(g.refPos, g.refPos+g.length);
                    after="-";
                }
                int absoluteRef = g.refPos+refStart+1;
                called.add(Variant.buildDELETION(absoluteRef, kid, g.length,
                        "unknown",absoluteRef+"|"+before+"/"+after+"|"));

                //REF COOR      0123456789012345  6
                //REFER         AAAATTTTCCCCCGGG__G
                //QUERY         AAAATGA_CCCC_GGGCCG
                //ALIGN COOR    0123456789012345678

                // so, the reference coordinates are not altered by the alignment
                // offset += 0


            } else {
                //INSERTION
                //Want sequence from query
                if (queryASleft) {
                    //rows are query
                    before = "-";
                    after=rows.substring(g.qPos, g.qPos+g.length);
                } else {
                    //columns are query
                    before = "-";
                    after = columns.substring(g.qPos, g.qPos+g.length);
                }

                int absoluteRef = g.refPos+refStart+1;
                called.add(Variant.buildINSERTION(absoluteRef,kid,after,
                        "unknown",absoluteRef+"|"+before+"/"+after+"|"));
            }
        }


        called.addAll(mismatchesCalled);

        hasVariantsCalled = true;
        Collections.sort(called);
        return called;
    }




    private Variant previousVariant(mutation currType, int refPos, int quPos, int refStart, String r, String q, int kid, int currStart){
        int pos;
        String str;
        Variant v = null;

        switch (currType){
            case SNP:
                pos = refPos+refStart-1;
                str = kid+"@"+pos+"[S]";
                v = Variant.buildSNP(pos,kid,Character.toString(q.charAt(quPos-1)),str,str );
                break;
            case INSERTION:
                pos = currStart+refStart-1;
                str = kid+"@"+pos+"[I]";

                v = Variant.buildINSERTION(pos,kid,
                        q.substring(currStart,quPos),
                        str, str);
                break;
            case DELETION:
                int length = refPos - currStart;
                pos = currStart+refStart;
                str = kid+"@"+pos+"[D]";
                v = Variant.buildDELETION(pos,kid,length,str,str);
                break;
            default:
                v = Variant.buildDELETION(-1,kid,0,"NONE","NONE");
                break;
        }

        return v;
    }

    public void printAlignment() {
        System.out.println(top);
        System.out.println(left);
    }
}
