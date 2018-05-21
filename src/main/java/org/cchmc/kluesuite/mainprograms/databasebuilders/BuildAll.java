package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabaseAllDisk;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import static java.lang.System.exit;

/**
 * Created by jwc on 4/12/18.
 */
public class BuildAll {


    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Takes a FASTA dna fle and builds a KidDbDiskAll sequence database");
            System.err.println("ARG 0  : location to place databases (path and prefix)");
            System.err.println("ARG 1  : Fasta file");
            System.err.println("ARG 2  : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            System.err.println("       : beware: scales as O(n log n); n need not be huge");
            exit(0);
        }

        LogStream.startStdStreams();

        LogStream.stderr.printlnTimeStamped("Synchronize time systems");

        String fastA = args[1];
        String rocksPrefix = args[0];

//        String rocksKlueName =          rocksPrefix+KidDatabaseAllDisk.KMER_SUFFIX;
//        String rocksKlueTemp =          rocksPrefix+KidDatabaseAllDisk.TEMPORARY_KLUE_SUFFIX;
//        String rocksKlueStartEndName =  rocksPrefix+KidDatabaseAllDisk.STARTEND_SUFFIX;
//        String kidDiskAll =             rocksPrefix + KidDatabaseAllDisk.DISKALL_SUFFIX;
//        String kidDiskAllTemp =         rocksPrefix + KidDatabaseAllDisk.TEMPORARY_KIDDB_SUFFIX;
//
//        LogStream.stderr.printlnTimeStamped("Opening construction RocksDB backends");
//
//            RocksDbKlue startEnd = new RocksDbKlue(rocksKlueStartEndName, false);
//            KidDatabaseAllDisk kddTemp = new KidDatabaseAllDisk(rocksPrefix, kidDiskAllTemp, false);
//            RocksDbKlue kmersTemp = new RocksDbKlue(rocksKlueTemp, false);
//
//            long MK_SIZE = Integer.parseInt(args[2])*1000L*1000L;
//            if (MK_SIZE > Integer.MAX_VALUE) {
//                MK_SIZE = (long) (Integer.MAX_VALUE);
//            }
//            LogStream.stderr.printTimeStamped("Initialize MemoryKlueArray\tsize\t"+MK_SIZE);
//
//            MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE, kmersTemp );
//
//
//        LogStream.stderr.printlnTimeStamped("\tCOMPLETE: Opening RocksDB backends");
//
//        LogStream.stderr.printlnTimeStamped("Importing FNA file");
//
//        // ****************************MAJOR STEP***************************************
//        kddTemp.importFnaEverything(fastA, hpkfia, startEnd);
//        // ****************************MAJOR STEP***************************************
//
//        LogStream.stderr.printlnTimeStamped("\tCOMPLETE: Importing FNA file");
//
//        hpkfia.dumpToDatabase();
//        hpkfia.shutDown();
//        startEnd.shutDown();
//        kddTemp.shutDown();

        LogStream.stderr.printlnTimeStamped("Combining Databases in Order");

        // ****************************MAJOR STEP***************************************
        KidDatabaseAllDiskTemporaryCombine.combine(rocksPrefix);
        // ****************************MAJOR STEP***************************************



        LogStream.stderr.println("Construction complete.  You may now run this command for clean-up.");
        LogStream.stderr.println("\trm -r *deleteme*");

        LogStream.stderr.printlnTimeStamped("Final Time // END EXECUTION");
    }


}
