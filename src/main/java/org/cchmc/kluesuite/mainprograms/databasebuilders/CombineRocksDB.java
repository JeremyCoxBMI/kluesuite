package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.zip.DataFormatException;

/**
 * Created by jwc on 4/12/18.
 *
 * DOES NOT SUPPORT DUPLICATE KEYS across Databases
 *
 */
public class CombineRocksDB {

    static {
        RocksDB.loadLibrary();
    }


    public static int maxfiles = 30;

     public static void combine(String destination, ArrayList<String> sources) throws RocksDBException, DataFormatException {
         CombineRocksDB crdb = new CombineRocksDB(sources, destination, maxfiles);
         crdb.agglomerateAndWriteData();
         crdb.shutDown();
     }


    /**
     * Frequency at which speed reports are made.
     * Every million is meant to be less verbose, but perhaps still too much.
     */
    static long period = 10L*1000*1000;

    protected String[] dbs;
    protected RocksDB[] rdbs;
    protected RocksDB master;

    protected RocksIterator[] its;
    protected PriorityQueue<CombineRocksDB.LookUp> pq;
    protected TimeTotals tt;

    protected long pause = 0;


    public CombineRocksDB(ArrayList<String> databases, String masterPath, int MAXFILES) throws RocksDBException {

//        LogStream.stderr.println("Class is \t"+databases.toArray()[0].getClass());
        dbs = (String[]) databases.toArray(new String[databases.size()]);

        int miniDbOpenFiles = 5;

        tt = new TimeTotals();
        tt.start();

        System.out.println("Opening master database :\t"+masterPath);

        Options options = new Options().setCreateIfMissing(true).setMaxOpenFiles(MAXFILES);
        master = RocksDB.open(options, masterPath);

        rdbs = new RocksDB[dbs.length];
        its = new RocksIterator[dbs.length];

        //In theory, should only ever have dbs.length entries (one per db), but let's be careful.
        pq = new PriorityQueue<CombineRocksDB.LookUp>(dbs.length*2, new CombineRocksDB.LookUpComparator());



        int k = 0;
        try {
            for (/*k = 0*/; k < dbs.length; k++) {
                System.out.println("Opening read-only database :\t" + dbs[k]);
                rdbs[k] = RocksDB.openReadOnly(options, dbs[k]);

                its[k] = rdbs[k].newIterator();
                its[k].seekToFirst();
                putValueAndNext(k);
            }

//            master = RocksDB.open(options, masterPath);
        } catch (RocksDBException e) {
            e.printStackTrace();
            for (int z=0; z < k; z++)   rdbs[k].close();
            master.close();
            throw e;
        }


    }


    protected CombineRocksDB(){}

    /**
     * Accesses appropriate Iterator and gets the value, adding to the processing queue
     * @param k
     */
    protected void putValueAndNext(int k){
        //we already moved to next(), so if it is not valid, do nothing
        if(its[k].isValid()) {
            pq.add(new CombineRocksDB.LookUp(k, its[k].key(), its[k].value()));
            its[k].next();
        } //else this database is empty
    }


//    public void agglomerateAndWriteData(){
//        agglomerateAndWriteData((1L << 62));    //MAX KMER31 value + 1
//    }
//
//
//    public void agglomerateAndWriteData(int frequency)
//    {
//        agglomerateAndWriteData(frequency, (1L << 62));    //MAX KMER31 value + 1
//
//    }


    public void agglomerateAndWriteData() throws DataFormatException {
        CombineRocksDB.LookUp curr, temp;
        byte[] value;

        //Pull items off priority queue in LEXOGRAPHIC ORDER, then write them to new database

        long reset_time = System.currentTimeMillis() + 1000;
        long now_time;

        LogStream.stderr.println("Begin agglomerating by iterating keys");

        //While pq is not empty, keep processing
        long k = 0;
        try {
            while (!pq.isEmpty()) {
                curr = pq.remove();
                putValueAndNext(curr.index);

                //check to see if next item is same; if so, agglomerate entries
                temp = pq.peek();
                if (temp != null && temp.equalsKey(curr.key)) {
                    throw new DataFormatException("CombineRocksDB :: two databases have the key\t" + temp.key);
                }

                if (k % period == 0){
                    LogStream.stderr.printlnTimeStamped("Writing\t"+curr);
                }
                master.put(curr.key, curr.value);
                k++;
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
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

    /**
     * Struct to store key/value pairs for sorting on priority queue before writing to database
     */
    public static final class LookUp {

        int index;
        public byte[] key;
        public byte[] value;

        LookUp(int i, byte[] k, byte[] v){
            index=i;
            key=k;
            value=v;
        }

        public boolean equalsKey(byte[] other) {
            boolean result = true;
            if (key.length == other.length){
                for (int k=0; k< key.length; k++){
                    if (key[k] != other[k]){
                        result = false;
                        break;
                    }
                }
            } else {
                result = false;
            }
            return result;
        }

        //TODO not sure this is the order RocksDB reports them in
        public int compare(LookUp lu){
            int result=0;
            if (key.length != lu.key.length){
                for (int k=0; k < key.length && result != 0; k++){
                    result = key[k] - lu.key[k];    //0 if same, continue
                }
            } else {
                result = key.length - lu.key.length;
            }

            return result;
        }

        public String toString(){
            return "queue "+index+" key "+new String(key)+" value "+new String(value);
        }
    }

    /**
     * Allows comparison within priority queue
     */
    public final class LookUpComparator implements Comparator<CombineRocksDB.LookUp> {
        @Override
        public int compare(CombineRocksDB.LookUp lookUp, CombineRocksDB.LookUp t1) {
            return lookUp.compare(t1);
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


    public void shutDown(){
        master.close();
        for (RocksDB k : rdbs){
            k.close();
        }
    }



}
