package org.cchmc.kluesuite.masterklue;

import org.apache.commons.cli.ParseException;

/**
 * Created by jwc on 7/12/17.
 *
 * This is the master program for running all kluesuite programs.
 *
 * Program is set up this way so that we can make one executable
 *
 * Idea for master program is to take sub-program as args[0]
 * like :   $ executable  program_name [arguments]
 *
 *
 */
public class KlueSuite {

    public static void main(String[] args) {


        CommandParse.processCommand(args);


    }
}
