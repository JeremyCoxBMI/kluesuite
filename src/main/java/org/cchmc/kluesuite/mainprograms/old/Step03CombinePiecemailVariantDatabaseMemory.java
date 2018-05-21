package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.IOException;
import java.sql.Timestamp;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/9/17.
 *
 *
 * DEPRECATED?????
 *
 */
public class Step03CombinePiecemailVariantDatabaseMemory {


    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Takes a UCSC variants file and builds entries in kmer database, as well as Variant database.");
            System.out.println("ARG 0 : VariantDatabase Memory to make");
            System.out.println("ARG 1+ : location variant database(s) Memory to combine (cannot share KID)");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());





        String[] fileNames = new String[args.length-1];
        VariantDatabaseMemory[] vdm = new VariantDatabaseMemory[fileNames.length];
        for(int k=1; k<args.length; k++){
            fileNames[k-1] = args[k];
            vdm[k-1] = new VariantDatabaseMemory();
        }


        for (int k=0; k < fileNames.length;k++){
            System.err.println("\tLoading VariantDatabaseMemory (unsafe) file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
            System.err.println("\tLoading VariantDatabaseMemory (unsafe) file : \t"+fileNames[k]);
            try {
                vdm[k].loadFromFileUnsafe(fileNames[k]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println("\tLoad VariantDatabasememory file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        }



        System.err.println("\tSaving VariantDatabaseMemory (combined)(unsafe) file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        try {
            VariantDatabaseMemory.saveToFileUnsafe(args[0],vdm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("\tSave VariantDatabasememory file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
    }
}
