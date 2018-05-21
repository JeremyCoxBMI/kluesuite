package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.rocksDBklue.MultipleDatabaseReadOnlyRocksKlue;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.variantklue.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by osboxes on 01/08/2017
 */

public class vKlatProgram {

    boolean DEBUG = false;
    static int ANNOUNCE_PERIOD = 1000;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("ARG 0 : variant database (Rocks)");
            System.err.println("ARG 1 : KidDatabaseMemory");
            System.err.println("ARG 2 : FastA query file");
            System.err.println("ARG 3+ : Kmer database(s)");
            System.err.println("Program writes to STDOUT");
            exit(0);
        }


        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.err.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Loading KidDatabaseMemory\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        KidDatabaseMemory kd = new KidDatabaseMemory();
        kd.loadNumbers(args[1]);






//        System.err.println("Loading Variant Database Disk\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//        VariantDatabaseDisk vd = new VariantDatabaseDisk(args[0],true);

        System.err.println("Loading Kmer Database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KLUE rocksklue;

        if (args.length == 4) { //one database
            rocksklue = new RocksDbKlue(args[3], true);
        } else {
            System.err.println("Loading Multi-file Kmer Database");
            String[] DBlist = new String[args.length-4];
            for (int k=4; k< args.length; k++){
                DBlist[k-4] = args[k];
            }
            rocksklue = new MultipleDatabaseReadOnlyRocksKlue(DBlist);
        }

        System.err.println("Loading Variant Database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseDisk vd = new VariantDatabaseDisk(args[0], true);
        System.err.println("Loading Complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        System.err.println("Verify VariantDatabaseOLD has data:");
//        System.err.println(vd.getIndel(1, new pair(9000,11000)));


        String queryName = "NONE", querySequence ="";
        SuperString querySS = new SuperString();

        try(BufferedReader br = new BufferedReader(new FileReader(args[2]))) {

            int k = 0;
            for(String line; (line = br.readLine()) != null; ) {
                if (line.charAt(0) == '>') {
                    k++;
                    //do alignment
                    querySequence = querySS.toString();
                    //Don't process empty query!
                    if (querySequence.length() > 30) {
                        System.err.println("Processing query \t"+ queryName);

                        AlignmentKLAT1 alig = new AlignmentKLAT1(querySequence,queryName,rocksklue);
                        alig.resultsVariant(kd,vd);  //only difference from KLAT
                    }

                    if (k % ANNOUNCE_PERIOD == 0){
                        System.err.println("Total alignments so far : "+k+"\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
                    }

                    //reset
                    queryName = line.substring(1);
                    querySS = new SuperString();
                } else {
                    querySS.addAndTrim(line.replace("\n",""));
                }

            } //end for

            br.close();

        } //end try
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}



