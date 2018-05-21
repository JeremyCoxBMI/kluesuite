package org.cchmc.kluesuite;

import junit.framework.Assert;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.junit.Test;


import java.io.FileNotFoundException;
import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by osboxes on 6/2/17.
 *
 * KEEP THESE TESTS
 * TODO NEED FastA file distributed
 */
public class KidDatabaseMemoryImportExportUnsafeTest {

    @Test
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes a FastA file and builds kmer database, 1 database per sequence.");
            System.out.println("ARG 0 : location to FastA file (all)");
            System.out.println("ARG 1 : location Kid Database (unsafe) to build");
            //System.out.println("ARG 2 : location DnaBitString database");
            exit(0);
        }


//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks(args[0]);
//        rkd.restartDb();


        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS() + "\n");

        System.err.println("Importing FastA file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseMemory me = new KidDatabaseMemory();
        try {
            me.importFNA(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        UnsafeMemory mu = new UnsafeMemory(2*UnsafeMemory.getWriteUnsafeSize(me.getSequence(1),UnsafeMemory.DNABITSTRING_TYPE));
        me.getSequence(1).writeUnsafe(mu);
        org.junit.Assert.assertEquals(mu.getPos(),UnsafeMemory.getWriteUnsafeSize(me.getSequence(1),UnsafeMemory.DNABITSTRING_TYPE));
        mu.reset();
        mu = new UnsafeMemory(2*UnsafeMemory.getWriteUnsafeSize(me.getSequence(0),UnsafeMemory.DNABITSTRING_TYPE));
        me.getSequence(0).writeUnsafe(mu);
        org.junit.Assert.assertEquals(mu.getPos(),UnsafeMemory.getWriteUnsafeSize(me.getSequence(0),UnsafeMemory.DNABITSTRING_TYPE));


        System.err.println("Importing FastA file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Saving KidDatabaseMemory file\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        me.fileName = args[1]; //+".JavaSerialized";
        me.saveToFileUnsafe();
        System.err.println("Saving KidDatabaseMemory file -- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("Loading KidDatabaseMemory file\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        KidDatabaseMemory me2 = KidDatabaseMemory.loadFromFileUnsafe(args[1]);
        System.err.println("Loading KidDatabaseMemory file -- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        Assert.assertEquals(me.getName(1),me2.getName(1));
        try {
            org.junit.Assert.assertEquals(me.getSequence(1,50,100,false),me2.getSequence(1,50,100,false));
            org.junit.Assert.assertEquals(me.getSequence(1,900,1000,false),me2.getSequence(1,900,1000,false));
        } catch (Exception e) {
            e.printStackTrace();
        }
        org.junit.Assert.assertEquals(me.getMaxKid(), me2.getMaxKid());
        org.junit.Assert.assertEquals(me.getName(1),me2.getName(1));
        org.junit.Assert.assertEquals(me.contains("FAKE"), true);
        org.junit.Assert.assertEquals(me2.contains("FAKE"), true);
        org.junit.Assert.assertEquals(me.getMaxKid(), me2.getMaxKid());
        org.junit.Assert.assertEquals(me.getNextKid(), me2.getNextKid());
        org.junit.Assert.assertEquals(me.getSequenceLength(1),me2.getSequenceLength(1));
        org.junit.Assert.assertEquals(me.getWriteUnsafeSize(),me2.getWriteUnsafeSize());

        org.junit.Assert.assertEquals(me2.getSequence(2),null);
    }
}

