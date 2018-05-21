package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by osboxes on 13/09/16.
 */
public class TestDnaBitString {

    @Test
    public void testSerializor(){
        DnaBitString test = new DnaBitString("AGCAGGGGGGCTTATTATTACCCCCCCTGCTCGGGGCGGGACATTCTGTGATGGGCTGGGCTTTATGCGG");
        DnaBitString copy = new DnaBitString(test.toByteArray());
        System.out.println("Comparing test and copy in testSerializor()");
        System.out.println(test);
        System.out.println(copy);
        Assert.assertEquals(test.toString(),copy.toString());

        test = new DnaBitString("AGCAGGGXGGCTTATTATTACCCCC?CTGCTCGGGGCGGGACATTCTGT-ATGGGCTGGGCTTTATGCGG");
        copy = new DnaBitString(test.toByteArray());
        System.out.println("Comparing test and copy in testSerializor()");
        System.out.println(test);
        System.out.println(copy);
        Assert.assertEquals(test.toString(),copy.toString());
    }


    @Test
    public void testSerializorHuge() {
        SuperString dirt8k = new SuperString();
        SuperString clean8k = new SuperString();
        SuperString dirt40k = new SuperString();
        SuperString clean40k = new SuperString();
        SuperString dirt250M = new SuperString();
        SuperString clean250M = new SuperString();

        String basic = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGAZCGATCG";
        String clean = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCG";

        for (int i=0; i<100; i++){
            dirt8k.addAndTrim(basic);
            clean8k.addAndTrim(clean);
        }

        for (int i=0; i<500; i++){
            dirt40k.addAndTrim(basic);
            clean40k.addAndTrim(clean);
        }

        for (int i=0; i<3130000; i++){
            dirt250M.addAndTrim(basic);
            clean250M.addAndTrim(clean);
        }

        DnaBitString test;
        DnaBitString copy;



        System.out.println("Comparing test and copy in testSerializor :: dirt8k");
        test = new DnaBitString(dirt8k.toString());
        copy= new DnaBitString(test.toByteArray());
        Assert.assertEquals(test.toString(), copy.toString());

        System.out.println("Comparing test and copy in testSerializor :: clean8k");
        test = new DnaBitString(clean8k.toString());
        copy= new DnaBitString(test.toByteArray());
        Assert.assertEquals(test.toString(), copy.toString());

        System.out.println("Comparing test and copy in testSerializor :: clean40k");
        test = new DnaBitString(clean40k.toString());
        copy= new DnaBitString(test.toByteArray());
        Assert.assertEquals(test.toString(), copy.toString());

        System.out.println("Comparing test and copy in testSerializor :: dirt40k");
        test = new DnaBitString(dirt40k.toString());
        copy= new DnaBitString(test.toByteArray());
        Assert.assertEquals(test.toString(), copy.toString());

//        System.out.println("Comparing test and copy in testSerializor :: dirt250M");
//        test = new DnaBitString(dirt250M.toString());
//        copy= new DnaBitString(test.toByteArray());
//        Assert.assertEquals(test.toString(), copy.toString());
//
//        System.out.println("Comparing test and copy in testSerializor :: clean250M");
//        test = new DnaBitString(clean250M.toString());
//        copy= new DnaBitString(test.toByteArray());
//        Assert.assertEquals(test.toString(), copy.toString());
    }


    @Test
    public void testSerializorHugeRocksDB() {
        SuperString dirt8k = new SuperString();
        SuperString clean8k = new SuperString();
        SuperString dirt40k = new SuperString();
        SuperString clean40k = new SuperString();

        SuperString dirt250M = new SuperString();
        SuperString clean250M = new SuperString();

        String basic = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGAZCGATCG";
        String clean = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCG";

        for (int i=0; i<100; i++){
            dirt8k.addAndTrim(basic);
            clean8k.addAndTrim(clean);
        }

        for (int i=0; i<500; i++){
            dirt40k.addAndTrim(basic);
            clean40k.addAndTrim(clean);
        }

        for (int i=0; i<3130000; i++){
            dirt250M.addAndTrim(basic);
            clean250M.addAndTrim(clean);
        }

//        RocksKidDatabase rkd = new RocksKidDatabase("./junkDBSrocks", false);
//        rkd.fileName = "./junkKID";     //change file save location

        KidDatabaseMemory rkd = new KidDatabaseMemory();


        DnaBitString test;
        DnaBitString copy;
        DnaBitString.SUPPRESS_WARNINGS = true;
        Kid temp;

        int offsetKID = rkd.getMaxKid();

        temp = new Kid("dirt8k");
        rkd.add(temp, dirt8k.toString());
        System.out.println("Comparing test and copy through RocksDB in testSerializor :: dirt8k");
        test = new DnaBitString(dirt8k.toString());
        copy= rkd.getSequence(offsetKID+1);
        Assert.assertEquals(test.toString(), copy.toString());

        temp = new Kid("dirt40k");
        rkd.add(temp, dirt40k.toString());
        System.out.println("Comparing test and copy through RocksDB in testSerializor :: dirt40k");
        test = new DnaBitString(dirt40k.toString());
        copy= rkd.getSequence(offsetKID+2);
        Assert.assertEquals(test.toString(), copy.toString());

        temp = new Kid("dirt250M");
        rkd.add(temp,dirt250M.toString());
        System.out.println("Comparing test and copy through RocksDB in testSerializor :: dirt250M");
        test = new DnaBitString(dirt250M.toString());
        copy= rkd.getSequence(offsetKID+3);
        Assert.assertEquals(test.toString(), copy.toString());


        temp = new Kid("clean8k");
        rkd.add(temp,clean8k.toString());
        System.out.println("Comparing test and copy through RocksDB in testSerializor :: clean8k");
        test = new DnaBitString(clean8k.toString());
        copy= rkd.getSequence(offsetKID+4);
        Assert.assertEquals(test.toString(), copy.toString());

        temp = new Kid("clean40k");
        rkd.add(temp,clean40k.toString());
        System.out.println("Comparing test and copy through RocksDB in testSerializor :: clean40k");
        test = new DnaBitString(clean40k.toString());
        copy= rkd.getSequence(offsetKID+5);
        Assert.assertEquals(test.toString(), copy.toString());

        temp = new Kid("clean250M");
        rkd.add(temp,clean250M.toString());
        System.out.println("Comparing test and copy through RocksDB in testSerializor :: clean250M");
        test = new DnaBitString(clean250M.toString());
        copy= rkd.getSequence(offsetKID+6);
        Assert.assertEquals(test.toString(), copy.toString());


    }




    @Test
    public void testIterator(){
        DnaBitString test = new DnaBitString("AGCAGGGGGGCTTATTATTACCCCCCCTGCTCGGGGCGGGACATTCTGTGATGGGCTGGGCTTTATGCGG");
        String pos3 = "AGGGGGGCTTATTATTACCCCCCCTGCTCGG";
        Iterator<Kmer31> it = test.iterator();
        it.next();
        it.next();
        it.next();
        Kmer31 boing = it.next();
        Assert.assertEquals(pos3, boing.toString());
        Assert.assertEquals(pos3, test.getSequence(3,34));
    }


    @Test
    public void testIterator2(){
        String original = "AGCAGGGGGGCTTATTATTACCCCCCCTGCTCGGGGCGGGACATTCTGTGATGGGCTGGGCTTTATGCGG";
        DnaBitString test = new DnaBitString(original);
        System.out.println("\nTesting constructor and Iterator");
        System.out.println(original);
        //System.out.println(test);
        Iterator<Kmer31> it = test.iterator();
        String indent="";
        while (it.hasNext()) {
            System.out.println(indent+it.next());
            indent+=" ";
        }
    }

    @Test
    public void testIterator3(){
        String original = "AGCAGGGGGGCTTATTATTACCCCCCCTGCTXCGGGGCGGGACATTCTGTGATGGGCTGGGCT";
        DnaBitString test = new DnaBitString(original);
        System.out.println("\nTesting constructor and Iterator: contains invalid char, so should be all null but two ends");
        System.out.println(original);
        //System.out.println(test);
        Iterator<Kmer31> it = test.iterator();
        String indent="";
        int k=0;
        while (it.hasNext()) {
            Kmer31 temp = it.next();
            System.out.println(indent+temp);
            if (k==0){
                Assert.assertEquals(temp.toString(), "AGCAGGGGGGCTTATTATTACCCCCCCTGCT");
            } else if (!it.hasNext()){
                Assert.assertEquals(temp.toString(), "CGGGGCGGGACATTCTGTGATGGGCTGGGCT");
            } else {
                Assert.assertEquals(temp, null);
            }
            indent+=" ";
            k++;
        }

    }

    @Test
    public void testReverse(){
        DnaBitString test = new DnaBitString("AGCAGGGGGGCTTATTATTACCCCCCCTGCTCGGGGCGGGACATTCTGTGATGGGCTGGGCTTTATGCGG");
        String pos3    = "AGGGGGGCTTATTATTACCCCCCCTGCTCGG";
        String reverse = "CCGAGCAGGGGGGGTAATAATAAGCCCCCCT";
        Assert.assertEquals(reverse,test.getSequenceReverseStrand(3,34));
    }

    @Test
    public void testByteConversion(){

        DnaBitString buddy = new DnaBitString("CCWGGGGXGGGGAAAAXAAAATTTTXTTTTKCC");
        DnaBitString cds = new DnaBitString(buddy.toByteArray());
        Assert.assertEquals(buddy.toString(), cds.toString());
    }

    @Test
    public void testGetSequence(){
        DnaBitString test = new DnaBitString("AGCAGGGGGGCTTATTATTACCCCCCCTGCTCGGGGCGGGACATTCTGTGATGGGCTGGGCTTTATGCGG");

        String pos39 = test.getSequence(39,70);
        System.out.println("testGetSequenece() : " +pos39);
        Assert.assertEquals(pos39, "GACATTCTGTGATGGGCTGGGCTTTATGCGG");
    }



}
