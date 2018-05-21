package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.IOException;
import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by osboxes on 20/04/17.
 *
 * REMAKE of HumanVariantDatabaseBuildStep0X  series
 * Using new classes, new strategies.  Starting over from scratch.
 *  -- using Kryo
 *  -- using memory only objects except for kmer
 *  -- not including many human sequences with "_" in name
 *
 * 1)  KidDatabaseMemory / DnaBitString database
 * 2)  Build normal K-mer database
 * 3)  Build VariantDatabaseMemory
 * 4)  Write Variants to K-mer database
 * 5)  Recompile K-mer in-order database and in-order 16 part databases
 * 6)  Per your option, convert
 *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 */



public class Step03BuildVariantDatabaseMemory {

    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Takes a UCSC variants file and builds entries Variant database.");
            System.out.println("ARG 0 : location to Kid Database Disk");
            System.out.println("ARG 1 : location Variant Database Memory to build");
            System.out.println("ARG 2 : UCSC variant definitions file");
            System.out.println("ARG 3 : prefix for chromosome names (if used in FastA file)");
            //TODO remove
            System.out.println("ARG 4 : (Override) location to Kid Database Disk Database");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        VariantDatabaseMemory vd = new VariantDatabaseMemory(args[1]); //  + ".vd3");
        System.err.println("Loading Kid Database Disk\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        KidDatabaseMemory rkd;
//        rkd = (KidDatabaseMemory) KidDatabaseMemoryNoDnaBitString.loadFromFileUnsafe(args[0]);
        KidDatabaseDisk rkd = KidDatabaseDisk.loadFromFileUnsafe(args[0],args[4]);
        // rkd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);


        String prefix = args[3];


        System.err.println("Accepts only UCSC Variant database format.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tImporting VariantDatabaseMemory from file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        try {
            vd.importValues(args[2], rkd, prefix);
            System.err.println("");
            System.err.println("\t\tsnpMap has "+vd.snpMap.keySet().size()+" entries.");
            System.err.println("\t\t\tsnpMap KID list");
            for (int k : vd.snpMap.keySet()){
                System.err.println("\t\t\t"+k+"\t\tPosition entries : "+vd.snpMap.get(k).keySet().size());
            }
            System.err.println("\t\tindelMap has "+vd.indelMap.keySet().size()+" entries.");
            System.err.println("\t\t\tindelMap KID list");
            for (int k : vd.indelMap.keySet()){
                System.err.println("\t\t\t"+k+"\t\tPosition entries : "+vd.indelMap.get(k).keySet().size());
            }
//            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has "+vd.snpMap.get(1));
//            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has keys :"+vd.snpMap.get(1).keySet());
//            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has "+vd.snpMap.get(1).keySet().size()+" keys.");

//            TreeMap<Integer, Variant[]> temp = vd.snpMap.get(1);
//            Iterator<Integer> it = temp.keySet().iterator();
//
//            Integer nextOffset = it.next();
//            Integer y = temp.get(nextOffset).length;
//            System.out.println("\t\t\tAt position " + nextOffset + ", there are # Variants = " + temp.get(nextOffset).length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("\tImporting Variant Database Memory complete.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tSaving Variant Database Memory file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("\tSaving Variant Database Memory file : \t"+vd.getFilename());
        try {
            vd.saveToFileUnsafe();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("\tSave Variant Database Memory file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());



    } //end main
}
