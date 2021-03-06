package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.CombineRocksDbIntoMaster;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.System.exit;

/**
 * As CombineRocksDatabases, but takes a file with a list of databases.  THEN, makes reasonably sized chunks.
 * Saving combining them for another day.
 *
 */
public class CombineRocksDatabasesFileListSplits {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Proper syntax is ' java -cp classpath/kluesuite.jar -Xmx[arg3]m [program] [arg1 : database to make] [arg2 : list of databases to combine as txt file]'");
            exit(0);
        }

//        String[] databases = new String[args.length - 1];
//        for (int k = 1; k < args.length; k++) {
//            databases[k - 1] = args[k];
//        }

        String[] databases = null;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(args[1]));
            String line = reader.readLine();
            int size = Integer.parseInt(line);
            databases = new String[size];
            for( int k=0; k<size; k++){
                line = reader.readLine();
                databases[k] = line.trim();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            exit(0);
        }

        //unclear what this value should be
        int maxfiles = 30;
        TimeTotals time = new TimeTotals();
        time.start();
        String[] tmpdbs;
        for(int k=0; k<databases.length; k++){
            int lower = k;
            int upper = Math.min(k+40,databases.length);
            tmpdbs = new String[upper-lower];
            for(int j=0; k+j < upper; j++){
                tmpdbs[j] = databases[k+j];
            }
            System.err.println(" # * # * # Starting import on chunk beginning with "+String.format("%04d", k)+" : "+time.toHMS());
            CombineRocksDbIntoMaster crdbim = new CombineRocksDbIntoMaster(tmpdbs, args[0]+"."+String.format("%04d", k), maxfiles);
            crdbim.agglomerateAndWriteData();

        }

    }



}
