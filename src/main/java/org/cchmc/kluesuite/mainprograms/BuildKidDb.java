package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Created by osboxes on 18/08/16.
 */
public class BuildKidDb {


    public static void importKidOnly(String inputfile, KidDatabaseMemory myKidDB){
        System.err.println("\n\t*********\n\tLoading file "+inputfile+"\n\t*********\n");
        try(BufferedReader br = new BufferedReader(new FileReader(inputfile))) {
            for(String line; (line = br.readLine()) != null; ) {
                if( line.charAt(0) == '>'){
                    myKidDB.add(new Kid(line.trim()));	//DOES NOT include "\n"
                }
            } //end for
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        java.util.Date start, finish, temp;
        start = new java.util.Date();
        System.out.println("execution begins " + new Timestamp(start.getTime()));

        KidDatabaseMemory myKidDB = new KidDatabaseMemory();


        String currentSeq = "";
        boolean ignore = true; //do not write empty sequence to database

        int currentKID = 3;
        myKidDB.add(new Kid("Reserved for testing KID 1"));
        myKidDB.add(new Kid("Reserved for testing KID 2"));
        myKidDB.add(new Kid("Reserved for testing KID 3"));


        temp = new java.util.Date();
        System.err.println("\tStep 10\t" + new Timestamp(temp.getTime()));
        //String inputfile = "viral.fna";
        String inputfile = Settings_OLD.VirusFNA;
        importKidOnly(inputfile, myKidDB);


        temp = new java.util.Date();
        System.err.println("\tStep 20\t" + new Timestamp(temp.getTime()));
        //inputfile = "fungi.fna";
        inputfile = Settings_OLD.FungiFNA;
        importKidOnly(inputfile, myKidDB);


        temp = new java.util.Date();
        System.err.println("\tStep 30\t" + new Timestamp(temp.getTime()));
        //inputfile = "bacteria.fna";
        inputfile = Settings_OLD.BacteriaFNA;
        importKidOnly(inputfile, myKidDB);


        temp = new java.util.Date();
        System.err.println("\tStep 32\t" + new Timestamp(temp.getTime()));
        //inputfile = "archaea.fna";
        inputfile = Settings_OLD.ArchaeaFNA;
        importKidOnly(inputfile, myKidDB);

        temp = new java.util.Date();
        //myKidDB.fileName = "kmerpos_KID_DB.dat.bin";
        myKidDB.fileName = Settings_OLD.KidDbLocation;
        System.err.println("\tStep 40\t" + new Timestamp(temp.getTime()) + "\t save File :> " + myKidDB.fileName);

        myKidDB.saveToFileUnsafe();

    }
}
