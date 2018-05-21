package org.cchmc.kluesuite.klat2;

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
public class PartialAlignment2 implements PartialAlignment,Comparable<PartialAlignment2>, Comparator<PartialAlignment2> {

    private final SmithWatermanTruncated2 swt2;

    private final SparseTable mySparseTable;


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

    @Override
    public float getPercentIdentity() {
        return 0;
    }

    public int numMatches;

    boolean gapISquery = true;  //Eventually deprecated this

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
     * #TODO ISSUE #84 :: remove lenth, add queryLength, referenceLength
     * BlastN format6 reporting term
     * this is alignment length
     * OKAY so blast length is greater of query_len and ref_len
     * reports are INCLUSIVE query :: end - start + 1
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


//
//    /**
//     * builds top and left Strings (optional)
//     *
//     * @param table
//     * @param row
//     * @param col
//     */
//    private void buildPath(ArrayList<TableEntry[]> table, int row, int col) {
//        SuperString topTemp = new SuperString();
//        SuperString leftTemp = new SuperString();
//
//        int n = columns.length();
//        int m = rows.length();
//
//        if (row < m) {
//            for (int k = 0; k < m - row; k++) {
//                topTemp.addAndTrim(".");
//                leftTemp.add(rows.charAt(m - 1 - k));
//            }
//        }
//
//        if (col < n) {
//            for (int k = 0; k < n - col; k++) {
//                topTemp.add(columns.charAt(n-1-k));
//                leftTemp.addAndTrim(".");
//            }
//        }
//
//        while (row > 0 || col > 0) {
//            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
//                //Traceback to DIAGONALLY   UP and LEFT
//                col--;
//                row--;
//                topTemp.add(columns.charAt(col));
//                leftTemp.add(rows.charAt(row));
//
//            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
//                //Traceback LEFT
//                col--;
//                if (row == 0) {
//                    leftTemp.addAndTrim(".");
//                }
//                else {
//                    leftTemp.addAndTrim("_");
//                }
//
//                topTemp.add(columns.charAt(col));
//
//            } else if (table.get(row)[col].move == TableEntry.DOWN) {
//                //Traceback UP
//                row--;
//                if (col == 0){
//                    topTemp.add('.');
//                }
//                else{
//                    topTemp.add('_');
//                }
//
//                leftTemp.add(rows.charAt(row));
//            }
//            int DEBUG=2;
//        }
//
//
//
//        top = topTemp.toReverseString();
//        left = leftTemp.toReverseString();
//    }

    /**
     *
     * @param swt       Already constructed Smith-Waterman table for the alignment (never modified, just read)
     * @param columns   Text representing reference string for alignment.
     * @param rows      Text representing query string for alignment.
     * @param row       Row coordinate where alignment starts (this is specific to one possibility, this alignment)
     * @param col       Column coordinate where alignment starts (this is specific to one possibility, this alignment)
     * @param kid       KLUE ID corresponding to the reference sequence
     * @param refStart  The position where the reference sequence starts (0-indexed? I think)
     */
    public PartialAlignment2(SmithWatermanTruncated2 swt, String columns, String rows, int row, int col, int kid, int refStart) {
        this(swt, columns, rows, row, col);
        this.kid = kid;
        this.refStart = refStart;
    }


    /**
     * Builds an alignment based on precalculated table and position (row, col) corresponding to an alignment in that table
     *
     * @param swt       Already constructed Smith-Waterman table for the alignment (never modified, just read)
     * @param columns
     * @param rows
     * @param row
     * @param col
     */
    public PartialAlignment2(SmithWatermanTruncated2 swt, String columns, String rows, int row, int col) {
        mismatchesCalled = new ArrayList<>();
        queryASleft = true;
        this.columns = columns;
        this.rows = rows;

        send = col;
        qend = row;

        //we cannot know this yet
        //SENTINEL flag for uninitialized
        sstart = -1;
        send = -1;
        swt2 = swt;
        mySparseTable = swt2.sparseTable;

//        inGap = false;
//        gapISleft = false;
//        gapStart = -1;
//        gapEnd = -1;
//        gapPos = -1;
//        mismatch = 0;
//
//
//        gaps = new ArrayList<AlignmentGap>();
//        called = new ArrayList<Variant>();


//        sparseTable.get(sparseTable.size()-1);
//        score = sparseTable.get(row)[col].score;
        score = buildPathAdvanced(row, col);
    }


//    private void buildPathAdvancedNoString(ArrayList<TableEntry[]> table, int row, int col) {
//        //buildPathAdvanced(table, row, col);
//        int n = columns.length();
//        int m = rows.length();
//        numMatches = 0;
//        numAligned = 0;
//
//
//        //BY DEFINTION,  !( row < m && col < n), both cannot be true
//        //execute one block, other block, or neither
//
//        //skip first rows that are not aligned
//        if (row < m) {
//            if (queryASleft) qstart = row-1;
//            else sstart = row-1;
//        }
//
//        //skip first columns that are not aligned
//        if (col < n) {
//            if (queryASleft) sstart = col-1;
//            else qstart = col-1;
//        }
//
//        inGap = false;
//
//        int alignPositionReverse = 0;
//        while (row > 0 || col > 0) {
//            if (table.get(row)[col].move == TableEntry.DIAGONAL) {
//                //Traceback to DIAGONALLY   UP and LEFT
//
//                //case 1: A match!
//                if (table.get(row)[col].score > table.get(row - 1)[col - 1].score){
//                    numMatches++;
//                }
//                //case 2: A mismatch!
//                else{
//                    mismatch++;
//                    //subtract 1 because of extra blank column
//                    if (queryASleft) {
//                        processMismatches(rows.substring(row-1, row), columns.substring(col-1, col), col-1);
//                    } else {
//                        processMismatches( columns.substring(col-1, col), rows.substring(row-1, row), row-1);
//                    }
//                }
//
//                col--;
//                row--;
//
//
//                //CASE 1 & CASE 2: Match or mismatch, numAligned is incremented
//                //numAligned counts the total number of matches, mismatches, and gaps in alignment
//                //here, match or mismatch
//                numAligned++;
//
//                //case 3: A gap / insertion or deletion (per queryASleft)
//            } else if (table.get(row)[col].move == TableEntry.RIGHT) {
//                //Traceback LEFT
//
//
//                if (row != 0) {   //row 0 does not count towards alignment; these are trailing or leading sequences
//                    numAligned++;
//                    processGap2(TableEntry.RIGHT, row, col, alignPositionReverse);
//                } else {
//                    break;
//                }
//                col--;
//
//                // case 4: A gap / insertion or deletion (per queryASleft)
//            } else if (table.get(row)[col].move == TableEntry.DOWN) {
//                //Traceback UP
//
//                if (col != 0) { //col 0 does not count towards alignment; these are trailing or leading sequences
//                    numAligned++;
//                    processGap2(TableEntry.DOWN, row, col, alignPositionReverse);
//                } else {
//                    break;
//                }
//                row--;
//            }
//
//            if (qend == -1 && (row == 0 || col == 0)) {
//                if (queryASleft) {
//                    qend = row;     //already decremented, so the 1 indexing corrected to 0 indexing
//                    send = col;
//                } else {
//                    qend = col;
//                    send = row;
//                }
//            }
//
//            if (qstart == -1) {
//                if (queryASleft) {
//                    if (row < m) {
//                        qstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
//                    }
//                } else {
//                    if (col < n) {
//                        qstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
//                    }
//                }
//            }
//
//            if (sstart == -1) {
//                if (queryASleft) {
//                    if (col < n) {
//                        sstart = col;     //already decremented, so the 1 indexing corrected to 0 indexing
//                    }
//                } else {
//                    if (row < m) {
//                        sstart = row;     //already decremented, so the 1 indexing corrected to 0 indexing
//                    }
//                }
//            }
//
//            alignPositionReverse++;
//        } // end while (trace path through table)
//
//        buildGaps2();
//
//
//        //reverse all gap position coordinate
//        //they were written in reverse, because alignment crawler is right to left, length is unknown
//        //gap coordinates easily reversed, as AlignmentKLAT1 is built in reverse, BUT gap offset will need to be processed in CALL VARIANTs?
//        //probably should not change here --> unless we write a callVariants2 to match
//        for (AlignmentGap x : gaps){
//            //            x.pos = numAligned - x.pos - 1;
//            x.pos = numAligned - x.pos - x.length+1;  //gap start is now gap end, so gap end becomes gap start
//
////            AlignmentGap2 z = (AlignmentGap2) x;
//            //row and column stored correctly as refPosition or qPosition, not offset for length
//            //THEY ARE NOW!!!
//
////            if (z.gapISquery) {
////                z.qPos = z.qPos - z.length + 1;
////            }else{
////                z.refPos = z.refPos - z.length + 1;
////            }
//
//
//        }
//
//        //COMBINED SNP calls into a chain
//        Collections.sort(mismatchesCalled);
//        //eliminate adjacent calls
//        for (int k=1; k< mismatchesCalled.size(); k++){
//            Variant a = mismatchesCalled.get(k-1);
//            Variant b = mismatchesCalled.get(k);
//            //both are SNPS
//            if(a.start+a.length == b.start){
//                a.length += b.length;
//                a.insertSequence += b.insertSequence;
//                String[] splitsA = a.detailedName.split("\\||/");
//                String[] splitsB = b.detailedName.split("\\||/");
//                a.detailedName = splitsA[0]+"|"+(splitsA[1]+splitsB[1]+"/"+splitsA[2]+splitsB[2]).replace("|","")+"|";
//                a.name = a.name;
//                //a.name
//                mismatchesCalled.remove(b);
//                k--;
//            }
//        }
//
//        //sorted in descending order, want ascending to parse gaps.
//        Collections.reverse(gaps);
//
//        //Because I am foolish, I have start and end reversed:
//        int temp;
//        temp = qstart;
//        qstart = qend;
//        qend = temp;
//        temp = sstart;
//        sstart = send;
//        send = temp;
//
//
//
//        pident = ((float)numMatches) / ((float)numAligned);
//
//        //see https://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
//        //let S = smith-waterman score
//        evalue = K * m * n * Math.exp(-1*score*lambda);
//        Double b = (lambda*score - Math.log(K)) / Math.log(2.0);
//        bitscore = b.intValue();
//
//        fastKLATscore = numMatches + Kmer31.KMER_SIZE_MINUS_ONE;
//
//        gapopen = gaps.size();
//
//        length = numAligned;
//
//    }

    private void processMismatches(String seq, String ref, int refCoor) {
        Variant v;
        //TODO are these strings called in correct order?
        v = Variant.buildSNP(refCoor + refStart,kid,seq,
                "unknown",(refCoor+refStart)+"|"+ref+"/"+seq+"|");
        mismatchesCalled.add(v);
    }

    private int buildPathAdvanced(int row, int col){
        int n = columns.length();
        int m = rows.length();
        numMatches = 0;
        numAligned = 0;

        if (row < m) {
            qend = row-1;
        }

        if (col < n) {
            send = col-1;
        }

        Box2 currBox;

        //does not reprocess if already done
        mySparseTable.calculateAlignment();

        int r = row;
        int c = col;
        int prevScore;
        boolean first = true;

        //crawl over alignments to calculate scores
        for (int k=mySparseTable.lastIdx; k>=0; k--){
            currBox = mySparseTable.get(k);
            CalculatedMatches curr = (CalculatedMatches) currBox;
            if (currBox.type == BoxType.CALCULATED) {
                //end when reaching corner ONLY
                while(r > currBox.srow || c > currBox.scol ){

                    // don't count if reaching top corner
                    // UNLESS leftmost
                    if (r == currBox.srow && c == currBox.scol //top corner
                            && k!=0) { //not leftmost
                        TableEntry t = curr.getTableEntry(r, c);

                        if (t.move == TableEntry.DIAGONAL) {


                            prevScore = curr.getTableEntry(r - 1, c - 1).score;
                            if (t.score < prevScore) {
                                //CASE 1: mismatch
                                mismatch++;
                                //TODO what does this do?  Do I need it?
                                processMismatches(rows.substring(r - 1, r), columns.substring(c - 1, c), c - 1);
                            } else {
                                //CASE 2: match
                                numMatches++;
                            }
                            numAligned++;

                            //TRACEBACK _diagonal up and left_
                            c--;
                            r--;

                        } else if (t.move == TableEntry.DOWN) {
                            //case 3: A gap / deletion
                            numAligned++;
                            processGap3(TableEntry.DOWN, r, c, numAligned);
                            //TRACEBACK _up_
                            r--;
                        } else if (t.move == TableEntry.RIGHT) {
                            //case 4: A gap / insertion
                            numAligned++;
                            processGap3(TableEntry.RIGHT, r, c, numAligned);
                            //TRACEBACK _left_
                            c--;
                        }
                    }
                }
            } else if (currBox.type == BoxType.EXACT){
                int length = currBox.ecol - currBox.scol + 1;
                numMatches += length;
                //numAligned counts the total number of matches, mismatches, and gaps in alignment
                //here, match or mismatch
                numAligned += length;
                r = currBox.srow - 1;
                c = currBox.scol - 1;
            }

            //now decremented to beginning -- end
            //already decremented, so the 1 indexing corrected to 0 indexing
            if (r == 0 || c == 0) {
                //TODO I need to save this for reporting  WHERE to save?
                break;
            }
        }

        mySparseTable.calculated = true;

        buildGaps3();

        pident = ((float)numMatches) / ((float)numAligned);

        //see https://www.ncbi.nlm.nih.gov/BLAST/tutorial/Altschul-1.html
        //let S = smith-waterman score
        evalue = K * m * n * Math.exp(-1*score*lambda);
        Double b = (lambda*score - Math.log(K)) / Math.log(2.0);
        bitscore = b.intValue();

        fastKLATscore = numMatches; // + Kmer31.KMER_SIZE_MINUS_ONE;

        gapopen = gaps.size();

        length = numAligned;

        fastKLATscore = mySparseTable.getFastKlatScore();

        return score = mySparseTable.getSmithWatermanScore();

    } // end constructor


    /**
     * Alternative way to track gaps, more quickly.
     * Does not require calculation when running.
     * Paired with buildGaps3 for after the fact
     *
     * @param direction whether the gap is RIGHT or DOWN movement
     * @param row   coordinate in table
     * @param col   coordinate in table
     */
    private void processGap3(int direction, int row, int col, int alignPosition) {
        if (gapsQuery == null){
            gaps2positions = new ArrayList<>();
            gaps2refPositions = new ArrayList<>();
            gaps2queryPositions = new ArrayList<>();
            gapsQuery = new ArrayList<>();
        }

        //We don't need this now?
        gaps2positions.add(alignPosition);

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
    }

    /**
     *
     */
    private void buildGaps3(){

        if (gapsQuery == null || gapsQuery.size() == 0){
            return;
        }

        //write gaps to gaps object as they are found
        int gapStart=0;
//        boolean queryStart = gapsQuery.get(0);
        int k;

        //TODO check this code works or not
        //NEED TO GO OVER IT -- tired

        for (k=1; k< gaps2positions.size();k++){
            boolean continueOn = (gapsQuery.get(k) == gapsQuery.get(gapStart)  //same side
                    && (gaps2positions.get(k) - gaps2positions.get(gapStart)) == (k-gapStart) );  //contiguous


            if (!continueOn){

                //subtraction reversed: coordinates reversed
                addGap3(gapStart,
                        gaps2refPositions.get(gapStart) - gaps2refPositions.get(k-1),
                        gaps2queryPositions.get(gapStart) - gaps2queryPositions.get(k-1));

                gapStart = k;
            }

        }
        //write final
        addGap3(gapStart,
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
    private void addGap3(int gapStart, int refOffset, int queryOffset) {
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
        return "SCORE: "+score+" of "+numAligned* SmithWatermanTruncated2.MATCH+" %id "+ pident *100+"\n"+top + "\n" + left;
    }

    /**
     * Full output of alignment statistics
     * @return
     */
    @Override
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


//    public ArrayList<Variant> callVariants(){
//
//        if (DO_NOT_CALCULATE_ALIGNMENT_STRINGS){
//            return callVariants2();
//        }
//
//        if (hasVariantsCalled){
//            return called;
//        }
//
//        //what about coordinate offset from multiple variants?   not sure
//        //the coordinates in reference string remain absolute and are used for the reference
//        //deletions and inserts should still occur at these absolute coordinates
//
//        for (int k=0; k < gaps.size()-1; k++){
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
//            //boolean gapISquery = (x.gapISquery == y.gapISquery);
//            if (end == nextStart && gapISquery){
//                gaps.set(k,new AlignmentGap(x.pos, x.length+y.length,x.gapISquery));
//                gaps.remove(k+1);
//                k--;
//                System.out.println("  WARNING :: gaps had to be combined for this alignment");
//            }
//        }
//        gapopen = gaps.size();
//
//        for (AlignmentGap g : gaps){
//
//            //g.pos is relative to the string they refer to (not reference coordinates)
//            if (g.gapISquery){
//                //DELETION
//                if (queryASleft) {
//                    called.add(Variant.buildDELETION(g.pos+refStart+1, kid, g.length,
//                            "<"+columns.substring(g.pos,g.pos+g.length())+"/->", "unknown:"+(g.pos+refStart+1)));
//                } else {
//
//                    called.add(Variant.buildDELETION(g.pos+refStart+1, kid, g.length,
//                            "<"+rows.substring(g.pos,g.pos+g.length())+"/->", "unknown:"+(g.pos+refStart+1)));
//                }
//            } else {
//                //INSERTION
//                if (queryASleft){
//
//                    String insert = columns.substring(g.pos,g.pos+g.length());
//                    called.add(Variant.buildINSERTION(g.pos+refStart+1,kid,insert,
//                            "<-/"+insert+">", "unknown:"+(g.pos+refStart-g.length+1)));
//                } else {
//                    String insert = rows.substring(g.pos,g.pos+g.length());
//                    called.add(Variant.buildINSERTION(g.pos+refStart+1,kid,insert,
//                            "<-/"+insert+">", "unknown:"+(g.pos+refStart-g.length+1)));  //Not sure why -g.length is needed
//                }
//            }
//        }
//
//        called.addAll(mismatchesCalled);
//
//        hasVariantsCalled = true;
//        return called;
//    }


    /**
     * Returns variants calculcated, so that position is relative to the start of the reference sequence
     * Part of the processGaps2 upgrade / no AlignmentKLAT1 String calculations  ISSUE #69  ISSUE #71  ISSUE #30 ISSUE #44
     * @return
     */
    public ArrayList<Variant> callVariants(){

        //TODO CIGAR STRINGS

        if (hasVariantsCalled){
            return called;
        }

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

    @Override
    public int compareTo(PartialAlignment2 o) {
        return swt2.compareTo(o.swt2);
    }

    @Override
    public int compare(PartialAlignment2 o1, PartialAlignment2 o2) {
        return o1.swt2.compareTo(o2.swt2);
    }
}
