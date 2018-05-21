package org.cchmc.kluesuite;

import org.cchmc.kluesuite._oldclasses.Permutator;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.variantklue.*;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by osboxes on 15/12/16.
 */
public class TestPermutatorClass {

    String sequence = "TCAAAGGTATCCACTCAACAAGATTACAGTAATACAGTACTATCCTAATATATACATAGTTTTTTACCATCTCACAATGT" +
            "AAACTATGTATGTCTAACTCCCACCATCAATCCCATCAATGAACCTTGGTAAGAGTTCAGATTCAATCAGATGAAAAACA" +
            "ACACTTCAGGGAGAACAATTGTTCTAGAGCAGAGTGGTTATTGTACTACAACAACGCATCGTTGAACACAACATCTCGCG" +
            "TTTTTCGCAACTCTCAACCAGGCTGGGTAAATCAAATTGCTTTGTCGACTGTCTCGTAGAACGCAAGGTCTCGCGTTTTT" +
            "CGCAACTTGATATCTGGACTTACAGGAGGACTCAGCTTGGCCCACTACCAGAACAATAGCATTTCAGAACAGAGACATGC" +
            "TACTGTGATTCCACCACCTCAGACAGGGCTGTTTTTG";

    @Test
    public void testaIterator(){
        System.out.println("\n\ntestaIterator() *************************\n");
        VariantDatabase vd = new VariantDatabaseMemory();
        int KID = 4;

        vd.addIndel(KID, Variant.buildDELETION(5, KID, 1, "rsJWC123", "<6|G/->"));
        vd.addIndel(KID, Variant.buildINSERTION(15, KID, "aaa", "rsJWC456", "<16|-/AAA>"));
        vd.addSNP(KID, Variant.buildSNP(75, KID, "W", "rsJWC1988", "<76|A/T>"));
        vd.addSNP(KID, Variant.buildSNP(417, KID, "B", "rsJWCclarity", "<418|C/T,G>"));

        Iterator<Variant> it = vd.iterator(4);

        while (it.hasNext()){
            System.out.println(""+it.next());
        }
    }

//    @Test
//    public void testLookups() {
//
//        System.out.println("\n\ntestLookups() *************************\n");
//        KLUE klue = new HeapKlueHashMap(10000);
//
//        RocksKidDatabase rkd = new RocksKidDatabase("/tmp/rdbs",false);
//
//        rkd.addAndTrim(new Kid(">chump"), sequence);
//        int KID = rkd.indexOf("chump");
//
//        VariantDatabase1 vd = new VariantDatabase1();
//
//        vd.addIndel(KID, Variant.buildDELETION(5, KID, 1, "rsJWC123", "<6|G/->"));
//        vd.addIndel(KID, Variant.buildINSERTION(15, KID, "aaa", "rsJWC456", "<16|-/AAA>"));
//        vd.addSNP(KID, Variant.buildSNP(75, KID, "W", "rsJWC1988", "<76|A/T>"));
//        vd.addSNP(KID, Variant.buildSNP(417, KID, "B", "rsJWCclarity", "<418|C/T,G>"));
//
//        PermutatorOLD perm = new PermutatorOLD(klue, rkd,vd);
//
//        perm.processVariants();
//
//        Kmer31 kto = new Kmer31(2297705832352112188L);  //should return indel flag
//        System.out.println("\nThis sequence should look up indel flag\n"+new PositionList(klue.get(kto.toLong())));
//
//        kto = new Kmer31(574826278842732003L);  //should return indel flag
//        System.out.println("\nThis sequence should look up START and INDEL flag\n"+new PositionList(klue.get(kto.toLong())));
//
//
//        kto = new Kmer31("GATTCCACCACCTCAGACAGGGCTGTTTTTG");
//        System.out.println("\nThis sequence should NOT look up SNP flag\n"+new PositionList(klue.get(kto.toLong())));
//
//        kto = new Kmer31("GATTCCACCACTTCAGACAGGGCTGTTTTTG");
//        System.out.println("\nThis sequence should look up SNP flag\n"+new PositionList(klue.get(kto.toLong())));
//
//        kto = new Kmer31("GATTCCACCACGTCAGACAGGGCTGTTTTTG");
//        System.out.println("\nThis sequence should look up SNP flag\n"+new PositionList(klue.get(kto.toLong())));
//
//
//        // This query matches the end INCLUDING SNP
//        String query = "TACTGTGATTCCACCACGTCAGACAGGGCTGTTTTTG";
//
//        System.out.println("\n***********\nSINGLE SNP at position 417: flags 00001000\n***********\n");
//
//        AlignmentKLAT1 alig = new AlignmentKLAT1(query,"snp417",klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        alig.testAll(rkd);
//
//        //This query matches beginning with a deletion at 5
//        query = "TCAAAGTATCCACTCAACAAGATTACAGTAATACAGTACTATCCTAATATATACA";
//
//        System.out.println("\n***********\nSINGLE DELETION at position 5: flags 00000100\n***********\n");
//
//        alig = new AlignmentKLAT1(query,"delete005",klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        alig.testAll(rkd);
//    }

//    @Test
//    public void testGenerateVariants(){
//        System.out.println("\n\ntestGenerateVariants() *************************\n");
//        RocksKidDatabase rkd = new RocksKidDatabase("/tmp/rdbs",false);
//        rkd.addAndTrim(new Kid(">chump"), sequence);
//        int KID = rkd.indexOf("chump");
//
//        VariantDatabase1 vd = new VariantDatabase1();
//
//        vd.addIndel(KID, Variant.buildDELETION(5, KID, 1, "rsJWC123", "<6|G/->"));
//        vd.addIndel(KID, Variant.buildINSERTION(15, KID, "aaa", "rsJWC456", "<16|-/AAA>"));
//        vd.addSNP(KID, Variant.buildSNP(75, KID, "W", "rsJWC1988", "<76|A/T>"));
//        vd.addSNP(KID, Variant.buildSNP(417, KID, "B", "rsJWCclarity", "<418|C/T,G>"));
//
//        //PermutatorOLD perm = new PermutatorOLD(myKidDb, vd);
//        StringAndVariants[] result = vd.getAllVariants(1,3,93,rkd, false);
//        for (StringAndVariants sb : result){
//            System.out.println(sb.s);
//        }
//    }

    @Test
    public void testGenerateSequenceAllVariants() {


        String checkCN = ">hg38chr1||1||13";
        String checkCS = "AAATTTCCCGGG";
        String checkVN = ">hg38chr1||1||13||rs201095316[D]<4|-/T>,rs568927205[I]<7|-/aaa>,rs777429169[D]<10|-/GG>";
        String checkVS = "AAATTaaaCCCG";

        DnaBitString dns = new DnaBitString(checkCS);
        ArrayList<Variant> vars = new ArrayList<>();

        vars.add( Variant.buildDELETION(3,1,1,"test1", "test1:delete1"));
        vars.add( Variant.buildINSERTION(6,1,"aaa","test2","test2:insert2"));
        vars.add( Variant.buildDELETION(9,1,2,"test3","test3:delete2"));

//        MemoryKlueTreeMap klue = new MemoryKlueTreeMap();
////        KidDatabaseMemory kd = new KidDatabaseMemory();
////        kd.addAndTrim(new Kid("testSequence"));
////        kd.storeSequence(1,checkCS);
//        VariantDatabaseMemory vdm = new VariantDatabaseMemory();

//        vdm.addIndel(1, Variant.buildDELETION(3,1,1,"test1", "test1:delete1"));
//        vdm.addIndel(1, Variant.buildINSERTION(6,1,"aaa","test2","test2:insert2"));
//        vdm.addIndel(1, Variant.buildDELETION(9,1,2,"test3","test3:delete2"));

//        Permutator p = new Permutator(klue,kd,vdm);

        StringAndVariants[] sav = Permutator.generateSequenceAllVariants(vars,0,12,dns);

        System.out.println(StringAndVariants.arrayToString(sav));

        Assert.assertEquals(checkVS,sav[0].s);


        ArrayList<Variant> vars2 = new ArrayList<>();

        String checkVS2a = "AAATCCCGGG";
        String checkVS2b = "AAACCGGG";

        vars2.add(Variant.buildDELETION(3,1,2,"test2a", "test2a:delete2"));
        vars2.add(Variant.buildDELETION(3,1,4,"test2b", "test2b:delete4"));

        sav = Permutator.generateSequenceAllVariants(vars2,0,12,dns);
        System.out.println(StringAndVariants.arrayToString(sav));
        Assert.assertEquals(checkVS2a,sav[0].s);
        Assert.assertEquals(checkVS2b,sav[1].s);

    }
}
