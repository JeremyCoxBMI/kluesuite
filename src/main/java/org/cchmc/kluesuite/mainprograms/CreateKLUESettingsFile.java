package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.masterklue.KLUEsettings;

import static java.lang.System.exit;


/**
 * Created by osboxes on 5/24/17.
 */
public class CreateKLUESettingsFile {


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("program requires 1 argument: path to the file to create");
            exit(0);
        }

        //   /home/osboxes/workspace/kluesuite/src/main/java/KLUEsettings.default.properties
        KLUEsettings.initialize();
        KLUEsettings.saveSettings(args[0]);
    }
}
