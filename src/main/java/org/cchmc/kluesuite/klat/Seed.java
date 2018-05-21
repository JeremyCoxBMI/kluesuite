package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.masterklue.KLATsettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by osboxes on 22/07/16.
 *
 * AlignmentKLAT1 Seed Class
 * A helper class and function library for AlignmentKLAT1 class
 *
 * K-mer lookups become individual seeds, which get combined into in bigger seeds according to KLATsettings parameters
 *
 * Start is start of reference sequence, so if in Reverse, it will be greater than end
 * NOTE ranges are in [ inclusive, exclusive ) format; second number may seem incorrect
 *
 * 2016-Aug-04  v1.6    Changed toString so that hits precede adjacency score, to match constructor
 * 2016-11-18           Add SNP and INDEL flags
 *
 *
 * 2017-09-13   seed merging functions need total rework
 *                 believe mayMergeAgglomeratedSeeds() is correct
 * 2018-02-07   entire Seed class under review and being tested.  Total design comments included.
 *                 WILL NOT consider strided seed calculations at this time
 *
 *                 DESIGN DECISION:  Reverse seeds have query sequence from least to greatest coordinates,
 *                                      But reference sequence coordinates are greatest to least
 *
 */
public class Seed implements Comparable<Seed>, Comparator<Seed> {

    public int myKid;

    /**
     * Position within the query INCLUSIVE
     */
    public int queryStart;

    /**
     * Last position within the query EXCLUSIVE
     * This marks the last k-mer start position, not the rightEdge of the k-mer reach
     */
    public int queryEnd;

    /**
     * AlignmentKLAT1 starting position, INCLUSIVE
     * If in reverse, start > end
     */
    public int start;

    /**
     * AlignmentKLAT1 stopping position EXCLUSIVE (i.e. 1 higher than last)
     * This marks the last k-mer start position, not the rightEdge of the k-mer reach
     */
    public int end;

    /**
     * Is it a reverse sequence
     */
    public boolean isReverse;

    /**
     * number of kmer hits this seed represents
     */
    public int hits;

    /**
     * This tracks the longest run of adjacent kmers in the Seed.
     * Must be adjacent in QUERY and REFERENCE SEQUENCE
     * 2018-02-08 fixed several bugs not enforcing reference adjacency
     */
    public int adjacency;

    /**
     * Indicates that the SEED contains positions flagged as SNP, meaning containing an alternate DNA sequence.
     */
    public boolean snp;

    /**
     * Indicates that the SEED contains positions flagged as INDEL.  This requires additional checking by KLAT.
     */
    public boolean indel;

    /**
     * Indicates the fastKLATscore based on the seeds agglomerated
     * TODO: for Klat2, this field is unnecessary, remove
     */
    public int fastKLATscore;

//    public int getFastKlatScore() { return hits + Kmer31.KMER_SIZE - 1; }


    public static Seed DEFAULT = new Seed(-1,-1,-1,-1,false,-1,-1,-1);

    public Seed(){
        queryStart=0;
        queryEnd =0;
        start =0;
        end =1;
        isReverse=false;
        hits =0;
        adjacency=0;
        fastKLATscore=0;
        snp = false;
        indel = false;
    }

        /**
     * DEPRECATED constructor
     * @param qStart
     * @param qEnd
     * @param start
     * @param end
     * @param reverse
     * @param hits
     * @param adjacency
     * @param myKid
     */
    public Seed(int qStart, int qEnd, int start, int end, boolean reverse, int hits, int adjacency, int myKid){
        this.queryStart = qStart;
        this.queryEnd = qEnd;
        this.start=start;
        this.end = end;
        this.isReverse = reverse;
        this.hits = hits;
        this.adjacency = adjacency;
        this.myKid = myKid;
        this.snp = false;
        this.indel = false;

        //NOT IMPLEMENTED for old constructor
        fastKLATscore=hits;
    }

    /**
     * #ConstructorA#
     * Default constructor, expanded
     * Can only correctly calculate fast klat score if hits==adjacency (adjacent seeds agglomerated)
     *      Because if there is a gap, we must know #gaps and each gap size to know
     * @param qStart
     * @param qEnd
     * @param start
     * @param end
     * @param reverse
     * @param hits
     * @param adjacency
     * @param myKid
     * @param snp
     * @param indel
     */
    public Seed(int qStart, int qEnd, int start, int end, boolean reverse, int hits, int adjacency, int myKid, boolean snp, boolean indel){
        this.queryStart = qStart;
        this.queryEnd = qEnd;
        this.start=start;
        this.end = end;
        this.isReverse = reverse;
        this.hits = hits;
        this.adjacency = adjacency;
        this.myKid = myKid;
        this.snp = snp;
        this.indel = indel;

        this.fastKLATscore = hits + Kmer31.KMER_SIZE - 1;
        if (hits != adjacency) {
            System.err.println("WARNING\thits and adjacency not equal using this constructor.  fastKLATscore unknown.");
            System.err.println("Seed::Seed(int qStart, int qEnd, int start, int end, boolean reverse, int hits, int adjacency, int myKid, boolean snp, boolean indel");

        }
    }

    /**
     * #ConstructorB#
     *
     * includes directly setting fastKLATscore
     * @param qStart
     * @param qEnd
     * @param start
     * @param end
     * @param reverse
     * @param hits
     * @param adjacency
     * @param myKid
     * @param snp
     * @param indel
     * @param fastKLATscore
     */
    public Seed(int qStart, int qEnd, int start, int end, boolean reverse, int hits, int adjacency, int myKid, boolean snp, boolean indel, int fastKLATscore){
        this.queryStart = qStart;
        this.queryEnd = qEnd;
        this.start=start;
        this.end = end;
        this.isReverse = reverse;
        this.hits = hits;
        this.adjacency = adjacency;
        this.myKid = myKid;
        this.snp = snp;
        this.indel = indel;

        this.fastKLATscore = fastKLATscore;

    }

    /**
     * #ConstructorCopy#
     * @param copy
     */
    public Seed( Seed copy ){
        this.queryStart = copy.queryStart;
        this.queryEnd = copy.queryEnd;
        this.start=copy.start;
        this.end =copy.end;
        this.isReverse = copy.isReverse;
        this.hits = copy.hits;
        this.adjacency = copy.adjacency;
        this.myKid = copy.myKid;
        this.snp = copy.snp;
        this.indel = copy.indel;
        this.fastKLATscore = copy.fastKLATscore;
    }

    /**
     * #ConstructorD#
     * This constructor assumes we are making of length 1
     * Analgous to single entry in look-up table
     */
    public Seed( int qStart, int start, boolean reverse, int myKid){
        this.queryStart = qStart;
        this.queryEnd = qStart + 1;
        this.start=start;
        this.isReverse = reverse;

        if (isReverse)
            this.end =start-1;
        else
            this.end =start+1;

        //Assume length 1
        this.hits = 1;
        this.adjacency = 1;
        this.myKid = myKid;
        this.snp = false;
        this.indel = false;
        this.fastKLATscore = Kmer31.KMER_SIZE;  //reading frame 31 for 1 base
    }

    /**
     * #ConstructorE#
     * This constructor set all the flags appropriately for the SEED, including INDEL and SNP
     *
     * Converts a Position class to a Seed with extra information
     * @param qStart
     * @param qEnd
     * @param pos
     * @param myKid
     */
    public Seed(int qStart, int qEnd, Position pos, int myKid) {



        this.queryStart = qStart;
        this.queryEnd = qEnd;


        if (pos != null) {
            this.start=pos.getPosition();
            if (pos.getFlag(Position.REVERSE)) {
                this.isReverse = true;
                this.end = start - 1;
            } else {
                this.isReverse = false;
                this.end = start + 1;
            }
            this.snp = pos.getFlag(Position.SNP);
            this.indel = pos.getFlag(Position.INDEL);
        } else {
            Exception e = new Exception("\tWARNING\tnull Position passed to Seed constructor.");
            e.printStackTrace();
            this.start = 0;
            this.isReverse = false;
            this.end = start;
            this.snp = false;
            this.indel = false;
        }
        this.hits = 1;
        this.adjacency = 1;
        this.myKid = myKid;
        this.fastKLATscore = Kmer31.KMER_SIZE;  //reading frame 31 for 1 base
    }


    /**
     * Extracts Position from the Seed
     * NOTE this includes only the START position
     *      TODO Why do we need this?
     * @return
     */
    public Position getPosition(){
        Position result = new Position(myKid,start);
        result.setFlag(Position.SNP,this.snp);
        result.setFlag(Position.INDEL,this.indel);
        result.setFlag(Position.REVERSE, isReverse);
        return result;
    }



    /**
     * determine if seeds should be merged:
     * they are adjacent, but DO NOT overlap on query or reference
     * 2017-07-11 rewriting to addAndTrim latter criterion
     * 2017-07-14   needs to be gap aware
     *
     * CRITERIA FOR A MERGE
     *  1) both forward or both reverse
     *  2) reference coordinates are within WHISKER distance of each other, do not overlap
     *  3) query coordinates within WHISKER distance of each other, do not overlap
     *  4) Both Query and Reference are in strictly increasing order
     *      //TODO is this necessary?   does this break reverse?
     * @param a
     * @param b
     * @return
     */

    public static Seed mergeIfAble(Seed a, Seed b) {
//        Seed result = new Seed(-1,-1, -1,-1,a.isReverse,-1,-1, -1);
        //BUG: need to use copy constructor
        Seed result, temp;


        /*
         * Fast KLAT score merging:
         * CASE 1: the seeds are adjacent as both reference and query sequences
         * CASE 2: the seeds are adjacent as reference sequences
         * CASE 3: the seeds are not adjacent, reference sequences closer than 31 to each other
         * CASE 4: other  (not adjacent, ref sequences significant distance)
         */

        //TODO does this work for reverse?  may have broke it, see more universal code below that was replaced
        //Reverse seeds have start and end reversed??  (CHECK)

        //result initialized
        result = new Seed(Seed.DEFAULT);

        if (b == null || a == null){
            return result;
        }

        //put in order
        if (b.start < a.start) {
            //result used as temporary value
            temp = a;
            a = b;
            b = temp;
        }




        //calculate limits of overlap permitted
        int minA = a.start - KLATsettings.MAX_SEED_REFERENCE_GAP; //INCLUSIVE
        int maxA = a.end + KLATsettings.MAX_SEED_REFERENCE_GAP + 1;  //EXCLUSIVE


        int minAq = a.queryStart - KLATsettings.MAX_SEED_QUERY_GAP; //INCLUSIVE
        int maxAq = a.queryEnd + KLATsettings.MAX_SEED_QUERY_GAP + 1;  //EXCLUSIVE


        boolean ADJACENT,
                QUERY_DISTANCE,
                IN_ORDER,
                REF_WHISKER,
                QUERY_WHISKER,
                REF_OVERLAP = false,
                OVERLAP,
                QUERY_OVERLAP=false, KID_MATCH;

        int adjacency, hits;

        //calculate if they overlap within range of each other within the KID reference sequence
        //TODO Issue #65 Issue #64 this is wrong
        REF_WHISKER = ((b.start >= minA && b.start < maxA) || (b.end > minA && b.end <= maxA));

        QUERY_WHISKER = ((b.queryStart >= minAq && b.queryStart < maxAq) || (b.queryEnd > minAq && b.queryEnd <= maxAq));

        // range b:[r,s] cannot overlap a:[w,x]
        //Stop is exclusive, so different comparators needed
        QUERY_OVERLAP = ((a.queryStart < b.queryEnd && a.queryStart >= b.queryStart) ||
                (a.queryEnd <= b.queryEnd && a.queryEnd > b.queryStart));


        int refOverlap = 0;


//        if ((a.queryStart < b.queryEnd && a.queryStart >= b.queryStart)){
//            QUERY_OVERLAP = true;
//            if (a.queryEnd <= b.queryEnd){
//                //case 1: totally overlapping
//                overlap = Math.max(a.queryEnd-a.queryStart, overlap);
//            } else {
//                //case 2: overlap at edge
//                overlap = Math.max(b.queryEnd - a.queryStart, overlap);
//            }
//
//        } else if (a.queryEnd <= b.queryEnd && a.queryEnd > b.queryStart){
//            QUERY_OVERLAP = true;
//            if (a.queryEnd >= b.queryEnd){
//                //case 1: totally overlapping
//                overlap = Math.max(b.queryEnd-b.queryStart, overlap);
//            } else {
//                //case 2: overlap at edge
//                overlap = Math.max(a.queryEnd - b.queryStart, overlap);
//            }
//        }




        //Need to prevent combining when they are not in increasing order for both query and reference
        IN_ORDER = (a.queryEnd <= b.queryStart && a.end <= b.start) ||
                        (b.queryEnd <= a.queryStart && b.end <= a.start);


        //TODO Issue #65
        REF_OVERLAP =   (a.start < b.end && a.start >= b.start) || (a.end <= b.end && a.end > b.start);

//        //modified for overlapping FastKlatScore
//        if (a.start < b.end && a.start >= b.start){
//            REF_OVERLAP = true;
//
//            if (a.end <= b.end){
//                //case 1: totally overlapping
//                refOverlap = Math.max(a.end-a.start, refOverlap);
//            } else {
//                //case 2: overlap at edge
//                refOverlap = Math.max(b.end - a.start, refOverlap);
//            }
//
//        } else if (a.end <= b.end && a.end > b.start){
//            REF_OVERLAP = true;
//            if (a.end >= b.end){
//                //case 1: totally overlapping
//                refOverlap = Math.max(b.end-b.start, refOverlap);
//            } else {
//                //case 2: refOverlap at edge
//                refOverlap = Math.max(a.end - b.start, refOverlap);
//            }
//        }


        KID_MATCH = (a.myKid == b.myKid);

        if (KID_MATCH && REF_WHISKER && QUERY_WHISKER && IN_ORDER && !QUERY_OVERLAP && !REF_OVERLAP) {

            result.hits = a.hits + b.hits; //what about overlap?
            result.myKid = a.myKid;


            result.fastKLATscore = fastKLATscoreFromSeeds(a,b);

            //calculate adjacency
            //two cases: the seeds are adjacent to each other or not
            if (a.queryEnd == b.queryStart && a.isAdjacencyStreak() && b.isAdjacencyStreak()) {   //TODO reverse?  I think so, we just putting them in forward order and calculating
                result.adjacency = a.adjacency + b.adjacency;

            } else {
                result.adjacency = Math.max(a.adjacency, b.adjacency);

//                //if adjacent, reduce score due to overlap

//                if (refOverlap < Kmer31.KMER_SIZE){
//                    result.fastKLATscore -= refOverlap;
//                }

                //result.fastKLATscore = overlappingKmersFastKlat(a, b);
//
//                if (a.queryEnd == b.queryStart){
//                    result.fastKLATscore -= (Kmer31.KMER_SIZE-1);
//                }
            }


            if (a.isReverse) {
                result.start = Math.max(a.start, b.start);
                result.end = Math.min(a.end, b.end);
            } else {
                result.start = Math.min(a.start, b.start);
                result.end = Math.max(a.end, b.end);
            }

            if (a.isReverse) {
                result.queryStart = Math.max(a.queryStart, b.queryStart);
                result.queryEnd = Math.min(a.queryEnd, b.queryEnd);
            } else {
                result.queryStart = Math.min(a.queryStart, b.queryStart);
                result.queryEnd = Math.max(a.queryEnd, b.queryEnd);
            }

            result.indel = a.indel || b.indel;
            result.snp = a.snp || b.snp;

        }
        //else seeds should not merge

//        if (result.equals(Seed.DEFAULT)) result = null;
        return result;
    }




    /**
     * If two seeds are adjacent, returns true.  For forward and reverse.
     * @param a
     * @param b
     * @return
     */
    public static boolean mayMergeAdjacentSeeds(Seed a, Seed b){
        return isAdjacent(a,b);
    }


    /**
     * CONSECUTIVE SEEDS
     *
     * CRITERIA FOR A MERGE
     *  0) Seeds are result of mayMergeAdjacentSeeds, which produces agglomerated seeds
     *
     *  1) both forward or both reverse
     *  2) reference coordinates are within WHISKER distance of each other, seed positions do not overlap
     *  3) query coordinates within WHISKER distance of each other, seed position do not overlap
     *
     *  may choose to prevent merging without a gap: minGap of 0 means seeds are adjacent
     *
     * @param a
     * @param b
     * @param minGap the gap distance required between seeds, can be 0 or more (INCLUSIVE)
     *               minGap = 1 means they cannot be adjacent
     * @return
     */
    public static boolean mayMergeConsecutiveSeeds(Seed a, Seed b, int minGap, int maxGap){
        //DEFAULT maxGap = KLATsettings.WHISKERS_LENGTH_ALIGNMENT

        if (a.myKid != b.myKid){
            return false;
        }

        if (b == null || a == null){
            return false;
        }

        Seed temp;

        if (a.isReverse != b.isReverse) {
            return false;
        }

        //ENSURE seeds in correct order
        //query always in increasing order
        if (    (b.queryStart < a.queryStart)   ){
            //result used as temporary value
            temp = a;
            a = b;
            b = temp;
        }


        //KEY DIFFERENCE WITH MAY MERGE AGGLOMERATED SEEDS (ISSUE #64)
        //Adjacent seeds are adjacent, these must have a gap of at least 1
        boolean noOverlap = b.queryStart - a.queryEnd >= minGap;

        boolean withinWhisker = b.queryStart < a.queryEnd + maxGap; //b starts within whisker


//        //ENDS are EXCLUSIVE
//        return b.queryStart - a.queryEnd == 0
//                &&
//                Math.abs(a.end - b.start) == 0;  //REVERSE AND FORWARD


        if (a.isReverse){
            //Checking for reference or query overlap  Issue #65
            //Adjacent seeds are adjacent, these must have a gap of at least 1
            noOverlap = noOverlap && a.end - b.start >= minGap;
            //Checking seeds are close enough
            //Whisker must extend to the next seed position or farther
            withinWhisker = withinWhisker || b.start > a.end - maxGap; //b starts within whisker

        } else {
            //Checking for reference or query overlap
            //Adjacent seeds are adjacent, these must have a gap of at least 1
            noOverlap = noOverlap && b.start - a.end >= minGap;
            //Checking seeds are close enough
            //Whisker must extend to the next seed position or farther
            withinWhisker = withinWhisker || b.start < a.end + maxGap; //b starts within whisker

        }

        //ASSERTED early they match
        // boolean kidMatch = (a.myKid == b.myKid);
        // return noOverlap && withinWhisker && kidMatch;

        return noOverlap && withinWhisker;
    }

    /**
     *  CRITERIA FOR A MERGE
     *  0) these seeds agglomerate seeds:   are not adjacent or consecutive seeds
     *      NOTE THERE IS NOT TEST to guarantee this is so; this criterion is based on STEP 3 of Seed agglomeration
     *
     *  1) both forward or both reverse
     *  2) reference coordinates are within WHISKER distance of each other, whole sequences do not overlap
     *  3) query coordinates within WHISKER distance of each other, whole sequences do not overlap
     * @param a
     * @param b
     * @param kmerSize
     * @return
     */
    public static boolean mayMergeAgglomeratedSeeds(Seed a, Seed b, int kmerSize){
        //boolean result = false;

        if (a.myKid != b.myKid){
            return false;
        }

        int KMER_SIZE_MINUS_ONE = kmerSize - 1;
        if (b == null || a == null){
            return false;
        }

        Seed temp;

        if (a.isReverse != b.isReverse) {
            return false;
        }

            //ENSURE seeds in correct order
            //query always in increasing order
        if (    (b.queryStart < a.queryStart)   ){
            //result used as temporary value
            temp = a;
            a = b;
            b = temp;
        }

        int aRefRightEdge;
        int aQueRightEdge = a.queryEnd + KMER_SIZE_MINUS_ONE;

        //Issue #64
        boolean noOverlap = aQueRightEdge < b.queryStart;

        boolean withinWhisker = b.queryStart < a.queryEnd + KLATsettings.WHISKERS_LENGTH_ALIGNMENT; //b starts within whisker

        if (a.isReverse){
            aRefRightEdge = a.end - KMER_SIZE_MINUS_ONE;  //because EXCLUSIVE ==> INCLUSIVE
            //noOverlap = noOverlap && a.end - b.start >= 1;
            //Checking for reference or query overlap  Issue #65
            noOverlap = noOverlap && aRefRightEdge > b.start;
            //Checking seeds are close enough
            //Whisker must extend to the next seed position or farther
            withinWhisker = withinWhisker || b.start > a.end - KLATsettings.WHISKERS_LENGTH_ALIGNMENT; //b starts within whisker

        } else {
            aRefRightEdge = a.end + KMER_SIZE_MINUS_ONE;  //because EXCLUSIVE ==> INCLUSIVE
            //Checking for reference or query overlap
            noOverlap = noOverlap && aRefRightEdge < b.start;
            //Checking seeds are close enough
            //Whisker must extend to the next seed position or farther
            withinWhisker = withinWhisker || b.start < a.end + KLATsettings.WHISKERS_LENGTH_ALIGNMENT; //b starts within whisker

        }
        //ASSERTED early they match
        // boolean kidMatch = (a.myKid == b.myKid);
        // return noOverlap && withinWhisker && kidMatch;

        return noOverlap && withinWhisker;
    }






    /**
     * merges two seeds, assuming they are allowed
     * @param a
     * @param b
     * @return
     */
    public static Seed merge(Seed a, Seed b) {
        Seed result = new Seed();

        if (a.isReverse != b.isReverse){
            return null;
        }
        result.isReverse = a.isReverse;

        result.hits = a.hits + b.hits; //what about overlap?
        result.myKid = a.myKid;
        result.fastKLATscore = fastKLATscoreFromSeeds(a, b);
        result.queryStart = Math.min(a.queryStart, b.queryStart);
        result.queryEnd = Math.max(a.queryEnd, b.queryEnd);

        result.indel = a.indel || b.indel;
        result.snp = a.snp || b.snp;


        //calculate adjacency
        //two cases: the seeds are adjacent to each other or not
        boolean adjacencyStreak =
                a.isAdjacencyStreak() &&
                b.isAdjacencyStreak() &&
                a.end == b.start &&
                a.queryEnd == b.queryStart;

        if (adjacencyStreak) {
            result.adjacency = a.adjacency + b.adjacency;
        } else {
            result.adjacency = Math.max(a.adjacency, b.adjacency);
        }

        if (a.isReverse) {
            result.start = Math.max(a.start, b.start);
            result.end = Math.min(a.end, b.end);
        } else {
            result.start = Math.min(a.start, b.start);
            result.end = Math.max(a.end, b.end);
        }

        return result;
    }



    private static int fastKLATscoreFromSeeds(Seed a, Seed b) {
        //we know the seeds extend from right edge (end) as K-mer size
        // we need to watch for overlap on REFERENCE side
        //If adjacent, subtracts full KMER_SIZE

        if (a.isReverse != b.isReverse){
            return -1;
        }

        Seed t;
        if (a.queryStart > b.queryStart){
            t=a;
            a=b;
            b=t;
        }

        //distance is the total number of intervening bases allowed to attribute to a's edge
        //max is k-1 intervening bases
        int distance = Math.min( Math.abs(a.end - b.start), b.queryStart - a.queryEnd);
//        if (a.isReverse){
//            distance = Math.min(a.end - b.start, b.queryStart - a.queryEnd);
//        } else {
//            distance = Math.min(b.start - a.end, b.queryStart - a.queryEnd);  //number of intervening bases, a.end is EXCLUSIVE, b.start in INCLUSIVE
//        }

        if (distance > (Kmer31.KMER_SIZE-1)){
            distance = (Kmer31.KMER_SIZE-1);
        }

        //a fastKLATscore must be reduced by reducing the right extension which overlaps

        return a.fastKLATscore + distance - (Kmer31.KMER_SIZE-1) + b.fastKLATscore  ;
    }

    /**
     * Deprecated
     * @param a
     * @param b
     * @return
     */
    private static int overlappingKmersFastKlat(Seed a, Seed b) {
//
//
//
//
//        int astart = a.start - (Kmer31.KMER_SIZE-1);
//        int astop  = a.end + (Kmer31.KMER_SIZE-1);
//
//        int bstart = b.start - (Kmer31.KMER_SIZE-1);
//        int bstop  = b.end + (Kmer31.KMER_SIZE-1);
//
//
////        Seed a1 = new Seed(a);
////        Seed b1 = new Seed(b);
////
////
////        a1.start = a1.start - (Kmer31.KMER_SIZE-1);
////        a1.end  = a1.end + (Kmer31.KMER_SIZE-1);
////
////        b1.start = b1.start - (Kmer31.KMER_SIZE-1);
////        b1.end  = b1.end + (Kmer31.KMER_SIZE-1);
//
//
//        int refOverlap = 0;
//                //modified for overlapping FastKlatScore
//        if (astart < bstop && astart >= bstart){
//            if (astop <= bstop){
//                //case 1: totally overlapping
//                refOverlap = Math.max(astop-astart, refOverlap);
//            } else {
//                //case 2: overlap at edge
//                refOverlap = Math.max(bstop - astart, refOverlap);
//            }
//        } else if (astop <= bstop && astop > bstart){
//
//            if (astop >= bstop){
//                //case 1: totally overlapping
//                refOverlap = Math.max(bstop-bstart, refOverlap);
//            } else {
//                //case 2: refOverlap at edge
//                refOverlap = Math.max(astop - bstart, refOverlap);
//            }
//        }
//
//        return a.fastKLATscore+b.fastKLATscore - refOverlap; //
        return 0;
    }


    public static boolean isAdjacent( Seed a, Seed b){

        if (a.myKid != b.myKid){
            return false;
        }

        //We don't know which seed is "first"
        if (a.isReverse != b.isReverse) {
            return false;
        } else if(a.queryStart > b.queryStart) { //TODO needs to properly handle reverse
            //SWAP
            Seed t = b;
            b = a;
            a = t;
        }

        if(AlignmentKLAT1.DEBUG == true) System.err.println("SEED.isAdjacent\t"+a+"\t"+b+"\t"+(a.queryEnd == b.queryStart  &&   a.end == b.start ));


        return b.queryStart == a.queryEnd
                &&
                a.end == b.start;  //REVERSE AND FORWARD

        //ENDS are EXCLUSIVE
//        return b.queryStart - a.queryEnd == 0
//                &&
//                Math.abs(a.end - b.start) == 0;  //REVERSE AND FORWARD

        //return (  (a.queryEnd+ KLATsettings.LOOKUP_STRIDE-1) == b.queryStart  &&   (a.end+ KLATsettings.LOOKUP_STRIDE-1) == b.start  );
    }


    /**
     * All seeds are considered for pairing with adjacent seeds to roll up into bigger seeds.
     * This goes beyond adjacent, looks to combine allowing for gaps
     * O(n^2), very bad for low complexity
     *
     * 2017-07   need to be gap aware:  don't combine if gap too big
     *
     * @param seeds
     * @return
     */
    public static ArrayList<Seed> combineSeeds(ArrayList<Seed> seeds){
        //Collections.sort(seeds);  //this ruins the prexisting natural order established, increasing time

        //save process time
        if (seeds.size() < 2) return seeds;


        int lastSize = -1;

        //2017.07  changing algorithm from combining seeds with first possibility and allowing multiple combinations

        while(lastSize != seeds.size()) {
            //Collections.sort(seeds);
            lastSize = seeds.size();
            ArrayList<Integer> indexesToRemoveBecauseCombined = new ArrayList<>();

            Seed temp;
            for (int k = 0; k < seeds.size() - 1; k++) {
                //if a seed has already been combined previously, ignore in this loop
                if (!indexesToRemoveBecauseCombined.contains(k)) {

                    int looper = seeds.size();  //do not need to look to combining seed a with seed (a+b) generated in the loop
                    for (int j = k + 1; j < looper; j++) {


                        temp = mergeIfAble(seeds.get(k), seeds.get(j));

                        //hits = -1 for not combined, must be at least one hit to be real
                        if (temp.hits > 0) {
                            indexesToRemoveBecauseCombined.add(j);
                            indexesToRemoveBecauseCombined.add(k);
                            seeds.add(temp);
                            if (AlignmentKLAT1.DEBUG == true)
                                System.err.println("MERGE TRUE  \t" + seeds.get(k) + "\t" + seeds.get(j));

                        } else {
                            if (AlignmentKLAT1.DEBUG == true)
                                System.err.println("MERGE FALSE \t" + seeds.get(k) + "\t" + seeds.get(j));
                        }
                    }
                }
                seeds = seeds; //debug
            } //end for

            //remove seeds in reverse, so indexes do not need to be adjusted by previous removals
            Collections.sort(indexesToRemoveBecauseCombined, Collections.<Integer>reverseOrder());
            int prev = -1, curr;
            for (int k = 0; k < indexesToRemoveBecauseCombined.size(); k++) {
                curr = indexesToRemoveBecauseCombined.get(k);
                //ignore indexes already removed
                if (curr != prev) {
                    seeds.remove(curr);
                }
                prev = curr;
            }

            //reset loop
            //seeds just gets deleted as it goes forward
            "BREAK POINT".equals("DEBUG");
        }

        return seeds;
    }


    /**
     *  Just looks if seeds next to one another in the list should be combined.
     *  It is highly likely a merge is needed with neighbor, so why do O(n^2), when first pass can be O(n)
     * @param seeds
     * @return
     */
    public static ArrayList<Seed> combineConsecutiveSeeds(ArrayList<Seed> seeds) {

        //save process time
        if (seeds.size() < 2) return seeds;


        ArrayList<Integer> indexesToRemoveBecauseCombined = new ArrayList<>();

        Seed temp;
        //int looper = seeds.size();  //do not need to look to combining seed a with seed (a+b) generated in the loop
        for (int k=0; k<seeds.size()-1; k++){
            //for (int j=k+1; j < looper; j++) {
            for (int j=k+1; j < seeds.size(); j++) {  //size is now decreasing

                //determine adjacency
                boolean areAdjacent = false;
                //forward
                Seed a = seeds.get(k);
                Seed b = seeds.get(j);

                //reverse
                //TODO

                temp = mergeIfAble(a, b);
                if (temp.hits > 0) {
                        if (AlignmentKLAT1.DEBUG == true)
                            System.err.println("MERGE TRUE  \t" + seeds.get(k) + "\t" + seeds.get(j));

                    //remove previous seeds, it is not needed for future roll-ups
                    //it has found a perfect adjacent match
                    seeds.set(k, temp);  //replacement method
                    seeds.remove(j);
                    k--;  //break kicks us up to for k loop, to start comparing new seed
                    break;
                } else if(Seed.seedsOverlap(a,b)){

                    //seeds.set(k,a); //a is already at k
                    if (b.hits > a.hits) {
                        indexesToRemoveBecauseCombined.add(k);
                    }  //else k is already a
                    else {
                        indexesToRemoveBecauseCombined.add(j);
                    }

                }
            } //end for j
            seeds=seeds; //debug
        } //end for i


        return seeds;

    }





    /**
     * Only adjacency streaks can be combined with adjacency streaks and increase
     * adjacency total
     *
     * @return
     */
    public boolean isAdjacencyStreak(){
        return (queryEnd - queryStart) == adjacency && Math.abs(start - end) == adjacency;
    }

    public ReferenceSequenceRequest toRefSeqRequest(KidDatabaseMemory myKidDB, int queryLength) {
        return new ReferenceSequenceRequest(this, myKidDB, queryLength);
    }


    @Override
    public String toString() {
        if (isReverse)
            return "{QUERY from "+Integer.toString(queryStart)
                    +" to "+Integer.toString(queryEnd)
                    +", REF from "+Integer.toString(start)
                    +" to "+Integer.toString(end)
                    + " REV"
                    +", HITS "+ hits +" ADJ "+adjacency+" kid "+myKid
                    +" SNP/INDEL "+snp+"/"+indel+"}";
        else
            return "{QUERY from "+Integer.toString(queryStart)
                    +" to "+Integer.toString(queryEnd)
                    +", REF from "+Integer.toString(start)
                    +" to "+Integer.toString(end)
                    + " FWD"
                    +", HITS "+ hits +" ADJ "+adjacency+" kid "+myKid
                    +" SNP/INDEL "+snp+"/"+indel+"}";
    }

    @Override
    public boolean equals(Object o){
        Seed other = (Seed) o;

        boolean equals = true;

        //short circuit to skip tests
        if (equals && isReverse != other.isReverse) { equals = false; }
        if (equals && adjacency != other.adjacency) { equals = false; }
        if (equals && hits != other.hits) { equals = false; }
        if (equals && fastKLATscore != other.fastKLATscore) { equals = false; }

        if (equals && queryStart != other.queryStart) { equals = false; }
        if (equals && queryEnd != other.queryEnd) { equals = false; }
        if (equals && start != other.start) { equals = false; }
        if (equals && end != other.end) { equals = false; }
        if (equals && snp != other.snp) { equals = false; }
        if (equals && indel != other.indel) { equals = false; }

        return equals;
    }

    @Override
    //TODO is this incomplete?
    public int compareTo(Seed seed) {
        int result = 0;

        if (this.myKid != seed.myKid){
            result = myKid - seed.myKid;
        } else if (queryStart == seed.queryStart){
            if (queryEnd == seed.queryEnd){
                if (start == seed.start) {
                    result = end - seed.end;
                } else {
                    result = start - seed.start;
                }
            } else {
                result = queryEnd - seed.queryEnd;
            }
        } else {
            return queryStart - seed.queryStart;
        }
        return result;
    }

    @Override
    public int compare(Seed seed, Seed t1) {
        return seed.compareTo(t1);
    }


    /**
     * eliminate duplicate alignments to a query range,
     * picking best score
     *
     * @param seeds
     * @return
     */
    public static ArrayList<Seed> eliminateDuplicates(ArrayList<Seed> seeds) {

        //save process time
        if (seeds.size() < 2) return seeds;

        ArrayList<Seed> result = new ArrayList<Seed>();
//        Collections.sort(seeds);

        Seed a;
        Seed max = new Seed(Seed.DEFAULT);

        //look for parts that match in query coordinates or reference coordinates
        Seed queryMax = null;

        int lastSize = -1;

        //eliminate seeds until loop no longer needed
        while (lastSize != seeds.size()) {
            lastSize = seeds.size();
            Collections.sort(seeds);

            for (int k = 0; k < seeds.size(); k++) {
                Seed query = seeds.get(k);
//                Seed x = mergeIfAble(query, queryMax);
                if (queryMax == null) {
                    //first time through loop
                    queryMax = query;

                    //if query or reference sequences match, use only one.
//                } else if (x.myKid != -1) {  //Sentinel
//                    //merged seed is valid
//                    result.addAndTrim(x);
                } else if (seedsOverlap(query, queryMax)
                        ) {
                    //matching position
                    if (query.hits == queryMax.hits && calculateFastKlatScoreAdjacentSeed(query) > calculateFastKlatScoreAdjacentSeed(queryMax)) {
                        //whichever has greater coverage wins
                        queryMax = query;
                    } else if (query.hits > queryMax.hits) {
                        queryMax = query;
                    }
                }else {
                    //moved on to new position
                    //write previous maximum
                    result.add(queryMax);

//                if (queryMax.hits >= KLATsettings.MIN_SEED_HITS && queryMax.adjacency >= KLATsettings.MIN_SEED_ADJACENCY) {
//                    result.addAndTrim(queryMax);
//                }
                    queryMax = query;
                    }
            } //end for
            //addAndTrim last
            if (queryMax != null) result.add(queryMax);
            queryMax = null;
            seeds = result;
            result = new ArrayList<>();
        } //end while


        result = seeds;
        return result;
    }



    private static boolean seedsOverlap(Seed query, Seed queryMax) {

        boolean result = false;
        //CASE 1:
        // Seed one is insided seed two
        //CASE 2:
        // Seed two is inside seed one
        //CASE 3:
        // Seeds are exact match   if we use INCLUSIVE range, this is a repeat

        if (query.myKid != queryMax.myKid){
            return false;
        }


        //CASE 1 : Seed one is inside seed two
        if (queryMax.queryStart >= query.queryStart &&
                queryMax.queryEnd <= query.queryEnd &&
                queryMax.start >= query.start &&
                        queryMax.end <= query.end){
            result = true;

        //CASE 2 : Seed two is inside seed one
        } else if (!result &&  //short circuit if already true
                queryMax.queryStart <= query.queryStart &&
                queryMax.queryEnd >= query.queryEnd &&
                queryMax.start <= query.start &&
                queryMax.end >= query.end) {
            result = true;
        }
        return result;
    }


    public static boolean samePositions(Seed a, Seed b){

        return  (a.queryStart == b.queryStart)  &&
                (a.queryEnd == b.queryEnd) &&
                (a.end == b.end) &&
                (a.start == b.start);
    }

    public static ArrayList<Seed> eliminateBelowFastKlatScore(ArrayList<Seed> seeds) {

        //        ArrayList<Seed> result = new ArrayList<Seed>();
        for(int k=0; k<seeds.size(); k++){
            if (Seed.calculateFastKlatScoreAdjacentSeed(seeds.get(k)) < KLATsettings.MIN_FAST_KLAT_SCORE){
                seeds.remove(k);
                k--;
            }
        }

        return seeds;
    }

    public static Double calculateFastKlatScoreAdjacentSeed(Seed seed) {
        //return (seed.hits+seed.adjacency) /2;



        //second gen formula
//        return new Double(seed.queryEnd-seed.queryStart+seed.end-seed.start)/2;

        //third generation formula

        if (seed.isAdjacencyStreak()) {
            return new Double(seed.hits + Kmer31.KMER_SIZE - 1);
        } else {
            return new Double(seed.fastKLATscore);
        }

    }

}
