package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;
import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster2;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabaseAllDisk;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import static org.cchmc.kluesuite.mainprograms.databasebuilders.CombineRocksDB.maxfiles;

/**
 * Created by jwc on 4/23/18.
 *
 * FOLLOWS BuildPieces parallel program
 *
 */
public class CombinePiecesMultipleKmerRocks16 {

    public static ArrayList<String> findExistingParts(String prefix) {
        ArrayList<String> filePartNames = new ArrayList<String>(100);

        String fname = prefix;
        File tmp = new File(fname);
        //tmp = new File(kidDiskAllTemp+String.format("%02d", k));
        int k = 1;
        while (tmp.exists()) {
            filePartNames.add(fname);
            fname = new String(prefix +".p"+k);
            k++;
            tmp = new File(fname);
        }

        return filePartNames;
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Takes a pieces of multiple databases and combines them");
            System.err.println("ARG 0  : final database name (16 part)");
            System.err.println("ARG 1+ : location to place databases (path and prefix)");
            System.err.println("       : a.k.a the master prefix used in all steps)");

        }

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
        //calls 16 part KLUE constructor for master
        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, args[0], maxfiles, true);
        crdbim.agglomerateAndWriteData();
        crdbim.shutDown();
    }


}
