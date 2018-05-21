package org.cchmc.kluesuite._oldprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.DnaBitStringToDbCheckAll;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;
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

public class vKLUEdatabaseBuildStep02VerifySequences {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Takes a DnaBitString database and KidDatabaseMemory database and builds kmer database.");
            System.out.println("ARG 0 : location to KidDatabaseMemory (i.e. memory only version)");
            System.out.println("ARG 1 : location kmer database");
            System.out.println("ARG 2 : maixmum number of entries expectected (Millions, integer)");
            //System.out.println("ARG 2 : location DnaBitString database");
            exit(0);
        }

//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFileNoRocks(args[0]);
//        rkd.restartDb();


        KidDatabaseMemory rkd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

        RocksDbKlue rocksklue = new RocksDbKlue(args[1],true);


        int arraySize = 300*1000000;
        arraySize = Integer.parseInt(args[2]) * 1000 * 1000;
        System.err.println("Estimated memory usage for HeapKlue :: "+16*arraySize+" MB");
        //OSBOXES
        //arraySize = 500*1000*1000;

        //debug
//        arraySize = 150*1000*1000;

        int haltSize = arraySize/2;
        MemoryKlueHeapFastImportArray hklue;  //2017-03-29  BUG: not initiliazed?

        DnaBitString dns;
        DnaBitStringToDbCheckAll td;


        Iterator<String> it = rkd.nameIterator();

        PositionList pl = new PositionList();

        MemoryKlueTreeMap t, prev;
//        prev = new HeapKlueHashMap.KVpair(-1,-1);

        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        while (it.hasNext()){
            String s = it.next();

            //do not process names with underscore, "FAKE" is not entry to process
            if (s.indexOf('_')==-1 && !s.equals("FAKE") ) {
//                hklue = new MemoryKlueHeapFastImportArray(arraySize);
                int KID = rkd.getKid(s);
                dns = rkd.getSequence(KID);
//                System.err.println("DNS first 70\t"+dns.getSequence(11035,11099));
                System.err.println("\nDNS first 70\t"+dns.getSequence(0,70));


                DnaBitStringToDbCheckAll dtd = new DnaBitStringToDbCheckAll(dns, rocksklue, KID);
                System.err.println("\nDNS CHECK\t");
                dtd.writeAllPositions();
            }
//                td = new DnaBitStringToDbHeapKlueFast(dns, hklue, KID);
//
//
//                td.writeAllPositions();
//                System.out.print(KID+"\t"+s+"\t ... was imported|\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//
//                hklue.resetLastToMax();
//                //hklue.heapify();
////                System.err.println("***DEBUG***\tlastIdx\t"+hklue.lastIdx);
//                System.err.println("\t\tHeapify completed	"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
////                System.err.println("***DEBUG***\tlastIdx\t"+hklue.lastIdx);
//
//                int k=-1;
//                while (hklue.hasNext()){
////                    System.err.println("\t\tExporting to Disk");
//                    t = hklue.remove();
//                    if (t.key != prev.key) {
//                        //rocksklue.put(t.key, t.value);
//
//                        //short circuits on null / may not exist
//                        pl.addWithTrim(rocksklue.get(t.key));
////                        System.err.println("Adding Kmer to Disk\t"+new Kmer31(t.key)+"\tpl:\t"+pl);
////                        rocksklue.put(t.key, pl.toArrayListLong());
//                        rocksklue.append(t.key, pl);
//                        k++;
//                        if (k % (50*1000*1000) == 0){
//                            System.out.println("\t\tWriting k-mer "+ k /1000/1000+" million\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//                        }
//
//                        //DEBUG
////                        pl = new PositionList( rocksklue.get(t.key) );
////                        System.err.println("Verify was written\t"+new Kmer31(t.key)+"\tpl:\t"+pl);
//
//                        //System.out.println("Adding key, value pair\n\t" + prev.key + "\n\t" + pl);
//                        //System.out.println(prev.key + "\t" + Arrays.toString(pl.toLongArray()));
//                        pl = new PositionList(t.value);
//                        //if (k % (maxSize/10) == 0) System.out.println("\t\texport progress: "+k/1000.0/1000.0+" million\t"+writeTime);
//                    } else {
//                        System.err.println("WARNING  Found identical key in new memoryklue.remove().");
//                        pl.addWithTrim( t.value );
//                    }
//
//                    prev = t;
//                }
//                //debug
//                //break;  // while (it.hasNext())
//            }


        }

        rocksklue.shutDown();
    }


}
