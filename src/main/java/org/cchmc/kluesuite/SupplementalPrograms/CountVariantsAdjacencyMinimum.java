package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.variantklue.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/28/17.
 *
 * This program tells you on a given ref sequence, the number of Variants which have minimum adjacency (input number)
 */
public class CountVariantsAdjacencyMinimum {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("This program tells you on a given ref sequence, the number of Variants which have minimum adjacency");
            System.out.println("ARG 0 : location variant database (unsafe) to read");
            System.out.println("ARG 1 : range of kid to process (e.g.  1  or  8-10");
            exit(0);
        }


        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());



        Set<Integer> s = new HashSet<Integer>();//vd.getKeys();
        //int max_adjacent=Integer.parseInt(args[1]);
        String[] range = args[1].split("-");
        if (range.length == 1){
            s.add(Integer.parseInt(range[0]));
        } else {
            for (int k = Integer.parseInt(range[0]); k <= Integer.parseInt(range[1]); k++){  //range is inclusive both ends
                s.add(k);
            }
        }

        HashMap<Integer,Integer> countToFrequency = new HashMap<Integer,Integer>();
        countToFrequency.put(1,0);
        countToFrequency.put(2,0);
        countToFrequency.put(3,0);
        countToFrequency.put(4,0);
        countToFrequency.put(5,0);

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        int total = 0;

        System.err.println("\tBegin Counting.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("set of kid to process is : "+s);
        //int count = 0;
        for (Integer kid : s) {
            System.err.println("Starting permutations for KID\t" + kid + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
            //initialize

            VariantDatabaseMemoryIterator it = vd.iterator(kid);
            int stop, clustSize;

            Variant v = it.next();
            stop = PermutatorLimitAdjacent.calculateRefStop(v);
            clustSize = 1;

            while (it.hasNext()){

                v = it.next();  // always looking at next value
                if (PermutatorLimitAdjacent.insideRefStopRange(stop, v)) {
                    //continue cluster
                    clustSize++;
                    stop =  PermutatorLimitAdjacent.calculateRefStop(v);
                } else {
                    //record and new cluster
                    total += clustSize;
                    if (clustSize > 5) clustSize = 5;
                    int t = countToFrequency.get(clustSize);
                    countToFrequency.put(clustSize, t+1);
                    stop = PermutatorLimitAdjacent.calculateRefStop(v);
                    clustSize = 1;
                }

            }

            System.err.println("KID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        }

        System.err.println("size\t\tcount for cluster size\tcumulative V count");

        int vc = 0;

        for (int k=1; k<=5; k++){
            int t =countToFrequency.get(k);
            vc += k*t;
            String vc2 = Integer.toString(vc);
            if(k==5) vc2 = "";
            System.err.println(k+"\t==>\t"+t+"\t\t"+vc2);
        }
        System.err.println("\ttotal variants processed\t"+total);

        System.err.println("\tPROGRAM COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
    }
}
