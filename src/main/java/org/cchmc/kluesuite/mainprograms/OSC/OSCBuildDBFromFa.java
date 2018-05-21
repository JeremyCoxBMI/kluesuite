package org.cchmc.kluesuite.mainprograms.OSC;

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
 */
public class OSCBuildDBFromFa {


    public static void main(String[] args) {

        if (args.length != 4) {
            System.out.println("Takes a FastA file and builds kmer database");
            System.out.println("ARG 0 : location to Kid Database Disk");
            System.out.println("ARG 1 : location Kmer Database to build");
            System.out.println("ARG 2 : location (START and END) Kmer Database to build");  //contains start and end sequences
            System.out.println("ARG 3 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            System.out.println("ARG 4 : FastA file (must already be imported via DnaBitSTring to KidDatabaseMemory)");
            exit(0);
        }



        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(args[0]);

        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[1],false); // false = read and write
        RocksDbKlue startEndKlue = new RocksDbKlue(args[2],false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.out.println("\nInitialize MemoryKlueArray " + tt.toHMS() + "\n");
        long MK_SIZE = Integer.parseInt(args[3])*1000L*1000L;
        if (MK_SIZE > Integer.MAX_VALUE) {
            MK_SIZE = (long) (Integer.MAX_VALUE);
        }

        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE,rocksklue );

        ArrayList<Integer> kidList = new ArrayList<Integer>();
        System.out.println("\nkid import from FastA begins " + tt.toHMS() + "\n");

        try (BufferedReader br = new BufferedReader(new FileReader(args[4]))) {

            for (String line; (line = br.readLine()) != null; ) {
                if (line.charAt(0) == '>'){
                    int t =kd.getKid(line.substring(1));
                    if (t > 0){
                        kidList.add(t);
                    } else {
                        System.err.println("\tWARNING\tthis sequence name not found: "+line.substring(1));
                    }
                }
            }

        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }



        for (int kid : kidList) {
            System.err.println("Starting import or KID\t" + kid + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

            DnaBitStringToDb dbstd = new DnaBitStringToDb(kd.getSequence(kid),hpkfia,kid);
            dbstd.writeAllPositions(startEndKlue);


            System.err.println("KID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        }
        //dump remaining kmers in memory klue
        //will use new file if necessary
        hpkfia.dumpToDatabase();

    }//end main
}
