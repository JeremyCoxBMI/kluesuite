package org.cchmc.kluesuite;

import junit.framework.Assert;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.ShortKmer31;
import org.cchmc.kluesuite.wildklat.PermuteDnaString;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by COX1KB on 4/5/2018.
 */
public class TestPermuteDnaString {


    /*
     * This is the FastA standardized alphabet
     * R    :   A, G
     * Y    :   C, T, (U)
     * K    :   G, T, (U)
     * M    :   C, A
     * S    :   C, G
     * W    :   A, T, (U)
     * B    :   C, G, T, (U)
     * D    :   A, G, T, (U)
     * H    :   A, C, T, (U)
     * V    :   A, C, G
     * N    :   A, G, G, T (U)
     * ?    :   A, G, G, T (U)
     */

    @Test
    public void testCreatePermutationsWithoutGaps(){
        String test1 = "ACGTACGTVN";
        int numTest1 = 12;
        String test2 = "NACGTACGT";
        int numTest2 = 4;

        ArrayList<String> test1arr = PermuteDnaString.createPermutationsWithoutGaps(test1);
        PermuteDnaString.printArrayAsLines(test1arr,"test1\t",System.out);
        Assert.assertEquals(test1arr.size(), numTest1);

        System.out.println("");
        ArrayList<String> test2arr = PermuteDnaString.createPermutationsWithoutGaps(test2);
        PermuteDnaString.printArrayAsLines(test2arr, "test2\t", System.out);
        Assert.assertEquals(test2arr.size(), numTest2);

    }

    @Test
    public void testPermutationsToShortKmers(){
        String test2 = "NACGTACGT";
        int numTest2 = 4;

        ArrayList<String> test2arr = PermuteDnaString.createPermutationsWithoutGaps(test2);

        ArrayList<ShortKmer31> skm = PermuteDnaString.permutationsToShortKmers(test2arr);

        for (ShortKmer31 kmer : skm){
            System.out.println("\t\t"+kmer.toString());
        }

        Assert.assertEquals(skm.get(0).toString(), "AACGTACGTTTTTTTTTTTTTTTTTTTTTTT");
        Assert.assertEquals(skm.get(1).toString(), "CACGTACGTTTTTTTTTTTTTTTTTTTTTTT");
        Assert.assertEquals(skm.get(2).toString(), "TACGTACGTTTTTTTTTTTTTTTTTTTTTTT");
        Assert.assertEquals(skm.get(3).toString(), "GACGTACGTTTTTTTTTTTTTTTTTTTTTTT");
    }

}
