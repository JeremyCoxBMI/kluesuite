package org.cchmc.kluesuite.masterklue;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by osboxes on 24/04/17.
 *
 * masterklue exists to handle the various permutations of KLUE databases.  So programs load a single argument: settings file
 *
 * all variables relevant to program execution are static members
 * static members with "human" in name are human readable strings for the settings file only
 *
 * DEPRECATED
 */

public class KLUEsettings extends Settings {


    protected KLUEsettings(){}


    /**
     * *********************************
     * STATIC MEMBERS as GLOBAL SETTINGS
     * *********************************
     */


    /**
     * Path to this settings file!  Original!
     * MOVED to Settings
     */
//    public static final String SettingsPathHuman = "KLUE_Settings_Path";
//    public static String SettingsPath = "";


    /**
     * The type of file format to load for KidDatabaseMemory
     */

    public static final String kidDatabaseFormat = "KID_DATABASE_FORMAT";
    public static final KidDatabaseType KID_DATABASE_FORMAT = KidDatabaseType.MEMORY;

    /**
     * Path to KidDatabaseMemory
     */
    public static final String kidDatabasePath = "KID_DATABASE_PATH";
    public static String KID_DATABASE_PATH = "KID-database.bin";

    /**
     * Path to RocksKidDatabase
     */
    public static final String rocksKidDatabasePath = "ROCKS_KID_DATABASE_PATH";
    public static String ROCKS_KID_DATABASE_PATH = "";


    public static final String loadVariantDatabase = "VARIANT_DATABASE_EXISTS";
    public static String  VARIANT_DATABASE_EXISTS = "no";

    public static final String variantDatabasePath = "VARIANT_DATABASE_PATH";
    public static String VARIANT_DATABASE_PATH = "variants.bin";

    public static final String klueDatabaseFormat = "KLUE_DATABASE_FORMAT";
    public static final KlueType KLUE_DATABASE_FORMAT = KlueType.LOCAL;


    public static final String kmerDatabasePath = "KMER_Database_Path";
    public static String KMER_DATABASE_PATH = "k-mers.rocksDB";


    //Static initializer sets default values
    static
    {
        set = new SettingsFile("KLUEsettings.txt", "KLUEsettings");

        // Iterating by 10, so other lines can be easily inserted (BASIC FTW)

        set.add(new SettingsFileEntry(25, kidDatabaseFormat, KID_DATABASE_FORMAT, VarType.KIDDB ));
        set.add(new SettingsFileEntry(29, "# One of these must be left blank; either/or selection", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(30, kidDatabasePath, KID_DATABASE_PATH, VarType.STRING ));
        set.add(new SettingsFileEntry(40, rocksKidDatabasePath, ROCKS_KID_DATABASE_PATH, VarType.STRING ));
        set.add(new SettingsFileEntry(80, "# possible types are LOCAL, LOCAL_16_PARTS, SERVER, SERVER_16_PARTS",null, VarType.COMMENT));
        set.add(new SettingsFileEntry(90, klueDatabaseFormat, KLUE_DATABASE_FORMAT, VarType.KLUEDB ));

        set.add(new SettingsFileEntry(100, "# Path to database does not include the last two digits (00 thru 15) for 16 part KLUE", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(110, kmerDatabasePath, KMER_DATABASE_PATH, VarType.STRING ));

        set.add(new SettingsFileEntry(150, "# Variant Database is used for v-KLAT applications currently just with Human Genome", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(159, "# database exists:  'yes' or 'no'",null, VarType.COMMENT));
        set.add(new SettingsFileEntry(160, loadVariantDatabase, VARIANT_DATABASE_EXISTS, VarType.YESNO ));
        set.add(new SettingsFileEntry(170, variantDatabasePath, VARIANT_DATABASE_PATH, VarType.STRING ));


        set.processVariableList();
    }

    public static void initialize(){
        new KLUEsettings();
    }

}
