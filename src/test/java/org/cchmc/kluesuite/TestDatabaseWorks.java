package org.cchmc.kluesuite;

import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 3/6/18.
 */
public class TestDatabaseWorks {

    String rocksdb = "/data/1/nvme/hg38.klue.final";

    @Test
    public void testFirstKey(){
        RocksDbKlue klue = new RocksDbKlue(rocksdb, true);
        Assert.assertTrue(klue.getFirst() != null);
        System.out.println(klue.getFirst());

    }
}
