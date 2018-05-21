package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.klue.ShortKmer31;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.RocksIterator;

import java.util.ArrayList;


/**
 * Created by osboxes on 23/08/16.
 */
public class GetShortKmerRocksKlueTest {

    RocksDbKlue klue;
    ShortKmer31 seed =     new ShortKmer31("ATCGATCGATCGATCGATCG");
    ArrayList<Long> consecutive200;


    @Before
    public void setUp() {
        klue = new RocksDbKlue("/mnt/vmdk/kluesuite/rocksdbVirusFNAkmer31", true, 30);
//        KidDatabaseMemory myKidDB = new KidDatabaseMemory();
//        myKidDB = KidDatabaseMemory.loadFromFile(myKidDB.fileName);

        consecutive200 = new ArrayList<Long>();

        byte[] key, value;
        long longKey;

        RocksIterator it = klue.db.newIterator();
        it.seek(RocksDbKlue.longToBytes(seed.lowerbound));

        System.out.println("\n 200 Kmers in order matching " + seed);
        key = it.key();
        longKey = RocksDbKlue.bytesToLong(key);
        for (int k = 0; k < 200; k++) {
            consecutive200.add(longKey);
            it.next();
            key = it.key();
            longKey = RocksDbKlue.bytesToLong(key);
//            System.out.println(k+"\t"+new Kmer31(longKey));
        }

        for (int k = 150; k < 160; k++) {
            System.out.println(k+"\t"+new Kmer31(consecutive200.get(k)));
        }
    }

//    @Test
//    public void testBoundaryCheckOnSeek() {
////        ShortKmer31 seed =     new ShortKmer31("ATCGATCGATCGATCGATCG"); //length 20
//        ShortKmer31 seedNext = new ShortKmer31("ATCGATCGATCGATCGATCA"); //length 20
//
//
//        //code taken from RocksDbKlue
//        //modified, because we want to make sure the SEEK works
//        byte[] key, value;
//        long longKey;
//        RocksIterator it = klue.db.newIterator();
//
//        it.seek( RocksDbKlue.longToBytes(seed.lowerbound)  );
//
//        key = it.key();
//        longKey = RocksDbKlue.bytesToLong(key);
//        System.out.println("\ntestBoundaryCheckOnSeek()\n Checking Boundary checking on seek :: starting with ::                 "+seed);
//        while( seed.equal(longKey) ){
//            value = it.value();
//            System.out.println(new Kmer31(longKey));
//            it.next();
//            key = it.key();
//            longKey = RocksDbKlue.bytesToLong(key);
//        }
//
//        System.out.println("\n Iteration finished within the bounds of previous, now continuing with "+seedNext);
//
//        while( seedNext.equal(longKey) ){
//            value = it.value();
//            System.out.println(new Kmer31(longKey));
//            it.next();
//            key = it.key();
//            longKey = RocksDbKlue.bytesToLong(key);
//        }
//
//
//    }


    @Test
    public void testSeek() {
//        152	ATCGATCGATAGATTCAAAGACGACATAGAT
//        153	ATCGATCGATAGACAGCAACAGCAGCACAAG
//        154	ATCGATCGATAGAATTAATTTGACTATTGTC

//        155	ATCGATCGATAATTTATTAGTTACAAAAGAT
//        156	ATCGATCGATAATCTACTTGAGCTTTCTGTA
//        157	ATCGATCGATAATCCTTCTTTAGTAAACTGT
//        158	ATCGATCGATAATGTGTTTTTCATTAACATT
//        159	ATCGATCGATAATATTGATAAATCATTAGAT
//        160	ATCGATCGATAATAACCAAATGCGACTCCTC

//        161	ATCGATCGATAACTTTTTCTTGATACTTATT
//        162	ATCGATCGATAACTCTGCGACGTGAAACGAG
//        163	ATCGATCGATAACCCCAACATGTATCTGGTT
//        164	ATCGATCGATAACATCGATGAGTAATTCGTA
//        165	ATCGATCGATAAGTATACAATTTCGTTCACC
//        166	ATCGATCGATAAGCTAAAACAGCCAATCAGT


        ShortKmer31 shorty = new ShortKmer31("ATCGATCGATAAT");

        byte[] key, value;
        long longKey;
        RocksIterator it = klue.db.newIterator();

        it.seek( RocksDbKlue.longToBytes(shorty.lowerbound)  );

        key = it.key();
        longKey = RocksDbKlue.bytesToLong(key);

        System.out.println("\ntestSeek()\n Checking Boundary checking on seek :: starting with ::                 ");
        System.out.println(shorty+"\n");

        int k = 156;    //not sure why, index is 1 off
        while( shorty.equal(longKey) ){
            System.out.println(new Kmer31(longKey));
            Assert.assertEquals( new Kmer31(consecutive200.get(k)).toString(), new Kmer31(longKey).toString());
            it.next();
            k++;
            key = it.key();
            longKey = RocksDbKlue.bytesToLong(key);
        }
    }

    @Test
    public void testRocksDbKlueGetShortKmer() {
        ShortKmer31 shorty = new ShortKmer31("ATCGATCGATAAT");

        byte[] key, value;
        long longKey;
        RocksIterator it = klue.db.newIterator();

        PositionList original, extracrispy;

        it.seek( RocksDbKlue.longToBytes(shorty.lowerbound)  );

        key = it.key();
        longKey = RocksDbKlue.bytesToLong(key);

        System.out.println("\ntestRocksDbKlueGetShortKmer()\nComparing output from manual parsing and function");

        System.out.println("\nManual Positions ");

        original = new PositionList();
        while( shorty.equal(longKey) ){
            value = it.value();
            PositionList pl = new PositionList( RocksDbKlue.bytesToArrayListLong(value) );
            original.add(pl.toArrayListLong());
            System.out.println(new Kmer31(longKey));
            System.out.println(pl);
            it.next();
            key = it.key();
            longKey = RocksDbKlue.bytesToLong(key);
        }

        System.out.println("\nusing RocksKlueDb ");
        extracrispy = klue.getShortKmerMatches(shorty.toLong(), shorty.prefixLength);
        System.out.println(extracrispy);

        Assert.assertEquals(extracrispy.toString(),original.toString());
    }


    @After
    public void shutDown(){
        klue.shutDown();
    }
}
