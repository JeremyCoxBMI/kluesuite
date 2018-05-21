package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseDisk;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Created by osboxes on 01/08/2017
 */

public class vKlatProgramFromKlatAlignments {

    private static final int VARIANT_COLUMN = 12;
    boolean DEBUG = false;
    static int ANNOUNCE_PERIOD = 1000;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("ARG 0 : variant database disk");
            System.err.println("ARG 1 : KLAT Blast6 results file");
            System.err.println("ARG 2 : New v-KLAT Blast6 results file");
            System.err.println("Program writes to STDOUT");
            exit(0);
        }


        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.err.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());



        System.err.println("Loading Variant Database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseDisk vd = new VariantDatabaseDisk(args[0], true);
        System.err.println("Loading Complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        String queryName = "NONE", querySequence ="";
        SuperString querySS = new SuperString();

        try(BufferedReader br = new BufferedReader(new FileReader(args[1]))) {

            FileWriter fw = new FileWriter(args[2]);
            BufferedWriter writer = new BufferedWriter(fw);

            int k = 0;
            for(String line; (line = br.readLine()) != null; ) {
                String[] splits = line.trim().split("\t");
                String text = splits[VARIANT_COLUMN];
                ArrayList<Variant> vars = Variant.buildFromText(text);
                for (Variant v: vars) {
                    v = Variant.checkVariantInVariantDatabaseAndModify(v, vd);
                }
                writer.write(formatString(splits, vars));

            } //end for line

            br.close();
            writer.flush();
            fw.flush();
            writer.close();
            fw.close();


        } //end try
        catch (FileNotFoundException e) {
            e.printStackTrace();
            exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String formatString(String[] splits, ArrayList<Variant> vars) {
        SuperString result;
        result = new SuperString();
        for (int k=0; k < VARIANT_COLUMN; k++){
            result.addAndTrim(splits[k]+"\t");
        }
        result.addAndTrim(Variant.variantNameList(vars));
        result.add("\n");

        return result.toString();
    }

}



