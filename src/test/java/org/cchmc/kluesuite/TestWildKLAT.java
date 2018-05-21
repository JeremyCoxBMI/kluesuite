package org.cchmc.kluesuite;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.rocksDBklue.RocksShortKLUE;
import org.cchmc.kluesuite.wildklat.PermuteDnaString;
import org.junit.Assert;
import org.junit.Test;
import org.rocksdb.RocksDB;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.DataFormatException;

/**
 * Created by jwc on 4/5/18.
 */
public class TestWildKLAT {

    @Test
    public void testWithHumanDB(){
        System.out.println("\n\n****************testWithHumanDB******************");

        String rocksdbname = "/data/1/nvme/hg38.klue.final";

        RocksDbKlue rocks = new RocksDbKlue(rocksdbname, true);

        RocksShortKLUE sklue = new RocksShortKLUE(rocks, null);

        String chr10sequence = "CCAAAGTCACGGTACTTTCCTACTCTTAAATAATTTATCAAGAGTCCTGA";

        String test16 = chr10sequence.substring(0,16);
        System.out.println("Testing "+test16);

        PositionList pl = PermuteDnaString.shortKmerLookUpNoGaps(test16, sklue);

        System.out.println("Positions found are "+pl);

    }

    @Test
    public void testPermuteDnaStringGetShortKmerNoGaps(){
        System.out.println("\n\n****************testRockKlueDb******************");

        KidDatabaseDisk kdd = new KidDatabaseDisk("test.shortkmer31.kid","test.shortkmer31.rocks",false);
        RocksDbKlue klue = new RocksDbKlue("test.shortkmer31.kmer", false);

        RocksShortKLUE sklue = new RocksShortKLUE(klue, null);

        SuperString test1 = new SuperString();
        String s1 = "ACGTGTGTAA";
        String s2 = "CCGTGTGTAA";

        //klue 0 indexed
        test1.add(s2);  //coord 0
        for (int k=0; k<5; k++) test1.add(s1);  //10, 20, 30, 40, 50
        test1.add(s2);  //60
        test1.add("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
        test1.add(s2); //110

        kdd.add(new Kid("test1"));
        int kid = kdd.getKid("test1");

        try {
            DnaBitString dns = new DnaBitString(test1);
            DnaBitStringToDb dtd = new DnaBitStringToDb(dns,klue,kid);
            dtd.writeAllPositions();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        Position p;
        PositionList pl;
        Set<Integer> pos;

        pl = PermuteDnaString.shortKmerLookUpNoGaps(s1, sklue);
        pos = new HashSet<Integer>();

        System.out.println(pl.toString());
        for (int z=0; z < pl.length(); z++){
            p = pl.get(z);
            pos.add(p.getPosition());
        }


        Assert.assertTrue(pos.contains(10));
        pos.remove(10);
        Assert.assertTrue(pos.contains(20));
        pos.remove(20);
        Assert.assertTrue(pos.contains(30));
        pos.remove(30);
        Assert.assertTrue(pos.contains(40));
        pos.remove(40);
        Assert.assertTrue(pos.contains(50));
        pos.remove(50);
        Assert.assertTrue(pos.isEmpty());

        pl = klue.getShortKmerMatches(new ShortKmer31(s2).toLong(), s2.length());
        pos = new HashSet<Integer>();
        for (int z=0; z < pl.length(); z++){
            p = pl.get(z);
            pos.add(p.getPosition());
        }

        System.out.println(pl.toString());

        Assert.assertTrue(pos.contains(60));
        pos.remove(60);
        Assert.assertTrue(pos.contains(0));
        pos.remove(0);
        //RocksDbKlue cannot find k-mer at end of sequence
        Assert.assertFalse(pos.contains(110));
        Assert.assertTrue(pos.isEmpty());

    }
}
