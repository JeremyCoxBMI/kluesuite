package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;

import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by jwc on 8/9/17.
 *
 * DEPRECATED
 */

public class ConvertKidDatabaseMemoryToKidDatabaseRocks {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Takes a UCSC variants file and builds entries in kmer database, as well as Variant database.");
            System.out.println("ARG 0 : location to KidDatabaseMemory Memory file");
            System.out.println("ARG 1 : location KidDatabaseMemory RocksDb file");
            System.out.println("ARG 2 : location to put RocksDb");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Loading KidDatabaseMemory\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseMemory kd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

        System.err.println("Converting to KidDatabaseRocks\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseDisk.DEBUG = true;
        KidDatabaseDisk kd2 = KidDatabaseDisk.builderKidDatabase128DBS_ShallowCopy(kd, args[1],args[2]);

        System.err.println("Saving KidDatabaseRocks\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        kd2.saveToFileUnsafe();
        System.err.println("Complete\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        kd2.shutDown();
    } //end main()

}
