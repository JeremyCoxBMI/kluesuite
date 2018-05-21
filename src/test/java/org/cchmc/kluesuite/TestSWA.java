package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.PartialAlignment1;
import org.cchmc.kluesuite.klat.SmithWatermanAdvanced;
import org.cchmc.kluesuite.klat.SmithWatermanOriginal;
import org.cchmc.kluesuite.variantklue.Variant;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 8/26/17.
 */
public class TestSWA {

    @Test
    public void testKnownEdgeCase(){
        String a = "BACKONCHEESE";
        String b = "EESEBACON";




        PartialAlignment1.DO_NOT_CALCULATE_ALIGNMENT_STRINGS = false;

        SmithWatermanOriginal swo = new SmithWatermanOriginal(b,a);
        for (PartialAlignment1 p : swo.bestAlignments()){
            System.out.println(p.toString());
        }

//        SmithWatermanAdvanced swa = new SmithWatermanAdvanced(b,a);
//
//        for (PartialAlignment1 p : swa.bestAlignments()){
//            System.out.println(p.toString());
//        }

    }

    @Test
    public void testPidentA(){
        PartialAlignment1.DO_NOT_CALCULATE_ALIGNMENT_STRINGS = false;

        String q = "ATCGATC";
        String r = "ATCAGATC";
        SmithWatermanAdvanced swa;
        int alignLen;
        int match;

        System.out.println("\n###############");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);

        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
        }

        q = "AAAATTTTCCCCCGGGG";
        r = "AAAATGACCCCGGGCCG";
        System.out.println("\n###############");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            Assert.assertEquals(p.numAligned,19);
            Assert.assertEquals(p.pident,new Float(13)/19,0.01);
        }

        q = "AAAATGGGA";
        r = "AAAACGGA";

        System.out.println("\n###############");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");
        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            Assert.assertEquals(p.numAligned,9);
            Assert.assertEquals(p.pident,new Float(7)/9,0.01);
        }


        q = "AAAATGGGATG";
        r = "AAAACGGACG";

        System.out.println("\n###############");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");
        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            Assert.assertEquals(p.numAligned,11);
            Assert.assertEquals(p.pident,new Float(8)/11,0.01);
        }



        q = "AAAAAAAATTTTCCCCCGGGG";
        r = "AAAATGACCCCGGGCCGGGGGG";
        alignLen = 20;
        match = 14;

        System.out.println("\n###############");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");
        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            Assert.assertEquals(p.numAligned,alignLen);
            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
        }


        //CALL NEW processGap2  and buildGap2
        PartialAlignment1.DO_NOT_CALCULATE_ALIGNMENT_STRINGS = true;

        System.out.println("\n###############");
        System.out.println("Using new gap calculations");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            Assert.assertEquals(p.numAligned,alignLen);
            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
        }



        q = "AAAATTTTCCCCCGGGG";
        r = "AAAATGACCCCGGGCCG";
        alignLen = 19;
        match = 13;

        System.out.println("\n###############");
        System.out.println("Using new gap calculations");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            System.out.println(Variant.variantNameList(p.callVariants2()));
            Assert.assertEquals(p.numAligned,alignLen);
            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
        }
//
//
//
//        //mismatch and gap calculations together
//        q = "AAAATGGGAT";
//        r = "AAAACGGAC";
//        alignLen = 10;
//        match = 7;
//
//        System.out.println("\n###############");
//        System.out.println("Using new gap calculations");
//        System.out.println(q);
//        System.out.println(r);
//        System.out.println("###############");
//
//        swa = new SmithWatermanAdvanced(q,r);
//        for (PartialAlignment1 p : swa.bestAlignments()){
//            System.out.println(p.toString());
//            Assert.assertEquals(p.numAligned,alignLen);
//            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
//        }

        r = "AAAATTCCCCC";
        q = "AAAAGGCCCCC";
        alignLen = 11;
        match = 9;

        System.out.println("\n###############");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            System.out.println(Variant.variantNameList(p.callVariants2()));
            Assert.assertEquals(p.numAligned,alignLen);
            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
        }


        PartialAlignment1.DO_NOT_CALCULATE_ALIGNMENT_STRINGS = true;

        r = "AAAATTTTCCCCCGGGG";
        q = "AAAATGACCCCGGGCCG";
        alignLen = 19;
        match = 13;

        System.out.println("\n###############");
        System.out.println("Using new gap calculations");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            System.out.println(Variant.variantNameList(p.callVariants2()));
            Assert.assertEquals(p.numAligned,alignLen);
            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
        }



        r = "AAAATTCCCCC";
        q = "AAAAGGCCCCC";
        alignLen = 11;
        match = 9;

        System.out.println("\n###############");
        System.out.println("Using new gap calculations");
        System.out.println(q);
        System.out.println(r);
        System.out.println("###############");

        swa = new SmithWatermanAdvanced(q,r);
        for (PartialAlignment1 p : swa.bestAlignments()){
            System.out.println(p.toString());
            System.out.println(Variant.variantNameList(p.callVariants2()));
            Assert.assertEquals(p.numAligned,alignLen);
            Assert.assertEquals(p.pident,new Float(match)/alignLen,0.01);
        }


    }





}
