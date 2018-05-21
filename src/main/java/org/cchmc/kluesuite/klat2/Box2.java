package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;

import java.util.Comparator;

/**
 * Helper class to SparseTable; these are struct data elements
 */
public class Box2 implements Comparable<Box2>, Comparator<Box2> {

    public BoxType type;

    /**
     * starting row of diagonal, INCLUSIVE
     */
    public int srow;

    /**
     * starting column of diagonal, INCLUSIVE
     */
    public int scol;

    /**
     * ending row of diagonal, *INCLUSIVE*
     */
    public int erow;

    /**
     * ending row of diagonal, *INCLUSIVE*
     */
    public int ecol;

//    /**
//     * upon construction, determines minimum possible FastKlatScore for the region
//     */
//    public int minimumFastKlatScore;

    /**
     * cumulative minimum possible FastKlatScore for the region
     */
    public int cumulativeMinimumFastKlatScore;


    /**
     * cumulative maximum possible FastKlatScore for the region
     */
    public int cumulativeMaximumFastKlatScore;

    /**
     * cumulative minimum possible FastKlatScore for the region
     */
    public int cumulativeMinimumSWscore;


    /**
     * cumulative maximum possible FastKlatScore for the region
     */
    public int cumulativeMaximumSWscore;



    /**
     * calculated fast klat score
     * value of -1 is uninitialized
     */
    public int cumulativeActualFastKlatScore;

    /**
     * calculated fast klat score
     * value of -1 is uninitialized
     */
    public int cumulativeSmithWatermanScore;


    public Box2(int srow, int scol, int erow, int ecol ) {
        this.scol = scol;
        this.srow = srow;
        this.erow = erow;
        this.ecol = ecol;

        cumulativeActualFastKlatScore = -1;
        cumulativeSmithWatermanScore = -1;
        type = BoxType.NONE;
    }

    protected Box2(){}

    public Box2(Seed s){
        this.srow = s.queryStart;
        this.erow = s.queryEnd;
        this.scol = s.start;
        this.ecol = s.end;


        cumulativeActualFastKlatScore = -1;
        cumulativeSmithWatermanScore = -1;
        type = BoxType.NONE;

    }

    public boolean isCalculated(){
        return cumulativeActualFastKlatScore != -1;
    }

    /**
     * MUST OVERRRIDE IN CHILDREN
     * @param prevSWscore
     * @param prevFastKlatScore
     */
    public void calculateScores(int prevSWscore, int prevFastKlatScore){
        cumulativeActualFastKlatScore = prevFastKlatScore;
        cumulativeSmithWatermanScore = prevSWscore;
    }

    @Override
    public int compareTo(Box2 o) {
        return srow - o.srow;
    }

    @Override
    public int compare(Box2 o1, Box2 o2) {
        return o1.srow - o2.srow;
    }
}
