package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.klue.KidDatabaseDisk;

import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by jwc on 3/22/18.
 */
public class Debian01BuikdKidDbDisk_B_piecemail_sequences {


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Creates a database with stored sequences from some of the KidDatabase");
            System.out.println("ARG 0  : location to place KidDatabaseDisk database (masterfile) ==> directory determined by masterfile name");
            System.out.println("ARG 1  : Chunk size");
            System.out.println("ARG 2  : Chunk number to execute");
            System.out.println("ARG 3  : FastA file used to build DB");
            System.out.println("");
            System.out.println("Your arguments were:  length = " + args.length);
            System.out.println(Arrays.toString(args));
            exit(0);
        }

        String filename = args[0];
        String filenameFA = args[3];
        int chunkSize = Integer.parseInt(args[1]);
        int chunk = Integer.parseInt(args[2]);

        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(filename);

        int size = kd.getLast();

        int numChunks = (size-1)/chunkSize + 1;

        System.err.println("There are\t"+numChunks+"\twith final chunk ending at\t"+size);
        int startIdx = 1 + chunk*chunkSize;
        int stopIdx = (chunk+1)*chunkSize;
        if (stopIdx > size) {
            stopIdx = size;
        }

        kd.startNewPiecemail(chunk);
        kd.processPiecesmailRange(startIdx,stopIdx+1, filenameFA);  //INCLUSIVE stop to EXLUSIVE end

        kd.shutDown();
    }
}
