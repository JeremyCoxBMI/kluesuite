package org.cchmc.kluesuite._oldvklueprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.variantklue.StructPermutationStrings;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemoryIterator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/19/17.
 *
 * Imports a small test into memory and sees if we replicate problem on cluster.
 *
 */
public class vKLUEsmallTestStep04 {

    static int max_adjacent = 3;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("ARG 0 : source FA file");
            System.out.println("ARG 1 : source variants file");
            System.out.println("ARG 2 : klue database to use");
            exit(0);
        }



        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseMemory kd  = new KidDatabaseMemory(); //KidDatabaseMemory.loadFromFileUnsafe(args[0]);
        try {
            kd.importFNA(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory(args[1]+".vdm");
        try {
            vd.importValues(args[1],kd,"hg38");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[2],false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        Set<Integer> s = new HashSet<Integer>();

        s.add(1);
        int count = 0;
        for (Integer kid : s) {
            System.err.println("Starting KID\t" + kid + "k processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
            PermutatorLimitAdjacent perm = new PermutatorLimitAdjacent(rocksklue, kd, vd);
            perm.MAX_ADJACENT = max_adjacent;


            VariantDatabaseMemoryIterator it = vd.iterator(1);
            StructPermutationStrings sps = perm.processVariants(it,1,0);

            System.err.println("KID\t" + kid + "k processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        }

        }
}
