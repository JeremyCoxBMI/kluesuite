package org.cchmc.kluesuite._oldvklueprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.KVpair;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.Iterator;

import static java.lang.System.exit;

/**
 * Created by osboxes on 20/04/17.
 *
 *
 * REMAKE of HumanVariantDatabaseBuildStep0X  series
 * Using new classes, new strategies.  Starting over from scratch.
 *  -- using Kryo
 *  -- using memory only objects except for kmer
 *  -- not including many human sequences with "_" in name
 *
 * 1)  KidDatabaseMemory / DnaBitString database
 * 2)  Build normal K-mer database
 * 3)  Build VariantDatabaseOLD
 * 4)  Write Variants to K-mer database
 * 5)  Recompile K-mer in-order database and in-order 16 part databases
 * 6)  Per your option, convert
 *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 */

public class vKLUEdatabaseBuildStep02 {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Takes a DnaBitString database and KidDatabaseMemory database and builds kmer database.");
            System.out.println("ARG 0 : location to KidDatabaseMemory (i.e. memory only version)");
            System.out.println("ARG 1 : location kmer database");
            System.out.println("ARG 2 : maximum number of entries to hold in memory (Millions, integer)");
            //System.out.println("ARG 2 : location DnaBitString database");
            exit(0);
        }

//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks(args[0]);
//        rkd.restartDb();


        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        int arraySize;
        int size = Integer.parseInt(args[2]);
        arraySize =  size * 1000 *1000;
        //arraySize must be doubled because this is KLUEv2
        arraySize *= 2;
        int haltSize = arraySize / 2;


        System.err.println("Intializing HeapKlue : arraySize ::\t"+arraySize);

        MemoryKlueHeapFastImportArray hklue;  //2017-03-29  BUG: not initiliazed?
        hklue = new MemoryKlueHeapFastImportArray(arraySize);



        System.out.print("Loading  Kid Database from file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");
        KidDatabaseMemory rkd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
        System.out.print("Finished Kid Database from file\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");


//        System.err.println("Importing FastA file\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//
////        KidDatabaseMemory.debug = true;
//        //DnaBitStringKryoSerializer.debug = true;
//
//        KidDatabaseMemory rkd = new KidDatabaseMemory();
//        try {
//            System.out.println("\tFilename : "+args[0]);
//            rkd.importFNA(args[0]);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        System.err.println("Importing FastA file -- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());





        RocksDbKlue rocksklue = new RocksDbKlue(args[1], false);

        DnaBitString dns;

        Iterator<String> it = rkd.nameIterator();

        PositionList pl = new PositionList();
        KVpair t, prev;
        prev = new KVpair(-1, -1);



        while (it.hasNext()) {
            String s = it.next();

            //do not process names with underscore, "FAKE" is not entry to process
            if (s.indexOf('_') == -1 && !s.equals("FAKE")) {


                int KID = rkd.getKid(s);
                dns = rkd.getSequence(KID);

                DnaBitStringToDb td = new DnaBitStringToDb(dns, hklue, KID);
                td.writeAllPositions();

                hklue.dumpToDatabase();

                System.out.println(KID + "\t" + s + "\t ... was imported|\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
            }
        }

        System.out.print("Closing rocksklue");
        rocksklue.shutDown();

    }

}
