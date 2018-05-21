package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klat.KmerSequence;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksDB;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by jwc on 3/6/18.
 */
public class DebianRandomDatabaseTestAllDrivesUsingKidDB {

    public static String[] drives = {
            "/data/1/nvme/hg38.klue.final",
            "/data/1/b/hg38.klue.final",
            "/data/1/f/hg38.klue.final",
//            "/data/3/d/hg38.klue.final",
//            "/data/5/c/hg38.klue.final"
    };

    public static String KidDbName = "/data/1/f/hg38.2018.02.12/hg38.kidDB";
    public static String KidDbRocksName = "/data/1/f/hg38.2018.02.12/hg38.kidDB.disk";

    //public final static long seed = 1234567890L;
    public final static long seed = 9876543210L;

    public final static int reportA =              100 * 1000;
    public final static int reportB =             1000 * 1000;
    public final static int reportC =        10 * 1000 * 1000;


    //public final static int numIterations = 100 * 1000;

    //DEBUG
    public final static int numIterations = 14300; //about 100k reads

    public static LogStream stdout;
    public static LogStream stderr;

    public static void main(String[] args) throws Exception {

        TimeTotals tt = new TimeTotals();
        TimeTotals ttRand = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();


        String stamp = (new Timestamp(System.currentTimeMillis())).toString();
        stamp = stamp.replace(' ','_');
        stdout = LogStream.MessageClassBuilder(stamp+".logfile.out.txt", System.out);
        stderr = LogStream.MessageClassBuilder(stamp+".logfile.err.txt", System.err);

        stdout.println(  tt.toHMS() + "\tSynchronize time systems\t" + new Timestamp(System.currentTimeMillis())  );


        stdout.println(tt.toHMS()+"\tOpening/Loading KidDatabaseDisk\t" + new Timestamp(System.currentTimeMillis()));

        //stdout.println(tt.toHMS()+"\t\t" + new Timestamp(System.currentTimeMillis()));

        Random rand = new Random(seed);

        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(KidDbName, KidDbRocksName);

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
        stdout.println(Arrays.toString(drives));

        double x;
        long nanoseconds;


        //stdout.println(tt.toHMS()+"\t\t" + new Timestamp(System.currentTimeMillis()));
        for (String file : drives ){
            stdout.print("\n\t");
            stdout.println(tt.toHMS()+"\tstarting with\t"+file+"\t" + new Timestamp(System.currentTimeMillis()));

            RocksDbKlue klue = new RocksDbKlue(file, true);

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
            klue.shutDown();
        }

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
