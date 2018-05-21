package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;

import static java.lang.System.exit;

/**
 * Created by osboxes on 02/09/16.
 *
 * Modified 4/24/2017 to accept 1 database argument to combine.
 *
 * This also removes any duplicates and sorts the value.
 */
public class CombineRocksDatabases {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Proper syntax is '[arg0 : database to make] [args1+ : >= 1 databases to combine]'");
            System.err.println("Alternatively, can be used to convert an out-of-order database to a written-in-order database, by using 1 database in old arguments.");
            exit(0);
        }

        String[] databases = new String[args.length - 1];
        for (int k = 1; k < args.length; k++) {
            databases[k - 1] = args[k];
        }

        //unclear what this value should be
        int maxfiles = 30;
        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, args[0], maxfiles);
        crdbim.agglomerateAndWriteData();
    }

}
