package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.klue.KeyValuePair;
import org.cchmc.kluesuite.multifilerocksdbklue.Prefix16;
import org.cchmc.kluesuite.multifilerocksdbklue.Rocks16Klue;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by jwc on 3/13/18.
 */
public class TestRocks16Klue {


    static String db16 = "/data/1/nvme/hg38.klue16.final";
    static String db = "/data/1/nvme/hg38.klue.final";
    static String kidDB = "/data/1/f/hg38.2018.02.12/hg38.KidDB.disk";
    public final static String klue16file = "/data/1/nvme/klue16.txt";
    static String[] files16 = new String[16];

    @Test
    public void testRealFiles() {


        Rocks16Klue klue16 = new Rocks16Klue(true, klue16file);

        for (int j = 0; j < 16; j++) {
            System.err.println("Part " + j);
            long t = Prefix16.prefixIntToKmer31Long(j);
            System.err.println(new PositionList(klue16.get(t)));
        }

        klue16.shutDown();
    }


    @Test
    public void testIterator() {
        Rocks16Klue klue16 = new Rocks16Klue(true, klue16file);

        Iterator<KeyValuePair> it; // = klue16.iterator();

        for (int j = 0; j < 16; j++) {
            System.err.println("Part " + j);
            it = klue16.iterator(Prefix16.prefixIntToKmer31Long(j));
            System.err.println(it.next().positions);
        }


        klue16.shutDown();

    }


}
