package org.cchmc.kluesuite.mainprograms;

/**
 * Created by osboxes on 24/04/17.
 *  *
 * REMAKE of HumanVariantDatabaseBuildStep0X  series
 * Using new classes, new strategies.  Starting over from scratch.
 *  -- using Kryo
 *  -- using memory only objects except for kmer
 *  -- not including many human sequences with "_" in name
 *
 * 1)  KidDatabaseMemory / DnaBitString database
 * 2)  Build normal K-mer database
 * 3)  Build VariantDatabaseOLD
 * 4)  Write Variants to K-mer database
 * 5)  Recompile K-mer in-order database and in-order 16 part databases
 * 6)  Per your option, convert
 *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 */
public class Step05CombineVariantDatabases {


    public static void main(String[] args) {
        System.out.println("Step05 is run from command line: ");
        System.out.println("\tjava -cp kluesuite.jar org.cchmc.kluesuite.mainprograms/CombineRocksDatabases <new database path> <old database path>");

        System.out.println("\nthen in parallel:");
        System.out.println("\tjava -cp kluesuite.jar org.cchmc.kluesuite.mainprograms/BuildRocks16Combine <new database path> 00 <old database path>");
        System.out.println("\tjava -cp kluesuite.jar org.cchmc.kluesuite.mainprograms/BuildRocks16Combine <new database path> 01 <old database path>");
        System.out.println("\t..");
        System.out.println("\tjava -cp kluesuite.jar org.cchmc.kluesuite.mainprograms/BuildRocks16Combine <new database path> 15 <old database path>");

        System.out.println("");
    }
}
