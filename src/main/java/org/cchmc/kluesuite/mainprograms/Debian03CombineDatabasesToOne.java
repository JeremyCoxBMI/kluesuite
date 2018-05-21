package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;

import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by jwc on 2/22/18.
 */
public class Debian03CombineDatabasesToOne {

    //Proper syntax is '[arg0 : database to make] [arg1: frequency to write] [args2+ : >= 1 databases to combine]'
    public static void main(String[] args) {

        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        if (args.length < 3) {
            System.err.println("Proper syntax is '[arg0 : database to make] [arg1 : grequency to write] [args1+ : >= 1 databases to combine]'");
            System.err.println("Alternatively, can be used to convert an out-of-order database to a written-in-order database, by using 1 database in old arguments.");
            exit(0);
        }

        String[] databases = new String[args.length - 2];
        int frequency = Integer.parseInt(args[1]);

        for (int k = 2; k < args.length; k++) {
            databases[k - 2] = args[k];
        }

        //unclear what this value should be
        int maxfiles = 30;


        long resumePoint = 0L;
        resumePoint = 2185045312656031220L;


        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, args[0], maxfiles, resumePoint);
        crdbim.agglomerateAndWriteData(frequency);

        System.out.print("Finished computations\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");
    }
}
