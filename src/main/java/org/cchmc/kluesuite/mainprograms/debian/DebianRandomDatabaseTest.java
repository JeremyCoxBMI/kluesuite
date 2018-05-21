package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.Random;

/**
 * Created by jwc on 3/6/18.
 */
public class DebianRandomDatabaseTest {


    public static void main(String[] args) {


        //TODO: build random storage indexes before building index, just save the ones you want  (save memory)

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

        System.out.print("Finished intializing; now loading keys\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        long[] keys = klue.getKeys();

        int numKeys = keys.length;

        ttRand.start();

        System.out.print("Loading keys complete; building test sets\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        long[] realValues = new long[numIterations];
        long[] fakeValues = new long[numIterations];


        for (int j=0; j < numIterations; j++){
            realValues[j] = generateKey(rand, keys, numKeys, true);
            fakeValues[j] = generateKey(rand, keys, numKeys, false);
        }


        System.out.print("Test set build, testing real values.\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        ttRand.start();
        for (int j=0; j < numIterations; j++) {
            klue.get(realValues[j]);
        }

        ttRand.stop();
        System.out.print("\tRESULTS\n\t\tReal Values\n\tTime to access disk\t" + ttRand.toHMS());
        long nanoseconds = ttRand.timePassed();
        double x = new Double(nanoseconds) / 1000000000;
        x = numIterations / x;
        System.out.print("\tNumber of iterations\t" + numIterations);
        System.out.print("\tNanoseconds         \t" + nanoseconds);
        System.out.print("\tAccess Speed (Hz)   \t" + x);

        ttRand = new TimeTotals();
        ttRand.start();
        for (int j=0; j < numIterations; j++) {
            klue.get(fakeValues[j]);
        }

        ttRand.stop();
        System.out.print("\tRESULTS\n\t\tFake Values\n\tTime to access disk\t" + ttRand.toHMS());
        nanoseconds = ttRand.timePassed();
        x = new Double(nanoseconds) / 1000000000;
        x = numIterations / x;
        System.out.print("\tNumber of iterations\t" + numIterations);
        System.out.print("\tNanoseconds         \t" + nanoseconds);
        System.out.print("\tSeconds             \t" + new Double(nanoseconds) / 1000000000);
        System.out.print("\tAccess Speed (Hz)   \t" + x);




        System.out.print("Program Finished\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

    }

    /**
     * Generates 50% real and 50% fake keys
     *
     * Strange boundary checking because human alone has 1.5 billion keys, int goes up to 2.1 billion
     * @param rand
     * @param keys
     * @param numKeys
     * @return
     */
    static long generateKey(Random rand, long[] keys, int numKeys, boolean real){
        long result;

                int k = rand.nextInt(numKeys);

        if (real){
            //real key
            result = keys[k];
        } else {
            if (k == numKeys-1) {
                k--; //if on last position, use next to last to stay in boundaries
            }
            //int z = k - numKeys;
            //fake key
            result = (keys[k]+keys[k+1])/2;  //62 bit numbers make 63 bit numbers, still signed

        }

        return result;
    }


}
