package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.helperclasses.MinHeapIntegersByArray;
import org.cchmc.kluesuite.helperclasses.MinHeapLongsByArray;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by jwc on 3/6/18.
 */
public class DebianRandomDatabaseTestAllDrives {

    public static String[] drives = {
            "/data/1/nvme/hg38.klue.final",
            "/data/1/b/hg38.klue.final",
            "/data/1/f/hg38.klue.final",
            "/data/3/d/hg38.klue.final",
            "/data/5/c/hg38.klue.final"
    };

    public final static long seed = 1234567890L;

    public final static int reportA =              100 * 1000;
    public final static int reportB =             1000 * 1000;
    public final static int reportC =        10 * 1000 * 1000;
//    public final static int numIterations =  10 * 1000 * 1000 + 1;
    public final static int numIterations =        100 * 1000 + 1;

    public static LogStream stdout;
    public static LogStream stderr;

    public static void main(String[] args) throws IOException {


        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        String stamp = (new Timestamp(System.currentTimeMillis())).toString();
        stamp = stamp.replace(' ','_');
        stdout = LogStream.MessageClassBuilder(stamp+".logfile.out.txt", System.out);
        stderr = LogStream.MessageClassBuilder(stamp+".logfile.err.txt", System.err);


        stdout.println(  tt.toHMS() + "\tSynchronize time systems\t" + new Timestamp(System.currentTimeMillis())  );
        //stdout.println(tt.toHMS()+"\t\t" + new Timestamp(System.currentTimeMillis()));

        Random rand = new Random(seed);

        RocksDbKlue klue = new RocksDbKlue(drives[0], true);

        stdout.println(tt.toHMS()+"\tFinish Initialization.  Counting keys\t" + new Timestamp(System.currentTimeMillis()));

        int numKeys = klue.countKeys();

        stdout.println(tt.toHMS()+"\tGenerating random indexes to pull; sorting\t" + new Timestamp(System.currentTimeMillis()));

        HashSet<Integer> indexes = new HashSet<Integer>(numIterations);

        for (int j=0; j < numIterations+1; j++){

            indexes.add(rand.nextInt(numKeys));
        }


        Iterator<Integer> it = indexes.iterator();
//        stdout.println("DEBUG :: "+it.next()+"\t"+it.next()+"\t"+it.next()+"\t"+it.next());

        stdout.println(tt.toHMS()+"\tFinding keys mapped to indexes.\t" + new Timestamp(System.currentTimeMillis()));

        long[] realValues;
        realValues = klue.getValuesAtIndexes(indexes);
        klue.shutDown();

        stdout.println(tt.toHMS()+"\tReal values extracted by Indexes; building fake values\t" + new Timestamp(System.currentTimeMillis()));

        //repeats are eliminated, hence the array is not full
        //long[] sortedReal = new long[realValues.length]

        MinHeapLongsByArray mhiba = new MinHeapLongsByArray(realValues.length);
        long[] fakeValues = new long[realValues.length-1];
        for (int k=0; k < realValues.length; k++){
            mhiba.append(realValues[k]);
        }

        mhiba.heapify();
        long prev, curr;
        prev = mhiba.remove();
        for (int k=1; k < realValues.length; k++){
            if(mhiba.hasNext())
                curr = mhiba.remove();
            else
                break;
            fakeValues[k-1]= (curr+prev)/2;
            prev = curr;
        }

        //In order, randomize
        //shuffleArray(fakeValues, rand );

        stdout.println("Real, sorted");
        printArray(realValues,5);
        stdout.println("Fake, sorted");
        printArray(fakeValues,5);

        //realValues and fakeValues now IN ORDER
        stdout.println(tt.toHMS()+"\tRandomize Real Values\t" + new Timestamp(System.currentTimeMillis()));
        shuffleArray(realValues, rand );
        stdout.println(tt.toHMS()+"\tRandomize Fake Values\t" + new Timestamp(System.currentTimeMillis()));
        shuffleArray(fakeValues, rand );
//
        stdout.println("Real, randomized");
        printArray(realValues,5);
        stdout.println("Fake, randomized");
        printArray(fakeValues,5);



        stdout.print("\n\n");
        stdout.println(tt.toHMS()+"\tTesting all databases with randomized keys\t" + new Timestamp(System.currentTimeMillis()));
        stdout.println(Arrays.toString(drives));

        double x;
        long nanoseconds;


        for (String file : drives ){

            stdout.print("\n\t");
            stdout.println(tt.toHMS()+"\tstarting with\t"+file+"\t" + new Timestamp(System.currentTimeMillis()));

            klue = new RocksDbKlue(file, true);

            stdout.println("\t\tRESULTS (IOPS)");
            stdout.println("\t\t\tReal Values");

            ttRand = new TimeTotals();
            ttRand.start();
            for (int j=0; j < realValues.length; j++) {
                klue.get(realValues[j]);

                if (j == reportA || j == reportB || j == reportC){
                    x = new Double(j) /
                            ttRand.timePassedFromStart()
                            * 1000000000;

                    stdout.println("\t\t\tafter "+j+" iterations\tAccess Speed (Hz)   \t" + x);
                }
            }

            ttRand.stop();

            stdout.print("\t\t\t");

            nanoseconds = ttRand.timePassedFromStart();
            x = new Double(realValues.length) /
                    nanoseconds
                    * 1000000000;

            stdout.println("\t\t\tNumber of iterations\t" + realValues.length);
            stdout.println("\t\t\tNanoseconds         \t" + nanoseconds);
            stdout.println("\t\t\tSeconds             \t" + new Double(nanoseconds) / 1000000000);
            stdout.println("\t\t\tAccess Speed (Hz)   \t" + x);

            stdout.println("\n\t\t\tFake Values");

            ttRand = new TimeTotals();
            ttRand.start();
            for (int j=0; j < fakeValues.length; j++) {
                klue.get(fakeValues[j]);
                if (j == reportA || j == reportB || j == reportC){
                    x = new Double(j) /
                            ttRand.timePassedFromStart()
                            * 1000000000;
                    stdout.println("\t\t\tafter "+j+" iterations\tAccess Speed (Hz)   \t" + x);
                }
            }

            ttRand.stop();

            stdout.println(tt.toHMS()+"\t\t" + new Timestamp(System.currentTimeMillis()));
            nanoseconds = ttRand.timePassedFromStart();
            x = new Double(fakeValues.length) /
                    nanoseconds
                    * 1000000000;

            stdout.println("\t\t\tNumber of iterations\t" + fakeValues.length);
            stdout.println("\t\t\tNanoseconds         \t" + nanoseconds);
            stdout.println("\t\t\tSeconds             \t" + new Double(nanoseconds) / 1000000000);
            stdout.println("\t\t\tAccess Speed (Hz)   \t" + x);

        }

        klue.shutDown();
        stdout.println(tt.toHMS()+"\tProgram Finished\t" + new Timestamp(System.currentTimeMillis()));
    }

    private static void printArray(long[] arr, int m) throws IOException {
        SuperString ss = new SuperString();

        ss.add('{');
        ss.add(Long.toString(arr[0]));
        for(int k=1; k<m;k++){
            ss.add(',');
            ss.add(Long.toString(arr[k]));
        }
        ss.add(" ... ");
        for (int k = arr.length-m; k<arr.length; k++){
            ss.add(',');
            ss.add(Long.toString(arr[k]));
        }
        ss.add('}');

        stdout.println(ss.toString());

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

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(long[] ar, Random rnd)
    {
        long a;
        int index;
        for (int i = ar.length - 1; i > 0; i--) {
            index = rnd.nextInt(i);
            // Simple swap
            a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

}
