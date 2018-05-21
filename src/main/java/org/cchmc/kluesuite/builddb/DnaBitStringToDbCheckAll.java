package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.klue.*;

/**
 * Created by osboxes on 19/05/17.
 *
 * A class to verify that all of DnaBitString was properly written to disk database.
 */
public class DnaBitStringToDbCheckAll extends DnaBitStringToDb {


    public DnaBitStringToDbCheckAll(DnaBitString notAcopy, KLUE writeToMe, int KID) {
        super(notAcopy, writeToMe, KID);
    }

    //public DnaBitStringToDbCheckAll


    //public void writeAllPositions()

    public void verifyAllKmersWritten(){
        writeAllPositions();
    }


    //THIS NOW serves function to CheckAll
    @Override
    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP){
        debugKmerWritten++;
//        System.out.println("BUCKY");

        boolean result = true;


        Kmer31 revWord = word.reverseStrand();
        Position loc = new Position(KID, pos);

        //System.err.println("Writing\t"+word);

        Position revLoc = new Position(KID, pos + Kmer31.KMER_SIZE -1 );

        PositionList pl = new PositionList(klue.get(word.toLong()));
        result = (result && pl.contains(loc));  // TRUE IFF both true, otherwise not in database

        if (!forwardOnly){
            pl = new PositionList(klue.get(word.toLong()));
            result = (result && pl.contains(loc));
        }

        System.out.println(result+"  \tSequence\t"+word+"\tpositions\t"+pl);
    }

}
