package org.cchmc.kluesuite;

import org.apache.commons.cli.ParseException;
import org.cchmc.kluesuite.masterklue.CommandParse;
import org.cchmc.kluesuite.masterklue.KlueSuite;
import org.junit.Test;

/**
 * Created by jwc on 8/11/17.
 */
public class TestKlueSuite {


    @Test
    public void testBadSubcommand(){
        //KlueSuite.main(new String[] {"monkey"});
        System.out.println("################### Bad Sub Command ###################");

        String[] args = new String[] {"monkey"};

            CommandParse.processCommand(args);

        System.out.println();
    }

    @Test
    public void testHelp(){
        System.out.println("################### AppendRocksDb help ###################");
        String[] args = new String[] {"AppendRocksDb"};

            CommandParse.processCommand(args);

        System.out.println();
    }

    @Test
    public void testEmpty(){
        System.out.println("################### Empty string -- top level help ###################");
        String[] args = new String[] {""};

            CommandParse.processCommand(args);

        System.out.println();
    }


    @Test
    public void testAppendRocksDbBadArgs(){
        System.out.println("################### Append bad parse ###################");
        String[] args = new String[] {"AppendRocksDb","-db","-extra"};
        CommandParse.processCommand(args);

        System.out.println();
    }

    @Test
    public void testAppendRocksDbGoodArgs(){
        System.out.println("################### Append good parse ###################");
        String[] args = new String[] {"AppendRocksDb","-db", "bob", "-extra", "multithread", "cheeseburger"};
        CommandParse.processCommand(args);

        System.out.println();
    }

}
