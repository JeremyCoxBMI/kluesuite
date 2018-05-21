package org.cchmc.kluesuite.rocksDBklue;

import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.klue.ShortKLUE;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.util.ArrayList;

/**
 * Created by COX1KB on 4/5/2018.
 *
 * RocksShortKLUE does short k-mer look-ups for 2 part database
 *
 * We have regular k-mer database, and then the startEnd klue database, which has start and end k-mers
 *
 */
public class RocksShortKLUE implements ShortKLUE {

    RocksDbKlue kmer;
    RocksDbKlue shortkmer;

    /**
     * if startEndDatabase is missing, one may use 'null'
     * @param kmerDatabase
     * @param startEndDatabase
     */
    public RocksShortKLUE(RocksDbKlue kmerDatabase, RocksDbKlue startEndDatabase){
        kmer = kmerDatabase;
        shortkmer = startEndDatabase;
    }

    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        PositionList result;
        result = kmer.getShortKmerMatches(shorty, prefixLength);
        if (shortkmer != null)
            result.add( shortkmer.getShortKmerMatches(shorty, prefixLength));
        return result;
    }

}
