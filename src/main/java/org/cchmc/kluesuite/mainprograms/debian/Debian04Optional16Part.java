package org.cchmc.kluesuite.mainprograms.debian;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;
import org.cchmc.kluesuite.multifilerocksdbklue.Rocks16Klue;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by jwc on 3/9/18.
 */
public class Debian04Optional16Part {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Arg 0  : Database path & name");
            System.out.println("Arg 1+ : List of files to combine");
            System.err.println("Alternatively, can be used to convert an out-of-order database to a written-in-order database, by using 1 database in old arguments.");
            exit(0);
        }

        Rocks16Klue klue16 = new Rocks16Klue(args[0],false);

        String[] databases = new String[args.length - 1];
//        int frequency = Integer.parseInt(args[1]);

        for (int k = 1; k < args.length; k++) {
            databases[k - 1] = args[k];
        }

        //unclear what this value should be
        int maxfiles = 30;


        long resumePoint = 0L;
        //resumePoint = 2185045312656031220L;


        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, klue16, maxfiles, resumePoint);
        crdbim.agglomerateAndWriteData();

        //shutdown input databases
        crdbim.shutDown();

        //shutdown output databases
        klue16.shutDown();


    }

}
