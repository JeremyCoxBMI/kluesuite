package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
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
 * Combines a single database into master database.
 * This is wholely not recommended, as the master database will not have been written in order.
 *
 */
public class AddRocksDbIntoMaster {

    /**
     * Frequency at which speed reports are made.
     * Every million is meant to be less verbose, but perhaps still too much.
     */
    static long period = 1L*1000*1000;

    protected String[] dbs;
    protected RocksDbKlue[] rdbs;
    protected RocksDbKlue master;

    protected RocksIterator[] its;
    protected PriorityQueue<LookUp> pq;
    protected TimeTotals tt;


    public AddRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES){
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

    /**
     * Allows resuming at a certain sequence, inclusive
     * Note that  value.sortAndRemoveDuplicates(); prevents duplicates
     *
     * @param databases
     * @param masterPath
     * @param MAXFILES
     * @param start
     */
    public AddRocksDbIntoMaster(String[] databases, String masterPath, int MAXFILES, Kmer31 start){
        dbs = databases;
//        this.myKidDB = myKidDB;

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
            its[k].seek(RocksDbKlue.longToBytes(start.toLong()));
            putValueAndNext(k);
        }
    }

    protected AddRocksDbIntoMaster(){}

    /**
     * Accesses appropriate Iterator and gets the value, adding to the processing queue
     * @param k
     */
    protected void putValueAndNext(int k){
        //we already moved to next(), so if it is not valid, do nothing
        if(its[k].isValid()) {
            byte[] key = its[k].key();
            byte[] value = its[k].value();
            //System.out.println("\t\tFirst key found is "+new Kmer31(RocksDbKlue.bytesToLong(key)));
            pq.add(new LookUp(k, RocksDbKlue.bytesToLong(key), RocksDbKlue.bytesToArrayListLong(value)));
            its[k].next();
        } //else this database is empty
    }

    /**
     * Parses database contents, combines entries if needed, and writes final record.
     */
    public void agglomerateAndWriteData(){
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

            //These 2 lines is the only difference in code between CombineRocksDbIntoMaster and AddRocksDbIntoMaster
            ArrayList<Long> longs = master.get(temp.key);
            if (longs != null) value.add( longs );

            k++;
            if (k % period == 1){
                //double pct = ((curr.key / 1L << 54) / new Double(1L << 8))*100;
                //FIXED     see _oldclasses.BitShiftFun
                double pct = (new Double(curr.key >> 54) / new Double(1L << 8))*100;
                System.out.println("\tWriting Kmer31 number "+k/period+" million to master :: "+new Kmer31(curr.key)+"   records/s = "+(k*1000000000L)/tt.timePassedFromStart()+"\t"+pct+"%");
            }
            value.sortAndRemoveDuplicates();
            master.put(curr.key, value.toArrayListLong());
        }
    }

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
     * Returns subdirectory names (not subdirectories)
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
        AddRocksDbIntoMaster crdbim = new AddRocksDbIntoMaster(databases, "master", maxfiles);
        crdbim.agglomerateAndWriteData();

    }
}
