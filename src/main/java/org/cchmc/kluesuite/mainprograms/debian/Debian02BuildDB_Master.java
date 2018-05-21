package org.cchmc.kluesuite.mainprograms.debian;

import static java.lang.System.exit;

/**
 * Created by jwc on 2/6/18.
 * See Debian01BuikdKidDbDisk for plan of program series.
 */
public class Debian02BuildDB_Master {

    //periodicity with which KID import reported
    static int PERIOD = 1000;

    public static void main(String[] args) {

        //to be run multiple parts in parallel
        // so you run chunk number 0, 1, 2, 3, .... in parallelel
        if (args.length != 6) {
            System.out.println("Takes a Kid Database and builds kmer database");
            System.out.println("ARG 0 : location to Kid Database Disk");
            System.out.println("ARG 1 : location Kmer Database to build");
            System.out.println("ARG 2 : location (START and END) Kmer Database to build");  //contains start and end sequences
            System.out.println("ARG 3 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            System.out.println("ARG 4 : kid chunk number to start (first is 0)" );
            System.out.println("ARG 5 : size of kid chunks");
            exit(0);
        }
//        LogStream.startStdStreams();


        int chunk = Integer.parseInt(args[4]);
        int chunk_size = Integer.parseInt(args[5]);

        String DBname = args[1];

        for (;chunk*chunk_size < (12*1000*1000); chunk++ ) {
            args[1] = DBname + "."+chunk;
            args[4] = Integer.toString(chunk);
            System.out.println("Building DB part named "+args[4]);
            Debian02BuildDB.main(args);
        }

    }//end main
}
