package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.io.FileNotFoundException;
import java.sql.Timestamp;

/**
 * Created by jwc on 4/5/18.
 *
 * Strategy: store your metagenome as many small fasta files
 * Step 1: Build Kid Database (primaryt key) index
 * Step 2: Compile other databases piecemail
 *
 * THIS CREATES TEXT FILES THAT MUST BE CONCATENATED IN ORDER
 * regrettfully, existing functions could not accomodate this
 *
 * ORDER of concatenation does not matter.  Changing the order of the output file changes the meaning of database
 *
 * Needs to store text strings as ArrayList<>, beware memory requirement is not minimal
 */

public class KidDatabaseTextBuilder {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Takes  FASTA dna fle(s) and builds a KidDatabase, saves as text file");
            System.err.println("ARG 0  : location to place database (path and prefix)");
            System.err.println("ARG 1  : Number of sequences (may be larger than actual, but not smaller)");
            System.err.println("ARG 2+ : FastA file(s)");
        }

        LogStream.startStdStreams();

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();

        LogStream.stderr.printTimeStamped("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        //suppress annoying messages
        KidDatabaseDisk.squelch = true;




        for (int k=2; k<args.length; k++) {
            try {
                KidDatabaseDisk kdd = new KidDatabaseDisk(args[0]+"."+(k-1)+"."+"kidDb",args[0]+"."+args[k]+".disk",false);
                LogStream.stderr.printlnTimeStamped("\n\tImporting FNA file\tpart\t"+(k-1)+"\t"+args[k]);
                kdd.importFnaNoSequencesStored(args[k], Integer.parseInt(args[1]));
                LogStream.stderr.printlnTimeStamped("\tDumping Kid Database text file \t"+kdd.fileName+".txt");
                kdd.saveToFileText();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        //build new importFnaSequence by range






    }


}
