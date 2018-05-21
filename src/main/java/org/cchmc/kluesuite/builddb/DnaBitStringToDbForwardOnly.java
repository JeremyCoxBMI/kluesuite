package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.Position;

/**
 * Created by jwc on 8/11/17.
 */
public class DnaBitStringToDbForwardOnly extends DnaBitStringToDb {


    public DnaBitStringToDbForwardOnly(DnaBitString notAcopy, KLUE writeToMe, int KID) {
        super(notAcopy, writeToMe, KID);
    }



    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP){
        debugKmerWritten++;

        Position loc = new Position(KID, pos);

        if (START) {
            loc.setFlag(Position.START, true);
        }
        if (STOP) {
            loc.setFlag(Position.STOP, true);
        }
        klue.append(word.toLong(), loc.toLong());
    }
}
