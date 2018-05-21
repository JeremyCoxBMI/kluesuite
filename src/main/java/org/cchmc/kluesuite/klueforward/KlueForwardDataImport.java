package org.cchmc.kluesuite.klueforward;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.*;

/**
 * Created by osboxes on 27/09/16.
 *
 * As KlueDataImport, but does not write to reverseStrand
 *
 * 2017-03-31   Implemnented
 *
 * This class may be deprecated, because we now write to DnaBitStrings first
 *
 */
public class KlueForwardDataImport extends KlueDataImport{


    public KlueForwardDataImport(KLUE klue, KidDatabaseMemory myKidDB) {
        super(klue, myKidDB);
    }

    /**
     * Function is used in KlueDataImport; here we redefine it to never write reverse strand.
     * @param word
     * @param KID
     * @param pos
     * @param START
     * @param STOP
     * @param tt
     */
    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP, TimeTotals tt){
        recordForwardToStore(word,KID,pos,START,STOP,tt);
    }

    public void recordForwardToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP, TimeTotals tt){
        if (debug){ System.out.println("\tputting this kmer onto store :: "+word.toString());}
//        Kmer31 revWord = word.reverseStrand();
        Position loc = new Position(KID, pos);
//        Position revLoc = new Position(KID, pos + Kmer31.KMER_SIZE -1 );
//        revLoc.setFlag(Position.REVERSE, true);
        if (START) {
            loc.setFlag(Position.START, true);
//            revLoc.setFlag(Position.STOP, true);
        }
        if (STOP) {
            loc.setFlag(Position.STOP, true);
//            revLoc.setFlag(Position.START, true);
        }
        tt.pause();
        klue.append(word.toLong(), loc.toLong());
//        klue.append(revWord.toLong(), revLoc.toLong());
        tt.unPause();
    }
}
