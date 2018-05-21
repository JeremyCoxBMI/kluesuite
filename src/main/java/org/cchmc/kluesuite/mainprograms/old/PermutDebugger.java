package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite._oldclasses.Permutator;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by osboxes on 09/05/17.
 */
public class PermutDebugger {


    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Takes a UCSC variants file and builds entries in kmer database, as well as Variant database.");
            System.out.println("ARG 0 : location to KidDatabaseMemory");
            System.out.println("ARG 1 : location variant database to build");
            System.out.println("ARG 2 : UCSC variant definitions file");
            System.out.println("ARG 3 : prefix for chromosome names (if used in FastA file)");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

//        KidDatabaseMemory kd = new KidDatabaseMemory();
//        kd.addWithTrim(new Kid("hg38chr1"));


        System.err.println("\tImporting Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory(args[1]);
        try {
            vd.importValues(args[2],kd,args[3]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());



        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[1]+".deleteme",false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        Set<Integer> s = vd.getKeys();

        Permutator.DEBUG = false;

        Permutator perm = new Permutator(rocksklue, kd, vd);

        perm.processVariants(1, 0);

        rocksklue.shutDown();

    } //end main



}
