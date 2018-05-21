package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.helperclasses.MinHeapLongsByArray;
import org.cchmc.kluesuite.klat.KmerSequence;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.multifilerocksdbklue.Rocks16Klue;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by jwc on 3/9/18.
 */
public class DebianKlue16SpeedTests {

    public static  LogStream stdout;
    public static  LogStream stderr;

    public final static long seed = 1234567890L;

    public final static int reportA =              100 * 1000;
    public final static int reportB =             1000 * 1000;
    public final static int reportC =        10 * 1000 * 1000;
    public static int numIterations =  10 * 1000 * 1000 + 1;

    public final static String klue16file = "/data/1/nvme/klue16.txt";

    public static void main(String[] args) throws Exception {


        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();

        String stamp = (new Timestamp(System.currentTimeMillis())).toString();
        stamp = stamp.replace(' ','_');
        //stamp = stamp.replace(':','-');
        LogStream.startStdStreams("/home/jwc/workspace/logfiles/"+stamp+".");

        stdout = LogStream.getStdout();
        stderr = LogStream.getStderr();


        stdout.println(  tt.toHMS() + "\tSynchronize time systems\t" + new Timestamp(System.currentTimeMillis())  );


        String db16 = "/data/1/nvme/hg38.klue16.final";
        String db = "/data/1/nvme/hg38.klue.final";
        String kidDB = "/data/1/f/hg38.2018.02.12/hg38.KidDB.disk";


        String[] files16 = new String[16];

        try(BufferedReader br = new BufferedReader(new FileReader(klue16file))) {
            int k=0;
            for(String line; (line = br.readLine()) != null; ) {
                line = line.trim();
                files16[k] = line;
                k++;
                if (k==16) break;   //prevents white space on lines past first 16 from crashing program
            }

        }


        Rocks16Klue klue16 = new Rocks16Klue(klue16file, files16 ,true);
        RocksDbKlue klue = new RocksDbKlue(db, true);







        Random rand = new Random(seed);


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
//        shuffleArray(fakeValues, rand );

        stdout.println("Real, sorted");
        printArray(realValues,5);
        stdout.println("Fake, sorted");
        printArray(fakeValues,5);
//
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


        double x;
        long nanoseconds;




        stdout.print("\n\t10 million random reads");
        stdout.println(tt.toHMS()+"\tstarting with\t"+db16+"\t" + new Timestamp(System.currentTimeMillis()));

//            klue = new RocksDbKlue(file, true);

//        stdout.println("\t\tRESULTS (IOPS)");
//        stdout.println("\t\t\tReal Values");
//
//        ttRand = new TimeTotals();
//        ttRand.start();
//        for (int j=0; j < realValues.length; j++) {
//            klue16.get(realValues[j]);
//
//            if (j == reportA || j == reportB || j == reportC){
//                nextOffset = new Double(j) /
//                        ttRand.timePassedFromStart()
//                        * 1000000000;
//
//                stdout.println("\t\t\tafter "+j+" iterations\tAccess Speed (Hz)   \t" + nextOffset);
//            }
//        }
//
//        ttRand.stop();
//
//        stdout.print("\t\t\t");
//
//        nanoseconds = ttRand.timePassedFromStart();
//        nextOffset = new Double(realValues.length) /
//                nanoseconds
//                * 1000000000;
//
//        stdout.println("\t\t\tNumber of iterations\t" + realValues.length);
//        stdout.println("\t\t\tNanoseconds         \t" + nanoseconds);
//        stdout.println("\t\t\tSeconds             \t" + new Double(nanoseconds) / 1000000000);
//        stdout.println("\t\t\tAccess Speed (Hz)   \t" + nextOffset);

        stdout.println("\n\t\t\tFake Values");

        ttRand = new TimeTotals();
        ttRand.start();
        for (int j=0; j < fakeValues.length; j++) {
            klue16.get(fakeValues[j]);
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


        //#####################
        //TEST 2 :: sequential
        //##################

        //Create Sorted Indexes
        mhiba = new MinHeapLongsByArray(realValues.length);



        fakeValues = new long[realValues.length-1];
        for (int k=0; k < realValues.length; k++){
            mhiba.append(realValues[k]);
        }

        mhiba.heapify();
        prev = mhiba.remove();
        realValues[0] = prev;
        for (int k=1; k < realValues.length; k++){
            if(mhiba.hasNext()) {
                curr = mhiba.remove();
                realValues[k] = curr;
            }
            else {
                break;
            }
            fakeValues[k-1]= (curr+prev)/2;
            prev = curr;
        }

        stdout.println("Real, sorted");
        printArray(realValues,5);
        stdout.println("Fake, sorted");
        printArray(fakeValues,5);


        stdout.print("\n\t10M sequential reads");
        stdout.println(tt.toHMS()+"\tstarting with\t"+db16+"\t" + new Timestamp(System.currentTimeMillis()));

//            klue = new RocksDbKlue(file, true);

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

        //#####################
        //TEST 3 :: byKidDB
        //##################

        numIterations = 14300;
        stdout.println(tt.toHMS()+"\tOpening/Loading KidDatabaseDisk\t" + new Timestamp(System.currentTimeMillis()));

        //stdout.println(tt.toHMS()+"\t\t" + new Timestamp(System.currentTimeMillis()));
        rand = new Random(seed);
        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(kidDB);

        stdout.println(tt.toHMS()+"\tFinish Initialization.  Creating random Strings to look-up\t" + new Timestamp(System.currentTimeMillis()));

        ArrayList<ArrayList<Long>> randomLookups = new ArrayList<ArrayList<Long>>(numIterations);

        int numKid = kd.getMaxKid() - 1;//kid starts at 0, but 1 is first correct value


        stdout.println(tt.toHMS()+"\tGenerating Random Sequences/K-mer look-up sets\t" + new Timestamp(System.currentTimeMillis()));
        for (int k=0; k<numIterations;k++){
            int kid = rand.nextInt(numKid) + 1; //kid starts at 0, but 1 is first correct value
            int length = kd.getSequenceLength(kid);
            int start = rand.nextInt(length - 100);
            String sequence = kd.getSequence(kid,start,start+100,false);
            KmerSequence result = new KmerSequence(sequence);
            randomLookups.add(  result.getAllForwardArrayList()  );
        }



        stdout.print("\n\n");
        stdout.println(tt.toHMS()+"\tTesting all databases with random substrings\t" + new Timestamp(System.currentTimeMillis()));

        stdout.print("\nRandom real human subsequences\t");
        stdout.println(tt.toHMS()+"\tstarting with\t"+db16+"\t" + new Timestamp(System.currentTimeMillis()));


        stdout.println("\t\tRESULTS (IOPS)");

        ttRand = new TimeTotals();
        ttRand.start();
        for (int j=0; j < randomLookups.size(); j++) {
            klue.getAllPL(randomLookups.get(j));
        }

        ttRand.stop();

//            stdout.print("\t\t\t");

        nanoseconds = ttRand.timePassedFromStart();
        x = new Double(randomLookups.size()) /
                nanoseconds
                * 1000000000;

        stdout.println("\t\t\tNumber of iterations\t" + randomLookups.size()+"\tor\t"+70 * numIterations+"\tk-mers");
        stdout.println("\t\t\tNanoseconds         \t" + nanoseconds);
        stdout.println("\t\t\tAccess Speed (query) (Hz)   \t" + x);
        stdout.println("\t\t\tAccess Speed (kmers) (Hz)   \t" + 70*x);



        klue16.shutDown();
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
