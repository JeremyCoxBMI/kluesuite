package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by osboxes on 26/08/16.
 */
public class KlatProgram {

    static int ANNOUNCE_PERIOD = 1000;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("ARG 0 : kmer database");
            System.err.println("ARG 1 : KidDatabaseRocks file");
            System.err.println("ARG 2 : input FA file");
            System.err.println("ARG 3 : min Fast Klat score");
            System.err.println("Program writes to STDOUT");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.err.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        //KLATSettingsOLD.MIN_FAST_KLAT_SCORE = Integer.parseInt(args[3]);
        KLATsettings.MIN_FAST_KLAT_SCORE = Integer.parseInt(args[3]);

        System.err.println("Loading KidDatabaseDisk\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        //KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[1]);
        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(args[1]);


        System.err.println("Loading Kmer Database\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[0], true);

        System.err.println("Loading Complete\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        AlignmentKLAT1.DEBUG = false;

        String queryName = "NONE", querySequence = "";
        SuperString querySS = new SuperString();




        try (BufferedReader br = new BufferedReader(new FileReader(args[2]))) {

            int k = 0;
            for (String line; (line = br.readLine()) != null; ) {
                if (line.charAt(0) == '>') {
                    k++;
                    //do alignment
                    querySequence = querySS.toString();
                    //Don't process empty query!
                    if (querySequence.length() > 30) {
                        System.err.println("Processing query \t" + queryName);


//                        System.err.println(new PositionList(rocksklue.get(new Kmer31(querySequence.substring(0,31)).toLong())));
                        AlignmentKLAT1 alig = new AlignmentKLAT1(querySequence, queryName, rocksklue);
//                        alig.pollKmersForPositions();
//                        alig.calculateAlignmentTables();
//                        alig.calculateSeeds();
                        System.out.print(alig.results(kd));
                        alig=null;
                    }

                    if (k % ANNOUNCE_PERIOD == 0) {
                        System.err.println("Total alignments so far : " + k + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
                    }

                    //reset
                    queryName = line.substring(1);
                    querySS = new SuperString();
                } else {
                    querySS.addAndTrim(line.replace("\n", ""));
                }

            } //end for

            br.close();

        } //end try
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        querySequence = querySS.toString();
        //Don't process empty query!
        if (querySequence.length() > 30) {
            System.err.println("Processing query \t" + queryName);


            AlignmentKLAT1 alig = new AlignmentKLAT1(querySequence, queryName, rocksklue);
            System.out.print(alig.results(kd));

//            System.err.println("Quitting, to test memory usage.");
//            exit(0);

        }
    }
}


//Database
//        /data/1/nvme/HG38build/hg38.kmers
//        KidDatabaseMemory
//        /data/1/nvme/HG38build/KidDatabaseMemory.unsafe
//        Test file
//        /data/1/nvme/HG38build/testChr11_500k.fa
