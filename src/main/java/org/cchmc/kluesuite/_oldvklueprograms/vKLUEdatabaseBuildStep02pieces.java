//package org.cchmc.kluesuite._oldvklueprograms;
//
//import org.cchmc.kluesuite.TimeTotals;
//import org.cchmc.kluesuite.builddb.FastaToRocksKluePieces;
//import org.cchmc.kluesuite.klue.KidDatabaseMemory;
//
//import java.io.FileNotFoundException;
//import java.sql.Timestamp;
//
//import static java.lang.System.exit;
//
///**
// * Created by osboxes on 20/04/17.
// *
// *
// * REMAKE of HumanVariantDatabaseBuildStep0X  series
// * Using new classes, new strategies.  Starting over from scratch.
// *  -- using Kryo
// *  -- using memory only objects except for kmer
// *  -- not including many human sequences with "_" in name
// *
// * 1)  KidDatabaseMemory / DnaBitString database
// * 2)  Build normal K-mer database
// * 3)  Build VariantDatabaseOLD
// * 4)  Write Variants to K-mer database
// * 5)  Recompile K-mer in-order database and in-order 16 part databases
// * 6)  Per your option, convert
// *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
// * 7) Update KidDatabaseMemory with detailed entries (optional)
// */
//
//public class vKLUEdatabaseBuildStep02pieces {
//
//    public static void main(String[] args) {
//        if (args.length != 4) {
//            System.out.println("Takes a FastA file and builds kmer database, 1 database per sequence.");
//            System.out.println("ARG 0 : location to FastA file (all)");
//            System.out.println("ARG 1 : location to FastA file (one sequence or many)");
//            System.out.println("ARG 2 : location kmer database to build");
//            System.out.println("ARG 3 : maixmum number of entries expectected (Millions, integer)");
//            //System.out.println("ARG 2 : location DnaBitString database");
//            exit(0);
//        }
//
//
////        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks(args[0]);
////        rkd.restartDb();
//
//
//        TimeTotals tt = new TimeTotals();
//        tt.start();
//        java.util.Date timer = new java.util.Date();
//
//        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");
//
//        int arraySize;
//        int size = Integer.parseInt(args[3]);
//        arraySize =  size * 1000 *1000;
//        //arraySize must be doubled because this is KLUEv2
//        arraySize *= 2;
//        int haltSize = arraySize / 2;
////        System.err.println("Estimated memory usage for HeapKlue :: "+16*size+" MB");
//
//
//        System.err.println("Importing FastA file\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//        KidDatabaseMemory rkd = new KidDatabaseMemory();
//        try {
//            rkd.importFNA(args[0]);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        System.err.println("Importing FastA file -- COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//
//
////        Iterator<String> it = rkd.nameIterator();
//
////        PositionList pl = new PositionList();
////        HeapKlueHashMap.KVpair t, prev;
////        prev = new HeapKlueHashMap.KVpair(-1, -1);
//
//
//        FastaToRocksKluePieces ftrkp = new FastaToRocksKluePieces(rkd,arraySize);
//        try {
//            ftrkp.importFNA(args[1],args[2]);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//}
