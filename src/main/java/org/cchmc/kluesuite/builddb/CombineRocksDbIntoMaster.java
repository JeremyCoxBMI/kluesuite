package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.multifilerocksdbklue.Rocks16Klue;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created by osboxes on 18/08/16.
 *
 *
 * This combines multiple databases into one, writing IN ORDER for speed optimization.
 *
 */
public class CombineRocksDbIntoMaster {

    /**
     * Frequency at which speed reports are made.
     * Every million is meant to be less verbose, but perhaps still too much.
     */
    static long period = 1L*1000*1000;

    protected String[] dbs;
    protected RocksDbKlue[] rdbs;
    protected KLUE master;

    protected RocksIterator[] its;
    protected PriorityQueue<LookUp> pq;
    protected TimeTotals tt;

    protected long pause = 0;


    public CombineRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES){
        dbs = databases;


        int miniDbOpenFiles = 5;

        tt = new TimeTotals();
        tt.start();

        System.out.println("Opening master database :\t"+masterPath);
        master = new RocksDbKlue(masterPath,false,MAXFILES);

        rdbs = new RocksDbKlue[dbs.length];
        its = new RocksIterator[dbs.length];

        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
        pq = new PriorityQueue<LookUp>(dbs.length*2, new LookUpComparator());

        for (int k=0; k<dbs.length; k++){
            System.out.println("Opening read-only database :\t"+dbs[k]);
            rdbs[k] = new RocksDbKlue(dbs[k],true,miniDbOpenFiles);
            its[k] = rdbs[k].newIterator();
            its[k].seekToFirst();
            putValueAndNext(k);
        }


    }

    public CombineRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES, boolean sixteenParts){



        dbs = databases;


        int miniDbOpenFiles = 5;

        tt = new TimeTotals();
        tt.start();

        System.out.println("Opening master database :\t"+masterPath);

        if (!sixteenParts) {
            master = new RocksDbKlue(masterPath, false, MAXFILES);
        } else {
            master = new Rocks16Klue(masterPath, false);
        }

        rdbs = new RocksDbKlue[dbs.length];
        its = new RocksIterator[dbs.length];

        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
        pq = new PriorityQueue<LookUp>(dbs.length*2, new LookUpComparator());

        for (int k=0; k<dbs.length; k++){
            System.out.println("Opening read-only database :\t"+dbs[k]);
            rdbs[k] = new RocksDbKlue(dbs[k],true,miniDbOpenFiles);
            its[k] = rdbs[k].newIterator();
            its[k].seekToFirst();
            putValueAndNext(k);
        }


    }

//    public CombineRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES, long from, long to){
//        dbs = databases;
////        this.myKidDB = myKidDB;
//
//        int miniDbOpenFiles = 5;
//
//        tt = new TimeTotals();
//        tt.start();
//
//        System.out.println("Opening master database :\t"+masterPath);
//        master = new RocksDbKlue(masterPath,false,MAXFILES);
//
//        rdbs = new RocksDbKlue[dbs.length];
//        its = new RocksIterator[dbs.length];
//
//        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
//        pq = new PriorityQueue<LookUp>(dbs.length*2, new LookUpComparator());
//
//        for (int k=0; k<dbs.length; k++){
//            System.out.println("Opening read-only database :\t"+dbs[k]);
//            rdbs[k] = new RocksDbKlue(dbs[k],true,miniDbOpenFiles);
//            its[k] = rdbs[k].newIterator();
//            its[k].seekToFirst();
//
//            putValueAndNext(k);
//        }
//
//
//    }



    protected CombineRocksDbIntoMaster(){}


    public CombineRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES, long resume, int pause) {

        dbs = databases;


        int miniDbOpenFiles = 5;

        tt = new TimeTotals();
        tt.start();

        System.out.println("Opening master database :\t"+masterPath);
        master = new RocksDbKlue(masterPath,false,MAXFILES);

        rdbs = new RocksDbKlue[dbs.length];
        its = new RocksIterator[dbs.length];

        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
        pq = new PriorityQueue<LookUp>(dbs.length*2, new LookUpComparator());

        for (int k=0; k<dbs.length; k++){
            System.out.println("Opening read-only database :\t"+dbs[k]);
            rdbs[k] = new RocksDbKlue(dbs[k],true,miniDbOpenFiles);
            its[k] = rdbs[k].newIterator();
            //its[k].seekToFirst();
            its[k].seek(RocksDbKlue.longToBytes(resume));
            putValueAndNext(k);

//            //BUILT IN PAUSE
//            long start = System.nanoTime();
//            long end=0;
//            do{
//                end = System.nanoTime();
//            }while(start + pause >= end);

        }

        this.pause = (long) pause;

    }


    public CombineRocksDbIntoMaster(String[] databases, KLUE klue, int MAXFILES, long resume) {

        dbs = databases;


        int miniDbOpenFiles = 5;

        tt = new TimeTotals();
        tt.start();

        master = klue;

        rdbs = new RocksDbKlue[dbs.length];
        its = new RocksIterator[dbs.length];

        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
        pq = new PriorityQueue<LookUp>(dbs.length*2, new LookUpComparator());

        for (int k=0; k<dbs.length; k++){
            System.out.println("Opening read-only database :\t"+dbs[k]);
            rdbs[k] = new RocksDbKlue(dbs[k],true,miniDbOpenFiles);
            its[k] = rdbs[k].newIterator();
            //its[k].seekToFirst();
            its[k].seek(RocksDbKlue.longToBytes(resume));
            putValueAndNext(k);
        }


    }

    public CombineRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES, long resume) {

        dbs = databases;


        int miniDbOpenFiles = 5;

        tt = new TimeTotals();
        tt.start();

        System.out.println("Opening master database :\t"+masterPath);
        master = new RocksDbKlue(masterPath,false,MAXFILES);

        rdbs = new RocksDbKlue[dbs.length];
        its = new RocksIterator[dbs.length];

        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
        pq = new PriorityQueue<LookUp>(dbs.length*2, new LookUpComparator());

        for (int k=0; k<dbs.length; k++){
            System.out.println("Opening read-only database :\t"+dbs[k]);
            rdbs[k] = new RocksDbKlue(dbs[k],true,miniDbOpenFiles);
            its[k] = rdbs[k].newIterator();
            //its[k].seekToFirst();
            its[k].seek(RocksDbKlue.longToBytes(resume));
            putValueAndNext(k);
        }


    }

    /**
     * Accesses appropriate Iterator and gets the value, adding to the processing queue
     * @param k
     */
    protected void putValueAndNext(int k){
        //we already moved to next(), so if it is not valid, do nothing
        if(its[k].isValid()) {
            byte[] key = its[k].key();
            byte[] value = its[k].value();
            pq.add(new LookUp(k, RocksDbKlue.bytesToLong(key), RocksDbKlue.bytesToArrayListLong(value)));
            its[k].next();
        } //else this database is empty
    }


    public void agglomerateAndWriteData(){
        agglomerateAndWriteData((1L << 62));    //MAX KMER31 value + 1
    }


    public void agglomerateAndWriteData(int frequency)
    {
        agglomerateAndWriteData(frequency, (1L << 62));    //MAX KMER31 value + 1

    }


    /**
     *
     * @param frequency
     * @param upperbound    default (1L << 62)
     */
    public void agglomerateAndWriteData(int frequency, long upperbound)
    {
        LookUp curr, temp;
        PositionList value;
        tt.start();

        //Pull items off priority queue in LEXOGRAPHIC ORDER, then write them to new database


        int count = frequency;
        long reset_time = System.currentTimeMillis() + 1000;
        long now_time;

        //While pq is not empty, keep processing
        long k = 0;
        while (!pq.isEmpty()){
            curr = pq.remove();
            value = new PositionList(curr.posz);
            putValueAndNext(curr.index);

            if (curr.key >= upperbound) break;

            //check to see if next item is same; if so, agglomerate entries
            temp = pq.peek();
            while( temp != null && temp.key == curr.key){
                temp = pq.remove();
                value.add(temp.posz);
                putValueAndNext(temp.index);
                temp = pq.peek();
            }

            k++;
            if (k % period == 1){
                //double pct = ((curr.key / 1L << 54) / new Double(1L << 8))*100;
                double pct = (new Double(curr.key >> 54) / new Double(1L << 8))*100;
                Kmer31 x = new Kmer31(curr.key);

                System.out.println("\tWriting Kmer31 number "+k/period+" million to master :: "+x+"("+x.toLong()+")"+"\t   (average) records/s = "+(k)/tt.timePassedFromStartSeconds()+"\t"+pct+"%");
            }
            value.sortAndRemoveDuplicates();

            if (count >= frequency){
                //burn cycles
                while (true){
                    if (System.currentTimeMillis() > reset_time){
                        break;
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                count = 0;
            }
            master.put(curr.key, value.toArrayListLong());
            count++;
        }
    }


    /**
     * Parses database contents, combines entries if needed, and writes final record.
     */
    public void agglomerateAndWriteData(long upperbound){
        LookUp curr, temp;
        PositionList value;
        tt.start();

        //Pull items off priority queue in LEXOGRAPHIC ORDER, then write them to new database

        //While pq is not empty, keep processing
        long k = 0;
        while (!pq.isEmpty()){
            curr = pq.remove();
            value = new PositionList(curr.posz);
            putValueAndNext(curr.index);

            //check to see if next item is same; if so, agglomerate entries
            temp = pq.peek();
            while( temp != null && temp.key == curr.key){
                temp = pq.remove();
                value.add(temp.posz);
                putValueAndNext(temp.index);
                temp = pq.peek();
            }

            k++;
            if (k % period == 1){
                //double pct = ((curr.key / 1L << 54) / new Double(1L << 8))*100;
                double pct = (new Double(curr.key >> 54) / new Double(1L << 8))*100;
                Kmer31 x = new Kmer31(curr.key);
                System.out.println("\tWriting Kmer31 number "+k/period+" million to master :: "+x+"("+x.toLong()+")"+"\t   (average) records/s = "+(k*1000000000L)/tt.timePassedFromStart()+"\t"+pct+"%");
            }
            value.sortAndRemoveDuplicates();
            if (curr.key >= upperbound) break;
            master.put(curr.key, value.toArrayListLong());



//            //BUILT IN PAUSE
//            long start = System.nanoTime();
//            long end=0;
//            do{
//                end = System.nanoTime();
//            }while(start + this.pause >= end);
        }
    }

    /**
     * moves iterator
     * @param lowerbound  long representing target Kmer31
     */
    public void seekTo(long lowerbound) {
        for (int k=0; k<dbs.length; k++){
            its[k].seek(RocksDbKlue.longToBytes(lowerbound));
        }
    }

    public void agglomerateAndWriteWithFlush() {
        LookUp curr, temp;
        PositionList value;
        tt.start();

        //Pull items off priority queue in LEXOGRAPHIC ORDER, then write them to new database

        //While pq is not empty, keep processing
        long k = 0;
        while (!pq.isEmpty()){
            curr = pq.remove();
            value = new PositionList(curr.posz);
            putValueAndNext(curr.index);

            //check to see if next item is same; if so, agglomerate entries
            temp = pq.peek();
            while( temp != null && temp.key == curr.key){
                temp = pq.remove();
                value.add(temp.posz);
                putValueAndNext(temp.index);
                temp = pq.peek();
            }

            k++;
            if (k % period == 1){
                //double pct = ((curr.key / 1L << 54) / new Double(1L << 8))*100;
                double pct = (new Double(curr.key >> 54) / new Double(1L << 8))*100;
                System.out.println("\tWriting Kmer31 number "+k/period+" million to master :: "+new Kmer31(curr.key)+"   records/s = "+(k*1000000000L)/tt.timePassedFromStart()+"\t"+pct+"%");
            }

            //master.putSynchronous(curr.key, value.toArrayListLong());
            master.put(curr.key, value.toArrayListLong());
            //BUILT IN PAUSE
            long start = System.nanoTime();
            long end=0;
            do{
                end = System.nanoTime();
            }while(start + this.pause >= end);

        }
    }


    /**
     * Struct to store key/value pairs for sorting on priority queue before writing to database
     */
    public final class LookUp {
        int index;
        long key;
        ArrayList<Long> posz;
        LookUp(int i, long k, ArrayList<Long> p){
            index = i;
            key=k;
            posz=p;
        }
    }

    /**
     * Allows comparison within priority queue
     */
    public final class LookUpComparator implements Comparator<LookUp> {
        @Override
        public int compare(LookUp lookUp, LookUp t1) {
            int result;
            long chump = lookUp.key - t1.key;
            if (chump > (long) Integer.MAX_VALUE){
                result = Integer.MAX_VALUE;
            } else if (chump < (long) Integer.MIN_VALUE){
                result = Integer.MIN_VALUE;
            } else {
                result = (int) chump;
            }
            return result;
        }
    }



    /**
     * Returns subdirectory names (not subdirectories) ?what?
     * @return
     */
    public static String[] getLocalDirectories(String path){
        File file = new File(path+"/");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return directories;
    }


    public static void main(String[] args) {
        String path = "./";
        String[] dirs = getLocalDirectories(path);

        period = 100*1000;

        int noFiles = dirs.length;
        String[] databases = new String[noFiles];
        for (int k = 0; k<noFiles; k++){
            databases[k] = path + dirs[k];
        }
//        KidDatabaseMemory myKidDB = KidDatabaseMemory.loadFromFile("kmerpos_KID_DB.dat.bin");

        //unclear what this value should be
        int maxfiles = 30;
        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, "master", maxfiles);
        crdbim.agglomerateAndWriteData();

    }

    public void shutDown(){
        master.shutDown();
        for (KLUE k : rdbs){
            k.shutDown();
        }
    }
}
