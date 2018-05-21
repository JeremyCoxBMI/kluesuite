package org.cchmc.kluesuite.klat2;

/**
 * This class represents the first and second of paired sequences.
 */

public class SparseTablePaired{

    SparseTable f1;
    SparseTable s2;



    /**
     * number of gaps between paired reads distance is gapLength + 1
     */
    int gapLength;

    boolean overlap;

    boolean calculated;

    public SparseTablePaired(SparseTable first, SparseTable second){
        f1=first;
        s2=second;
        gapLength = 0;
        overlap = false;
        calculated = false;
    }


    public void calculateAlignment() {

        f1.calculateAlignment();
        s2.calculateAlignment();

        //check that reference positions overlap
        int secondStart, firstEnd;
        //need to be absolute reference sequences

        // ********************************
        //TODO
        secondStart = 0;
        firstEnd = 0;
        // ********************************

        gapLength = secondStart - firstEnd - 1;
        overlap = gapLength < 0;
        calculated = true;
    }

    public int getFastKlatScore() {
        if (!calculated){
            f1.calculateAlignment();
            s2.calculateAlignment();
        }
        return  f1.getLast().cumulativeActualFastKlatScore + s2.getLast().cumulativeActualFastKlatScore;
    }

    public boolean isCalculated(){
        return f1.isCalculated() && s2.isCalculated();
    }

    public int getSmithWatermanScore() {
        if (!isCalculated()){
            f1.calculateAlignment();
            s2.calculateAlignment();
        }
        return  f1.getLast().cumulativeSmithWatermanScore + s2.getLast().cumulativeSmithWatermanScore;
    }


    SparseTable getFirst(){
        return f1;
    }

    SparseTable getSecond(){
        return s2;
    }

    public int getCumulativeMaximumFastKlat() {
//        if (!calculated){ calculateAlignment(); }
        return  f1.getLast().cumulativeMaximumFastKlatScore + s2.getLast().cumulativeMaximumFastKlatScore;
    }


    public int getCumulativeMinimumFastKlat(){
//        if (!calculated){ calculateAlignment(); }
        return f1.getLast().cumulativeMinimumFastKlatScore + s2.getLast().cumulativeMinimumFastKlatScore;
    }


    public int getCumulativeMaximumSW() {
//        if (!calculated){ calculateAlignment(); }
        return  f1.getLast().cumulativeMaximumSWscore + s2.getLast().cumulativeMaximumSWscore;
    }


    public int getCumulativeMinimumSW() {
//        if (!calculated){ calculateAlignment(); }
        return  f1.getLast().cumulativeMinimumSWscore + s2.getLast().cumulativeMinimumSWscore;
    }

}
