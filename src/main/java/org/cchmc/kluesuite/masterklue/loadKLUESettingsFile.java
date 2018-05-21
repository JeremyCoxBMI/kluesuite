package org.cchmc.kluesuite.masterklue;

import java.io.FileNotFoundException;

import static java.lang.System.exit;

/**
 * Created by osboxes on 24/04/17.
 *
 *
 * CODE EXAMPLE
 */
public class loadKLUESettingsFile {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.out.println("ARG 0 : location KLUEsettingsFile to read");

            exit(0);
        }

        KLUEsettings ks;

        try {
            KLUEsettings.initialize();
            KLUEsettings.loadSettings(args[0]);
            System.out.println(KLUEsettings.rocksKidDatabasePath+"="+KLUEsettings.ROCKS_KID_DATABASE_PATH);
            System.out.println("filename="+KLUEsettings.getFileName());

        } catch (Exception e) {
            System.err.println("Loading file failed");
            e.printStackTrace();
        }



    }
}
