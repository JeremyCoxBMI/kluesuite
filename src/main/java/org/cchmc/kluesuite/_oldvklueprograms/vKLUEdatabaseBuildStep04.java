package org.cchmc.kluesuite._oldvklueprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite._oldclasses.Permutator;
import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.variantklue.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
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
 *              KidDatabaseMemory and VariantDatabase to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 *
 *
 * DEPRECATED
 */



public class vKLUEdatabaseBuildStep04 {


    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Takes an (unsafe) Variant Database and builds entries in (new) kmer database.");
            System.out.println("ARG 0 : location KidDatabaseMemory to read (unsafe)");
            System.out.println("ARG 1 : location variant database (unsafe) to read");
            System.out.println("ARG 2 : kmer database to be written (combine later with human genome non-varaint DB)");
            System.out.println("ARG 3 : number of max_adjacent variants written into database");
            System.out.println("ARG 4 : range of chromosomes to process (e.g.  1  or  8-10");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        Set<Integer> s = new HashSet<Integer>();//vd.getKeys();
        int max_adjacent=Integer.parseInt(args[3]);
        String[] range = args[4].split("-");
        if (range.length == 1){
            s.add(Integer.parseInt(range[0]));
        } else {
            for (int k = Integer.parseInt(range[0]); k <= Integer.parseInt(range[1]); k++){  //range is inclusive both ends
                s.add(k);
            }
        }


        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

//        //DEBUG BLOCK ALTERNATIVE to save MEMORY
//        KidDatabaseMemory kd = KidDatabaseMemoryNoDnaBitString.loadFromFileUnsafe(args[0]);
//
//        SuperString ss = new SuperString();
//        for (int k=0;k<1000; k++)   ss.addAndTrim("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//        kd.addAndTrim(new Kid("bogus"));
//        kd.storeSequence(1,ss,tt);
//
//        // END :: DEBUG BLOCK ALTERNATIVE FOR MEMORY

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[2],false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());



        Permutator.DEBUG = false;

        int count = 0;
        for (Integer kid : s) {
            System.err.println("Starting KID\t" + kid + "k processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
            PermutatorLimitAdjacent perm = new PermutatorLimitAdjacent(rocksklue, kd, vd);
            perm.MAX_ADJACENT = max_adjacent;

            VariantDatabaseMemoryIterator it = vd.iterator(1);
            StructPermutationStrings sps = perm.processVariants(it,1,0);
//            perm.processVariants();

            System.err.println("KID\t" + kid + "k processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

//            if (count % 10000 == 0) {
//                System.err.println("Variant\t" + count / 10000 + "\t nextOffset 10k processed" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
//                System.err.println("\t" + kd.getName(kid));
//                System.err.println("Permutations: ");

//                //how do I print them  -- function unwritten
//                System.err.println(perm.toString());
//            }
        }


        System.err.println("Import to kmer database complete: Last Variant was #\t"+count+"\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        rocksklue.shutDown();

    } //end main



}
