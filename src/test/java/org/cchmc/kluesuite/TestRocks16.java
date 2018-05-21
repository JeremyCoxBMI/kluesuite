package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.multifilerocksdbklue.Rocks16Klue;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.cchmc.kluesuite.multifilerocksdbklue.Rocks16Klue.makeChunks;

/**
 * Created by osboxes on 02/11/16.
 */
public class TestRocks16 {

    String badString1 = "I am bad string: wrong length";
    String badString2 = "AAAAAATTTGCTCXTTTTTTCCCCCCCCCCG";
    String buckwheat  = "1234567890123456789012345678901";
    String DNA01      = "GGAAAAAAAAATTTTTTTTTTCCCCCCCCGG";
    String DNA02      = "GGAAAAAAAAATTTTUUUUTTCCCCCCCCGG";
    String DNA02final = "GGAAAAAAAAATTTTTTTTTTCCCCCCCCGG";
    String DNA02rev   = "CCGGGGGGGGAAAAAAAAAATTTTTTTTTCC";
    String DNA03      =  "UGTAATAATAATTATUAUUATCACCACCGGC";
    String DNA03min15 = "TGTAATAATAATTATTTTTTTTTTTTTTTTT";
    String DNA03max15 = "TGTAATAATAATTATAAAAAAAAAAAAAAAA";
    String binaryDemo = "TTTTTTTTTTTTTTTTTTTTTTTTTAAGGCC";
    String lowercase  =        "GgAAAaaAAAATtTTUUUuTtCCCCCCCCGc";
    String lowercaseCorrect  = "GGAAAAAAAAATTTTTTTTTTCCCCCCCCGC";
    String twoCompSad = "AAGGCCTTTTTTTTTTTTTTTTTTTTTTTTT";
    String one        = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTC";
    String two        = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTG";
    String three      = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTA";
    String thirtyfive   = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTGTA";
    String thirtyfiveInv = "AAAAAAAAAAAAAAAAAAAAAAAAAAAACAT";
    String biggie     = "CTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";


    public static void main(String[] args) {

        //Rocks16Klue.makeChunks();

    }


    @Test
    public void testSplit(){
        Kmer31 temp = new Kmer31(DNA01);
        int k = Rocks16Klue.split(temp.toLong());
        Assert.assertEquals(k, 10);


        temp = new Kmer31(DNA02rev);
        k = Rocks16Klue.split(temp.toLong());
        Assert.assertEquals(k, 5);

        temp = new Kmer31(DNA03max15);
        k = Rocks16Klue.split(temp.toLong());
        Assert.assertEquals(k, 2);

        temp = new Kmer31(thirtyfiveInv);
        k = Rocks16Klue.split(temp.toLong());
        Assert.assertEquals(k, 15);
    }

    @Test
    public void testMakeChunks(){
        long[] keys = new long[9];
        long[] splits = new long[9];
        long[] checked = new long[9];


        keys[0] = (new Kmer31(DNA01)).toLong();
        keys[1] = (new Kmer31(DNA02)).toLong();
        keys[2] = (new Kmer31(DNA02final)).toLong();
        keys[3] = (new Kmer31(DNA02rev)).toLong();
        keys[4] = (new Kmer31(DNA03)).toLong();
        keys[5] = (new Kmer31(DNA03min15)).toLong();
        keys[6] = (new Kmer31(DNA03max15)).toLong();
        keys[7] = (new Kmer31(biggie)).toLong();
        keys[8] = (new Kmer31(thirtyfiveInv)).toLong();

        for (int k=0; k<keys.length;k++){
            splits[k] = Rocks16Klue.split(keys[k]);
            checked[k] = keys[k] / (1L << 58);
        }
        ArrayList<ArrayList<Long>> chunks = makeChunks(keys);

        System.out.println("KEYS");
        System.out.println(Arrays.toString(keys));
        System.out.println("SPLITS");
        System.out.println(Arrays.toString(splits));
        System.out.println("CHECKED");
        System.out.println(Arrays.toString(checked));

        System.out.println("CHUNKS");
        for (ArrayList<Long> chunk : chunks ){
            System.out.println(chunk);
        }
    }

    @Test
    public void testLookups(){
        //Rocks16Klue klue = new Rocks16Klue("/mnt/Dshare/kluesuite.microbiome/klue16/microbiomeKmer31",true);
        //At this point, just run VerifyKlue16
    }

}
