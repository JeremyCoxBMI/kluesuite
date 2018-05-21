package org.cchmc.kluesuite;

import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabaseAllDisk;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 4/23/18.
 */
public class TestKidDatabaseAll {

    @Test
    public void testIndexNames(){
        String prefix = "/data/3/d/hg38.2018.04.12/hg38";
        String path = "/data/3/d/hg38.2018.04.12/hg38.deleteme.kid.temp";

        KidDatabaseAllDisk kdad = new KidDatabaseAllDisk(prefix, path, true);

        for (int idx=1; idx <= 25; idx++) {
            String s = kdad.getSequenceName(idx);
            int z = kdad.getKid(s);
            LogStream.stderr.println("idx\t"+idx+"\tname\t"+s+"\tKID\t"+z);
            Assert.assertEquals(idx, z);
        }
    }


    @Test
    public void testSequenceFunctions(){
        String prefix = "/data/3/d/hg38.2018.04.12/hg38";
        String path = "/data/3/d/hg38.2018.04.12/hg38.deleteme.kid.temp";

        KidDatabaseAllDisk kdad = new KidDatabaseAllDisk(prefix, path, true);

        long total = 0;
        try {
            for (int idx = 1; idx <= 25; idx++) {
                String s = kdad.getSequence(idx, 105000, 105050, false);
                int l = kdad.getLength(idx);
                total += l;
                LogStream.stderr.println("idx\t" + idx + "\tlen\t" + l + "\tsubSequence");
                LogStream.stderr.println(s);
                Assert.assertNotEquals(null, s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogStream.stderr.println("Number of bases stored :: "+total);
    }
}
