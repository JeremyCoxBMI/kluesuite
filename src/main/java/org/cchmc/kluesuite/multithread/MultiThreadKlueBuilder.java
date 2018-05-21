package org.cchmc.kluesuite.multithread;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

/**
 * Created by COX1KB on 3/24/2018.
 */
public class MultiThreadKlueBuilder {

    protected KLUE[] klueArr;
    int numThreads;
    int numDatabases;


    MultiThreadKlueBuilder(int numThreads, int numDatabases){
        this.numThreads = numThreads;
        this.numDatabases = numDatabases;
    }

    MultiThreadKlueBuilder addRocksDbKlue( String filename, int maxFiles){
        klueArr = new KLUE[numDatabases];

        System.err.println("MultithreadKlueBuilder::initialize "+numDatabases+" databases.");
        for (int k=0; k < numDatabases; k++){
            klueArr[k] = new RocksDbKlue(filename, false, maxFiles);
        }
        return this;
    }

    MultiThreadKlue build(){
        return new MultiThreadKlue(numThreads, numDatabases, klueArr);
    }
}
