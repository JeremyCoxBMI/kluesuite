package org.cchmc.kluesuite._oldprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemoryIterator;
import org.junit.Assert;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

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



public class vKLUEdatabaseBuildStep03Verify {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Takes a UCSC variants file and builds entries in kmer database, as well as Variant database.");
            System.out.println("ARG 0 : location to KidDatabaseDisk (Unsafe)(noDBS)");
            System.out.println("ARG 1 : location variant database to verify (Unsafe)");
            System.out.println("ARG 2 : UCSC variant definitions file");
            System.out.println("ARG 3 : prefix for chromosome names (if used in FastA file)");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        VariantDatabaseMemory vd = new VariantDatabaseMemory(args[1]); //  + ".vd3");
        System.err.println("Loading KidDb (Unsafe)(noDBS)\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseDisk rkd = KidDatabaseDisk.loadFromFileUnsafe(args[0]);
//        rkd = (KidDatabaseMemory) KidDatabaseMemoryNoDnaBitString.loadFromFileUnsafe(args[0]);
        // rkd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);


        String prefix = args[3];


        System.err.println("Accepts only UCSC Variant database format.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tImporting VariantDatabaseMemory from file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        try {
            vd.importValues(args[2], rkd, prefix);
//            System.err.println("");
//            System.err.println("\t\tsnpMap has "+vd.snpMap.keySet().size()+" entries.");
//            System.err.println("\t\t\tsnpMap KID list");
//            for (int k : vd.snpMap.keySet()){
//                System.err.println("\t\t\t"+k+"\t\tPosition entries : "+vd.snpMap.get(k).keySet().size());
//            }
//            System.err.println("\t\tindelMap has "+vd.indelMap.keySet().size()+" entries.");
//            System.err.println("\t\t\tindelMap KID list");
//            for (int k : vd.indelMap.keySet()){
//                System.err.println("\t\t\t"+k+"\t\tPosition entries : "+vd.indelMap.get(k).keySet().size());
//            }
////            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has "+vd.snpMap.get(1));
////            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has keys :"+vd.snpMap.get(1).keySet());
//            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has "+vd.snpMap.get(1).keySet().size()+" keys.");

            TreeMap<Integer, Variant[]> temp = vd.snpMap.get(1);
            Iterator<Integer> it = temp.keySet().iterator();

            Integer x = it.next();
            Integer y = temp.get(x).length;
            System.out.println("\t\t\tAt position " + x + ", there are # Variants = " + temp.get(x).length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("\tImporting VariantDatabaseMemory complete.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tLoading VariantDatabaseMemory (unsafe) file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("\tLoading VariantDatabaseMemory (unsafe) file : \t"+vd.getFilename());
        VariantDatabaseMemory vd2 = new VariantDatabaseMemory();

        try {
            vd2.loadFromFileUnsafe(args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("\tLoading VariantDatabaseMemory file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        Set<Integer> keys = vd.getKeys();

        for (int kid : keys) {
            VariantDatabaseMemoryIterator it = vd.iterator(kid);
            VariantDatabaseMemoryIterator it2 = vd2.iterator(kid);
            while(it.hasNext()){
                Variant next1 = it.next();
                Variant next2 = it2.next();
                Assert.assertTrue(next1.equals(next2));
            }
        }



    } //end main
}
