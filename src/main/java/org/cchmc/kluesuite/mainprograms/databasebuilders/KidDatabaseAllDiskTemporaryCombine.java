package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabaseAllDisk;
import org.cchmc.kluesuite.mainprograms.CombineRocksDatabases;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;
import static org.cchmc.kluesuite.mainprograms.databasebuilders.CombineRocksDB.maxfiles;

/**
 * Created by jwc on 3/31/18.
 */
public class KidDatabaseAllDiskTemporaryCombine {

    /**
     * Combines temporary kmer databases (RocksDbKlue) and KidDatabaseDiskAll databases
     *
     * @param prefix
     */
    public static void combine(String prefix) {

        LogStream.startStdStreams();

        LogStream.stderr.printlnTimeStamped(" ****** Combining Kid Databases ******");

        String rocksKlueTemp =          new String(prefix+ KidDatabaseAllDisk.TEMPORARY_KLUE_SUFFIX);
        String rocksKlueName =          new String(prefix+KidDatabaseAllDisk.KMER_SUFFIX);
        String kidDiskAll =             new String(prefix + KidDatabaseAllDisk.DISKALL_SUFFIX);
        String kidDiskAllTemp =         new String(prefix + KidDatabaseAllDisk.TEMPORARY_KIDDB_SUFFIX);
        String startEndKlueName = prefix + KidDatabaseAllDisk.STARTEND_SUFFIX;


        String base = kidDiskAllTemp;
        ArrayList<String> filePartNames = new ArrayList<String>(100);

        String fname = base;
        File tmp = new File(fname);
        //tmp = new File(kidDiskAllTemp+String.format("%02d", k));
        int k = 1;
        while(tmp.exists()){
            filePartNames.add(fname);
            fname = new String(base+String.format(".p%02d", k));
            k++;
            tmp = new File(fname);
        }

//        LogStream.stderr.printlnTimeStamped(" ****** Combining KidDatabaseDiskAll ******");
//        LogStream.stderr.println("\t\t\t\tdestination:\t"+kidDiskAll);
//        LogStream.stderr.println("\t\t\t\tsources:");
//        LogStream.stderr.println(Arrays.toString(filePartNames.toArray(new String[filePartNames.size()])));
//
//        try {
//            //combining RocksDB databases without possible identical keys to merge (it is NOT a KLUE)
//            //combine(String destination, ArrayList<String> sources)
//            CombineRocksDB.combine(kidDiskAll, filePartNames);
//        } catch (RocksDBException e) {
//            e.printStackTrace();
//        } catch (DataFormatException e) {
//            e.printStackTrace();
//        }
//
//
        base = rocksKlueTemp;
        filePartNames = new ArrayList<>(100);
        //filePartNames.add(rocksKlueName);
        fname = base;
        tmp = new File(fname);
        //tmp = new File(kidDiskAllTemp+String.format("%02d", k));
        k = 1;
        while(tmp.exists()){
            filePartNames.add(fname);
            fname = base+".p"+k;
            k++;
            tmp = new File(fname);
        }

        LogStream.stderr.printlnTimeStamped(" ****** Combining Kmers ******");
        LogStream.stderr.println("\t\t\t\tdestination:\t"+rocksKlueName);
        LogStream.stderr.println("\t\t\t\tsources:");
        LogStream.stderr.println(Arrays.toString(filePartNames.toArray(new String[filePartNames.size()])));

        //must use rocksDbKlue combiner to combine positions across databases
        CombineRocksDbIntoMaster crdbim;

        long resume = 0L;
//        resume = 739120573182454933L;
        crdbim = new CombineRocksDbIntoMaster((String[]) filePartNames.toArray(new String[filePartNames.size()]),
                rocksKlueName,
                maxfiles,
                resume);
        crdbim.agglomerateAndWriteData();
        crdbim.shutDown();

//        // StartEnd is already written, does not need to be combined
//
//        LogStream.stderr.printlnTimeStamped(" ****** Combining StartEnd Kmers ******");
//        filePartNames = CombinePieces.findExistingParts(startEndKlueName);
//        LogStream.stderr.println("\t\t\t\tdestination:\t"+startEndKlueName);
//        LogStream.stderr.println("\t\t\t\tsources:");
//        LogStream.stderr.println(Arrays.toString(filePartNames.toArray(new String[filePartNames.size()])));
//
//        //must use rocksDbKlue combiner to combine positions across databases
//        crdbim = new CombineRocksDbIntoMaster((String[]) filePartNames.toArray(new String[filePartNames.size()]), startEndKlueName, maxfiles);
//        crdbim.agglomerateAndWriteData();
//        crdbim.shutDown();

    }


}
