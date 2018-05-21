package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.cchmc.kluesuite.multithread.KidDatabaseThreadSafe;

/**
 * Created by osboxes on 15/08/16.
 *
 * Struct for passing around combined seed parameters in order to get the sequence for alignment
 */
public class ReferenceSequenceRequest {


    public int myKID;
    public boolean reverse;

    /** If in reverse, start will be higher than end
     *  Where reference sequence request begins (INCLUSIVE)
     */
    public int start;

    /**
     *  Where reference sequence request ends (EXCLUSIVE)
     */
    public int stop;

    public KidDatabaseMemory myKidDB;


    /**
     *
     * @param kid
     * @param reverse
     * @param start
     * @param stop
     * @param myKidDB
     * @param queryStart
     * @param queryStop
     */
    public ReferenceSequenceRequest(int kid, boolean reverse, int start, int stop, KidDatabaseMemory myKidDB, int queryStart, int queryStop, int queryLength){
        //this(s.myKid, s.isReverse, s.start, s.end, myKidDB, s.queryEnd - s.queryStart);

        myKID = kid;
        this.reverse = reverse;

        int length;

        //
        if (KidDatabaseThreadSafe.ON == true){
            length = KidDatabaseThreadSafe.getSequenceLength(kid);
        } else {
            length = myKidDB.getSequenceLength(kid);
        }

        //Just copying the reference
        this.myKidDB = myKidDB;


        //whiskers must extend past query edge
        int right = queryLength - queryStop + 1;  //INCLUSIVE minus EXCLUSIVE
        int left = queryStart;


        /*
                 TODO: we want to reduce whiskers if we know we are very close to a perfect match, to reduce alignment time
                 This seems like this can be done when the aligner is rewritten rather than a quick fix here
        */

        int diff = 0;

        //correct length so queryLength comes out of whiskers
//        int diff = queryLength; // - (end - start);
//        if (diff > 0) diff /= 2;
//        else diff = 0;


        // for reverse, flip start and end
        if (this.reverse){
            this.start = Math.max( stop - right - KLATsettings.WHISKERS_LENGTH_ALIGNMENT, 0 );
            this.stop = Math.min( start + left + KLATsettings.WHISKERS_LENGTH_ALIGNMENT, length);
        } else {
            this.start = Math.max( start - left - KLATsettings.WHISKERS_LENGTH_ALIGNMENT, 0 );
            this.stop = Math.min( stop + right + KLATsettings.WHISKERS_LENGTH_ALIGNMENT, length);
        }
        length = length;  //debug
    }

    public ReferenceSequenceRequest(Seed s, KidDatabaseMemory myKidDB, int queryLength){
        //call constructor
        this(s.myKid, s.isReverse, s.start, s.end, myKidDB, s.queryEnd, s.queryStart, queryLength);
    }

    public String getReferenceSequence(){

        if (AlignmentKLAT1.DEBUG && myKID != 1)  return "";
        try {
            if (KidDatabaseThreadSafe.ON){
                return KidDatabaseThreadSafe.getSequence(myKID, start, stop, reverse);
            } else {
                return myKidDB.getSequence(myKID, start, stop, reverse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String toString() {
        return myKID + " : from "+start+" to "+stop+" rev:"+reverse;
    }

    public static boolean areEqual(ReferenceSequenceRequest a, ReferenceSequenceRequest b) {
        return a.myKID == b.myKID && a.reverse == b.reverse && a.start == b.start && a.stop == b.stop;
    }
}
