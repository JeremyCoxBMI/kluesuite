package org.cchmc.kluesuite.memoryklue;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Created by osboxes on 28/09/16.
 *
 * Stores a KLUE object in memory.
 * Comapred to HeapKlueHashMap:
 *  - uses more memory
 *  - this allows better usage of keys, so that shortKmer can be implemented as well as other things.
 *
 */
public class MemoryKlueTreeMap implements KLUE {

    TreeMap<Long, long[]> treeMap;

    Integer currentKeyIndex = null;
    ArrayList<Long> keys = null;

    public MemoryKlueTreeMap(){
        treeMap = new TreeMap<Long, long[]>();
    }

    @Override
    public void put(long key, ArrayList<Long> positions) {
        long[] value = new long[positions.size()];
        for (int k=0; k<positions.size(); k++)
            value[k] = positions.get(k);
        treeMap.put(key, value);
    }

    @Override
    public void append(long key, long pos) {
        if (treeMap.containsKey(key)){
            long[] a = treeMap.get(key);
            long[] result = Arrays.copyOf(a, a.length+1);
            result[a.length] = pos; //result.length - 1 COINCIDENTALLY is a.length in this CASE
            treeMap.put(key, result);
        } else {
            long[] result = new long[]{pos};
            treeMap.put(key, result);
        }
    }

    public void append(long key, long[] pos) {
        if (treeMap.containsKey(key)){
            long[] a = treeMap.get(key);
            long[] result = Arrays.copyOf(a, a.length+pos.length);
            System.arraycopy(pos, 0, result, a.length, pos.length);
            treeMap.put(key, result);
        } else {
            treeMap.put(key, pos);
        }
    }

    @Override
    public ArrayList<Long> get(long key) {
        ArrayList<Long> result = new ArrayList<Long>();
        if(treeMap.get(key)==null)
            return null;
        for( long k : treeMap.get(key))
            result.add(k);
        return result;
    }

    @Override
    public ArrayList<ArrayList<Long>> getAll(long[] keys) {
        ArrayList<ArrayList<Long>> result = new ArrayList<ArrayList<Long>>();
        for (long key : keys){
            result.add(get(key));
        }
        return result;
    }

    @Override
    public ArrayList<PositionList> getAllPL(long[] keys) {
        ArrayList<PositionList> result = new ArrayList<PositionList>();
        for (long key : keys){
            result.add(new PositionList(treeMap.get(key)));
        }
        return result;
    }

    @Override
    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        //maybe a better way?   treeMap.subMap( from, to)
        // where t has iterated up 1 on the lowest place value

        PositionList result = new PositionList();
        int shift = (Kmer31.KMER_SIZE - prefixLength)*2;
        SortedMap<Long, long[]> sm = treeMap.subMap(shorty, (shorty + 1L << shift));
        for( long k : sm.keySet() ) {
            long[] p = sm.get(k);
            result.add(p);
        }
        return result;
    }

    @Override
    public void shutDown() {
        treeMap = null;
    }

    public void append(long key, ArrayList<Long> value) {
        if (treeMap.containsKey(key)){
            long[] a = treeMap.get(key);
            long[] result = Arrays.copyOf(a, a.length+value.size());
            for(int k=0; k< value.size(); k++) {
                result[k + a.length] = value.get(k);
            }
            treeMap.put(key, result);
        } else {
            long[] result = new long[value.size()];
            for(int k=0; k< value.size(); k++) {
                result[k] = value.get(k);
            }
            treeMap.put(key, result);
        }
    }

    public void saveToFile(String filename){
        try {
            FileOutputStream fout = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this);
            oos.close();
            fout.close();
            System.out.println("\n\t Save to File Complete");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static MemoryKlueTreeMap loadFromFile(String filename) {
        MemoryKlueTreeMap result;
        try {
            FileInputStream fin = new FileInputStream(filename);
            ObjectInputStream ois = new ObjectInputStream(fin);
            result = (MemoryKlueTreeMap) ois.readObject();
            ois.close();
            System.out.println("\n\t KidDatabaseMemory :: Load from file Complete");
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean hasNext(){
        if (keys ==null){
            keys =new ArrayList<Long>(treeMap.keySet());
            Collections.sort(keys);
            currentKeyIndex = 0;
        }
        return currentKeyIndex < keys.size();

    }

    public void append(long key, PositionList p) {
        if (treeMap.containsKey(key)){
            long[] a = treeMap.get(key);
            long[] pos = p.toLongArray();
            long[] result = Arrays.copyOf(a, a.length+pos.length);
            System.arraycopy(pos, 0, result, a.length, pos.length);
            System.err.println("new combined entry\t"+Arrays.toString(result));
            treeMap.put(key, result);
        } else {
            treeMap.put(key, p.toLongArray());
        }
    }

    public void writeNext(KLUE kl){
        long key = keys.get(currentKeyIndex);
        currentKeyIndex++;
        kl.put(key, get(key));
    }


}
