package org.cchmc.kluesuite._oldprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite._oldclasses.Permutator;
import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.memoryklue.KVpair;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.variantklue.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/21/17.
 *
 * Load previous K-mer database see if values are correct
 */
public class vKLUEdatabaseBuildStep04BVerify {

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Takes an (unsafe) Variant Database and builds entries in (new) kmer database.");
            System.out.println("ARG 0 : location KidDatabaseMemory to read (unsafe)");
            System.out.println("ARG 1 : location variant database (unsafe) to read");
            System.out.println("ARG 2 : kmer database to be written (combine later with human genome non-varaint DB)");
            System.out.println("ARG 3 : number of max_adjacent variants written into database");
            System.out.println("ARG 4 : range of kid to process (e.g.  1  or  8-10");
            System.out.println("ARG 5 : size of MemoryKlue (in millions)");
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

        int MK_SIZE = Integer.parseInt(args[5])*1000*1000;


        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

//        //DEBUG BLOCK ALTERNATIVE to save MEMORY
//        KidDatabaseMemory kd = KidDatabaseMemoryNoDnaBitString.loadFromFileUnsafe(args[0]);
//
//        SuperString ss = new SuperString();
//        for (int k=0;k<1000; k++)   ss.addWithTrim("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//        kd.addWithTrim(new Kid("bogus"));
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


        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray(MK_SIZE );


        Permutator.DEBUG = false;

        //int count = 0;
        for (Integer kid : s) {
            System.err.println("Starting KID\t" + kid + "k processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
            PermutatorLimitAdjacent perm = new PermutatorLimitAdjacent(hpkfia, kd, vd);
            perm.MAX_ADJACENT = max_adjacent;

            VariantDatabaseMemoryIterator it = vd.iterator(1);
            StructPermutationStrings sps = perm.processVariants(it,1,0);

            System.err.println("KID\t" + kid + "k processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        }

        hpkfia.heapify();

        System.out.println("Start export to RocksDbKlue \t"+tt.toHMS());
        PositionList pl= new PositionList();
        KVpair t;// = hpkfia.remove();

        //prev will not match first read, using Sentinel=-5
        KVpair prev = new KVpair(-5, null);

        int lastSize = 101;

        int k=0;
        while (hpkfia.hasNext()){
            t = hpkfia.remove();

            //System.err.println("Next key pair\t"+t);//debug

            if (t.key != prev.key) { //first time through, will not match, auto-written; should never match
                //bug never happens, don't waste time
//                pl = new PositionList(t.value);

                //rocksklue.put(t.key, t.value);


                // *************************
                //DEBUG  CHECK

                ArrayList<Long> arr = rocksklue.get(t.key);
                boolean check = true;
                for (int z=0; z < t.value.size(); z++){
                    if (!arr.get(z).equals(t.value.get(z)))   check = false;
                }
                if (!check) {
                    System.out.println("Value written NOT successfully.");
                }
                //System.out.println(prev.key + "\t" + Arrays.toString(pl.toLongArray()));
                // *************************


                k++;
                //if (k % (maxSize/10) == 0) System.out.println("\t\texport progress: "+k/1000.0/1000.0+" million\t"+writeTime);
            } else {
                System.err.println("WARNING  Found identical key in new memoryklue.remove().");
                //pl.addWithTrim( t.value );
            }
            if (hpkfia.percentageRemaining() < lastSize){
                System.out.println("Heap is now "+hpkfia.percentageRemaining()+"% full.\t"+tt.toHMS());
                lastSize = hpkfia.percentageRemaining();
            }

            prev = t;
        }

        System.err.println("Import to kmer database complete: Last Variant was #\t"+k+"\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        rocksklue.shutDown();
        System.out.println("Complete export to RocksDbKlue \t"+tt.toHMS());

    } //end main

}
