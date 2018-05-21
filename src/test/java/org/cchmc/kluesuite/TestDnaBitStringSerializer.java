package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.junit.Assert;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.zip.DataFormatException;

/**
 * Created by osboxes on 25/09/16.
 *
 *
 * DnaBitString No Longer "Serializes", but instead stores itself as long[]
 * Still, test is valid
 *
 */
public class TestDnaBitStringSerializer {

    @Test
    public void testBoth() {
        SuperString test = new SuperString();
        test.addAndTrim("AAAAATTTTTCCCCCGGGGG");
        test.addAndTrim("XR");
        test.addAndTrim("CCCCCGGGGGAAAAATTTTT");

        DnaBitString dns = null;
        try {
            dns = new DnaBitString(test);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }


        byte[] bytes = dns.toByteArray();

        System.out.println(dns.toString());
        DnaBitString chubbz = new DnaBitString(bytes);

        Assert.assertEquals(dns.toString(), chubbz.toString());

    }

    @Test
    public void testRocksDb() {
        SuperString test = new SuperString();
        test.addAndTrim("AAAAATTTTTCCCCCGGGGG");
        test.addAndTrim("XR");
        test.addAndTrim("CCCCCGGGGGAAAAATTTTT");

        DnaBitString dns = null;
        try {
            dns = new DnaBitString(test);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        Options options = new Options().setCreateIfMissing(true).setMaxOpenFiles(Settings_OLD.MAX_FILES);


        RocksDB rd = null;
        try {
            rd = RocksDB.open(options, "/mnt/vmdk/deleteme");
        } catch (RocksDBException e) {
            e.printStackTrace();
        }

        byte[] bytes = dns.toByteArray();

        byte[] key = RocksDbKlue.longToBytes(45L);

        try {
            rd.put(key, bytes);
            bytes = rd.get(key);
            DnaBitString chubbz = new DnaBitString(bytes);

            System.out.println(dns.toString());
            System.out.println(chubbz.toString());
            Assert.assertEquals(dns.toString(), chubbz.toString());

        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

}
