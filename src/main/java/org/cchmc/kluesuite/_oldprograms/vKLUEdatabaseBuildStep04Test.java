package org.cchmc.kluesuite._oldprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseIterator;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by osboxes on 24/04/17.
 *
 * REMAKE of HumanVariantDatabaseBuildStep0X  series
 * Using new classes, new strategies.  Starting over from scratch.
 *  -- using Kryo
 *  -- using memory only objects except for kmer
 *  -- not including many human sequences with "_" in name
 *
 * 1)  KidDatabaseMemory / DnaBitString database
 * 2)  Build normal K-mer database
 * 3)  Build VariantDatabaseOLD
 * 4)  Write Variants to K-mer database
 * 5)  Recompile K-mer in-order database and in-order 16 part databases
 * 6)  Per your option, convert
 *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 */



public class vKLUEdatabaseBuildStep04Test {


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Takes a UCSC variants file and builds entries in kmer database, as well as Variant database.");
            System.out.println("ARG 0 : location KidDatabaseMemory to read");
            System.out.println("ARG 1 : location variant database (Memory version) to read");
            System.out.println("ARG 2 : kmer database to be updated");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


//        VariantDatabase1 vd = new VariantDatabase1();
        System.err.println("Loading KidDb   (NOT IN TEST)\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFile(args[0]);

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[1]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        System.err.println("\tInitializing KLUE  (NOT IN TEST)\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//        RocksDbKlue rocksklue = new RocksDbKlue(args[2],false); // false = read and write

        VariantDatabaseIterator<Variant> it = vd.iterator(1);
        if (it.hasNext()) {
            System.out.println("Iterator first value :\n" + it.next());
        } else {
            System.out.println("WARNING!!!! Iterator has NO first value :\n");
        }

        Set<Integer> s = vd.getKeys();

        int count = 0;

    } //end main



}
