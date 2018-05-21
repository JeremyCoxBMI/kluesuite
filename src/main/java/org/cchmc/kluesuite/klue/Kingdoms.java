package org.cchmc.kluesuite.klue;

import java.util.HashMap;

/**
 * This class explicitely defines all Kingdoms used in KLUE construction.
 * Note that they, in NCBI taxonomy terms, may not all belong to the Kingdom clade, which is poorly defined.
 * (May be superkingdoms, etc)
 */
public class Kingdoms {

    //TODO these codes are wrong, just placeholders
    //These are NCBI taxonID numbers / codes
    public static int BACTERIA = 2;     //superkingdom
    public static int ARCHAEA = 2157;   //superkingdom
    public static int FUNGI = 4751;     //kingdom
    public static int VIRUS = 10239;    //superkingdom

    //public static int PROTISTS = 6;  //many "kingdoms" in Eukaryota

    //public static int MAMMALS = 5;
    //we need another way to store meaningful groupings

    public static HashMap<Integer, String> codeToName;

    static {
        codeToName = new HashMap<Integer, String>();
        codeToName.put(BACTERIA, "Bacteria");
        codeToName.put(ARCHAEA, "Archaea");
        codeToName.put(FUNGI, "Fungi");
        codeToName.put(VIRUS, "Viruses");
        //codeToName.put(, "");
    }

}
