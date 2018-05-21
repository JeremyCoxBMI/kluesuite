package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.helperclasses.Histogram;
import org.cchmc.kluesuite.helperclasses.MyInteger;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksIterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by jwc on 2018.03.06
 *
 * Take a RocksDbKlue and count the number of times a k-mer has x results
 *
 * Try a new implementation due to screwy results
 */
public class DatabaseFrequencyCount2 {

    public static void main(String[] args) {
        String rocksdbfile = args[0];
        String outputtext = args[1];

        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        Date timer = new Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS() + "\n");

        RocksDbKlue klue = new RocksDbKlue(rocksdbfile, true);

        System.out.print("Finished intializing; now counting \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");
        ttRand.start();

        //Map<Integer,Integer> freqTOcount = new HashMap<Integer,Integer>();

        Histogram hist = new Histogram();


        RocksIterator rit = klue.newIterator();

        Integer size;
        long c = 0;
        long periodicity = 1000000L;

        rit.seekToFirst();
        while (rit.isValid()) {

            size = rit.value().length / 8;

            hist.put(size);

            c++;
            if (c % (10 * periodicity) == 0) {
                System.err.println("\tnumber of entries processed (millions)\t" + c / periodicity);
            }

            rit.next();
        }
        System.out.println("\ttotal number of k-mers processed\t" + c);



        Iterator<MyInteger> it =  hist.freqIterator();


        long total=0;
        long count=0;
        try {

            FileWriter fw = new FileWriter(outputtext);
            BufferedWriter writer = new BufferedWriter(fw);

            System.out.println("# locations k-mer found\tfrequency");
            writer.write("# locations k-mer found\tfrequency\n");

            while (it.hasNext()) {
                MyInteger key = it.next();
                int ct = hist.get(key);
                System.out.println(key.toInt()+"\t"+ct);
                writer.write(key.toInt()+"\t"+ct+"\n");
                total += ((long) ct) * ((long) key.toInt());
                count += (long) ct;
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
