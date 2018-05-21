package org.cchmc.kluesuite;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.builddb.DnaBitStringToDbCheckAll;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.KVpair;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.klue.PositionList;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;

import static java.lang.System.exit;

/**
 * Created by osboxes on 19/05/17.
 */
public class TestHeapify {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes a DnaBitString database and KidDatabaseMemory database and builds kmer database.");
            System.out.println("ARG 0 : location to KidDatabaseMemory (i.e. memory only version)");
            System.out.println("ARG 1 : location kmer database");
            //System.out.println("ARG 2 : location DnaBitString database");
            exit(0);
        }

//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks(args[0]);
//        rkd.restartDb();


        KidDatabaseMemory rkd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

//        RocksDbKlue rocksklue = new RocksDbKlue(args[1],true);


        int arraySize = 1000*1000000;
        //OSBOXES
        //arraySize = 500*1000*1000;

        //debug
        arraySize = 150*1000*1000;

        int haltSize = arraySize/2;
        MemoryKlueHeapFastImportArray hklue;  //2017-03-29  BUG: not initiliazed?

        DnaBitString dns;
        DnaBitStringToDbCheckAll td;


        Iterator<String> it = rkd.nameIterator();

        PositionList pl = new PositionList();
        KVpair t, prev;
        prev = new KVpair(-1,-1);

        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        hklue = new MemoryKlueHeapFastImportArray(arraySize);

        System.out.print("Synchronize time systems\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        while (it.hasNext()){
            String s = it.next();

            //do not process names with underscore, "FAKE" is not entry to process
            if (s.indexOf('_')==-1 && !s.equals("FAKE") ) {

                int KID = rkd.getKid(s);
                dns = rkd.getSequence(KID);
//                System.err.println("DNS first 70\t"+dns.getSequence(11035,11099));


                DnaBitStringToDb dtd = new DnaBitStringToDb(dns, hklue, KID);
                dtd.writeAllPositions();
            }
        }


        System.out.println("\nPUSHED\n");
        int k = 0;
        for (k=0; k<10; k++){

            System.out.println(hklue.keys[k]+"\t"+new Position(hklue.values[k]));
        }

        hklue.heapify();

        System.out.println("\nHEAPIFIED\n");

        k=0;
        for (k=0; k<10; k++){

            System.out.println(hklue.keys[k]+"\t"+new Position(hklue.values[k]));
        }


        System.out.println("\nHEAPIFY PULL\n");
        KVpair kv;

        NumberFormat formatter = new DecimalFormat();
        formatter = new DecimalFormat("0.######E0");
        //System.out.println(formatter.format(maxinteger));


        for (k=0; k<30; k++){
            kv = hklue.remove();
            System.out.println(formatter.format(kv.key)+"\t"+new PositionList(kv.value));
        }



        //hklue.shutdown();

//        rocksklue.shutDown();
    }


}
