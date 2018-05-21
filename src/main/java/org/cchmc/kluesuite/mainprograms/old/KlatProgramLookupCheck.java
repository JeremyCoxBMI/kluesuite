package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klat.KmerSequence;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Created by osboxes on 26/08/16.
 */
public class KlatProgramLookupCheck {

    static int ANNOUNCE_PERIOD = 1000;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("ARG 0 : kmer database");
            System.err.println("ARG 1 : KidDatabaseMemory file");
//            System.err.println("ARG 2 : input FA file");
//            System.err.println("Program writes to STDOUT");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.err.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        System.err.println("Loading KidDatabaseMemory\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[1]);
//        String myseq = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCG"+
//                "CGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCTCGCT";
//        kd.addWithTrim(new Kid("joker"));
//        kd.storeSequence(1,myseq);
//
//        kd.saveNumbers("/mnt/vmdk/2017.04.Human.vKLUE/goodVersion/hg38.KidDatabaseMemory.numbers.txt");

//
//        System.out.println("Check sequence imported: ");
//        try {
//            System.out.println(kd.getSequence(1, 54, 74, false));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.exit(0);
//
//
//        KidDatabaseMemory kd2 = new KidDatabaseMemory();
//        kd2.loadNumbers("/mnt/vmdk/2017.04.Human.vKLUE/goodVersion/hg38.KidDatabaseMemory.numbers.txt");
//
//
//        System.out.println("Check sequence imported: ");
//        try {
//            System.out.println(kd2.getSequence(1, 54, 74, false));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.exit(0);

//
//        KidDatabaseMemory kd = new KidDatabaseMemory();
//
//
////                boolean save = true;
//        boolean save = false;
//
//        if (save) {
//            kd.fileName = args[1];
//            try {
//                kd.importFNA(args[2]);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            kd.saveNumbers(args[1]);
//        } else {
//            kd.loadNumbers(args[1]);
//        }


        System.out.println("Check sequence imported: ");
        try {
            System.out.println(kd.getSequence(1, 11000, 11050, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.exit(0);
//
        System.err.println("Loading Kmer Database\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[0], true);

        System.err.println("Loading Complete\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
//
////        AlignmentKLAT1.DEBUG = true;

                                                //taaccctaaccctaaccctaaccctaaccctaaccctaaccctaacccta

        String seq = "                           CTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAA";

//        kd.getSequence(1,9969,10069+1,true);

        System.err.println("testing database");
        System.err.println(seq);
        try {
//            System.err.println(kd.getSequence(0,9969,10069+1,false));
            System.err.println(kd.getSequence(1,9969,10069+1,false));
        } catch (Exception e) {
            e.printStackTrace();
        }
        KmerSequence kmers = new KmerSequence(seq);

        ArrayList<PositionList> posz = rocksklue.getAllPL(kmers.getAllForward());

        for (int k=0; k < seq.length() - 31; k++){
            Kmer31 test = new Kmer31(seq.substring(k,k+31));
            System.out.println(new PositionList(rocksklue.get(test.toLong())));
            System.out.println(posz.get(k));
            System.out.println("");
        }

    }
}
