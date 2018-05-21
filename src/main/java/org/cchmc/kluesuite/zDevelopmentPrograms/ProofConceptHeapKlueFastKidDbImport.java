package org.cchmc.kluesuite.zDevelopmentPrograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KlueDataImport;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.memoryklue.KVpair;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by osboxes on 27/09/16.
 */
public class ProofConceptHeapKlueFastKidDbImport {

    public static void main(String[] args) {

//        if (args.length != 4) {
//            System.out.println("ARG 1 : RockKidDb bin file");
//            System.out.println("ARG 2 : RockKidDb dnabitstring database");
//            System.out.println("ARG 3 : Location of database to create");
//            System.out.println("ARG 4 : Resume number; where to start analysis");
//            exit(0);
//        }
//
//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks(args[0]);
//        rkd.openNewRocksDbDnaBitString(args[1]);
//        RocksDbKlue klue = new RocksDbKlue(args[2],false);

//        RocksDbKlue klue = new RocksDbKlue("/mnt/vmdk/junk88",false);

        String inputFile = "/mnt/Dshare/fasta/test4.fna";

//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks("/mnt/vmdk/kluesuite/kmerpos_ROCKSDB_KID_DB.dat.bin");
//        rkd.openNewRocksDbDnaBitString("/mnt/Dshare/kluesuite.microbiome/clusterdnabitstring");
//        RocksDbKlue klue = new RocksDbKlue("/mnt/vmdk/testdevil636",false);



        KidDatabaseMemory rkd = new KidDatabaseMemory();
        try {
            rkd.importFNAold(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //int start = 4;


        //NEED 48 bytes minimum per entry in memory
        //Let's round it up to 50 just to be sure
        // 4E9 / 50 = 8E7
        int chunkSize4GB = 80 * 1000 * 1000;

        MemoryKlueHeapFastImportArray hklue = new MemoryKlueHeapFastImportArray(chunkSize4GB);

        //int start = 4 + Integer.parseInt(args[3]);

        int period = 50;    //50 file parts
        int thesize = rkd.size();

        DnaBitString dns;
        DnaBitStringToDb td;
        TimeTotals tt = new TimeTotals();
        tt.start();

        //DEBUGGING
        period = 50;
        //resuming
        //start = 26004;


        KlueDataImport kdi = new KlueDataImport(hklue, rkd);
        kdi.squelchVerify = true;
        try {
            kdi.readFNAfileSKIPto(inputFile,"");
        } catch (IOException e) {
            e.printStackTrace();
        }


        hklue.heapify();

        System.err.println("\tSimulate Writing HeapKlueHashMap to RocksDB file ");


        PositionList pl;
        KVpair t = hklue.remove();

        //Initial read
        pl = new PositionList(t.value);
        KVpair prev = t;

        int k=0;
        while (hklue.hasNext()){
            t = hklue.remove();
            if (t.key != prev.key) {
                //rocksklue.put(t.key, t.value);
                //System.out.println("Adding key, value pair\n\t" + prev.key + "\n\t" + pl);
                System.out.println(prev.key + "\t" + Arrays.toString(pl.toLongArray()));
                pl = new PositionList(t.value);
                k++;
                //if (k % (maxSize/10) == 0) System.out.println("\t\texport progress: "+k/1000.0/1000.0+" million\t"+writeTime);
            } else {
                System.err.println("WARNING  Found identical key in new remove.");
                pl.add( t.value );
            }

            prev = t;
        }
        //rocksklue.shutDown()
    }
}
