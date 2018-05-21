package org.cchmc.kluesuite._oldprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.sql.Timestamp;
import java.util.Iterator;

import static java.lang.System.exit;

/**
 * Created by osboxes on 6/7/17.
 */
public class vKLUEdatabaseBuildStep01UnsafeVerify {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Takes a FASTA dna fle and builds a DnaBitString and KidDatabaseMemory databases.");
            System.out.println("----Verifier program.----");
            System.out.println("ARG 0 : location to place KidDatabaseMemory database (Unsafe file)");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Loading database (unsafe)\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        KidDatabaseMemory meNoKryo = null;
//        meNoKryo = KidDatabaseMemory.loadFromFile(args[0]);//+".JavaSerialized");

        KidDatabaseMemory me = KidDatabaseMemory.loadFromFileUnsafe(args[0]);


        System.err.println("Loading Database (unsafe) -- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        System.err.println("Loading database (unsafe)(no dnabitstrings)\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        KidDatabaseMemory meNoKryo = null;
//        meNoKryo = KidDatabaseMemory.loadFromFile(args[0]);//+".JavaSerialized");

        KidDatabaseMemory me2 = KidDatabaseMemory.loadFromFileUnsafe(args[0]+".noDBS");

        System.err.println("Loading Database (unsafe)(no dns)-- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        String s= null;
        try {
            s = me.getSequence(1, 0, 100, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (s!= null) {
            System.err.println("Checking DNSbitString recall ::\n" + s);
        }

        System.err.println("Loading Database -- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        Iterator<String> it = me.nameIterator();
        System.out.println("\n***\nTesting Unsafe Loaded\n***\n");
        while (it.hasNext()){
            String name = it.next();
            System.out.println(">"+name);
            try {
                System.out.println(me.getSequence(me.getKid(name),0,50, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n***\nTesting Unsafe/NoDns Loaded\n***\n");
        it = me2.nameIterator();
        while (it.hasNext()){
            String name = it.next();
            System.out.println(">"+name);
            try {
                System.out.println(me.getSequence(me.getKid(name),0,50,false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
