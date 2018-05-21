package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.ShortKmer31;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by osboxes on 21/09/16.
 */
public class TestShortKmer31 {


    @Test
    public void testShortKmer31Constructor(){
        ShortKmer31 bob = new ShortKmer31("ATCGATCGATCGATCG");
        System.out.println("Binary string: "+bob.toBinaryString());
        Assert.assertEquals("0011000110110001101100011011000110000000000000000000000000000000",bob.toBinaryString());
    }


}
