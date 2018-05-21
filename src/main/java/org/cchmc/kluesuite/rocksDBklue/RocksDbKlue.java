package org.cchmc.kluesuite.rocksDBklue;

import org.cchmc.kluesuite.helperclasses.MinHeapIntegersByArray;
import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.klue.KeyValuePair;
import org.rocksdb.*;

import java.nio.ByteBuffer;
import java.util.*;

import static java.lang.System.exit;

/**
 * Version of KLUE to use RocksDB software
 *
 * 2016-08-15   v2.0    Imported from v1.6.  Minor changes, including required shortKmer function.
 * */
public class RocksDbKlue implements KLUE, ShortKLUE {

    String databasePath;
    Options options;
    public RocksDB db;

    static {
        RocksDB.loadLibrary();
    }

//    public RocksDbKlue(String path, boolean readonly){
//        databasePath = path;
//        options = new Options().setCreateIfMissing(true);
//        try {
//            if (readonly)  db = RocksDB.openReadOnly(options, databasePath);
//            else           db = RocksDB.open(options, databasePath);
//        } catch( RocksDBException e) {
//            System.out.println("RocksDbKlue Constructor failed.");
//            e.printStackTrace();
//        }
//    }

    protected RocksDbKlue() {
        //do nothing; super for inheritance only
    }

    public RocksDbKlue(String path, boolean readonly) {
        this(path, readonly, Settings_OLD.MAX_FILES);
    }

    public RocksDbKlue(String path, boolean readonly, int maxFiles) {
        databasePath = path;
        options = new Options().setCreateIfMissing(true).setMaxOpenFiles(maxFiles);

        try {
            if (readonly) db = RocksDB.openReadOnly(options, databasePath);
            else db = RocksDB.open(options, databasePath);
        } catch (RocksDBException e) {
            System.out.println("RocksDbKlue Constructor failed.\t" + databasePath);
            e.printStackTrace();
            exit(1);
        }
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void put(long key, ArrayList<Long> positions) {
        if (positions != null) {
            try {
                byte[] k = longToBytes(key);
                byte[] v = arrayListLongToBytes(positions);
                if (k != null && v != null)
                    db.put(k, v);
//                db.put(longToBytes(key), arrayListLongToBytes(positions));
            } catch (RocksDBException e) {

                e.printStackTrace();
                System.err.println("key\t" + key);
                System.err.println("positions\t" + positions);
                exit(1);
            }
        }
    }


    public void putSynchronous(long key, ArrayList<Long> positions) {
        WriteOptions wo = new WriteOptions();
        wo.setSync(true);

        if (positions != null) {
            try {
                byte[] k = longToBytes(key);
                byte[] v = arrayListLongToBytes(positions);
                if (k != null && v != null) {
                    db.put(k, v);
//                db.put(longToBytes(key), arrayListLongToBytes(positions));
                    db.put(wo, k, v);
                }
            } catch (RocksDBException e) {

                e.printStackTrace();
                System.err.println("key\t" + key);
                System.err.println("positions\t" + positions);
                exit(1);
            }
        }
    }


    public void put(long key, byte[] positions) {
        try {
            db.put(longToBytes(key), positions);
        } catch (RocksDBException e) {
            e.printStackTrace();
            exit(1);
        }
    }

    public void append(long key, long pos) {
        ArrayList<Long> temp = get(key);
        if (temp == null)
            temp = new ArrayList<Long>();
        temp.add(pos);
        put(key, temp);
    }

    public void append(long key, PositionList p) {
//        PositionList temp = new PositionList(get(key));
        ArrayList<Long> q = p.toArrayListLong();
        ArrayList<Long> temp = get(key);
        if (temp == null)
            temp = new ArrayList<Long>();
        for (Long x : q)
            temp.add(x);
        put(key, temp);
    }

    public ArrayList<Long> get(long key) {
        byte[] read;
        ArrayList<Long> result = new ArrayList<Long>();
        try {
            read = db.get(longToBytes(key));
            result = bytesToArrayListLong(read);
            return result;

        } catch (RocksDBException e) {
            e.printStackTrace();
            exit(1);
        }
        return null;
    }

    public ArrayList<ArrayList<Long>> getAll(long[] keys) {

        ArrayList<ArrayList<Long>> result = new ArrayList<ArrayList<Long>>();

//        RocksIterator it = newIterator();
//
//        for (int k=0; k<keys.length; k++) {
//            it.seek( longToBytes(keys[k]));
//            result.addWithTrim( bytesToArrayListLong(it.value()) );
//        }

        for (int k = 0; k < keys.length; k++) {
            result.add(get(keys[k]));
        }


        return result;
    }


    public ArrayList<ArrayList<Long>> getAll(ArrayList<Long> keys) {

        ArrayList<ArrayList<Long>> result = new ArrayList<ArrayList<Long>>();

        RocksIterator it = newIterator();

//        for (int k=0; k<keys.size(); k++) {
//            it.seek( longToBytes(keys.get(k)));
//            result.addWithTrim( bytesToArrayListLong(it.value()) );
//        }

        for (int k = 0; k < keys.size(); k++) {
            result.add(get(keys.get(k)));
        }

        return result;
    }


    public ArrayList<PositionList> getAllPL(long[] keys) {

        ArrayList<PositionList> result = new ArrayList<PositionList>();

//        RocksIterator it = newIterator();

//        for (int k=0; k<keys.length; k++) {
//            it.seek( longToBytes(keys[k]));
//            result.addWithTrim( new PositionList(bytesToArrayListLong(it.value())) );
//        }

        for (int k = 0; k < keys.length; k++) {
            result.add(new PositionList(get(keys[k])));
        }

        return result;
    }


    public ArrayList<PositionList> getAllPL(ArrayList<Long> keys) {

        ArrayList<PositionList> result = new ArrayList<PositionList>();

//        RocksIterator it = newIterator();
//
//        for (int k=0; k<keys.size(); k++) {
//            it.seek( longToBytes(keys.get(k)));
//            result.addWithTrim( new PositionList(bytesToArrayListLong(it.value())) );
//        }

        for (int k = 0; k < keys.size(); k++) {
            result.add(new PositionList(get(keys.get(k))));
        }


        return result;
    }

    @Override
    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        PositionList result = new PositionList();
        ShortKmer31 seed = new ShortKmer31(shorty, prefixLength);
        byte[] key, value;
        long longKey;
        RocksIterator it = db.newIterator();

        it.seek(longToBytes(seed.lowerbound));

        key = it.key();
        longKey = bytesToLong(key);
        while (seed.equal(longKey)) {
            value = it.value();
            result.add(bytesToArrayListLong(value));
            it.next();
            key = it.key();
            longKey = bytesToLong(key);
        }

        return result;
    }

    public RocksIterator newIterator() {
        return db.newIterator();
    }

    public void shutDown() {
        System.err.println("Shutting down KLUE database (RocksDB) " + databasePath);
        if (db != null) db.close();
        options.dispose();
    }

    static public ArrayList<Long> bytesToArrayListLong(byte[] bytes) {
        ArrayList<Long> result = new ArrayList<Long>();
        if (bytes == null)
            return null;
        else {
            ByteBuffer bb = ByteBuffer.allocate(bytes.length);
            bb.put(bytes);
            bb.position(0);
            for (int k = 0; k < (bytes.length / 8); k++) {
                result.add(bb.getLong());
            }
        }
        return result;
    }

    static public byte[] arrayListLongToBytes(ArrayList<Long> value) {
        if (value == null || value.size() == 0)
            return null;
        else {

            ByteBuffer bb = ByteBuffer.allocate(8 * value.size());
            for (long l : value) {
                bb.putLong(l);
            }
            return bb.array();
        }
    }

    static public byte[] longArrayToBytes(long[] value) {
        if (value == null)
            return null;
        else {

            ByteBuffer bb = ByteBuffer.allocate(8 * value.length);
            for (long l : value) {
                bb.putLong(l);
            }
            return bb.array();
        }
    }

    /**
     * compliments of http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa
     * NOTE the bytes get destroyed
     *
     * @return
     */
    public static long bytesToLong(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getLong();
    }

    /**
     * compliments of http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa
     *
     * @return
     */
    public static byte[] longToBytes(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public long[] getKeys() {

        System.err.println("Counting keys");
        int numKeys = countKeys();
        long[] result = new long[numKeys];

        RocksIterator it = newIterator();
        it.seekToFirst();
        int k = 0;
        long periodicity = 1000000L;

        System.err.println("Loading keys\tnumber found\t" + numKeys);
        while (it.isValid()) {
            result[k] = RocksDbKlue.bytesToLong(it.key());
            it.next();
            k++;
            if (k % (10 * periodicity) == 0) {
                System.err.println("\tnumber of entries processed (1M)\t" + k / periodicity);
            }
        }
        for (; k < numKeys; k++) {
            result[k] = 0L;
        }
        return result;
    }

    public int countKeys() {
        RocksIterator it = newIterator();
        it.seekToFirst();
        int result = 0;

        while (it.isValid()) {
            result++;
            it.next();
        }

        return result;
    }

    public PositionList getFirst() {

        RocksIterator it = newIterator();
        it.seekToFirst();

        return new PositionList(it.value());
    }

    public Map<Integer, Integer> countFrequency() {
        Map<Integer, Integer> freqTOcount = new HashMap<Integer,Integer>();

        RocksIterator rit = db.newIterator();

        Integer size;
        long c = 0;
        long periodicity = 1000000L;

        rit.seekToFirst();
        while (rit.isValid()) {

            //OPTION 1
            size = rit.value().length / 8;

            //OPTION 2

//            PositionList pl = new PositionList(it.value());
//            pl.sortAndRemoveDuplicates();
//            size = pl.toLongArray().length;

            if (freqTOcount.get(size) != null) {
                freqTOcount.put(size, freqTOcount.get(size) + 1);
            } else {
                freqTOcount.put(size, 1);
            }

            c++;
            if (c % (10 * periodicity) == 0) {
                System.err.println("\tnumber of entries processed (millions)\t" + c / periodicity);
            }

            rit.next();
        }
        System.out.println("\ttotal number of k-mers processed\t" + c);
        return freqTOcount;
    }

    public long[] getValuesAtIndexes(HashSet<Integer> indexes) {
        int numKeys = indexes.size();
        long[] result = new long[numKeys];

        RocksIterator it = newIterator();
        it.seekToFirst();
        int numReadKlue = 0; // number read
        int numReadToResult = 0;

        while (it.isValid()) {

//            if (numReadKlue == 671098883) {
//                boolean DEBUGIT = false;
//            }
            if (indexes.contains(numReadKlue)) {
                result[numReadToResult] = RocksDbKlue.bytesToLong(it.key());
                numReadToResult++;
            }

            numReadKlue++;
            if (numReadToResult == indexes.size()) {
                break;
            }
            it.next();
        }
        return result;


    }

    //presume mhiba has been heapified
    public long[] getValuesAtIndexes(MinHeapIntegersByArray mhiba) {
        int numKeys = mhiba.size;
        long[] result = new long[numKeys];

        RocksIterator it = newIterator();
        it.seekToFirst();
        int numReadKlue = 0; // number read
        int numReadToResult = 0;
        int seekingIndex = mhiba.remove();


        while (it.isValid()) {

            if (seekingIndex == numReadKlue) {
                result[numReadToResult] = RocksDbKlue.bytesToLong(it.key());
                numReadToResult++;
                seekingIndex = mhiba.remove();
            }

            numReadKlue++;
            if (numReadToResult == numKeys) {
                break;
            }
            it.next();
        }
        System.out.println("DEBUG\tnumber of keys found :: " + numReadToResult);
        return result;
    }

    public Iterator<KeyValuePair> iterator() {
        return new InOrderIterator(newIterator());
    }

    public void put(byte[] k, byte[] v) {
        try {
            if (k != null && v != null)
                db.put(k, v);
        } catch (RocksDBException e) {
            e.printStackTrace();
            System.err.println("key\t" + Arrays.toString(k));
            System.err.println("positions\t" + new PositionList(v));
            exit(1);
        }
    }

    public ArrayList<Long> get(byte[] key) {
        byte[] read;
        ArrayList<Long> result = new ArrayList<Long>();
        try {
            read = db.get(key);
            result = bytesToArrayListLong(read);
            return result;

        } catch (RocksDBException e) {
            e.printStackTrace();
            exit(1);
        }
        return null;
    }


    class InOrderIterator implements Iterator<KeyValuePair> {

        RocksIterator it;

        public InOrderIterator(RocksIterator me) {
            it = me;
            it.seekToFirst();
        }

        @Override
        public boolean hasNext() {
            return it.isValid();
        }

        @Override
        public KeyValuePair next() {
            KeyValuePair result = null;
            if (hasNext()) {
                result = new KeyValuePair(it.key(), it.value());
                next();
            }
            return result;
        }
    }
}
