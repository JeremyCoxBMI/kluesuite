package org.cchmc.kluesuite.rocksDBklue;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.PositionList;

import java.util.ArrayList;

/**
 * Created by jwc on 7/8/17.
 *
 * This allows opening multiple databases as one.  Granted, it's sloppy and poor performance, but easily lets you combine
 * for testing purposes.
 *
 * Implements only read functions
 */
public class MultipleDatabaseReadOnlyRocksKlue implements KLUE {

    String[] files;
    RocksDbKlue[] dbs;


    public MultipleDatabaseReadOnlyRocksKlue(String[] filenames){
        dbs = new RocksDbKlue[filenames.length];
        files = filenames;
        for (int k=0; k< filenames.length; k++){
            dbs[k] = new RocksDbKlue(filenames[k], false);
        }

    }


    @Override
    public void put(long key, ArrayList<Long> positions) {
        System.err.println("WARNING:\tMultipleDatabaseReadOnlyRocksKlue::put does nothing.");
    }

    @Override
    public void append(long key, long pos) {
        System.err.println("WARNING:\tMultipleDatabaseReadOnlyRocksKlue::append does nothing.");
    }

    @Override
    public ArrayList<Long> get(long key) {
        ArrayList<Long> result = new ArrayList<>();
        ArrayList<Long> temp;

        for (RocksDbKlue rk : dbs){
            temp = rk.get(key);
            for (Long l : temp){
                result.add(l);
            }
        }

        return result;
    }

    @Override
    public ArrayList<ArrayList<Long>> getAll(long[] keys) {

        ArrayList<ArrayList<Long>> result = new ArrayList<>();


        for (long key : keys) {
            for (RocksDbKlue rk : dbs) {
                result.add(rk.get(key));
            }
        }

        return result;
    }

    @Override
    public ArrayList<PositionList> getAllPL(long[] keys) {
        ArrayList<PositionList> result = new ArrayList<>();


        for (long key : keys) {
            for (RocksDbKlue rk : dbs) {
                result.add(new PositionList(rk.get(key)));
            }
        }

        return result;
    }

    @Override
    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        System.err.println("WARNING:\tMultipleDatabaseReadOnlyRocksKlue::getShortKmerMatches does nothing.");
        return null;
    }

    @Override
    public void shutDown() {
        for(RocksDbKlue rk : dbs){
            rk.shutDown();
        }
    }
}
