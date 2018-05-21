package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.multifilerocksdbklue.Prefix16;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 3/15/18.
 */
public class TestPrefix16 {

    @Test
    public void testByteToPrefixInt(){
        byte b;
        int p;

        b = 31;
        //00011111
        //--0111--
        p = Prefix16.byteToPrefixInt(b);
        Assert.assertEquals(7,p);

        b = 95;
        //01011111
        //--0111--
        p = Prefix16.byteToPrefixInt(b);
        Assert.assertEquals(7, p);

        b = 73;
        //01001001
        //--0010--
        p = Prefix16.byteToPrefixInt(b);
        Assert.assertEquals(2, p);

        b = 41;
        //00101001
        //--1010--
        p = Prefix16.byteToPrefixInt(b);
        Assert.assertEquals(10, p);

        Kmer31 t = new Kmer31("ATAGATAGATAGATAGATAGATAGATAGATA");
        long l = t.toLong();
        byte bs[] = t.toBytes();
        p = Prefix16.bytesToPrefixInt(bs);
        //AT codes to (3,0) = '1100' = 12
        Assert.assertEquals(12, p);

    }


}


