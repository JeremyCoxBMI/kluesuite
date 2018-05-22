package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;
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
public class CombinePiecesMultipleDatabasesRocks16 {

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
            System.err.println("ARG 0  : number of pieces");
            System.err.println("ARG 1  : final database name");
            System.err.println("ARG 2+ : location to place databases (path and prefix)");
            System.err.println("       : a.k.a the master prefix used in all steps)");

        }

        combine(args, args[1], Integer.parseInt(args[0]));
    }

    private static void combine(String[] args, String rocksPrefix, int numpieces) {


        ArrayList<String> kidDiskAllPieces = new ArrayList<>();
        ArrayList<String> kmerAllPieces = new ArrayList<>();
        ArrayList<String> startEndAllPieces = new ArrayList<>();

        ArrayList<String> temp;

        //arg 2 starts database to combine
        for (int k=2; k < args.length; k++) {
            String tempPrefix = args[k];
            //build file list for combination
            for (int p = 0; p < numpieces; p++) {
                String rocksKlueTemp = tempPrefix + KidDatabaseAllDisk.TEMPORARY_KLUE_SUFFIX + String.format(".c%02d", p);
                ;
                String rocksKlueStartEndName = tempPrefix + KidDatabaseAllDisk.STARTEND_SUFFIX + String.format(".c%02d", p);
                String kidDiskAllTemp = tempPrefix + KidDatabaseAllDisk.TEMPORARY_KIDDB_SUFFIX + String.format(".c%02d", p);

                temp = findExistingParts(rocksKlueTemp);
                kmerAllPieces.addAll(temp);
                temp = findExistingParts(kidDiskAllTemp);
                kidDiskAllPieces.addAll(temp);
                temp = findExistingParts(rocksKlueStartEndName);
                startEndAllPieces.addAll(temp);
            }
        }


        String kidDiskAll =             new String(rocksPrefix + KidDatabaseAllDisk.DISKALL_SUFFIX);
        try {
            //combining RocksDB databases without possible identical keys to merge (it is NOT a KLUE)
            //combine(String destination, ArrayList<String> sources)
            LogStream.stdout.printlnTimeStamped("Creating database\t"+kidDiskAll);
            LogStream.stdout.println("\t\tFrom\t"+kidDiskAllPieces);
            CombineRocksDB.combine(kidDiskAll, kidDiskAllPieces);
        } catch (RocksDBException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
        }



        String rocksKlueName =          new String(rocksPrefix+KidDatabaseAllDisk.KMER_SUFFIX);

        LogStream.stdout.printlnTimeStamped("Creating database\t"+rocksKlueName);
        LogStream.stdout.println("\t\tFrom\t"+(String[]) kmerAllPieces.toArray(new String[kmerAllPieces.size()]));

        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster((String[]) kmerAllPieces.toArray(new String[kmerAllPieces.size()]), rocksKlueName, maxfiles, true);
        crdbim.agglomerateAndWriteData();
        crdbim.shutDown();


        String startEndKlueName =       rocksPrefix + KidDatabaseAllDisk.STARTEND_SUFFIX;

        LogStream.stdout.printlnTimeStamped("Creating database\t"+startEndKlueName);
        LogStream.stdout.println("\t\tFrom\t"+(String[]) startEndAllPieces.toArray(new String[startEndAllPieces.size()]));
        
        crdbim = new CombineRocksDbIntoMaster((String[]) startEndAllPieces.toArray(new String[startEndAllPieces.size()]), startEndKlueName, maxfiles);
        crdbim.agglomerateAndWriteData();
        crdbim.shutDown();


    }

}
