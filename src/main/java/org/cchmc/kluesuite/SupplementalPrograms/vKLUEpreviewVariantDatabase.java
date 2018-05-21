package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/18/17.
 */
public class vKLUEpreviewVariantDatabase {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Takes an (unsafe) Variant Database and builds entries in (new) kmer database.");

            System.out.println("ARG 0 : location variant database (unsafe) to read");
            System.out.println("ARG 1 : kid to preview");
            exit(0);
        }


        String filename = args[0];
        int pKID = Integer.parseInt(args[1]);

        VariantDatabaseMemory vdm = new VariantDatabaseMemory();
        try {
            vdm.loadFromFileUnsafe(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TreeMap<Integer, Variant[]> i = vdm.indelMap.get(pKID);
        TreeMap<Integer, Variant[]> s = vdm.snpMap.get(pKID);

        ArrayList<Integer> iKeys = new ArrayList<Integer>(i.keySet());
        ArrayList<Integer> sKeys = new ArrayList<Integer>(s.keySet());

        Collections.sort(iKeys);
        Collections.sort(sKeys);

        System.out.println("*******************\nIndels\n*******************");

        for (int k=0; k<10;k++){
            System.out.println(iKeys.get(k)+"\t"+ Arrays.toString(i.get(iKeys.get(k))));
        }

        System.out.println("*******************\nsnps\n*******************");

        for (int k=0; k<10;k++){
            System.out.println(sKeys.get(k)+"\t"+Arrays.toString(s.get(sKeys.get(k))));
        }


    }

}
