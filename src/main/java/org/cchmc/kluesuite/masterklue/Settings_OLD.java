package org.cchmc.kluesuite.masterklue;

import java.io.*;
import java.util.Properties;

/**
 * !!! DEPRECATED !!!
 *
 * Created by osboxes on 15/08/16.
 *
 * THIS IS A GREAT SETTINGS FORMAT
 * We are moving towards DATABASE defined in a ".database.txt" file, so each database gets loaded in this way.
 */
public class Settings_OLD {

    /* # # # # #
       These settings are for testing purposes, they are not for end user.
    # # # # # */
    public static String KidDbLocation = "/mnt/vmdk/kluesuite/kmerpos_KID_DB.dat.bin";
    public static String VirusFNA = "/mnt/Dshare/genomes.NCBI/viral.fna";
    public static String BacteriaFNA = "/mnt/Dshare/genomes.NCBI/bacteria.fna";
    public static String ArchaeaFNA = "/mnt/Dshare/genomes.NCBI/archaea.fna";
    public static String FungiFNA = "/mnt/Dshare/genomes.NCBI/fungi.fna";

    /* # # # # #
       These settings are for testing purposes, they are not for end user.
    # # # # # */



    public static String SettingsPath = "/home/osboxes/workspace/kluesuite/src/main/java/Settings_OLD.properties";
    public static Properties prop = null;

//    public static String RocksDbKmer31 = "/mnt/vmdk/kluesuite/rocksdbkmer31";
    public static String RocksDbKmer31 = "/mnt/Dshare/kluesuite.microbiome/microbiome";

    //For using RocksDB to store raw sequences
    //RocksDB database, stores DnaBitStrings only
        //In theory, as these encode to interger (32bit), they could be in the main database using byte[4] keys
        //However, separation of concerns is a good thing.  Especially since the new database may be built using C++.
    public static String RocksDbDnaBitString = "/mnt/Dshare/kluesuite.microbiome/clusterdnabitstring";

    //Binary file containing other information
    public static String RocksDbKidDbLocation = "/mnt/vmdk/kluesuite/kmerpos_ROCKSDB_KID_DB.dat.bin";

    /**
     * The number of files a RocksDB instance may have open at any one time.  I found 40 was too many, 20 worked fine.
     * I have no idea why or how to make a choice.
     */
    public static int MAX_FILES = 20;

    public static void createDefaultProperties() {
        prop = new Properties();
        //prop.setProperty("SettingsPath",SettingsPath);
        prop.setProperty("klue_kmer_database",RocksDbKmer31);
        prop.setProperty("klue_bitstring_database",RocksDbDnaBitString);
        prop.setProperty("klue_kid_database",RocksDbKidDbLocation);
        prop.setProperty("MAX_rocksDB_files_open","20");
    }

    public static void loadSettings(String file){
        InputStream input = null;
        try {
            input = new FileInputStream(file);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            SettingsPath = file;
            RocksDbKmer31 = prop.getProperty("klue_kmer_database");
            RocksDbDnaBitString = prop.getProperty("klue_bitstring_database");
            RocksDbKidDbLocation = prop.getProperty("klue_kid_database");
            MAX_FILES = Integer.parseInt(prop.getProperty("MAX_rocksDB_files_open"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("WARNING: Settings_OLD.loadSettings() failed");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveSettings(){
        OutputStream output = null;

        try {
            output = new FileOutputStream(SettingsPath);
            prop.store(output, "See README.md for explanation of these settings.");
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    System.err.println("WARNING: Settings_OLD.saveSettings() failed");
                    e.printStackTrace();
                }
            }

        }
    }

}
