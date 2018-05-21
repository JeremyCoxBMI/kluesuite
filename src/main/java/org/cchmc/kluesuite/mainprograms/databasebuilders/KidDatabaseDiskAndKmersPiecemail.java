package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by jwc on 3/28/18.
 *
 */
public class KidDatabaseDiskAndKmersPiecemail {

    public static void main(String[] args) {
        if (args.length != 7) {
            System.err.println("Takes a FASTA dna fle and builds a KidDbDisk partial sequence database");
            System.err.println("ARG 0  : location to place databases (path and prefix)");
            System.err.println("ARG 1 : Fasta file");
            System.err.println("ARG 2 : chunk number");
            System.err.println("ARG 3 : number of chunks");
            System.err.println("ARG 4 : should the kidDbDisk unsafe file be created (0 or 1); indicate no (0) if too many sequences");
            System.err.println("\t\t\tindicate no (0) if too many sequences; text output file will be used");
            System.err.println("\t\t\tonly created using the LAST piece");
            System.err.println("ARG 5 : number sequences total");
            System.err.println("\t\t\tcan find with command grep -P '^>' <fastA file> | wc -l");
            System.err.println("ARG 6 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            System.err.println("");
            System.err.println("Your arguments were:\n  length = " + args.length);
            System.err.println(Arrays.toString(args));
            exit(0);
        }

        int chunk = Integer.parseInt(args[2]);
        int numChunks = Integer.parseInt(args[3]);
        int numSequences = Integer.parseInt(args[5]);
        boolean makeUnsafe = (Integer.parseInt(args[4]) == 1);

        int chunkSize = (numSequences +numChunks )/ numChunks;  //at least 1

        String kidDbName = args[0]+".kidDB."+String.format("%02d", chunk);
        String rocks128Name = args[0]+".kidDB.disk."+String.format("%02d", chunk);
        String rocksName = args[0]+".kmer."+String.format("%02d", chunk);;
        String startEndName = args[0]+".startEnd."+String.format("%02d", chunk);

        int lowerBound = chunkSize * chunk;
        int upperBound = chunkSize * (chunk+1);

        if (lowerBound == 0) lowerBound = 1;

        LogStream.startStdStreams();
        LogStream.stderr.println("lowerbound = "+lowerBound);
        LogStream.stderr.println("upperbound = "+upperBound);



        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();

        LogStream.stderr.printTimeStamped("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        //suppress annoying messages
        KidDatabaseMemory.squelch = true;

        //build new importFnaSequence by range
        LogStream.stderr.printlnTimeStamped("\tImporting FNA file");
//        KidDatabaseDisk kdd = new KidDatabaseDisk(kidDbName,rocks128Name,false);
//        kdd.importFnaBitSequences(args[1],lowerBound,upperBound);

        KidDatabaseDisk kdd  = KidDatabaseDisk.loadFileFromText(args[0]+".kidDB", chunk);
        int last_read = kdd.importOnlyFnaBitSequences(args[1],lowerBound,upperBound);

        KidDatabaseDisk.importSpecial = true;
        KidDatabaseDisk.offset = lowerBound - 1;



        LogStream.stderr.printlnTimeStamped("\tInitializing KLUE");
        RocksDbKlue rocksklue = new RocksDbKlue(rocksName,false); // false = read and write
        RocksDbKlue startEndKlue = new RocksDbKlue(startEndName,false); // false = read and write
        LogStream.stderr.printlnTimeStamped("\tKLUE Initialization complete");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LogStream.stderr.printTimeStamped("\nInitialize MemoryKlueArray " + tt.toHMS() + "\n");
        long MK_SIZE = Integer.parseInt(args[6])*1000L*1000L;
        LogStream.stderr.print("\n\t(DEBUG) Initialize MemoryKlueArray ARRAY SIZE:\t" + MK_SIZE + "\t"+(int)MK_SIZE+"\n");
        if (MK_SIZE > Integer.MAX_VALUE) {
            MK_SIZE = (long) (Integer.MAX_VALUE);
        }

        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE,rocksklue );

        boolean skip = true;
        if (chunk > 0) skip = false;    //must skip kid = 0
        for (int kid=lowerBound; kid < (chunkSize * (chunk+1)) && kid <= (numSequences); kid++){
            //check if kid exists
            if (kid - KidDatabaseDisk.offset >= last_read){
                break;
            }

            if (skip) {
                skip = false;
            }else {
                DnaBitStringToDb dbstd = new DnaBitStringToDb(kdd.getSequence(kid), hpkfia, kid);
                dbstd.writeAllPositions(startEndKlue);
            }
        }
        hpkfia.dumpToDatabase();

        //output database only on last piece, which is fully built
//        if (upperBound >= numSequences){
//            if (makeUnsafe) kdd.saveToFileUnsafe();
//            else kdd.saveToFileText();
//        }

        rocksklue.shutDown();
        startEndKlue.shutDown();
        kdd.shutDown();

    }
}
