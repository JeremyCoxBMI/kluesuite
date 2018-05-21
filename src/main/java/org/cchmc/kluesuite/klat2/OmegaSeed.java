package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jwc on 9/13/17.
 *
 * OmegaSeed is helper class to SmithWaterManTruncated
 * This takes a SuperSeed, calculates known_diagonals, rectangles, and maximum Fast KLAT score
 *
 * DEPRECATED
 */

//public class OmegaSeed {
//
//    /**
//     *  List of all diagonal adjacent seed streaks known to match by the seeds
//     */
//    ArrayList<Diagonal> knownDiagonals;
//
//    /**
//     * represent the range of the unaligned regions as rectangles from one position to another
//     * note if this touches end of sequences (0 or max cooridinate), it has more max_score
//     * THE ends of the adjacent seeds are INCLUDED in the rectangle coordinates, as this rectangle
//     * will be  filled in completely in the table
//     */
//    ArrayList<Diagonal> rectangles;
//
//
//    /**
//     * In SuperSeed, all Seeds are forced to have adjacency property
//     */
//    public SuperSeed ss;
//
//
//    public OmegaSeed(SuperSeed superSeed){
//        ss=superSeed;
//        initializeDiagonalRectangle();
//    }
//
//    private void initializeDiagonalRectangle() {
//
//        //Issue #76   TODO: is conversion to row/column coordinates accurate?
//
//        maxPossibleFastKlatScore = 0;
//        Diagonal d;
//
//        rectangles = new ArrayList<>(superSeed.numSeeds() + 1);
//        knownDiagonals = new ArrayList<>(superSeed.numSeeds());
//        seedFirst = true;  //will be proven false and set
//
//        int refStop;  //INCLUSIVE
//        int qStop;
//        if (queryISrows){
//            refStop = refStart + columns.length();
//            qStop = rows.length();
//        } else {
//            refStop = refStart + rows.length();
//            qStop = columns.length();
//        }
//
//        Iterator<Seed> it = superSeed.iterator();
//        Seed curr = null;
//
//
//        //PROCESS FIRST SEED
//        if (it.hasNext()){
//            curr = it.next();
//            knownDiagonals.add(seedToDiagonal(curr));
//            maxPossibleFastKlatScore += curr.hits;  //maxPossibleGapFastKlateScore(d, true);
//        }
//
//        //PROCESS CAP SEQUENCE BEFORE FIRST SEED, if it exists
//        if (!(curr.start == refStart || curr.queryStart == 0)) {
//            seedFirst = false;
//            d = leftCap(curr);
//            rectangles.add(d);
//            maxPossibleFastKlatScore += maxPossibleGapFastKlatScore(d, true);
//        }
//
//        //PROCESS SEEDS AND GAPS
//        Seed prev = curr;
//        while (it.hasNext()){
//            curr = it.next();
//
//            //write between gaps
//            d = processGapToDiagonal(curr, prev);
//            rectangles.add(d);
//            maxPossibleFastKlatScore += maxPossibleGapFastKlatScore(d, false);
//
//            //write new diagonal
//            knownDiagonals.add(seedToDiagonal(curr));
//            maxPossibleFastKlatScore += curr.hits;  //maxPossibleGapFastKlateScore(d, true);
//        }
//
//        //PROCESS LAST GAP, IFF EXISTS
//        //if last seed has gap afterwards
//
//        if (!(curr.end  == refStop || curr.queryEnd == qStop)) {
//            d = rightCap(curr);
//            rectangles.add(d);
//            maxPossibleFastKlatScore += maxPossibleGapFastKlatScore(d, true);
//        }//end if
//
//    }
//
//}
