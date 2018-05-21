package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Created by jwc on 4/27/18.
 */
public class DatabaseFrequencyExport {



    public static void main(String[] args) {
        String rocksdbfile = args[0];
        String outputtext = args[1];

        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS() + "\n");

        RocksDbKlue klue = new RocksDbKlue(rocksdbfile, true);

        System.out.print("Finished intializing; now counting \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS() + "\n");
        ttRand.start();

        //Map<Integer,Integer> freqTOcount = new HashMap<Integer,Integer>();

        try {

            FileWriter fw = new FileWriter(args[1]);
            BufferedWriter writer = new BufferedWriter(fw);
            RocksIterator rit = klue.newIterator();

            Integer size;
            long c = 0;
            long periodicity = 1000000L;

            rit.seekToFirst();
            while (rit.isValid()) {

                //OPTION 1
                size = rit.value().length / 8;

                //writer.write(new Long(RocksDbKlue.bytesToLong(rit.key()))+"\t"+size+"\n");
                writer.write(size+"\n");

                c++;
                if (c % (10 * periodicity) == 0) {
                    System.err.println("\tnumber of entries processed (millions)\t" + c / periodicity);
                }

                rit.next();
            }
            System.out.println("\ttotal number of k-mers processed\t" + c);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
