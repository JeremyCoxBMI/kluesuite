package org.cchmc.kluesuite.testmodules;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksIterator;

/**
 * Created by jwc on 2/22/18.
 *
 * Memory Efficient Method to read all the keys into memory, in lexicographic order
 *
 *
 * A test module, as it's only purpose is for testing.  Intention is to generate random keys found IN database
 *
 */
public class LoadKeysToMemory {

    long[] keys;
    RocksDbKlue klue;


    LoadKeysToMemory(RocksDbKlue klue){
        this.klue = klue;
        readKlueKeysToMemory();
    }


    public static long[] makeDatabaseKeys(RocksDbKlue klue){
        LoadKeysToMemory lktm = new LoadKeysToMemory(klue);
        return lktm.getKeys();
    }

    private void readKlueKeysToMemory(){
        int count = 0;

        RocksIterator it = klue.db.newIterator();
        it.seekToFirst();

        while (it.isValid()){
            count++;
            it.next();
        }

        keys = new long[count];

        int k = 0;

        it.seekToFirst();

        while (it.isValid()){
            keys[k] = RocksDbKlue.bytesToLong(it.key());
            k++;
            it.next();
        }
        return;
    }

    public int getSize(){
        return keys.length;
    }

    public long[] getKeys(){
        return keys;
    }

}
