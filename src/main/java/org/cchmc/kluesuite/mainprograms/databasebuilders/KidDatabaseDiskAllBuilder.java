package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabaseAllDisk;

import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by jwc on 4/12/18.
 *
 * This builds the full database, not piecemail
 */
public class KidDatabaseDiskAllBuilder {



    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Takes a FASTA dna fle and builds a KidDbDiskAll sequence database");
            System.err.println("ARG 0  : location to place databases (path and prefix)");
            System.err.println("ARG 1 : Fasta file");
            exit(0);
        }

        LogStream.startStdStreams();

        String fastA = args[0];
        String rocksPrefix = args[1];

        String rocksFinal = rocksPrefix + ".KidDB.diskall";
        String rocksTemp  = rocksPrefix + ".KidDB.deleteme.temp";

        KidDatabaseAllDisk kdd = new KidDatabaseAllDisk(rocksPrefix, rocksTemp, false);

        kdd.importFnaEverythingButKmers(fastA);

        //Write in order
        KidDatabaseAllDisk kddFinal = new KidDatabaseAllDisk(rocksPrefix, false);

        kdd.shutDown();
        kddFinal.shutDown();

        LogStream.stdout.println("Construction complete.  You may now run this command for clean-up.");
        LogStream.stdout.println("\trm -r "+rocksTemp);
    }
}
