package org.cchmc.kluesuite.multithread;

import org.cchmc.kluesuite.klue.KidDatabaseMemory;

/**
 * Created by jwc on 8/24/17.
 *
 * This is difficult because we have to modify other methods
 */
public class KidDatabaseThreadSafe {

    public static boolean ON = false;

    public static KidDatabaseMemory kd;

    public static synchronized String getSequence(int myKID, int start, int stop, boolean reverse) throws Exception {
        return kd.getSequence(myKID, start, stop, reverse);
    }

    public static synchronized int getSequenceLength(int kid) {
        return kd.getSequenceLength(kid);
    }

    public static synchronized String getName(int myKID) {
        return kd.getName(myKID);
    }
}
