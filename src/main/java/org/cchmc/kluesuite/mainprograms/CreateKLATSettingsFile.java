package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.masterklue.KLATsettings;

import static java.lang.System.exit;

/**
 * Created by osboxes on 5/25/17.
 */
public class CreateKLATSettingsFile {


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("program requires 1 argument: path to the file to create");
            exit(0);
        }

        //   /home/osboxes/workspace/kluesuite/src/main/java/KLUEsettings.default.properties

        KLATsettings.initialize();
        KLATsettings.saveSettings(args[0]);
    }

}
