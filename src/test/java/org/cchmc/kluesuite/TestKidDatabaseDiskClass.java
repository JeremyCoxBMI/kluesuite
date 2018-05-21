package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jwc on 2/15/18.
 */
public class TestKidDatabaseDiskClass {

    String sequence =
            "AGCTGAGCACTGGAGTGGAGTTTTCCTGTGGAGAGGAGCCATGCCTAGAG"+
            "TGGGATGGGCCATTGTTCATCTTCTGGCCCCTGTTGTCTGCATGTAACTT"+
            "AGCTGAGCACTGGAGTGGAGTTTTCCTGTGGAGAGGAGCCATGCCTAGAG"+
            "TGGGATGGGCCATTGTTCATCTTCTGGCCCCTGTTGTCTGCATGTAACTT"+
            "AGCTGAGCACTGGAGTGGAGTTTTCCTGTGGAGAGGAGCCATGCCTAGAG"+
            "TGGGATGGGCCATTGTTCATCTTCTGGCCCCTGTTGTCTGCATGTAACTT";

    String inputFA = "testfiles/TestKidDatabaseDiskClass.fa";

    //prototype header
    @Test
    public void test (){



    }

    @Test
    public void testImportFNA (){

        try {
            Files.deleteIfExists(Paths.get("test.Kid"));
            Files.deleteIfExists(Paths.get("test.Kid.rocks"));
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Temporary files to delete do not exist.");
        }

        KidDatabaseDisk kd = new KidDatabaseDisk("test.Kid", "test.Kid.rocks", false);

        try {
            kd.importFNA(inputFA);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Assert.assertEquals(kd.getSequence(1).toString(), sequence);

    }

    @Test
    public void testSubsequences(){

        try {
            Files.deleteIfExists(Paths.get("test.Kid"));
            Files.deleteIfExists(Paths.get("test.Kid.rocks"));
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Temporary files to delete do not exist.");
        }

        KidDatabaseDisk kd = new KidDatabaseDisk("test.Kid", "test.Kid.rocks", false);


        try {
            kd.importFNA(inputFA);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        for (int k=0; k< 3; k++){
            String s = "";
            int from = k*100;
            int to = from  +100;
            try {
                s = kd.getSequence(1,from, to, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String t = sequence.substring(from, to);
            //Assert.assertEquals(s,t);
            Assert.assertTrue(s.equals(t));
        }

        for (int k=0; k< 2; k++){
            String s = "";
            int from = k*100+50;
            int to = from  +100;
            try {
                s = kd.getSequence(1,from, to, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String t = sequence.substring(from, to);
            //Assert.assertEquals(s,t);
            Assert.assertTrue(s.equals(t));
        }

        kd.shutDown();
    }


    @Test
    public void testSaveLoad(){

        try {
            Files.deleteIfExists(Paths.get("test.Kid"));
            Files.deleteIfExists(Paths.get("test.Kid.rocks"));
        } catch (IOException e) {
//            e.printStackTrace();
            System.err.println("Temporary files to delete do not exist.");
        }

        KidDatabaseDisk kd = new KidDatabaseDisk("test.Kid", "test.Kid.rocks", false);

        try {
            kd.importFNA(inputFA);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        kd.saveToFileUnsafe();
        kd.shutDown();
        kd = KidDatabaseDisk.loadFromFileUnsafe("test.Kid");


        for (int k=0; k< 3; k++){
            String s = "";
            int from = k*100;
            int to = from  +100;
            try {
                s = kd.getSequence(1,from, to, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String t = sequence.substring(from, to);
            //Assert.assertEquals(s,t);
            Assert.assertTrue(s.equals(t));
        }

        for (int k=0; k< 2; k++){
            String s = "";
            int from = k*100+50;
            int to = from  +100;
            try {
                s = kd.getSequence(1,from, to, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String t = sequence.substring(from, to);
            //Assert.assertEquals(s,t);
            Assert.assertTrue(s.equals(t));
        }

        kd.shutDown();
    }

}
