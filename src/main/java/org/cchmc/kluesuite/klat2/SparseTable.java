package org.cchmc.kluesuite.klat2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by jwc on 10/5/17.
 *
 * This class stores the Box2 table elements and gives helper functions
 *
 */
public class SparseTable {

//    private class Pair{
//        final int r, c;
//        Pair (int x, int y){
//            r=x;
//            c=y;
//        }
//    }

    ArrayList<Box2> el;

    /**
     * the lastIdx in the Array List
     */
    public int lastIdx = -1;

    boolean calculated;


    //all letters corresponding to rows, note the index shift because first row and column in alignment table are not part of these strings
    public String rows;
    public String columns;

//    /**
//     * Contains the row indexes that must be calculated
//     */
//    HashSet<Integer> rows_to_calculate;

    SparseTable(int size, String rowz, String colz){
        el = new ArrayList<Box2>(size);
        calculated = false;
//        rows_to_calculate = new HashSet<Integer>();
        rows = rowz;
        columns = colz;
    }

    public void add(Box2 e){
        el.add(e);
        lastIdx++;

        if (e.type == BoxType.CALCULATED ){
            for (int r=e.srow; r<=e.erow; r++){

            }
        }
    }



    public void calculateAlignment(){

        if (!calculated){
            Iterator<Box2> it = iterator();
            Box2 b = it.next();
            Box2 prev;
            b.calculateScores(0,0);
            while (it.hasNext()){
                prev = b;
                b = it.next();
                b.calculateScores(prev.cumulativeSmithWatermanScore,prev.cumulativeActualFastKlatScore);
            }
            calculated = true;
        }
    }

    public Box2 get(int idx){
        return el.get(idx);
    }

    public Box2 getLast() {
        if (lastIdx <0){
            return null;
        } else {
            return el.get(lastIdx);
        }
    }


    public int getFastKlatScore() {
        if (!isCalculated()){
            calculateAlignment();
        }
        return  el.get(lastIdx).cumulativeActualFastKlatScore;
    }

    public int getSmithWatermanScore() {
        if (!isCalculated()){
            calculateAlignment();
        }
        return  el.get(lastIdx).cumulativeSmithWatermanScore;
    }

    public boolean isCalculated(){
        return calculated;
    }

    public Iterator<Box2> iterator(){
        return el.iterator();
    }

    public int getCumulativeMaximumFastKlat() {
//        if (!calculated){ calculateAlignment(); }
        return  el.get(lastIdx).cumulativeMaximumFastKlatScore;
    }

    public int getCumulativeMinimumFastKlat(){
//        if (!calculated){ calculateAlignment(); }
        return el.get(lastIdx).cumulativeMinimumFastKlatScore;
    }

    public int getCumulativeMaximumSW() {
//        if (!calculated){ calculateAlignment(); }
        return  el.get(lastIdx).cumulativeMaximumSWscore;
    }

    public int getCumulativeMinimumSW() {
//        if (!calculated){ calculateAlignment(); }
        return  el.get(lastIdx).cumulativeMinimumSWscore;
    }

}
