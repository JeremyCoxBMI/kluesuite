package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;


import java.io.FileNotFoundException;
import java.sql.Timestamp;


import static java.lang.System.exit;

/**
 * Created by osboxes on 6/7/17.
 *
 * Step01 in building a KLUE database
 *
 *
 * 1)  KidDatabaseMemory / DnaBitString database
 * 2)  Build normal K-mer database
 * 3)  Build VariantDatabaseOLD
 * 4)  Write Variants to K-mer database
 * 5)  Recompile K-mer in-order database and in-order 16 part databases
 * 6)  Per your option, convert
 *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 *
 *
 *
 *
 */
public class Step01KidDatabaseFromFasta {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes a FASTA dna fle and builds a DnaBitString and KidDatabaseMemory databases.");
            System.out.println("ARG 0 : Fasta file");
            System.out.println("ARG 1 : location to place KidDatabaseMemory database");
            exit(0);
        }

        //TODO: addWithTrim disk based KidDatabase

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        KidDatabaseMemory me = new KidDatabaseMemory();

        System.err.println("Importing FastA file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

//        KidDatabaseMemory.debug = true;
        //DnaBitStringKryoSerializer.debug = true;

        try {
            System.out.println("\tFilename : " + args[0]);
            me.importFNA(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.err.println("Importing FastA file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Saving KidDatabaseMemory file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        me.fileName = args[1]; //+".JavaSerialized";
//        me.saveToFile();
        me.saveToFileUnsafe();

        System.err.println("Saving KidDatabaseMemory file (Unsafe)-- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


//        System.err.println("Saving KidDatabaseMemoryNoDnaBitString (Unsafe) file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

//        KidDatabaseMemoryNoDnaBitString kndns = new KidDatabaseMemoryNoDnaBitString(me);
////        kndns.saveToFile();
//
//        kndns.saveToFileUnsafe();
//
//        System.err.println("Saving KidDatabaseNoBitString file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

    }
}
