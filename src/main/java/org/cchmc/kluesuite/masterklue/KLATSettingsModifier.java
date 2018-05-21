//package org.cchmc.kluesuite.masterklue;
//
//import org.apache.commons.cli.*;
//
///**
// * Created by MALS7H on 5/24/2017.
// *
// * What Does this do?
// * DEPRECATED?
// *
// */
//public class KLATSettingsModifier {
//    public static Options klatOpt;
//
//    static {
//        klatOpt = new Options();
//        klatOpt.addOption(Option.builder("x1")
//                            .hasArg(true)
//                            .desc("Settings_OLD file")
//                            .build());
//        klatOpt.addOption(Option.builder("x2")
//                            .hasArg(true)
//                            .desc("Another settings file")
//                            .build());
//        klatOpt.addOption("1", true, "One part of paired read");
//        klatOpt.addOption("2", true, "Second part of the paired read");
//        klatOpt.addOption("U", true,"Instead of -1 and -1, provide a single end reads");
//        klatOpt.addOption("h", "help", false, "help using this file");
//        /*
//        prop.setProperty("KLAT_Settings_Path",SettingsPath);
//        prop.setProperty("MIN_SEED_ADJACENCY",Integer.toString(MIN_SEED_ADJACENCY));
//        prop.setProperty("MIN_SEED_HITS",Integer.toString(MIN_SEED_HITS));
//        prop.setProperty("MAX_GAP",Integer.toString(MAX_GAP));
//        prop.setProperty("WildKLAT_MAX_PERMUTATIONS",Integer.toString(MAX_PERMUTATIONS));
//        prop.setProperty("MAX_SEED_ALIGN_GAP",Integer.toString(MAX_SEED_ALIGN_GAP));
//        prop.setProperty("MAX_SEED_QUERY_GAP",Integer.toString(MAX_SEED_QUERY_GAP));
//        prop.setProperty("WHISKERS",Integer.toString(WHISKERS));
//        prop.setProperty("FAST_STRIDE",Integer.toString(STRIDE));
//        prop.setProperty("FAST_STRIDE_OFFSET",Integer.toString(STRIDE_OFFSET));
//        prop.setProperty("FAST_STRIDE_MIN_LOOKUPS",Integer.toString(STRIDE_MIN_LOOKUPS));
//        prop.setProperty("FORCE_KMER",Integer.toString(FORCE_KMER));
//        */
//        klatOpt.addOption("out", false,"The output file");
//    }
//
//    public static void parseKlue(String[] args) throws ParseException {
//        //Do stuff
//        DefaultParser parser = new DefaultParser();
//        CommandLine cmd = parser.parse(klatOpt, args, KLATsettings.prop);
//        Option[] opts = cmd.getOptions();
//
//        if (cmd.hasOption("help")) {
//            HelpFormatter formatter = new HelpFormatter();
//            formatter.printHelp("Klat", "Key value disk space store for 31-mers and positions",
//                    klatOpt, "", true);
//        }
//        else {
//            String x1 = cmd.getOptionValue("x1");
//            //Load KLAT x1
//            try {
//                KLUEsettings.loadSettings(x1);
//            } catch (Exception e) {
//                throw new ParseException("Could not load file " + x1 + ": " + e.getMessage());
//            }
//            String x2 = cmd.getOptionValue("x2");
//            //Load KLAT x2
//            try {
//                KLATSettingsOLD.loadSettings(x2);
//            } catch (Exception e) {
//                throw new ParseException("Could not load file " + x2 + ": " + e.getMessage());
//            }
//
//            if(cmd.hasOption("out")) {
//                String out = cmd.getOptionValue("out");
//                //Set out file and save settings
//            }
//
//
//            //setup exclusive requirement
//            if (cmd.hasOption("1") && cmd.hasOption("2") && !cmd.hasOption("U")) {
//                //arguments check out
//                String one = cmd.getOptionValue("1");
//                String two = cmd.getOptionValue("2");
//            } else if (!cmd.hasOption("1") && !cmd.hasOption("2") && cmd.hasOption("U")) {
//                //arguments check out
//                String u = cmd.getOptionValue("U");
//            } else {
//                //error in arguments
//                throw new ParseException("Command must either have option 1 and 2 and not U or U and neither 1 nor 2");
//            }
//        }
//    }
//
//
//    public static void main(String[] args) {
//        String[] sampleArgs1 = "-x1 file1 -x2 file2 -1 m1 -2 m2 -out fileOut".split(" ");
//        String[] sampleArgs2 = "-x1 file1 -out fileOut".split(" ");                         //Error, missing arg -x2
//        String[] sampleArgs3 = "-x1 file1 -x2 file2 -U file".split(" ");
//        String[] sampleArgs4 = "-x1 file1 -x2 file2 -1 m1 -2 m2 -U file".split(" ");        //Error, cannot have -1, -2 and
//        String[] sampleArgs5 = {"-h"};
//
//        /*try {
//            System.out.println("testing: \"-x1 file1 -x2 file2 -1 m1 -2 m2 -out fileOut\"");
//            parseKlue(sampleArgs1);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//        }
//        try {
//            System.out.println("testing: \"-x1 file1 -out fileOut\"");
//            parseKlue(sampleArgs2);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//        }
//        try {
//            System.out.println("testing: \"-x1 file1 -x2 file2 -U file\"");
//            parseKlue(sampleArgs3);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//        }
//        try {
//            System.out.println("testing: \"-x1 file1 -x2 file2 -1 m1 -2 m2 -U file\"");
//            parseKlue(sampleArgs4);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//        }*/
//        try {
//            System.out.println("testing: \"-h\"");
//            parseKlue(sampleArgs5);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//        }
//
//    }
//}
