package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import static java.lang.System.exit;

/**
 * Created by osboxes on 02/09/16.
 *
 * Modified 4/24/2017 to accept 1 database argument to combine.
 *
 * This also removes any duplicates and sorts the value.
 */
public class CombineRocksDatabasesResume {

    public static void main(String[] args) {

        if (args.length < 3) {
            System.err.println("Proper syntax is ' java -cp classpath/kluesuite.jar -Xmx[memory]m [program] [arg0 : database to make] [arg1 : resume sequence] [args : >= 1 databases to combine]'");
            System.err.println("Alternatively, can be used to convert an out-of-order database to a written-in-order database, by using 1 database in old arguments.");
            exit(0);
        }

        for (int k=0; k<args.length; k++)   System.out.println("arg\t"+k+"\t"+args[k]);
        System.out.println();

        Kmer31 resume = new Kmer31(args[1]);
        System.out.println("resuming on sequence\t"+args[1]+"\t"+resume);

        String[] databases = new String[args.length - 2];
        for (int k = 2; k < args.length; k++) {
            databases[k - 2] = args[k];
        }

        //unclear what this value should be
        int maxfiles = 30;
        CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(databases, args[0], maxfiles, resume.toLong());

        crdbim.agglomerateAndWriteData();
    }

}
