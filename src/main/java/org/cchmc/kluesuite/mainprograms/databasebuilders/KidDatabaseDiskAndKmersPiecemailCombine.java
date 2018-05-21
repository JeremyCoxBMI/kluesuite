package org.cchmc.kluesuite.mainprograms.databasebuilders;

import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.mainprograms.CombineRocksDatabases;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.exit;

/**
 * Created by jwc on 3/31/18.
 */
public class KidDatabaseDiskAndKmersPiecemailCombine {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Takes piecemail database construction into final");
            System.err.println("ARG 0 : location to place databases (path and prefix)");
            System.err.println("ARG 1 : number of chunks");
            System.err.println(Arrays.toString(args));
            exit(0);
        }

//        try {
//            LogStream.startStdStreams("./combine.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//

        LogStream.startStdStreams();


        LogStream.stderr.printTimeStamped("Synchronize time systems");

//        if (args[1].equals("0")) {
//            LogStream.stderr.printTimeStamped("Loading KidDatabaseDisk (text file)");
//            KidDatabaseDisk kdd = KidDatabaseDisk.loadFileFromText(args[0]);
//        } else {
//            LogStream.stderr.printTimeStamped("Loading KidDatabaseDisk (unsafe memory file)");
//            KidDatabaseDisk kdd = KidDatabaseDisk.loadFromFileUnsafe(args[0]);
//        }


        LogStream.stderr.printTimeStamped(" ****** Combining Kid Databases ******\n");

        LogStream.stderr.printlnTimeStamped("Building base database");
        KidDatabaseDisk kdd = new KidDatabaseDisk(args[0]+".kidDB", args[0]+".kidDB.disk",false);

        LogStream.stderr.printlnTimeStamped("import From Text");
        kdd.importKidDBText(args[0]+".kidDB.txt");

        int numChunks = Integer.parseInt(args[1]);
        int numSequences = kdd.getLast();
        int chunkSize = (numSequences +numChunks )/ numChunks;
        int lowerBound, upperBound;


        LogStream.stderr.printlnTimeStamped("Extracting exceptions array");
        int k=0;
        File tmp = new File(args[0]+".kidDB."+String.format("%02d", k));
        ArrayList<String> filePartNames = new ArrayList<>(100);
        //filePartNames.add(args[0]+".kmer");
        String pieceName = args[0]+".kidDB."+String.format("%02d", k);

        tmp = new File(pieceName);
        LogStream.stderr.println("checking exists\t"+pieceName+"\t"+tmp);
        while (tmp.exists()){
            lowerBound = chunkSize * k;
            upperBound = chunkSize * (k+1);
            if (lowerBound == 0) lowerBound = 1;

            KidDatabaseDisk temp = KidDatabaseDisk.loadFromFileUnsafe(pieceName);
            temp.importSpecial = true;
            temp.offset = lowerBound - 1;

            for (int z=lowerBound; z < upperBound; z++){
                HashMap<Integer,Character> tempy = temp.getExceptions(z);
                kdd.putExceptions(z, tempy);
            }

            //iterate
            k++;
            pieceName = args[0]+".kmer."+String.format("%02d", k);
            tmp = new File(pieceName);
            LogStream.stderr.println("checking exists\t"+pieceName+"\t"+tmp);
            temp.shutDown();
        }

        LogStream.stderr.printlnTimeStamped("Saving unsafe KidDatabaseDisk");
        kdd.saveToFileUnsafe();

        kdd.shutDown();



        LogStream.stderr.printTimeStamped(" ****** Combining Kmer Databases ******\n");

        k=0;
        tmp = new File(args[0]+".kmer."+String.format("%02d", k));
        filePartNames = new ArrayList<>(100);
        filePartNames.add(args[0]+".kmer");
        pieceName = args[0]+".kmer."+String.format("%02d", k);

        tmp = new File(pieceName);
        LogStream.stderr.println("checking exists\t"+pieceName+"\t"+tmp);
        while (tmp.exists()){
            filePartNames.add(pieceName);



            //iterate
            k++;
            pieceName = args[0]+".kmer."+String.format("%02d", k);
            tmp = new File(pieceName);
            LogStream.stderr.println("checking exists\t"+pieceName+"\t"+tmp);
        }

        LogStream.stderr.println("NOT FOUND\t"+pieceName+"\t"+tmp);
        LogStream.stderr.println("files\n"+(String[])filePartNames.toArray());
        CombineRocksDatabases.main((String[])filePartNames.toArray());

        LogStream.stderr.printTimeStamped(" ****** Combining Kmer startEnd Databases ******\n");

        k=0;
        filePartNames = new ArrayList<>(100);
        filePartNames.add(args[0]+".startEnd");
        pieceName = args[0]+".startEnd."+String.format("%02d", k);
        tmp = new File(pieceName);
        LogStream.stderr.println("checking exists\t"+pieceName+"\t"+tmp);
        while (tmp.exists()){
            filePartNames.add(pieceName);

            //iterate
            k++;
            pieceName = args[0]+".startEnd."+String.format("%02d", k);
            tmp = new File(pieceName);
            LogStream.stderr.println("checking exists\t"+pieceName+"\t"+tmp);
        }
        LogStream.stderr.println("NOT FOUND\t"+pieceName+"\t"+tmp);
        LogStream.stderr.println("files\n"+(String[])filePartNames.toArray());
        CombineRocksDatabases.main((String[])filePartNames.toArray());


        //TODO:  need special class to combine them; cannot use PositionList representation in CombineRocksDatabases class
        LogStream.stderr.printTimeStamped(" ****** Combining KidDatabaseDisk sequence databases ******\n");
        k=0;
        tmp = new File( args[0]+".kidDB.disk."+String.format("%02d", k));
        filePartNames = new ArrayList<>(100);
        filePartNames.add( args[0]+".kidDB.disk");
        pieceName = args[0]+".kidDB.disk."+String.format("%02d", k);
        tmp = new File(pieceName);
        while (tmp.exists()){
            filePartNames.add(pieceName);


            //iterate
            k++;
            pieceName = args[0]+".kidDB.disk."+String.format("%02d", k);
            tmp = new File(pieceName);
        }

        LogStream.stderr.println("NOT FOUND\t"+pieceName+"\t"+tmp);
        LogStream.stderr.println("files\n"+(String[])filePartNames.toArray());
        CombineRocksDatabases.main((String[])filePartNames.toArray());

    }


}
