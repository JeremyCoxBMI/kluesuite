package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.Random;

/**
 * Created by jwc on 3/6/18.
 */
public class DebianRandomDatabaseTest {


    public static void main(String[] args) {

        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");


        String rocksdbfile = args[0];

        //default for testing is  1234567890L
        int seed = Integer.parseInt(args[1]);
        Random rand = new Random(seed);

        int numIterations = Integer.parseInt(args[2]);

        RocksDbKlue klue = new RocksDbKlue(rocksdbfile, true);

        System.out.print("Finished intializing; now loading keys\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        long[] keys = klue.getKeys();

        int numKeys = keys.length;

        ttRand.start();

        System.out.print("Loading keys complete; conducting test\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        for (int j=0; j < numIterations; j++){
            generateKey(rand, keys, numKeys);
        }

        System.out.print("Program Finished\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");
        ttRand.stop();
        System.out.print("\tRESULTS\n\tTime to access disk\t" + ttRand.toHMS());
        long nanoseconds = ttRand.timePassed();
        double x = new Double(nanoseconds) / 1000000000;
        x = numIterations / x;
        System.out.print("\tNumber of iterations\t" + numIterations);
        System.out.print("\tNanoseconds         \t" + nanoseconds);
        System.out.print("\tAccess Speed (Hz)   \t" + x);

    }

    /**
     * Generates 50% real and 50% fake keys
     * @param rand
     * @param keys
     * @param numKeys
     * @return
     */
    static long generateKey(Random rand, long[] keys, int numKeys){
        long result;

        int k = rand.nextInt(2*numKeys-1);

        if (k < numKeys){
            //real key
            result = keys[k];
        } else {
            int z = k - numKeys;
            //fake key
            result = (keys[z]+keys[z+1])/2;  //62 bit numbers make 63 bit numbers, still signed
        }

        return result;
    }


}
