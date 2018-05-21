package org.cchmc.kluesuite.mainprograms.OSC;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.io.FileNotFoundException;
import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by jwc on 2/6/18.
 */
public class OSCBuildKidDB {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes a FASTA dna fle and builds a DnaBitString and KidDatabaseMemory databases.");
            System.out.println("ARG 0 : location to place KidDatabaseMemory database");
            System.out.println("ARG 1+ : Fasta file(s)");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        KidDatabaseMemory me = new KidDatabaseMemory();

        System.err.println("Importing FastA file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        for (int k=1; k < args.length; k++) {
            try {
                System.out.println("\tFilename : " + args[k]);
                me.importFNA(args[k]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


        System.err.println("Importing FastA file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Saving KidDatabaseMemory file (Unsafe)\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        me.fileName = args[1]; //+".JavaSerialized";
        me.saveToFileUnsafe();

        System.err.println("PROGRAM COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Building/Saving KidDatabaseDisk (Unsafe) file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseDisk me2 =
                KidDatabaseDisk.builderKidDatabase128DBS_ShallowCopy(me, args[0]+".disk", args[0]+".disk.rocks");
        me2.setReadOnly(true);
        me2.saveToFileUnsafe();

        System.err.println("Saving KidDatabaseNoBitString file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());



    } //end main
}
