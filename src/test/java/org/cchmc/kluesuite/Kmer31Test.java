package org.cchmc.kluesuite;


import org.cchmc.kluesuite.klue.Kmer31;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;


/**
 * Created by osboxes on 14/08/16.
 */
public class Kmer31Test {

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

    @Test
    public void testWrongLength() {
        System.out.println("\nTesting wrong length, nonsense with constructor");
        Kmer31 bad1 = new Kmer31(badString1);
        System.out.println(bad1);
        assertFalse(bad1.isValid());
    }

    @Test
    public void testBadString2() {
        System.out.println("\nTesting DNA with 'X' with constructor");
        Kmer31 bad2 = new Kmer31(badString2);
        System.out.println(bad2);
        assertTrue(bad2.toLong() < 0);
        assertFalse(bad2.isValid());
    }

    @Test
    public void testEmptyConstructor() {
        System.out.println("\nTesting empty constructor");
        Kmer31 empty = new Kmer31();
        System.out.println(empty);
        assertEquals(empty.toLong(), 0L);
    }

    @Test
    public void testNonsenseCorrectLength(){
        System.out.println("\nTesting correct length, nonsense with constructor");
        Kmer31 bad3 = new Kmer31(buckwheat);
        System.out.println(bad3);
        Assert.assertFalse(bad3.isValid());
    }

    @Test
    public void testGood1(){

        System.out.println("\nTesting good sequence DNA01");
        System.out.println("Source sequence DNA01");
        System.out.println(DNA01);

        Kmer31 good1 = new Kmer31(DNA01);

        System.out.println(good1);

        Assert.assertTrue(good1.isValid());
    }

    @Test
    public void testToLong(){

        System.out.println("\nTesting toLong()");

        Kmer31 test = new Kmer31(biggie);
        long chump = test.toLong();
        System.out.println(test+"\t=\t"+chump);
        assertEquals(1L << 60,test.toLong());

        test = new Kmer31(one);
        chump = test.toLong();
        System.out.println(test+"\t=\t"+chump);
        assertEquals(1L,test.toLong());
        test = new Kmer31(two);
        chump = test.toLong();
        System.out.println(test+"\t=\t"+chump);
        assertEquals(test.toLong(),2L);
        test = new Kmer31(three);
        chump = test.toLong();
        System.out.println(test+"\t=\t"+chump);
        assertEquals(test.toLong(),3L);
        test = new Kmer31(thirtyfive);
        chump = test.toLong();
        System.out.println(test+"\t=\t"+chump);
        assertEquals(test.toLong(),35L);

    }

    @Test
    public void testConstructorLong(){
        long num = 123409875L;
        assertEquals(num, new Kmer31(num).toLong());
    }

    @Test
    public void testReverseStrand(){
        System.out.println("\nTesting reverseStrand()");

        Kmer31 test = new Kmer31(DNA02final);
        System.out.println(test);
        System.out.println(test.reverseStrand());
        Assert.assertEquals(DNA02final, test.reverseStrand().reverseStrand().toString());
        Assert.assertEquals(DNA02rev, test.reverseStrand().toString());


    }

    @Test
    public void testInverse(){
        Kmer31 test = new Kmer31(thirtyfive);
        Assert.assertEquals(thirtyfiveInv, test.inverseStrand().toString());
    }

    @Test
    public void testConstructorWithU(){
        System.out.println("\nTesting constructor with 'U'");

        Kmer31 test = new Kmer31(DNA02);
        Assert.assertEquals(DNA02final, test.toString());
    }

    @Test
    public void testCopyConstructor(){
        System.out.println("\nTesting copy constructor conserves isValid()");
        Kmer31 bad2 = new Kmer31(badString2);

        Assert.assertFalse(bad2.isValid());
        Assert.assertFalse(new Kmer31(bad2).isValid());
        Assert.assertEquals(bad2.toString(), new Kmer31(bad2).toString());
        Kmer31 good2 = new Kmer31(DNA02);
        Assert.assertTrue(good2.isValid());
        Assert.assertTrue(new Kmer31(good2).isValid());
        Assert.assertEquals(good2.toString(), new Kmer31(good2).toString());
    }

    @Test
    public void lowercase(){
        Kmer31 test = new Kmer31(lowercase);
        Assert.assertEquals(lowercaseCorrect, test.toString());
    }

    @Test
    public void testLexigraphicMath(){
        long fourTO16 = 1L << 32;
        Kmer31 test = new Kmer31(DNA03);

        long twoE32 = 1L << 32;

        long min = (test.toLong()/twoE32) * twoE32;
        long max = min + twoE32 - 1;
        System.out.println("\nTesting Lexigraphic Math");
        System.out.println(test.toString());
        System.out.println(new Kmer31(min).toString());
        System.out.println(new Kmer31(max).toString());

        Assert.assertEquals(DNA03max15, new Kmer31(max).toString());
        Assert.assertEquals(DNA03min15, new Kmer31(min).toString());




    }
    @Test
    public void testReverseStrand2(){
        String pos3 = "AGGGGGGCTTATTATTACCCCCCCTGCTCGG";
        Kmer31 test = new Kmer31(pos3);
        System.out.println("\nReverse test : "+test+" : "+test.reverseStrand());
    }


}
