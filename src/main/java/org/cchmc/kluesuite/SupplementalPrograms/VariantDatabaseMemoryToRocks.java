package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemoryIterator;
import org.cchmc.kluesuite.variantklue.VariantDatabaseDisk;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

import static java.lang.System.exit;

/**
 * Created by jwc on 8/1/17.
 *
 * VariantDatabaseMemoryIterator iterates by position
 *
 */
public class VariantDatabaseMemoryToRocks {



    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes an Variant Database Memory and builds entries in (new) kmer database.");
            System.out.println("ARG 0 : location variant database memory to read");
            System.out.println("ARG 1 : location of Variant Database Rocks to create");
            exit(0);
        }



        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        System.err.println("\tCreating Variant Database Rocks.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseDisk vdr = new VariantDatabaseDisk(args[1], false);
        System.err.println("\tCreated.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        Set<Integer> s = vd.getKeys();
        for (int k : s) {
            System.err.println("Copying over kid = "+k+"\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
            VariantDatabaseMemoryIterator it = new VariantDatabaseMemoryIterator(k, vd);
            Variant v = it.next();
            int currPosition = v.start;
            ArrayList<Variant> temp = new ArrayList<Variant>();
            temp.add(v);
            while (it.hasNext()){
                v = it.next();
                if (v.start == currPosition){
                    temp.add(v);
                } else {
                    //write previous
                    Position p = new Position(k, currPosition);
                    vdr.put(p,temp);

                    //reset
                    temp = new ArrayList<Variant>();
                    currPosition = v.start;
                    temp.add(v);
                }
            }
            //write last
            Position p = new Position(k, currPosition);
            vdr.put(p,temp);
        }

    }
}
