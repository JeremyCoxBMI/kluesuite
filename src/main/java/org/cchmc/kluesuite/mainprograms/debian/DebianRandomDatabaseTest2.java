package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.Random;

/**
 * Created by jwc on 3/6/18.
 *
 * Tests with purely random keys
 */
public class DebianRandomDatabaseTest2 {

    static long twoE62 = 1L << 62;

    public static void main(String[] args) {

        String rocksdbfile = args[0];
        //default for testing is  1234567890
        long seed = Long.parseLong(args[1]);
        int numIterations = Integer.parseInt(args[2]);



        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");




        Random rand = new Random(seed);



        RocksDbKlue klue = new RocksDbKlue(rocksdbfile, true);

        System.out.print("Finished intializing\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

//        long[] keys = klue.getKeys();
//
//        int numKeys = keys.length;

        ttRand.start();

        System.out.print("Conducting test\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        for (int j=0; j < numIterations; j++){
            long x = generateKey(rand);  //create variable as would real program
            klue.get(x);
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

    private static long generateKey(Random rand) {

        long x = rand.nextLong();
        if (x < 0){
            x *= -1;
        }
        return x % twoE62;

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
