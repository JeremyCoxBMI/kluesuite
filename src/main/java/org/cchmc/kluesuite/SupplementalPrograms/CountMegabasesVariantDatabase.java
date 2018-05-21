package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemoryIterator;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/19/17.
 */
public class CountMegabasesVariantDatabase {

    static int PERIOD = 1000 * 1000; //MEGABASE

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes an (unsafe) Variant Database and counts entries in MegaBase buckets.");

            System.out.println("ARG 0 : location variant database (unsafe) to read");
            System.out.println("ARG 1 : location KidDatabaseDisk to read (unsafe)");

            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Loading KidDb Disk\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//        KidDatabaseMemory kd = KidDatabaseMemory.loadFromFileUnsafe(args[1]);
        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(args[1]);
        System.err.println("Loading KidDb Disk Complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        //Mapping Megabase positions to count
        List<TreeMap<Integer, Integer>> trees;
        trees = new ArrayList();
        trees.add(null);  //space 0 (kid=0) is nothing

        //MEGABASE BUCKETS
        //for each chromosome
        for (int k=1; k<=25; k++) {
            System.out.println("****\nChromosome "+kd.getName(k)+"\n****");
            trees.add(new TreeMap<Integer,Integer>());
            int currMegabase = 0;
            int total = 0;
            VariantDatabaseMemoryIterator it = vd.iterator(k);
            Variant curr;
            while (it.hasNext()) {
                curr = it.next();  //in order
                if (curr.start >= (currMegabase+1)*PERIOD){
                    trees.get(k).put(currMegabase,total);
                    System.out.println(currMegabase+"\t"+total);
                    currMegabase += 1;
                    total=0;
                }
                total++;
            }
        }

    }
}
