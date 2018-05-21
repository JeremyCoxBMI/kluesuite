package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by jwc on 2018.03.06
 *
 * Take a RocksDbKlue and count the number of times a k-mer has nextOffset results
 */
public class DatabaseFrequencyCount {

    public static void main(String[] args) {
        String rocksdbfile = args[0];
        String outputtext = args[1];

        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS() + "\n");

        RocksDbKlue klue = new RocksDbKlue(rocksdbfile, true);

        System.out.print("Finished intializing; now counting \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");
        ttRand.start();

        //Map<Integer,Integer> freqTOcount = new HashMap<Integer,Integer>();

        Map<Integer,Integer> freqTOcount = klue.countFrequency();

        System.out.print("Finished counting; now output \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

//        // NOT NECESSARY
//        System.err.println("\tnumber of entries processed\t"+freqTOcount.keySet().size());
//        ArrayList<Integer> keys = new ArrayList(freqTOcount.keySet());
//        Collections.sort(keys);

        // /SETS
        Set<Integer> keys = freqTOcount.keySet();

        long total=0;
        long count=0;
        try {

            FileWriter fw = new FileWriter(outputtext);
            BufferedWriter writer = new BufferedWriter(fw);

            System.out.println("# locations k-mer found\tfrequency");
            writer.write("# locations k-mer found\tfrequency\n");

            for (Integer key : keys){
                System.out.println(key+"\t"+freqTOcount.get(key));
                writer.write(key+"\t"+freqTOcount.get(key)+"\n");
                total += (long) freqTOcount.get(key) * ((long)key);
                count += (long) freqTOcount.get(key);
            }

            writer.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Total number of k-mers reported by histogram\t"+count);
        System.out.println("Total number of positions reported by histogram\t"+total);

        System.out.print("Finished\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

    }

}
