package org.cchmc.kluesuite.multifilerocksdbklue;

import org.cchmc.kluesuite.klue.KeyValuePair;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksIterator;

import java.util.Iterator;

/**
 * Created by jwc on 3/13/18.
 *
 * TODO  // UNTESTED but simple and straightforward
  *
 * This is a simple iterator to parse over the multi-part database
 *
 */

public class Rocks16Iterator implements Iterator<KeyValuePair>{
    
    private RocksIterator[] its;
    //private PriorityQueue<KeyValuePair> queue;
    boolean hasNext;

    /**
     * stores the currently used database iterator
     */
    int current;
    
    public Rocks16Iterator(RocksDbKlue[] sixteen){
        its = new RocksIterator[16];
        hasNext = true;
        current = 0;
        for (int j=0; j < 16; j++){
            its[j] = sixteen[j].db.newIterator();
            its[j].seekToFirst();
            //queue.add(new KeyValuePair(its[j].key(), its[j].value(), j));
        }
    }

    public Rocks16Iterator(RocksDbKlue[] sixteen, long key){
        its = new RocksIterator[16];
        hasNext = true;
        current = 0;

        for (int j=0; j < 16; j++){
            its[j] = sixteen[j].db.newIterator();
        }

        seek(key);
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public KeyValuePair next() {
        KeyValuePair result;
        //definitely hasNext == true
        result = new KeyValuePair(its[current].key(),its[current].value());
        its[current].next();

        //this determines hasNext
        if (!its[current].isValid()){
            current++;
            if (current == 16){
                hasNext = false;
            }
        }
        return result;
    }
    
    public void seekToFirst(){
        initializeToFirst();
    }
    
    public void seek(long key){
        seek(RocksDbKlue.longToBytes(key));
    }
    
    public void seek(byte[] key){
        current = Prefix16.bytesToPrefixInt(key);

        //queue.clear();
        for (int j=current; j < 16; j++){  //iterators before this have expired their entries
            its[j].seek(key);
        }
    }
    
    private void initializeToFirst(){
        current = 0;
        for (int j=0; j < 16; j++){
            its[j].seekToFirst();
        }
    }
}
