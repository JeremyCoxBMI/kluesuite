package org.cchmc.kluesuite.memoryklue;

import org.cchmc.kluesuite.klue.PositionList;

import java.util.ArrayList;

/**
 * Created by jwc on 7/10/17.
 *
 * Simple struct for passing data
 * DEPRECATED
 *
 */
public class DatabaseEntry {

    long key;
    ArrayList<Long> pl;

    public DatabaseEntry(long key){
        this.key = key;
        pl = new ArrayList<>();
    }

    public void add(long valuePosition){
        pl.add(valuePosition);
    }

    public long getKey(){
        return key;
    }

    public ArrayList<Long> getValue(){
        return pl;
    }

}
