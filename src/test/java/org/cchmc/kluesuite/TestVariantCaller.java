package org.cchmc.kluesuite;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;
import org.cchmc.kluesuite.variantklue.Variant;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 8/15/17.
 */
public class TestVariantCaller {


//    @Test
//    public void testPartialAlignmentMismatches(){
//        String one = "ATCATC";
//        String two = "ATCGTC";
//
//        //AlignmentKLAT1 al = new AlignmentKLAT1()
//        PartialAlignment1 pa = new PartialAlignment1()
//
//    }

    @Test
    public void testVariantCaller(){

        MemoryKlueTreeMap klue = new MemoryKlueTreeMap();
        KidDatabaseMemory kd = new KidDatabaseMemory();
        kd.add(new Kid( "ref1"));
        kd.add(new Kid( "ref2"));
        kd.add(new Kid( "ref3"));




        String ref1 =    "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        String result1 = "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        Variant v = Variant.buildDELETION(50,1,1,"-/A", "-/A");

        DnaBitString dns = new DnaBitString(ref1);

        kd.storeSequence(1,dns);
        DnaBitStringToDb dbstd = new DnaBitStringToDb(dns,klue,1);
        dbstd.writeAllPositions();


        String ref2 =    "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        String result2 = "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCGAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        v = Variant.buildSNP(50,1,"G","G/T", "G/T");

        dns = new DnaBitString(ref2);
        kd.storeSequence(2,dns);
        dbstd = new DnaBitStringToDb(dns,klue,2);
        dbstd.writeAllPositions();



        String ref3 =    "CAAACTAACTGAATGTTAGAACCAACTCCTGATAAGTCTTGAACAAAAGATAGGATCCTCTATAAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGT";
        String result3 = "CAAACTAACTGAATGTTAGAACCAACTCCTGATAAGTCTTGAACAAAAGACCCTAGGATCCTCTATAAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGT";

        dns = new DnaBitString(ref3);
        kd.storeSequence(3,dns);
        dbstd = new DnaBitStringToDb(dns,klue,3);
        dbstd.writeAllPositions();


        AlignmentKLAT1 al = new AlignmentKLAT1(result1,"result1", klue);

        System.out.println(al.results(kd));


        al = new AlignmentKLAT1(result2,"result2", klue);

        System.out.println(al.results(kd));


        al = new AlignmentKLAT1(result3,"result2", klue);

        System.out.println(al.results(kd));


        String result3B = "CAAACTAACGGAATGTTAGAACCAACTCCTGATAAGTCTACAAAAGATAGGATCCTCTATAAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGT";
        //9: T/G
        //39: TGA/-
        al = new AlignmentKLAT1(result3B,"result3B", klue);

        System.out.println(al.results(kd));

    }

    @Test
    public void testSeedOverlap(){
        //[{QUERY from 0 to 14, REF from 6 to 26 FWD, HITS 8 ADJ 6 kid 1 SNP/INDEL false/false},
        //{QUERY from 0 to 18, REF from 0 to 24 FWD, HITS 12 ADJ 6 kid 1 SNP/INDEL false/false},
        Seed a = new Seed(0,14,6,26,false,8,6,1);
        Seed b = new Seed(0,18,0,24,false,12,6,1);
        //public Seed(int qStart, int qStop, int start, int end, boolean reverse, int hits, int adjacency, int myKid){

        Seed temp = Seed.mergeIfAble(a,b);

        Assert.assertEquals(temp.hits,-1);
        "DEBUG".equals("BREAK POINT");
    }



}
