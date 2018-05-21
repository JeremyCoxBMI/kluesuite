package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.PartialAlignment;
import org.cchmc.kluesuite.klat.PartialAlignment1;
import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klat.SmithWaterman;
import org.cchmc.kluesuite.masterklue.KLATsettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by jwc on Apr/23/18.
 *
 * To further enhance performance, we do not declare the entire table.
 * Java will not initialize a table empty, nor can we simply make array of uninitialized pointers
 * So we are stuck O(mn)
 * Instead, we only create mini-tables for the non-perfectly aligned tables.
 *
 * TERMINOLOGY
 * Cap Sequence: ????? TODO
 * ExactMatches: alignment already known as perfect (streak of exact seed matches)
 * CalculatedMatches: part of alignment not based on exact seed matches.
 *
 *
 * As SmithWatermanTruncated2, but uses  CalculatedMatches2 class instead of CalculatedMatches
 */


//
// each BOX will have field for the cumulative fastKLAT
// each box stores cumulative S-W score
// tables only kept for non-perfect boxes
// one arrayList for all Boxes
// we can sort them
//iterate over this structure to get alignment (or in reverse)


public class SmithWatermanTruncated3 implements SmithWaterman, Comparable<SmithWatermanTruncated3>, Comparator<SmithWatermanTruncated3> {

    static int MATCH = KLATsettings.SMITH_WATERMAN_MATCH;        //default 2
    static int MISMATCH = KLATsettings.SMITH_WATERMAN_MISMATCH;    //default -1
    static int GAP = KLATsettings.SMITH_WATERMAN_GAP;                //default -1

    /**
     * Whether PartialAlignment results contain text alignment (mostly for debugging)
     */
    public static boolean buildStrings = false;

    /**
     * True if rows represent query.  TRUE : This is the standard layout of tradition
     * force queryIsrows = true, cuz not necessary.
     * INCLUDE for reverse compatability
     */
    boolean queryISrows;

    /**
     * Values of query and reference
     * Note queryISrows indicates which is which
     * Note this is 0-index, but corresponding table coordinate is 1-indexed for same characters,
     * with 0 being all zero starting position
     */
    final String columns, rows;

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
     * Contains seeds calculated by parent Alignment Class, passed in during constructor
     */
    SuperSeed superSeed;

//    /**
//     *  this represents the maximum possible fast klat score that can be achieved by the alignment
//     */
//    public final int minPossibleFastKlatScore;

//    /**
//     *   the final calculated fastKlatScore
//     */
//    public int fastKlatScore;

    SparseTable sparseTable;

    /**
     *
     * @param query         query as nucleotide string
     * @param refseq        putative reference sequence as nucleotide string
     * @param superSeed     primer for known sub alignments
     * @param calculate     can be set to false to avoid CalculateMatches full sub alignments
     */
    public SmithWatermanTruncated3(String query, String refseq, SuperSeed superSeed, boolean calculate){
        this.kid = superSeed.myKid;
        this.refStart = superSeed.start;
        this.superSeed = superSeed;

        columns = refseq;
        rows = query;
        //buildSparseTable();

        buildSparseTable();
        if (calculate)  sparseTable.calculateAlignment();
    }

    /**
     * builds the sparse table, consisting of sorted Box2 pieces, representing the alignment.
     * Also calculated the minPossibleFastKlatScore
     * @return  (int)   minPossibleFastKlatScore
     */
    private int buildSparseTable() {
        int maxScore = 0;
        //Box2 temp;

        int refStop;  //INCLUSIVE
        int qStop;

        refStop = columns.length(); //columns
        qStop = rows.length();

        //TODO need to review here :: make sure each box is right size
        sparseTable = new SparseTable(superSeed.numSeeds()*2+2, rows, columns);  //maybe +1?

        seedFirst = true;  //will be proven false and set


        // **************************
        // build sparseTable
        // **************************


        Iterator<Seed> it = superSeed.iterator();
        Seed curr = null, prev = null;

        if (it.hasNext()) {
            curr = it.next();
        } else {
            return -1;
        }

        //PROCESS CAP SEQUENCE BEFORE FIRST SEED, if it exists
        //convert to faster comparison
        //!(curr.start == refStart || curr.queryStart == 0)
        if (curr.start != refStart || curr.queryStart != 0) {
            seedFirst = false;

            sparseTable.add(new CalculatedMatches2(curr, InitType.LEFT,
                    0,0,
                    0,0,
                    curr.queryStart, curr.start,refStart,
//  BUG 2017.Apr.04  :: the class is expecting full String
//                    rows.substring(0,curr.queryStart+1), columns.substring(0,curr.start+1)));
                    rows, columns));
            //queryEnd and end not used
        }

        //PROCESS FIRST SEED
        if (seedFirst) {
            sparseTable.add(new ExactMatches(curr,0,0,0,0));
        } else {
            sparseTable.add(new ExactMatches(curr,
                    sparseTable.getCumulativeMinimumFastKlat(),
                    sparseTable.getCumulativeMaximumFastKlat(),
                    sparseTable.getCumulativeMinimumSW(),
                    sparseTable.getCumulativeMaximumSW()
                    ));
        }

        prev=curr;
        //PROCESS SEEDS AND GAPS
        //First seed and cap (if it exists) processed
        while (it.hasNext()){
            curr = it.next();

            //write between gaps

            //2018.Apr.04    bug here in coordinate construction
            sparseTable.add(new CalculatedMatches2(
                    prev,
                    curr,
                    sparseTable.getCumulativeMinimumFastKlat(),
                    sparseTable.getCumulativeMaximumFastKlat(),
                    sparseTable.getCumulativeMaximumSW(),
                    sparseTable.getCumulativeMinimumSW(),
//                    rows.substring(prev.queryEnd,curr.queryStart+1), columns.substring(prev.end,curr.start+1)
                    rows, columns
            ));

            //write new diagonal
            sparseTable.add(new ExactMatches(curr,
                    sparseTable.getCumulativeMinimumFastKlat(),
                    sparseTable.getCumulativeMaximumFastKlat(),
                    sparseTable.getCumulativeMinimumSW(),
                    sparseTable.getCumulativeMaximumSW()
            ));

            prev = curr;
        }

        //PROCESS LAST GAP, IFF EXISTS
        //if last seed has gap afterwards

        //if neither reaches the end
        Box2 last = sparseTable.getLast();
        if (!(last.ecol  == refStop
                ||
                last.erow == qStop)) {

            //write between gaps
            //write right hand calculation
            sparseTable.add(new CalculatedMatches2(
                    curr,
                    InitType.RIGHT,
                    sparseTable.getCumulativeMinimumFastKlat(),
                    sparseTable.getCumulativeMaximumFastKlat(),
                    sparseTable.getCumulativeMaximumSW(),
                    sparseTable.getCumulativeMinimumSW(),
                    qStop, refStop, refStart,
//                    rows.substring(prev.queryEnd,rows.length()), columns.substring(prev.end,columns.length())
                    rows, columns
            ));


        }//end if

        return sparseTable.getCumulativeMinimumFastKlat();
    }

//    /**
//     * returns current cumulative MinFastKlat cumulative score as the table is built
//     * @return
//     */
//    private int lastMinFastKlat() {
//        return sparseTable.get(sparseTable.size()-1).cumulativeMinimumFastKlatScore;
//    }

//    private Box2 processGapToDiagonal(Seed curr, Seed prev, int previousMinFastKlatScore, int prevMaximumFastKlat) {
//            return new CalculatedMatches2(
//
//                    prev.queryEnd +Kmer31.KMER_SIZE_MINUS_ONE,
//                    prev.end - refStart + Kmer31.KMER_SIZE_MINUS_ONE,  //exclusive to INCLUSIVE: -1; 0 index to 1 index: +1
//                    curr.queryStart +1,   //INCLUSIVE to EXCLUSIVE
//                    curr.start - refStart +1, //INCLUSIVE to EXCLUSIVE
//                    previousMinFastKlatScore,
//                    prevMaximumFastKlat
//            );
//    }

    boolean isFastKlatCalculated() {
        return sparseTable.getFastKlatScore() < 0;
    }

    /**
     * If there are ties, multiple alignments will be returned.
     * @return
     */
    public ArrayList<PartialAlignment> bestAlignments2(){
        //enforce //            if (AlignmentKLAT1.calculateFastKlatScore(s) >= KLATsettings.MIN_FAST_KLAT_SCORE) {

        ArrayList<PartialAlignment> result = new ArrayList<>();
        ArrayList<PartialAlignment> temporary = new ArrayList<>();

        ArrayList<PairRC> prc = sparseTable.getWinnerAlignmentCoordinates();

        for (PairRC p : prc ){
            temporary.add( new PartialAlignment2(this, p, kid, buildStrings));
        }

        int max = Integer.MIN_VALUE;
        for (PartialAlignment p : temporary){
            if (p.getScore() > max) max = p.getScore();
        }
        for (PartialAlignment p : temporary){
            if (p.getScore() == max) result.add(p);
        }

        return result;
    }


    @Override
    public int compareTo(SmithWatermanTruncated3 o) {

        int bits = 0;
        if(isCalculated()) bits = 1;
        //else bits = 0;
        if (o.isCalculated()) bits += 2;
        //else bits += 0


        switch(bits){
            case 1:
                //I am calculated, o is not
                return getFastKlatScore() - o.getCumulativeMinimumFastKlat();
                //break;
            case 2:
                //I am not calculated, o is
                return getCumulativeMinimumFastKlat() - o.getFastKlatScore();
                //break;
            case 3:
                //we are both calculated
                return getFastKlatScore() - o.getFastKlatScore();
                //break;
            case 0:
            default:
                //neither are calculated
                return getCumulativeMinimumFastKlat() - o.getCumulativeMinimumFastKlat();
                //break;
        }
        //return 0;
    }

    @Override
    public int compare(SmithWatermanTruncated3 o1, SmithWatermanTruncated3 o2) {
        return o1.compareTo(o2);
    }

    /**
     * returns negative one if not calculated
     * @return
     */
    public int getFastKlatScore() {
        return  sparseTable.getFastKlatScore();
    }

    /**
     * returns negative one if not calculated
     * @return
     */
    public int getSmithWatermanScore() {
        return  sparseTable.getSmithWatermanScore();
    }

    public boolean isCalculated(){
        return sparseTable.isCalculated();
    }

    public Iterator<Box2> iterator(){
        return sparseTable.iterator();
    }

    public int getCumulativeMaximumFastKlat() {
        return  sparseTable.getCumulativeMaximumFastKlat();
    }

    public int getCumulativeMinimumFastKlat(){
        return sparseTable.getCumulativeMinimumFastKlat();
    }

    public int getCumulativeMaximumSW() {
        return  sparseTable.getCumulativeMaximumSW();
    }

    public int getCumulativeMinimumSW() {
        return  sparseTable.getCumulativeMinimumSW();
    }

    public Box2 getBox(int i) {
        if (i <= sparseTable.lastIdx)   return sparseTable.get(i);
        else return null;
    }

    @Override
    public ArrayList<PartialAlignment1> bestAlignments() {
        return null;
    }
}
