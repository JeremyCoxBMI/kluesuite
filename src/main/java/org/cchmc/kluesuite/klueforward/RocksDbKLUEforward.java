package org.cchmc.kluesuite.klueforward;

import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by osboxes on 27/09/16.
 *
 * TODO In addition to reverseStrand lookups instead of storage, possibly this could natively support 16 way database split.
 *
 */
public class RocksDbKLUEforward extends RocksDbKlue implements KLUEforward {

    public RocksDbKLUEforward(String path, boolean readonly) {
        super(path, readonly);
    }

    public RocksDbKLUEforward(String path, boolean readonly, int maxFiles) {
        super(path, readonly, maxFiles);
    }

    @Override
    /**
     * Find the reverse sequence key
     */
    public ArrayList<Long> getRevStrand(long key) {
        return get(new Kmer31(key).reverseStrand().toLong());
    }

    @Override
    public ArrayList<ArrayList<Long>> getAllRevStrand(long[] keys) {
        long[] revKeys = new long[keys.length];
        for (int k=0; k<keys.length; k++){
            revKeys[k] = new Kmer31(keys[k]).reverseStrand().toLong();
        }
        return getAll(revKeys);
    }

    @Override
    public ArrayList<PositionList> getAllPLRevStrand(long[] keys) {
        //QualifiedNameArray
        long[] revKeys = new long[keys.length];
        for (int k=0; k<keys.length; k++){
            revKeys[k] = new Kmer31(keys[k]).reverseStrand().toLong();
        }
        return getAllPL(revKeys);
    }

//    @Override
//    public PositionList getShortKmerMatchesRevStrand(long shorty, int prefixLength) {
//        return null;
//    }



    public static void main(String[] args) {

        KidDatabaseMemory myKidDB = new KidDatabaseMemory();
        RocksDbKLUEforward rocksklue = new RocksDbKLUEforward("deleteme.temp", false);
        KlueForwardDataImport kdi = new KlueForwardDataImport(rocksklue, myKidDB);

        String file = "/data/1/f/junk/test.fa";
        try {
            kdi.readFNAfileSKIPto(file,"");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String a = "GCAATACACTGAAAATGTTTAGACGGGCTCACATCACCCCATAAACAAA";
        String b = "CCAGAGTGTAGCTTAACACAAAGCACCCAACTTACACTTAGGAGATTT";

        PositionList p = new PositionList(rocksklue.get(new Kmer31(a.substring(0,31)).toLong()));

        System.out.println("look up first kmer");
        System.out.println(p.toString());

        p = new PositionList(rocksklue.get(new Kmer31(b.substring(0,31)).toLong()));

        System.out.println("look up second kmer");
        System.out.println(p.toString());


        p = new PositionList(rocksklue.get(
                new Kmer31(a.substring(0,31)).reverseStrand().toLong())
        );
        System.out.println("look up first reverse");
        System.out.println(p.toString());

    }
}


