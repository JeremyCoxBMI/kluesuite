package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;

import static java.lang.System.exit;

/**
 * Created by osboxes on 30/09/16.
 *
 * Build 16 piece database by combining smaller pieces.
 * Each 16 pieces execute in parallel as separate programs.
 *
 * * Modified 4/24/2017 to accept 1 database argument to combine.
 */
public class BuildRocks16Combine {


    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Arg 0  : Database path & name");
            System.out.println("Arg 1  : database portion to create from 00 to 15 (integer)");
            System.out.println("Arg 2+ : List of files to combine");
            System.err.println("Alternatively, can be used to convert an out-of-order database to a written-in-order database, by using 1 database in old arguments.");
            exit(0);
        }

        System.out.println("There are this many arguments: "+args.length);
        System.out.println("The last one is "+args[args.length-1]);

        String[] databases = new String[args.length - 2];
        for (int k = 2; k < args.length; k++) {
            databases[k - 2] = args[k];
        }

        int part = Integer.parseInt(args[1]);
        //unclear what this value should be
        int maxfiles = 30;
        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, args[0]+"."+part, maxfiles);

        //New functions added for BuildRocks16Combine to take limits
        crdbim.seekTo( ((long) part) << 58 );                       //inclusive starting point
        crdbim.agglomerateAndWriteData( ((long)part+1) << 58 );     //exclusive stopping point
    }

}
