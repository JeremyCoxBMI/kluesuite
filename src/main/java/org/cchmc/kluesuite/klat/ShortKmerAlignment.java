package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.klue.ShortKmer31;

import java.util.ArrayList;

/**
 * Created by osboxes on 23/08/16.
 *
 * For executing contingency to use shorter K-mers per
 * KLATsettings.REDO_ALIGNMENT_ON_LOW_SCORE
 *
 * PLACE HOLDER IMPLEMENTATION
 */
public class ShortKmerAlignment extends AlignmentKLAT1 {

    int kmerSize;

    public ShortKmerAlignment( String query, String queryName, KLUE klue, int k ) {
        super(query, queryName, klue);
        kmerSize = k;
    }

    @Override
    public void pollKmersForPositions() {
        //super.pollKmersForPositions();
        //posz = klue.getAllPL(kmers.getAllForward());
        ShortKmer31[] keys = ((ShortKmerSequence) kmers).getForwardShortKmer31();

        posz = new ArrayList<PositionList>();
        for (ShortKmer31 key : keys){
            posz.add(klue.getShortKmerMatches(key.toLong(),key.prefixLength));
        }
    }
}
