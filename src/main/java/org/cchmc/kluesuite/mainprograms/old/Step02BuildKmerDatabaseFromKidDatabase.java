//package org.cchmc.kluesuite.mainprograms;
//
//import org.cchmc.kluesuite.TimeTotals;
//import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
//import org.cchmc.kluesuite.klue.KidDatabaseDisk;
//import org.cchmc.kluesuite.klue.KidDatabaseMemory;
//import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
//import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
//
//import java.io.FileNotFoundException;
//import java.sql.Timestamp;
//import java.util.HashSet;
//import java.util.Set;
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
// *
// *
// * 2017-07-11   Updated to process subset of chromosomes/reference sequences more quickly
// * Supercedes   "vKLUEdatabaseBuildStep02pieces"  (first version)
// */
//
//public class Step02BuildKmerDatabaseFromKidDatabase {
//
//
//
//    // addWithTrim KidDatabase Memory
//    // AND KidDatabase Disk
//    // use a switch
//
//    public static void main(String[] args) {
//        if (args.length != 4) {
//            System.out.println("Takes a FastA file and builds kmer database, 1 database per sequence.");
//            System.out.println("ARG 0 : location to Kid Database Memory");
//            System.out.println("ARG 1 : location Kmer Database to build");
//            System.out.println("ARG 2 : range of kid to process (e.g.  1  or  8-10 (parallel piecemail processing is good)" );
//            System.out.println("ARG 3 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
//            exit(0);
//        }
//
//        long MK_SIZE = Integer.parseInt(args[3])*1000L*1000L;
//        if (MK_SIZE > Integer.MAX_VALUE) {
//            MK_SIZE = (long) (Integer.MAX_VALUE);
//        }
//
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
//
//
//        //list of Kid to process
//        Set<Integer> s = new HashSet<Integer>();//vd.getKeys();
//
//        String[] range = args[2].split("-");
//        if (range.length == 1){
//            s.addWithTrim(Integer.parseInt(range[0]));
//        } else {
//            for (int k = Integer.parseInt(range[0]); k <= Integer.parseInt(range[1]); k++){  //range is inclusive both ends
//                s.addWithTrim(k);
//            }
//        }
//
//
//
//
//        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//
//        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
//
//
//
//        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//        RocksDbKlue rocksklue = new RocksDbKlue(args[1],false); // false = read and write
//        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//
//        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE,rocksklue );
//
//
//        System.err.println("set of kid to process is : "+s);
//        //int count = 0;
//        for (Integer kid : s) {
//            System.err.println("Starting import or KID\t" + kid + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
//
//            DnaBitStringToDb dbstd = new DnaBitStringToDb(kd.getSequence(kid),hpkfia,kid);
//            dbstd.writeAllPositions();
//
//            //dump remaining kmers in memory klue
//            //will use new file if necessary
//            hpkfia.dumpToDatabase();
//            System.err.println("KID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
//        }
//
//
//
//
//
//
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
//        KidDatabaseDisk rkd = new KidDatabaseDisk("./aDelete", "./bDelete", false);
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
