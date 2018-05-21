package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.masterklue.KLATsettings;

import java.util.ArrayList;

/**
 * Created by jwc on 8/30/17.
 */
public class AlignmentKLAT2 {
    private int KMER_SIZE;


    //need to check for SuperSeeds addressing the same region, align them together

    //Needs to calculate true alignment scores and filter subsequence max possible scores against it

    //Needs to report S-W score, fastklat score, and % query identity (Is this fast klat score?)

    //Fast KLAT:  just report fast klat scores from the seeds?  need separate function


//    /**
//     *  This takes a table of Seeds corresponding columns to
//     *  Rows are sorted by KID (subtables)
//     *      then position least to greatest within subtable
//     *
//     *
//     *  TODO FIX -- missing functions;  use SparseTable?
//     * @param table
//     * @param r
//     * @param numCols
//     * @param reverse
//     * @param myKid
//     * @return
//     */
//    protected ArrayList<Seed> combineAdjacentSeeds(Position[][] table, int r, int numCols, boolean reverse, int myKid) {
//        //minimums ==> combine based on minimums?
//
//        //Initialize adjacent seeds
//        Seed streakerStart = new Seed(), temp = new Seed(), prev = new Seed();
//        int streakStart = -1;
//        Position pos;
//        ArrayList<Seed> tempSeeds = new ArrayList<Seed>();
//
//
//        //streakStart = -1 means no streak currently
//        for (
//                int c = 0;
//                c < numCols; c++)
//
//        {
//
//            //If there is a position to consider as a seed
//            if (table[r][c] != null) {
//                pos = table[r][c];
//
//                // If first streak defined / first hit in table
//                if (streakStart == -1) {
//                    streakStart = c;
//                    streakerStart = new Seed(c, c + 1, pos, myKid);
//
//                    prev = new Seed(c, c + 1, pos, myKid);
//                    if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//                } else {
//
//                    temp = new Seed(c, c + 1, pos, myKid);
//
//
////                    //First seed, start streak
////                    if (prev == null){
////                        streakStart = c; //new streak
////                        streakerStart = new Seed(c, c + 1, pos, myKid);
////                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
////                    }
////
////                    else
//                    //If Seed is not adjacent, addAndTrim last streak to list, otherwise streak continues
//                    if (!Seed.isAdjacent(prev, temp)){
//
//
//                        // create seed ending the streak
//                        //we cannot filter at this point.  2-mer seeds will not meet default criteria
//
//                        //no streak if prev is empty
//
//                        //MIN_SEED_ADJACENCY and //MIN_SEED_HITS enforced
//                        addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);
////                        tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
//
//                        streakStart = c; //new streak
//                        streakerStart = new Seed(c, c + 1, pos, myKid);
//                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//
//                    }
//                    prev = temp;
//
//                }
//            } else {    // else entry in table is blank
//
//                //create seed ending the streak
//                if (streakStart != -1) {
//                    tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
//                    prev = null;
//                }
//                streakStart = -1; // no streak; current alignment is null
//                //2017-07-26
//                //prev = null;
//            }
//        }
//        //record final streak
//        prev = temp; //bug -- ommitted line -- fixed 08-03
//        int l = (prev.queryStart - streakerStart.queryStart) + 1;
//        if (streakStart != -1) {
////            if(streakStart==20) {
////                int debugIT = 1;
////            }
//            if (streakerStart.equals(prev) || prev.myKid == 0){
//                tempSeeds.add(streakerStart);
//            } else {
//                tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);
//            }
//        }
//
//        //tempseeds now contains agglomerated seeds by row
//
////        //try to combine adjacent seeds across rows
////        for (int k=0; k < tempSeeds.size()-1; k++){
////            for (int j =k+1; j < tempSeeds.size();j++){
////                if (Seed.isAdjacent(tempSeeds.get(j), tempSeeds.get(k))){
////
////
////                    tempSeeds.set(k,Seed.mergeIfAble(tempSeeds.get(j),tempSeeds.get(k)));
////
////                    //Do we want to delete j?  It was combined with k
////                    //Yes -- we do not need seeds that are "subseeds" of other seeds
////                    tempSeeds.remove(j);
////                    j--;  //we removed it, so to not skip, subtract 1
////                }
////            }
////        }
//
//
//
//        return tempSeeds;
//    }




    /**
     * All seeds are "long seeds" meaning they have already been combined by adjacency.
     * @param inSeeds
     * @return
     */
    public static ArrayList<Seed> agglomerateSeeds(ArrayList<Seed> inSeeds, int kmerSize){
        ArrayList<Seed> results = new ArrayList<>();

        int sz=0;
        ArrayList<Seed> temp1;
        ArrayList<Seed> tempSeeds = new ArrayList<>();

        for (Seed s : inSeeds){
            tempSeeds.add(new Seed(s));
        }


        //complete: AGGLOMERATE ADJACENT SEEDS



        // #we know distance >1, but do not need to force this, it is already true
        // this process destructively removes them

        //EXPERIMENTAL PARAMATER  TODO
        KLATsettings.EXCLUSIVE_SEED_AGGLOMERATION_DISTANCE = 10;
        while (tempSeeds.size() != sz) {
            sz = tempSeeds.size();

            //contains list of new seeds

            for (int k = 0; k < sz - 1; k++) {
                for (int m = k + 1; m < sz; m++) {
                    if (Seed.mayMergeConsecutiveSeeds(inSeeds.get(k), inSeeds.get(m), 0, KLATsettings.EXCLUSIVE_SEED_AGGLOMERATION_DISTANCE)) //adjacency allowed
                    {
                        tempSeeds.set(k,Seed.merge(inSeeds.get(k), inSeeds.get(m)));
                        tempSeeds.remove(m);
                        //continue on from m; do not increment
                        m--;
                    }
                }
            }
        }


        sz = 0;  //else cannot enter loop

        //combine seeds with greater distance, combinatorically
        while (tempSeeds.size() != sz) {
            sz = tempSeeds.size();

            //contains list of new seeds
            temp1 = new ArrayList<>();

            for (int k = 0; k < sz - 1; k++) {
                for (int m = k + 1; m < sz; k++) {
                    if (Seed.mayMergeConsecutiveSeeds(tempSeeds.get(k), tempSeeds.get(m), 0, KLATsettings.WHISKERS_LENGTH_ALIGNMENT)) {
                        temp1.add(Seed.merge(tempSeeds.get(k), tempSeeds.get(m)));
                    }
                }
            }
            tempSeeds = temp1;
        }


        return results;
    }
}
