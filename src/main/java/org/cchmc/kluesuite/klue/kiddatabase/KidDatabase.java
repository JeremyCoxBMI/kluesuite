package org.cchmc.kluesuite.klue.kiddatabase;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;

import java.util.HashMap;

/**
 * Created by COX1KB on 4/25/2018.
 */
public interface KidDatabase {

    //manner of constructing database is irrelevant -- only the data access matters
    public Integer getLength(int kid);
    public Integer getKid(String name);
    public Integer getLength(String name);
    public String getSequenceName(int kid);
    public HashMap<Integer,Character> getExceptions(int kid);
    public DnaBitString getSequence(int myKID);
    public String getSequence(int myKID, int from, int to, boolean reverse) throws Exception;

    /**
     * any shut down behavior required
     */
    public void shutDown();

    void add(Kid bob);

    void storeSequence(int i, DnaBitString dbs);

}
