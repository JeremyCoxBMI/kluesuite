package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by osboxes on 11/05/17.
 */
public class MakeKidDatabaseChr1Only {

    public static void main(String[] args) {

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("Loading KidDatabaseMemory\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

        System.err.println("Loading KidDatabaseMemory Complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("Making Smaller KidDatabaseMemory\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

//        ArrayList<Integer> bob = new ArrayList();
//        for ( int z = 1; z < kd.size(); z++){
//            bob.addWithTrim(z);
//        }
        Integer[] bob2;

        bob2 = new Integer[24];
        for (int z=2; z < 26; z++) {
            bob2[z-2] = z;
        }

        Arrays.sort(bob2, Collections.reverseOrder());
        kd.removeSequences(bob2);

        System.err.println("Writing Smaller KidDatabaseMemory\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        kd.fileName = args[1];
        kd.saveToFileUnsafe();

        System.err.println("Writing Smaller KidDatabaseMemory complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
    }

}
