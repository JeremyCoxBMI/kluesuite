package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Created by osboxes on 11/05/17.
 */
public class MakeVariantDatabaseChr1Only {

    public static void main(String[] args) throws FileNotFoundException {

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Loading Variant Database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Loading Vairant Database Complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("Making Smaller Variant Database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        vd.removeAllButChr1();

        System.err.println("Writing Smaller KidDatabaseMemory\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        vd.setFilename(args[1]);
        try {
            vd.saveToFileUnsafe();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Writing Smaller KidDatabaseMemory complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

    }

}
