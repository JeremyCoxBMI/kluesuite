package org.cchmc.kluesuite.socketklue;

import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.socketklue.client.KlueResponseActor;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public interface SocketKLUE {

    void put(long key, ArrayList<Long> positions, KlueResponseActor<Void> response);
    void append(long key, long pos, KlueResponseActor<Void> response);
    void get(long key, KlueResponseActor<ArrayList<Long>> response);
    void getAll(long[] keys, KlueResponseActor<ArrayList<ArrayList<Long>>> response);
    void getAllPL(long[] keys, KlueResponseActor<ArrayList<PositionList>> response);
    void getShortKmerMatches(long shorty, int prefixLength, KlueResponseActor<PositionList> response);

    void shutDown();

}
