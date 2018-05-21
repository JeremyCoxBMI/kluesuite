package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.FileNotFoundException;

/**
 * Created by jwc on 7/10/17.
 */
public class TestMemoryKlueHeapFastImportArrayImport {

    public static void main(String[] args) {

        MemoryKlueHeapFastImportArray hklue;  //2017-03-29  BUG: not initiliazed?
        hklue = new MemoryKlueHeapFastImportArray(10*1000*1000);

        KidDatabaseMemory kd = new KidDatabaseMemory();
        try {
            //kd.importFNA("/data/1/nvme/chr1only/hg38.chr1.short.fa");
            kd.importFNA("/data/1/nvme/hg38.final.kmers/");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        DnaBitString dns = new DnaBitString("ACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACT");

        //RocksDbKlue rocksklue = new RocksDbKlue("./deleteme", true);
        RocksDbKlue rocksklue = new RocksDbKlue("./deleteme", true);



//        dns = kd.getSequence(1);
//        DnaBitStringToDb td = new DnaBitStringToDb(dns, hklue, 1);
//        td.writeAllPositions();
//
//        hklue.heapify();
//
//        PositionList pl= new PositionList();
//        HeapKlueHashMap.KVpair t;
//
//        //prev will not match first read, using Sentinel=-5
//        HeapKlueHashMap.KVpair prev = new HeapKlueHashMap.KVpair(-5, null);
//
//
//
//
//        RocksDbKlue rocksklue = new RocksDbKlue("./deleteme", false);
//
//        int k=0;
//        while (hklue.hasNext()){
//            t = hklue.remove();
//
//            if (t.key != prev.key) {
//
////                System.out.println("Adding\t"+new Kmer31(t.key)+"\n"+new PositionList(t.value));
//                rocksklue.put(t.key, t.value);
//                k++;
//                //if (k % (maxSize/10) == 0) System.out.println("\t\texport progress: "+k/1000.0/1000.0+" million\t"+writeTime);
//            } else {
//                System.out.println("WARNING  Found identical key in new memoryklue.remove().");
//                //pl.addAndTrim( t.value );
//            }
//
//            prev = t;
//        }


//        kd.addAndTrim(new Kid("first"));
//        kd.addAndTrim(new Kid("second"));
//        kd.addAndTrim(new Kid("third"));

        AlignmentKLAT1 alig;
        kd.add(new Kid("hg38chr1"));

//        String test01 = "ACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACT";
//        kd.storeSequence(2,new DnaBitString(test01));
//
//        alig = new AlignmentKLAT1(test01, "test01", rocksklue);
//        //results is a "do everything" function
//        System.out.print(alig.results(kd));

        // >hg38chr1||10057||10157
        //               ACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCCTAACCCT

        String test02 = "ACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCCTAACCCT";
        kd.storeSequence(3,new DnaBitString(test02));

//        alig = new AlignmentKLAT1(test02, "test02", rocksklue);
//        System.out.print(alig.results(kd));


//        jwc@kluedebian:/data/1/f/simulations.test/klat$ head -n 47444 ../simulated.1mill_random.variants.reads.fa.scrubbed.fa | tail
//          >hg38chr1||16927159||16927992||rs544153565[S]<16927246|C/G>
//          AACCAGGCCACACAGAAACACTCCAATTCACAGAATATGCACAGTGTTAGCCGCACACACAGCCAGATGCCACACAACCCCACACAGTCACAGAACAG
        String test03 = "AACCAGGCCACACAGAAACACTCCAATTCACAGAATATGCACAGTGTTAGCCGCACACACAGCCAGATGCCACACAACCCCACACAGTCACAGAACAG";


        alig = new AlignmentKLAT1(test03, "test03", rocksklue);
        System.out.print(alig.results(kd));

    }

}
