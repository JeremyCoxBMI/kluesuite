package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by jwc on 2/7/18.
 *
 * Debian01:  build a single KID database
 * Debian02:  build multiple database parts in parallel
 * Debian03:  build database parts -- 16.  (even if it all fits on one drive; consistency across testing is important)
 *          NOTE THERE WILL BE multiple parts of START_END database to manage
 */
public class Debian01BuikdKidDbDisk_A_names_only {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Takes a FASTA dna fle and builds a DnaBitString and KidDatabaseMemory databases.");
            System.out.println("ARG 0  : location to place KidDatabaseDisk database (masterfile) ==> directory determined by masterfile name");
            System.out.println("ARG 1+ : Fasta file(s)");
            System.out.println("");
            System.out.println("Your arguments were:  length = "+args.length);
            System.out.println(Arrays.toString(args));
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        //suppress annoying messages
        KidDatabaseMemory.squelch = true;
        KidDatabaseDisk me = new KidDatabaseDisk(args[0],args[0]+".disk",false);  //new in write mode

        System.err.println("Importing FastA file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        for (int k=1; k < args.length; k++) {
            try {
                System.out.println("\tFilename : " + args[k]);
                //TODO parse file once to get size ;; gotta add +1 because 0 is reserved
                me.importFnaNoSequencesStored(args[k], 7850222 + 5);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.err.println("Importing FastA file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Building/Saving KidDatabaseDisk (Unsafe) file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        me.setReadOnly(true);
        me.saveToFileUnsafe();

        System.err.println("Saving KidDatabaseDisk file -- COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("PROGRAM COMPLETE\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("There are a total of "+me.getLast()+" sequences stored as KID");

        me.shutDown();

    } //end main

} //end class
