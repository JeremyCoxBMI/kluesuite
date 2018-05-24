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
public class CombineRocksDbIntoMaster2 {




    public static void main(String[] args) {
        String path = "./";

        CombineRocksDbIntoMaster.period = 100*1000;

        int noFiles = args.length-1;
        String[] databases = new String[noFiles];
        for (int k = 0; k<noFiles; k++){
            databases[k] = path + args[k+1];
        }
//        KidDatabaseMemory myKidDB = KidDatabaseMemory.loadFromFile("kmerpos_KID_DB.dat.bin");

        //unclear what this value should be
        int maxfiles = 30;
        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, "master", maxfiles);
        crdbim.agglomerateAndWriteData();
        crdbim.shutDown();

    }


}
