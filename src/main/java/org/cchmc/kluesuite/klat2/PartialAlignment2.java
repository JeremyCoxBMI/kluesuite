package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.helperclasses.CIGARstring;
import org.cchmc.kluesuite.klat.*;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by jwc on 10/5/17.
 *
 * I anticipate being very similar to PartialAlignment1, although the code can be cleaned (no reverse compatability needed)
 */
public class PartialAlignment2 extends PartialAlignment {

    private final SmithWatermanTruncated3 swt3;

    private final SparseTable mySparseTable;

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

    /**
     *
     * @param swt              Pre-existing calculated SmithWatermanTruncated alignment powering this specific alignment
     * @param p                End point (can be multiple alignments)
     * @param kid
     * @param buildStrings     Should alignment strings for friendly print out by built
     */
    public PartialAlignment2(SmithWatermanTruncated3 swt, PairRC p, int kid, boolean buildStrings) {
        mismatchesCalled = new ArrayList<>();
        this.columns = swt.columns;
        this.rows = swt.rows;
        this.kid = kid;

        send = p.c;
        qend = p.r;

        //we cannot know this yet
        //SENTINEL flag for uninitialized
        sstart = -1;
        send = -1;
        swt3 = swt;
        mySparseTable = swt3.sparseTable;

        score = buildPathTruncated3(p.r,p.c, buildStrings);

    }

    private int buildPathTruncated3(int row, int col, boolean buildStrings){

        SuperString topSS = null;
        SuperString leftSS = null;
        if (buildStrings){
            topSS = new SuperString();
            leftSS = new SuperString();
        }


        CIGARstring cigar = new CIGARstring();

        int n = columns.length();
        int m = rows.length();
        numMatches = 0;
        numAligned = 0;
        int currGapType = TableEntry.EMPTY;

        if (row < m) {
            qend = row-1;
        }

        if (col < n) {
            send = col-1;
        }

        Box2 currBox;

        //does not reprocess if already done
        mySparseTable.calculateAlignment();

        int r, c; //row and column for parsing table; 0-indexed, relative
        int prevScore;
        boolean first = true;
        int FIRST_ROW_COL;

        //crawl over alignments to calculate scores
        for (int k=mySparseTable.lastIdx; k>=0; k--){
            currBox = mySparseTable.get(k);
            InitType init;

            if (currBox.type == BoxType.CALCULATED) {
                CalculatedMatches2 curr = (CalculatedMatches2) currBox;
                if ( k == mySparseTable.lastIdx ) { // InitType.RIGHT
                    init = InitType.RIGHT;
                    r = row - currBox.srow;
                    c = col - currBox.scol;
                    FIRST_ROW_COL = 1;
                }else if ( k == 0){
                    init= InitType.LEFT;
                    r = currBox.erow - currBox.srow - 1;
                    c = currBox.ecol - currBox.scol - 1;
                    FIRST_ROW_COL = 0;
                } else {
                    init = InitType.MID;
                    r = currBox.erow - currBox.srow - 1;
                    c = currBox.ecol - currBox.scol - 1;
                    FIRST_ROW_COL = 1;
                }


                //end when reaching top corner (which may is (1,1) for most calculated boxes)
                while(r >= FIRST_ROW_COL && c >= FIRST_ROW_COL ){

                    // Process Entry ; tabulates for each
                    // don't count if reaching top corner
                    // UNLESS leftmost
                    int currScore = curr.getScoreAt(r, c);
                    int move = curr.getMoveAt(r,c);

                    if (move == TableEntry.DIAGONAL) {
                        if (buildStrings){
                            topSS.add(columns.charAt(c+currBox.scol));
                            leftSS.add(rows.charAt(r + currBox.srow));
                        }
                        currGapType = TableEntry.EMPTY;
                        prevScore = curr.getScoreAt(r-1,c-1);
                        if (score < prevScore) {
                            //CASE 1: mismatch
                            mismatch++;
                            cigar.addMisMatch();
                        } else {
                            //CASE 2: match
                            numMatches++;
                            cigar.addMatch();
                        }
                        numAligned++;


                        //TRACEBACK _diagonal up and left_
                        c--;
                        r--;
                    } else if (move == TableEntry.DOWN) {
                        //case 3: A gap / deletion


                        //check if moving off table
                        if (buildStrings) {
                            leftSS.add(rows.charAt(r + currBox.srow));
                            if (init == InitType.LEFT && (col == 0)) {
                                topSS.add(" ");
                            } else {
                                topSS.add("_");
                            }
                        } else { //no building Strings allows aborting early
                            if (init == InitType.LEFT && (col == 0)) {  // stop condition
                                break;
                            } //else continue
                        }
                        numAligned++;
                        cigar.addDeletion();
                        if (currGapType != move){
                            currGapType = move;
                            gapopen++;
                        }

                        r--;
                    } else if (move == TableEntry.RIGHT) {
                        //case 4: A gap / insertion
                        //check if moving off table
                        if (buildStrings) {
                            topSS.add(columns.charAt(c+currBox.scol));
                            if (init == InitType.LEFT && (row == 0)) {
                                leftSS.add(" ");
                            } else {
                                leftSS.add("_");
                            }
                        } else { //no building Strings allows aborting early
                            if (init == InitType.LEFT && (col == 0)) {  // stop condition
                                break;
                            } //else continue
                        }

                        numAligned++;
                        cigar.addInsertion();
                        if (currGapType != move){
                            currGapType = move;
                            gapopen++;
                        }
                        //processGap3(TableEntry.RIGHT, r, c, numAligned);
                        //TRACEBACK _left_
                        c--;
                    }

                }
            } else if (currBox.type == BoxType.EXACT){
                int length = currBox.ecol - currBox.scol + 1;
                numMatches += length;
                //numAligned counts the total number of matches, mismatches, and gaps in alignment
                //here, match or mismatch
                numAligned += length;

                if (buildStrings) {
                    topSS.addReverse( columns.substring(currBox.scol, currBox.ecol + 1) );
                    leftSS.addReverse( rows.substring(currBox.srow, currBox.erow + 1) );
                }
            }
        }

//        mySparseTable.calculated = true;

        pident = ((float)numMatches) / ((float)numAligned);

        //see https://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
        //let S = smith-waterman score
        evalue = K * m * n * Math.exp(-1*score*lambda);
        Double b = (lambda*score - Math.log(K)) / Math.log(2.0);
        bitscore = b.intValue();

        fastKLATscore = numMatches; // + Kmer31.KMER_SIZE_MINUS_ONE;

//        gapopen = gaps.size();

        length = numAligned;

        fastKLATscore = mySparseTable.getFastKlatScore();

        if (buildStrings){
            top = topSS.toString();
            left = leftSS.toString();
        }
        CIGARString = cigar.toString();

        return score = mySparseTable.getSmithWatermanScore();

    } // end buildPathTruncated3


    /**
     * printable string representing alignment (reference over query)
     * @return printable string; null if no information
     */
    public String printAlignment(){

//        int row = qend;
//        int col = send;
//
//        if (top == null && left == null ) {
//            SuperString topSS = new SuperString();
//            SuperString leftSS = new SuperString();
//
//            boolean outsideRows = false;
//            boolean outsideCols = false;
//
//            int n = columns.length();
//            int m = rows.length();
//            numMatches = 0;
//            numAligned = 0;
//            int currGapType = TableEntry.EMPTY;
//
//            if (row < m) {
//                qend = row - 1;
//            }
//
//            if (col < n) {
//                send = col - 1;
//            }
//
//            Box2 currBox;
//
//            //does not reprocess if already done
//            mySparseTable.calculateAlignment();
//
//            int r, c; //row and column for parsing table; 0-indexed, relative
//            int prevScore;
//            boolean first = true;
//            int FIRST_ROW_COL;
//            int LAST_COL, LAST_ROW;
//
//            //crawl over alignments to calculate scores
//            for (int k = mySparseTable.lastIdx; k >= 0; k--) {
//                currBox = mySparseTable.get(k);
//                InitType init;
//
//                if (currBox.type == BoxType.CALCULATED) {
//                    CalculatedMatches2 curr = (CalculatedMatches2) currBox;
//                    if (k == mySparseTable.lastIdx) { // InitType.RIGHT
//                        init = InitType.RIGHT;
//                        r = row - currBox.srow;
//                        c = col - currBox.scol;
//                        FIRST_ROW_COL = 1;
//                        LAST_ROW = row;
//                        LAST_COL = col;
//                        outsideRows = true;
//                        outsideCols = true;
//                    } else if (k == 0) {
//                        init = InitType.LEFT;
//                        r = currBox.erow - currBox.srow - 1;
//                        c = currBox.ecol - currBox.scol - 1;
//                        FIRST_ROW_COL = 0;
//                        LAST_ROW = row-1;
//                        LAST_COL = col-1;
//                    } else {
//                        init = InitType.MID;
//                        r = currBox.erow - currBox.srow - 1;
//                        c = currBox.ecol - currBox.scol - 1;
//                        FIRST_ROW_COL = 1;
//                        LAST_ROW = row-1;
//                        LAST_COL = col-1;
//                    }
//
//
//                    //end when reaching top corner (which may is (1,1) for most calculated boxes)
//                    while (r >= FIRST_ROW_COL && c >= FIRST_ROW_COL) {
//
//                        // Process Entry ; tabulates for each
//                        // don't count if reaching top corner
//                        // UNLESS leftmost
//                        int move = curr.getMoveAt(r, c);
//
//                        if (move == TableEntry.DIAGONAL) {
//                            topSS.add(columns.charAt(c+currBox.scol));
//                            leftSS.add(rows.charAt(r+currBox.srow));
//                            //TRACEBACK _diagonal up and left_
//                            c--;
//                            r--;
//                        } else if (move == TableEntry.DOWN) {
//                            //case 3: A gap / deletion
//                            if (outsideRows){
//                                topSS.add(" ");
//                            } else {
//                                topSS.add("_");
//                            }
//                            leftSS.add(rows.charAt(r+currBox.srow));
//                            //TRACEBACK _up_
//                            r--;
//                        } else if (move == TableEntry.RIGHT) {
//                            //case 4: A gap / insertion
//                            if (outsideCols){
//                                leftSS.add(" ");
//                            } else {
//                                leftSS.add("_");
//                            }
//                            topSS.add(columns.charAt(c+currBox.scol));
//
//                            c--;
//                        }
//                        if (init == InitType.RIGHT && r < LAST_ROW)   outsideCols = false;
//                        if (init == InitType.RIGHT && c < LAST_COL)   outsideRows = false;
//                        if (init == InitType.LEFT && r == 0) outsideCols = true;
//                        if (init == InitType.LEFT && c == 0) outsideRows = true;
//                    }
//                } else if (currBox.type == BoxType.EXACT) {
//                    topSS.add(columns.substring(currBox.scol, currBox.ecol + 1));
//                    leftSS.add(rows.substring(currBox.srow, currBox.erow + 1));
//                }
//            }
//            top = topSS.toReverseString();
//            left = leftSS.toReverseString();
//
//        }
        if (top == null || left == null)
            return null;
        return top+"\n"+left;
    }



    /**
     * string in human readable form; may be query or reference, see queryASleft
     */
    String top;

    /**
     *  string in human readable form; may be query or reference, see queryASleft
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

//    /**
//     * All variants called
//     */
//    ArrayList<Variant> called;
//
//    private boolean hasVariantsCalled  = false;


    /**
     *
     * @param swt       Already constructed Smith-Waterman table for the alignment (never modified, just read)
     * @param row       Row coordinate where alignment starts (this is specific to one possibility, this alignment)
     * @param col       Column coordinate where alignment starts (this is specific to one possibility, this alignment)
     * @param kid       KLUE ID corresponding to the reference sequence
     * @param refStart  The position where the reference sequence starts (0-indexed? I think)
     */
    public PartialAlignment2(SmithWatermanTruncated3 swt, int row, int col, int kid, int refStart, boolean buildStrings) {
        this(swt, row, col, buildStrings);
        this.kid = kid;
        this.refStart = refStart;
    }


    /**
     * Builds an alignment based on precalculated table and position (row, col) corresponding to an alignment in that table
     *
     * @param swt       Already constructed Smith-Waterman table for the alignment (never modified, just read)
     * @param row
     * @param col
     */
    private PartialAlignment2(SmithWatermanTruncated3 swt, int row, int col, boolean buildStrings) {
        mismatchesCalled = new ArrayList<>();
        this.columns = swt.columns;
        this.rows = swt.rows;

        send = col;
        qend = row;

        //we cannot know this yet
        //SENTINEL flag for uninitialized
        sstart = -1;
        send = -1;
        swt3 = swt;
        mySparseTable = swt3.sparseTable;

        score = buildPathTruncated3(row, col, buildStrings);
    }


}
