package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.helperclasses.LogStream;

import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by jwc on 3/28/18.
 */
public class KidDatabaseDiskAndKmersPiecemailAutoDebian {

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

        String kidDbName = args[0]+".kidDB."+args[2];
        String rocks128Name = args[0]+".kidDB.disk."+args[2];
        String rocksName = args[0]+".kmer."+args[2];
        String startEndName = args[0]+".startEnd."+args[2];

        int numSequences = Integer.parseInt(args[5]);

        boolean makeUnsafe = (Integer.parseInt(args[4]) == 1);


        int chunk = Integer.parseInt(args[2]);
        int numChunks = Integer.parseInt(args[3]);

        int chunkSize = (numSequences +numChunks )/ numChunks;  //at least 1

        for (int k = chunk; chunk < numChunks;k++){
            LogStream.stderr.printlnTimeStamped("\n***************************************\nProcessing Chunk " + k);
            args[2] = Integer.toString(k);
            KidDatabaseDiskAndKmersPiecemail.main(args);
        }


    }
}
