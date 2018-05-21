package org.cchmc.kluesuite.masterklue;

import org.apache.commons.cli.*;
import org.cchmc.kluesuite.mainprograms.AppendRocksDb;

import java.security.cert.X509CRLEntry;
import java.util.*;

/**
 * do use do CommandParse.mainCommand.processCommand(args)
 * where args in a String[] of arguments
 * Created by mals7h on 5/23/2017.
 */
public class CommandParse {


    /**
     * This is the name of the compiled program or script calling the mainprogram/KlueSuite program
     * which calls this parser.
     * Note it is hard coded
     */
    static String program = "kluesuite";

    public static void processCommand(String[] args){
        mainCommand.processCommand(args);
    }



    /**
     * This class contains all subcommand branches
     */
    public static Command mainCommand = new Command(
            "",
            program+" [programname] [options...]",
            new Command[]{
                new Command(
                    "AppendRocksDb",
                    program+" AppendRocksDb -db [database] -extra [db1 db2 ...]",
                    new Command[]{},
                    new Option[]{
                        //-db argument, has one argument
                        Option.builder("db")
                            .required(true)
                            .desc("large database to addAndTrim small db")
                            .hasArg()
                            .numberOfArgs(1)
                            .build(),
                        Option.builder("extra")
                            .required(true)
                            .desc("list of databases to addAndTrim")
                            .hasArgs()
                            .build()
                    },
                    "Program to take multiple (small) databases and addAndTrim them into a large one",
                    new CommandCall() {
                        @Override
                        public void call(CommandLine command) {
//                            System.out.println("Hello World");
//                            String X = "db";
//                            System.out.println("-"+X+"\t"+command.getOptionValue(X));
//                            X = "extra";
//                            System.out.println("-"+X+"\t"+Arrays.toString(command.getOptionValues(X)));

                            AppendRocksDb ad = new AppendRocksDb();
                            //ad.commandCall(command);
                        }
                    }
                ),
                new Command(
                    "BuildKidDb",
                    program+" BuildKidDb [options...]",
                    new Command[]{},
                    new Option[]{
                            // INSERT ARGUMENTS HERE
                    },
                    "INSERT DESCRIPTION HERE",
                    new CommandCall() {
                        @Override
                        public void call(CommandLine command) {
                            // TODO FINISH THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            // BuildKidDb.main(command);
                        }
                    }
                ),
                new Command(
                    "BuildMiniDmFromRockKidDbOnHeap",
                    program+" BuildMiniDmFromRockKidDbOnHeap -bin ...",
                    new Command[]{},
                    new Option[]{
                            Option.builder("bin")
                                    .required(true)
                                    .desc("RockKidDb bin file")
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .build(),
                            Option.builder("dbin")
                                    .required(true)
                                    .desc("RockKidDb dnabitstring database")
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .build(),
                            Option.builder("dbout")
                                    .required(true)
                                    .desc("Location of database to create")
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .build(),
                            Option.builder("s")
                                    .required(true)
                                    .desc("Resume number OR where to start analysis (4 thru 53)")
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .build(),
                            Option.builder("arr")
                                    .required(true)
                                    .desc("Size of Heap Array to build, 1/20 of free memory.  IN MILLIONS.  Maximum = "+
                                            Integer.MAX_VALUE/1000/1000)
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .build(),
                            Option.builder("splits")
                                    .required(true)
                                    .desc("Number of parallel splits being made")
                                    .hasArg()
                                    .numberOfArgs(1)
                                    .build(),

                    },
                    "Takes an established DnaBitString and KidDatabase databases and imports a portion of " +
                            "sequences.",
                    new CommandCall() {
                        @Override
                        public void call(CommandLine command) {
                            // TODO FINISH THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                            // BuildMiniDmFromRockKidDbOnHeap.main(command);
                        }
                    }
                ),
                new Command(
                        "BuildRocks16Combine",
                        program+" BuildRocks16Combine -db [db path] -p [db path] -files [f1 f2 f3 ...]",
                        new Command[]{},
                        new Option[]{
                                Option.builder("db")
                                        .required(true)
                                        .desc("Database path & name")
                                        .hasArg()
                                        .numberOfArgs(1)
                                        .build(),
                                Option.builder("p")
                                        .required(true)
                                        .desc("database portion to create from 00 to 15 (integer)")
                                        .hasArg()
                                        .numberOfArgs(1)
                                        .build(),
                                Option.builder("files")
                                        .required(true)
                                        .desc("List of files to combine")
                                        .hasArgs()
                                        .build(),
                        },
                        "Build 16 piece database by combining smaller pieces.\n" +
                                " Each 16 pieces execute in parallel as separate programs" +
                                "\n" +
                                "Alternatively, can be used to convert an out-of-order database to a written-in-order" +
                                " database, by using 1 database in old arguments",
                        new CommandCall() {
                            @Override
                            public void call(CommandLine command) {
                                // TODO FINISH THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                // BuildRocks16Combine.main(command);
                            }
                        }
                ),
            },
            new Option[]{},
            "Main command for program",
            null
        );







    public static void main(String[] args) throws ParseException {
        Command testCommand = new Command(
                "",
                "test [subcmd] [options...]",
                new Command[]{
                        new Command(
                                "sub1",
                                "test sub1 -a1 [value] -a2 [v1 v2 v3 ...]",
                                new Command[]{},
                                new Option[]{
                                        //-db argument, has one argument
                                        Option.builder("a1")
                                                .required(true)
                                                .desc("first argument")
                                                .hasArg()
                                                .numberOfArgs(1)
                                                .build(),
                                        Option.builder("a2")
                                                .required(true)
                                                .desc("list of arguments")
                                                .hasArgs()
                                                .build()
                                },
                                "a test sub command",
                                new CommandCall() {
                                    @Override
                                    public void call(CommandLine command) {
                                        System.out.println(command.getOptionValue("a1"));
                                        System.out.println(Arrays.toString(command.getOptionValues("a2")));
                                    }
                                }
                        )
                },
                new Option[]{},
                "Test Command",
                null
        );

        testCommand.printHelp();
        System.out.println("sub1 -a1 a -a2 b c d e f g");
        testCommand.processCommand("sub1 -a1 a -a2 b c d e f g".split(" "));
        System.out.println("sub1 -a2 b c d e f g");
        testCommand.processCommand("sub1 -a2 b c d e f g".split(" "));
    }



    public static void interpetArguments(String[] args) {

        //Must be initialized to create static values
        KLATsettings.initialize();
        KLUEsettings.initialize();


    }

}


class Command {

    private String name, usage;
    private CommandCall callable;
    private Options options = new Options();
    private Command[] subCommands;
    private Option[] arguments;
    private String description;

    public Command(String name, String usage, Command[] subCmds, Option[] arguments, String description, CommandCall callable) {
        this.name = name;
        this.usage = usage;
        this.subCommands = subCmds;
        this.arguments = arguments;
        this.description = description;
        this.callable = callable;

        for(Option arg : arguments) {
            options.addOption(arg);
        }
    }

    public static HashSet<String> HELP_ARGS = new HashSet<String>();
    static {
        for(String str : new String[]{"help", "-help", "-h"}){
            HELP_ARGS.add(str.toUpperCase());
        }
    }

    public void processCommand(String[] args){

        //This recurses through subcommand layers
        //Currently, no subcommands of subcommands are programmed; here it recurses through levels

         if (subCommands.length > 0 && args.length > 0) {

            for (Command subCmd : subCommands) {
                if (subCmd.name.equalsIgnoreCase(args[0])) {
                    String[] newArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, newArgs, 0, newArgs.length);

                    //should be command call?
                    //recurse?

                    subCmd.processCommand(newArgs);

                    return;
                }
            }
            printHelp();
        }
        //executes command once all subcommand layers are parsed
        else {
            if(args.length == 0 || HELP_ARGS.contains(args[0].toUpperCase()) || callable == null) {
                printHelp();
            }
            else {
                try {
                    DefaultParser parser = new DefaultParser();

                    CommandLine line = parser.parse(options, args);


                    if (line.hasOption("h")) {
                        printHelp();
                    } else {
                        //Do thing with command
                        callable.call(line);
                    }
                } catch (ParseException e){
                        printHelp();
                }
            }
        }
    }


    public void printHelp() {

        HelpFormatter formatter = new HelpFormatter();
        String footer = "";

        if (subCommands.length > 0) {
            footer += "Sub Commands - \n";
            List<Command> cmdList = new ArrayList<Command>(Arrays.asList(subCommands));
            Collections.sort(cmdList, new Comparator<Command>() {
                public int compare(Command a, Command b) {
                    return a.name.compareTo(b.name);
                }
            });
            for (Command cmd : cmdList) {
                footer += "  " + cmd.name + "\n    - " + cmd.description;
            }
        }

        //TODO expand error messages here
        formatter.printHelp(usage, name, options, footer);
    }




}