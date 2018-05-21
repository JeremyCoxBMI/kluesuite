package org.cchmc.kluesuite.klue;

import java.util.ArrayList;

/**
 * Created by joe on 4/17/17.
 */
public interface AsyncKLUE {
    void put(long key, ArrayList<Long> positions, KlueCallback<Void> callback);
    void append(long key, long pos, KlueCallback<Void> cb);
    void get(long key, KlueCallback<ArrayList<Long>> cb);
    void getAll(long[] keys, KlueCallback<ArrayList<ArrayList<Long>>> cb);
    void getAllPL(long[] keys, KlueCallback<ArrayList<PositionList>> cb);
    void getShortKmerMatches(long shorty, int prefixLength, KlueCallback<PositionList> cb);

    void shutdown(KlueCallback<Void> cb);
}
