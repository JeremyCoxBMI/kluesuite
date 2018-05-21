package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.datastreams.FastaFile;
import org.cchmc.kluesuite.datastreams.FastaSequence;
import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.cchmc.kluesuite.multithread.KidDatabaseThreadSafe;
import org.cchmc.kluesuite.multithread.KlatMultithread;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.Iterator;

import static java.lang.System.exit;

/**
 * Created by jwc on 8/24/17.
 */
public class KlatProgramThreaded {

    static int ANNOUNCE_PERIOD = 1000;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("ARG 0 : kmer database");
            System.err.println("ARG 1 : Kid Database Disk file");
            System.err.println("ARG 2 : input FA file");
            System.err.println("ARG 3 : min Fast Klat score");
            System.err.println("Program writes to STDOUT");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.err.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        KLATsettings.MIN_FAST_KLAT_SCORE = Integer.parseInt(args[3]);

        System.err.println("Loading KidDatabaseDisk\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        //KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[1]);
        KidDatabaseDisk kd = KidDatabaseDisk.loadFromFileUnsafe(args[1]);

        System.err.println("Loading Kmer Database\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[0], true);
        System.err.println("Loading Complete\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        AlignmentKLAT1.DEBUG = false;
        KidDatabaseThreadSafe.ON = true;
        KidDatabaseThreadSafe.kd = kd;

        Iterator<FastaSequence> it = new FastaFile(args[2]).sequenceIterator();
        KlatMultithread km = new KlatMultithread(it, rocksklue);
        km.calculateAlignments();

    }
}
