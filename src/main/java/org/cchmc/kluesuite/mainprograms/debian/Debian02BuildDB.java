package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import static java.lang.System.exit;


import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Created by jwc on 2/6/18.
 * See Debian01BuikdKidDbDisk for plan of program series.
 */
public class Debian02BuildDB {

    //periodicity with which KID import reported
    static int PERIOD = 1000;

    public static void main(String[] args) {

        //to be run multiple parts in parallel
        // so you run chunk number 0, 1, 2, 3, .... in parallelel
        if (args.length != 8) {
            System.out.println("Takes a Kid Database and builds kmer database");
            System.out.println("ARG 0 : location to Kid Database Disk");
            System.out.println("ARG 1 : location Kmer Database to build");
            System.out.println("ARG 2 : location (START and END) Kmer Database to build");  //contains start and end sequences
            System.out.println("ARG 3 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            System.out.println("ARG 4 : kid chunk number to start (first is 0)" );
            System.out.println("ARG 5 : size of kid chunks");
            System.out.println("ARG 6 : source fna file");
            System.out.println("ARG 7 : number of exprected KID");
            exit(0);
        }
//        LogStream.startStdStreams();


        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
//        KidDatabaseDisk kd = new KidDatabaseDisk("junk","junk2",false);  //DEBUG FAKE

        //KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(args[0]);

        String FNAfile = "/data/5/a/refseq_genomes_dump/bacteria.metatranscriptome.2018.02.07.fna";
        KidDatabaseMemory.squelch = true;
        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFnaFile(args[6]  ,Integer.parseInt(args[7]) + 5, args[0]+".disk");


        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[1],false); // false = read and write
        RocksDbKlue startEndKlue = new RocksDbKlue(args[2],false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.out.println("\nInitialize MemoryKlueArray " + tt.toHMS() + "\n");
        long MK_SIZE = Integer.parseInt(args[3])*1000L*1000L;
        System.out.println("\n\t(DEBUG) Initialize MemoryKlueArray ARRAY SIZE:\t" + MK_SIZE + "\t"+(int)MK_SIZE+"\n");
        if (MK_SIZE > Integer.MAX_VALUE) {
            MK_SIZE = (long) (Integer.MAX_VALUE);
        }

        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE,rocksklue );

//        ArrayList<Integer> kidList = new ArrayList<Integer>();
//        System.out.println("\nkid import from FastA begins " + tt.toHMS() + "\n");
//
//        try (BufferedReader br = new BufferedReader(new FileReader(args[4]))) {
//
//            for (String line; (line = br.readLine()) != null; ) {
//                if (line.charAt(0) == '>'){
//                    int t =kd.getKid(line.substring(1));
//                    if (t > 0){
//                        kidList.add(t);
//                    } else {
//                        System.err.println("\tWARNING\tthis sequence name not found: "+line.substring(1));
//                    }
//                }
//            }
//
//        } catch (FileNotFoundException e) {
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        int chunk = Integer.parseInt(args[4]);
        int chunk_size = Integer.parseInt(args[5]);
        int start = chunk * chunk_size + 1;

        for (int k=0; k <= chunk_size; k++) {  //using 1 index counting
            int kid = start + k;
            if (k % PERIOD == 0)
                System.err.println("Starting import or KID\t" + kid + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

            if ( kid <= kd.getLast()) {
                DnaBitStringToDb dbstd = new DnaBitStringToDb(kd.getSequence(kid), hpkfia, kid);
                dbstd.writeAllPositions(startEndKlue);
            }
            else {
                System.err.println("Ending, this KID is above maximum:\t" + kid);
                break;
            }

            if (k % PERIOD == 0)
                System.err.println("\tKID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        }
        //dump remaining kmers in memory klue
        //will use new file if necessary
        hpkfia.dumpToDatabase();

        kd.shutDown();

    }//end main
}
