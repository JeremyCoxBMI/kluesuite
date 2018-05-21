package org.cchmc.kluesuite.mainprograms.old;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.builddb.AddRocksDbIntoMaster;
import org.cchmc.kluesuite.masterklue.MainProgram;

import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by osboxes on 18/09/16.
 * Program to take multiple (small) databases and addWithTrim them into a large one
 */
public class AppendRocksDb implements MainProgram {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Proper syntax is ' java -cp classpath/kluesuite.jar -Xmx[arg3]m [program] [arg1 : large database to addAndTrim small db]  [arg2+ : list of databases to addAndTrim]'");
            exit(0);
        }

        String[] databases = new String[args.length - 1];
        for (int k = 1; k < args.length; k++) {
            databases[k - 1] = args[k];
        }

        int maxfiles = 30;
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("\tTIME KEEPER\tStart  Initialization\t"+tt.toHMS());
        AddRocksDbIntoMaster ardbim = new AddRocksDbIntoMaster(databases, args[0], maxfiles);
        System.out.println("\tTIME KEEPER\tFinish Initialization\t"+tt.toHMS()+"\t"+"BEGIN APPEND");
        ardbim.agglomerateAndWriteData();
        System.out.println("\tTIME KEEPER\tProgram Complete     \t"+tt.toHMS());
    }


    @Override
    public void commandCall(CommandLine command) {
        String X = "db";
        String mainDB = command.getOptionValue(X);
        X = "extra";
        String[] databases = command.getOptionValues(X);

        int maxfiles = 30;
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("\tTIME KEEPER\tStart  Initialization\t"+tt.toHMS());
        AddRocksDbIntoMaster ardbim = new AddRocksDbIntoMaster(databases, mainDB, maxfiles);
        System.out.println("\tTIME KEEPER\tFinish Initialization\t"+tt.toHMS()+"\t"+"BEGIN APPEND");
        ardbim.agglomerateAndWriteData();
        System.out.println("\tTIME KEEPER\tProgram Complete     \t"+tt.toHMS());
    }
}
