package org.cchmc.kluesuite.klue;

import java.util.ArrayList;

/**
 * Created by joe on 4/18/17.
 */
public class InlineAsyncKLUE implements AsyncKLUE {

    private final KLUE klue;

    public InlineAsyncKLUE(KLUE klue) {
        this.klue = klue;
    }

    @Override
    public void put(long key, ArrayList<Long> positions, KlueCallback<Void> callback) {
        try {
            klue.put(key, positions);
            callback.callback(null);
        } catch (Exception e) {
            callback.exception(e);
        }
    }

    @Override
    public void append(long key, long pos, KlueCallback<Void> cb) {
        try {
            klue.append(key, pos);
            cb.callback(null);
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void get(long key, KlueCallback<ArrayList<Long>> cb) {
        try {
            cb.callback(klue.get(key));
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void getAll(long[] keys, KlueCallback<ArrayList<ArrayList<Long>>> cb) {
        try {
            cb.callback(klue.getAll(keys));
        } catch (Exception e) {
            cb.exception(e);
        }

    }

    @Override
    public void getAllPL(long[] keys, KlueCallback<ArrayList<PositionList>> cb) {
        try {
            cb.callback(klue.getAllPL(keys));
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void getShortKmerMatches(long shorty, int prefixLength, KlueCallback<PositionList> cb) {
        try {
            cb.callback(klue.getShortKmerMatches(shorty, prefixLength));
        } catch (Exception e) {
            cb.exception(e);
        }
    }

    @Override
    public void shutdown(KlueCallback<Void> cb) {
        try {
            klue.shutDown();
            cb.callback(null);
        } catch (Exception e) {
            cb.exception(e);
        }
    }
}
