package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;
import org.cchmc.kluesuite.variantklue.*;

import java.io.PrintStream;

/**
 * Created by jwc on 6/12/17.
 *
 * PROPERLY WRITTEN TEST CASES.  KEEP
 */
public class TestVklueStep04 {

    public static void main(String[] args){
//        System.err.println("***\nregular\n****");
//        testPermutatorLimitAdjacent();
//
//        System.err.println("***\nN1 : 11 steps\n****");
//        testPermutatorLimitAdjacentN1();
//
//        System.err.println("***\nN3 : 11 steps\n****");
//        testPermutatorLimitAdjacentN3();

        System.err.println("***\nrecreate crash\n****");
        recreateCrash();

    }


    public static void testPermutatorLimitAdjacent(){
        VariantDatabaseMemory vd = new VariantDatabaseMemory("junk");
        KidDatabaseMemory kd = new KidDatabaseMemory();

        DnaBitString.SUPPRESS_WARNINGS = true;
        PermutatorLimitAdjacent.DEBUG = true;

        PermutatorLimitAdjacent.MAX_ADJACENT=1;

        String sequence = "gactataactaaattaaCTATTGATAATATTATATCCAACAATTTCTAAC" +
                            "TTCCATGTGAAACAAAGATCAAATTTTTATTATTATTAtttttatttcag" +

                            "tatctttaagggtacaagtggtttttggttacatggatgaattgtatgat" +
                            "gataaagcctgggattttagtgtttctgtcacccgagtagtggaccttat" +
                            "acccaataaatagtttttcatccctcatccccctctcaaccttccccttc" +
                            "tgagtctccaatgtccattataccccagcctgtatgcctttgtgtaccca" +
                            "tagcttagctcgcacttataaaggagaccatttggtatttgtttttctat";
        kd.add(new Kid("Bacon"));
        kd.storeSequence(1, new DnaBitString(sequence));

        MemoryKlueTreeMap mklue = new MemoryKlueTreeMap();

        vd.addIndel(1, Variant.buildINSERTION(0,1,"+++", "one", "one"));
        vd.addIndel(1, Variant.buildINSERTION(30,1,"---", "two", "two"));
        vd.addIndel(1, Variant.buildINSERTION(60,1,"|||", "three", "three"));
        vd.addIndel(1, Variant.buildINSERTION(90,1,"}}}", "four", "four"));
        vd.addIndel(1,Variant.buildDELETION(340,1,14,"last14", "last14"));
        vd.addSNP(1, Variant.buildSNP(201,1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(202,1, "G", "xray", "xray"));


        PermutatorLimitAdjacent pla = new PermutatorLimitAdjacent(mklue, kd, vd);
        VariantDatabaseMemoryIterator it = vd.iterator(1);
        StructPermutationStrings sps = pla.processVariants(it,1,0);

        System.err.println();
        for (int k=0; k< sps.size(); k++){
//            System.out.println(Arrays.toString(sps.getStringAndVariantsArray(k)));
//            System.out.println(Arrays.toString(new ArrayList[]{sps.getVariantsArrayList(k)}));
            System.err.println("**************\t"+k);
            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getStringAndVariantsArray(k),System.err);
            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getVariantsArray(k),System.err);
            System.err.println();

        }

        //Grading
        StringAndVariants[] sava = sps.getStringAndVariantsArray(0);

        //Permut 0
        //Assert.assertEquals(sava[0].s, (sava[1].s.substring(3)));
        //Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());
        //Assert.assertNotEquals(sava[0].s.charAt(0), sava[1].s.charAt(0));


        //Permut 1
        sava = sps.getStringAndVariantsArray(1);
        //Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());

        //Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
        //Assert.assertEquals(sava[0].s.substring(30), sava[1].s.substring(30+3));


        //Permut 2
        sava = sps.getStringAndVariantsArray(2);
        //Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());

        //Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
        //Assert.assertEquals(sava[0].s.substring(30), sava[1].s.substring(30+3));

        //Permut 3
        sava = sps.getStringAndVariantsArray(3);
        //Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());

        //Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
        //Assert.assertEquals(sava[0].s.substring(30), sava[1].s.substring(30+3));

        //Permut 4
        sava = sps.getStringAndVariantsArray(4);
        //Assert.assertEquals(sava[0].s.length(), sava[1].s.length());

        //Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
        //Assert.assertEquals(sava[0].s.substring(31), sava[1].s.substring(31));
        //Assert.assertEquals(sava[1].s.charAt(30), 'T');

        //Permut 5
        sava = sps.getStringAndVariantsArray(5);
        //Assert.assertEquals(sava[0].s.length(), sava[1].s.length());

        //Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
        //Assert.assertEquals(sava[0].s.substring(31), sava[1].s.substring(31));
        //Assert.assertEquals(sava[1].s.charAt(30), 'G');

        //Permut 6
        sava = sps.getStringAndVariantsArray(6);
        int shorty = sava[1].s.length();
        //Assert.assertEquals(sava[0].s.length()-10, sava[1].s.length());

        //Assert.assertEquals(sava[0].s.substring(0,shorty), sava[1].s.substring(0,shorty));

   }




    public static void testPermutatorLimitAdjacentN3(){
        VariantDatabaseMemory vd = new VariantDatabaseMemory("junk");
        KidDatabaseMemory kd = new KidDatabaseMemory();

        DnaBitString.SUPPRESS_WARNINGS = true;
        PermutatorLimitAdjacent.DEBUG = true;

        PermutatorLimitAdjacent.MAX_ADJACENT=3;

        String sequence = "gactataactaaattaaCTATTGATAATATTATATCCAACAATTTCTAAC" +
                "TTCCATGTGAAACAAAGATCAAATTTTTATTATTATTAtttttatttcag" +

                "tatctttaagggtacaagtggtttttggttacatggatgaattgtatgat" +
                "gataaagcctgggattttagtgtttctgtcacccgagtagtggaccttat" +
                "acccaataaatagtttttcatccctcatccccctctcaaccttccccttc" +
                "tgagtctccaatgtccattataccccagcctgtatgcctttgtgtaccca" +
                "tagcttagctcgcacttataaaggagaccatttggtatttgtttttctat";
        kd.add(new Kid("Bacon"));
        kd.storeSequence(1, new DnaBitString(sequence));

        MemoryKlueTreeMap mklue = new MemoryKlueTreeMap();

        vd.addIndel(1, Variant.buildINSERTION(0,1,"+++", "one", "one"));
        vd.addIndel(1, Variant.buildINSERTION(30,1,"---", "two", "two"));
        vd.addIndel(1, Variant.buildINSERTION(60,1,"|||", "three", "three"));
        vd.addIndel(1, Variant.buildINSERTION(90,1,"}}}", "four", "four"));
        vd.addIndel(1,Variant.buildDELETION(340,1,14,"last14", "last14"));
        vd.addSNP(1, Variant.buildSNP(201,1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(202,1, "G", "xray", "xray"));


        PermutatorLimitAdjacent pla = new PermutatorLimitAdjacent(mklue, kd, vd);
        VariantDatabaseMemoryIterator it = vd.iterator(1);
        StructPermutationStrings sps = pla.processVariants(it,1,0);

        System.err.println();
        for (int k=0; k< sps.size(); k++){
//            System.out.println(Arrays.toString(sps.getStringAndVariantsArray(k)));
//            System.out.println(Arrays.toString(new ArrayList[]{sps.getVariantsArrayList(k)}));
            System.err.println("**************"+k);
            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getStringAndVariantsArray(k),System.err);

            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getVariantsArray(k),System.err);

            System.err.println();

        }

        //Grading
        StringAndVariants[] sava = sps.getStringAndVariantsArray(0);

//        //Permut 0
//        Assert.assertEquals(sava[0].s, (sava[1].s.substring(3)));
//        Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());
//        Assert.assertNotEquals(sava[0].s.charAt(0), sava[1].s.charAt(0));
//
//
//        //Permut 1
//        sava = sps.getStringAndVariantsArray(1);
//        Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());
//
//        Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
//        Assert.assertEquals(sava[0].s.substring(30), sava[1].s.substring(30+3));
//
//
//        //Permut 2
//        sava = sps.getStringAndVariantsArray(2);
//        Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());
//
//        Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
//        Assert.assertEquals(sava[0].s.substring(30), sava[1].s.substring(30+3));
//
//        //Permut 3
//        sava = sps.getStringAndVariantsArray(3);
//        Assert.assertEquals(sava[0].s.length()+3, sava[1].s.length());
//
//        Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
//        Assert.assertEquals(sava[0].s.substring(30), sava[1].s.substring(30+3));
//
//        //Permut 4
//        //Permut 5
//        sava = sps.getStringAndVariantsArray(4);
//        Assert.assertEquals(sava[0].s.length(), sava[1].s.length());
//
//        Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
//        Assert.assertEquals(sava[0].s.substring(31), sava[1].s.substring(31));
//        Assert.assertEquals(sava[1].s.charAt(30), 'T');
//
//
//        sava = sps.getStringAndVariantsArray(5);
//        Assert.assertEquals(sava[0].s.length(), sava[1].s.length());
//
//        Assert.assertEquals(sava[0].s.substring(0,30), sava[1].s.substring(0,30));
//        Assert.assertEquals(sava[0].s.substring(31), sava[1].s.substring(31));
//        Assert.assertEquals(sava[1].s.charAt(30), 'G');
//
//
//
//        //Permut 6
//        sava = sps.getStringAndVariantsArray(6);
//        int shorty = sava[1].s.length();
//        Assert.assertEquals(sava[0].s.length()-10, sava[1].s.length());
//
//        Assert.assertEquals(sava[0].s.substring(0,shorty), sava[1].s.substring(0,shorty));


    }



    public static void testPermutatorLimitAdjacentN1() {
        VariantDatabaseMemory vd = new VariantDatabaseMemory("junk");
        KidDatabaseMemory kd = new KidDatabaseMemory();

        DnaBitString.SUPPRESS_WARNINGS = true;
        PermutatorLimitAdjacent.DEBUG = true;

        PermutatorLimitAdjacent.MAX_ADJACENT = 1;

        String sequence = "gactataactaaattaaCTATTGATAATATTATATCCAACAATTTCTAAC" +
                "TTCCATGTGAAACAAAGATCAAATTTTTATTATTATTAtttttatttcag" +

                "tatctttaagggtacaagtggtttttggttacatggatgaattgtatgat" +
                "gataaagcctgggattttagtgtttctgtcacccgagtagtggaccttat" +
                "acccaataaatagtttttcatccctcatccccctctcaaccttccccttc" +
                "tgagtctccaatgtccattataccccagcctgtatgcctttgtgtaccca" +
                "tagcttagctcgcacttataaaggagaccatttggtatttgtttttctat";
        kd.add(new Kid("Bacon"));
        kd.storeSequence(1, new DnaBitString(sequence));

        MemoryKlueTreeMap mklue = new MemoryKlueTreeMap();

//        variants  [SNP    k 1     s 10106         l 1      T, SNP         k 1     s 10107         l 1      T,
//      INSERTION   k 1     s 10127         l 1      C, SNP         k 1     s 10137         l 1      T,
//        DELETION    k 1     s 10143
//        l 1      null,
//      DELETION         k 1     s 10145         l 1      null, SNP      k 1     s 10148         l 1      T,
// INSERTION   k 1     s 10164         l 1      C, SNP         k 1     s 10175         l 1      C,
// INSERTION   k 1     s 10176         l 1      C, SNP         k 1     s 10178         l 1      T]

        vd.addSNP(1, Variant.buildSNP(106, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(107, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(137, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(148, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(175, 1, "C", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(178, 1, "T", "xray", "xray"));




        vd.addIndel(1, Variant.buildINSERTION(127, 1, "C", "one", "one"));
        vd.addIndel(1, Variant.buildINSERTION(164, 1, "C", "two", "two"));
        vd.addIndel(1, Variant.buildINSERTION(176, 1, "C", "three", "three"));

        vd.addIndel(1, Variant.buildDELETION(143, 1, 14, "last14", "last14"));
        vd.addIndel(1, Variant.buildDELETION(145, 1, 14, "last14", "last14"));


        PermutatorLimitAdjacent pla = new PermutatorLimitAdjacent(mklue, kd, vd);
        VariantDatabaseMemoryIterator it = vd.iterator(1);
        StructPermutationStrings sps = pla.processVariants(it, 1, 0);

        System.err.println();
        for (int k = 0; k < sps.size(); k++) {
//            System.out.println(Arrays.toString(sps.getStringAndVariantsArray(k)));
//            System.out.println(Arrays.toString(new ArrayList[]{sps.getVariantsArrayList(k)}));
            System.err.println("**************" + k);
            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getStringAndVariantsArray(k), System.err);

            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getVariantsArray(k), System.err);

            System.err.println();

        }
    }


    public static void recreateCrash() {
//        variants  [DELETION       k 1     s 10227         l 1      null,
// DELETION         k 1     s 10227         l 27     null,
// SNP      k 1     s 10229         l 1      C, D

// ELETION    k 1     s 10229         l 1      null,
// SNP      k 1       s 10232         l 1      T,

// INSERTION   k 1     s 10234         l 1      A,
// SNP         k 1     s 10245         l 1      T,
// SNP         k 1     s 10246         l 1      T,
// SNP         k 1     s 10248         l 1      C,

// DELETION      k 1     s 10248         l 2      null,
// DELETION         k 1     s 10253         l 1      null,
// SNP      k 1     s 10255         l 1      C,
// SNP         k 1     s 10257         l 1      C,
// SNP         k 1     s 10278         l 1        C,

// SNP         k 1     s 10283         l 1      T,
// SNP         k 1     s 10289         l 1      T,
// SNP         k 1     s 10295         l 1      T,

// SNP         k 1     s 10300         l 1      G,
// SNP         k 1     s 10325  l 1       T,
// DELETION    k 1     s 10327         l 24     null,
// DELETION         k 1     s 10328         l 1      null,
// SNP      k 1     s 10331         l 1      T,
// SNP         k 1     s 10350         l 1      T, INSERTION   k 1     s 10351   l 1      A, INSERTION   k 1     s 10352         l 1      A]


        VariantDatabaseMemory vd = new VariantDatabaseMemory("junk");


        vd.addIndel(1, Variant.buildDELETION(227, 1, 1, "last14", "last14"));
        vd.addIndel(1, Variant.buildDELETION(227, 1, 27, "last14", "last14"));
        vd.addSNP(1, Variant.buildSNP(229, 1, "C", "xray", "xray"));
        vd.addIndel(1, Variant.buildDELETION(229, 1, 1, "last14", "last14"));

        vd.addSNP(1, Variant.buildSNP(232, 1, "T", "xray", "xray"));
        vd.addIndel(1, Variant.buildINSERTION(234, 1, "A", "three", "three"));
        vd.addSNP(1, Variant.buildSNP(245, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(246, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(248, 1, "C", "xray", "xray"));

        vd.addIndel(1, Variant.buildDELETION(248, 1, 2, "last14", "last14"));
        vd.addIndel(1, Variant.buildDELETION(253, 1, 1, "last14", "last14"));
        vd.addSNP(1, Variant.buildSNP(255, 1, "C", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(257, 1, "C", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(278, 1, "C", "xray", "xray"));

        vd.addSNP(1, Variant.buildSNP(283, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(289, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(295, 1, "T", "xray", "xray"));

        vd.addSNP(1, Variant.buildSNP(300, 1, "G", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(325, 1, "T", "xray", "xray"));
        vd.addIndel(1, Variant.buildDELETION(327, 1, 24, "last14", "last14"));
        vd.addIndel(1, Variant.buildDELETION(328, 1, 1, "last14", "last14"));
        vd.addSNP(1, Variant.buildSNP(331, 1, "T", "xray", "xray"));
        vd.addSNP(1, Variant.buildSNP(350, 1, "T", "xray", "xray"));


        KidDatabaseMemory kd = new KidDatabaseMemory();

        DnaBitString.SUPPRESS_WARNINGS = true;
        PermutatorLimitAdjacent.DEBUG = true;

        PermutatorLimitAdjacent.MAX_ADJACENT = 4;

        String sequence = "gactataactaaattaaCTATTGATAATATTATATCCAACAATTTCTAAC" +
                "TTCCATGTGAAACAAAGATCAAATTTTTATTATTATTAtttttatttcag" +

                "tatctttaagggtacaagtggtttttggttacatggatgaattgtatgat" +
                "gataaagcctgggattttagtgtttctgtcacccgagtagtggaccttat" +
                "acccaataaatagtttttcatccctcatccccctctcaaccttccccttc" +
                "tgagtctccaatgtccattataccccagcctgtatgcctttgtgtaccca" +
                "tagcttagctcgcacttataaaggagaccatttggtatttgtttttctat" +
                "tagcttagctcgcacttataaaggagaccatttggtatttgtttttctat";
        kd.add(new Kid("Bacon"));
        kd.storeSequence(1, new DnaBitString(sequence));

        MemoryKlueTreeMap mklue = new MemoryKlueTreeMap();

        PermutatorLimitAdjacent pla = new PermutatorLimitAdjacent(mklue, kd, vd);
        VariantDatabaseMemoryIterator it = vd.iterator(1);
        StructPermutationStrings sps = pla.processVariants(it, 1, 0);

        System.err.println();
        for (int k=0; k< sps.size(); k++){
//            System.out.println(Arrays.toString(sps.getStringAndVariantsArray(k)));
//            System.out.println(Arrays.toString(new ArrayList[]{sps.getVariantsArrayList(k)}));
            System.err.println("**************\t"+k);
            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getStringAndVariantsArray(k),System.err);
            TestVklueStep04.<StringAndVariants>printArraysPerLine(sps.getVariantsArray(k),System.err);
            System.err.println();

        }


    }


    private static <E> void printArraysPerLine(Object obj, PrintStream s){

        E[] arr = (E[]) obj;
        for (int k=0; k<arr.length; k++){
            s.println(arr[k]);
        }

    }

}
