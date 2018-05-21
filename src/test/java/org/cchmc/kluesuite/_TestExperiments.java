package org.cchmc.kluesuite;
import org.cchmc.kluesuite.SupplementalPrograms.SimulationVerifier;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.SuperString;
import org.junit.Test;

import java.util.Arrays;


import java.util.zip.DataFormatException;

/**
 * Created by jwc on 6/13/17.
 */
public class _TestExperiments {

    @Test
    public void bacon(){
        String s = "abc";
        System.out.println(s.substring(1,2));

        String query = "alpha||voodoo\tbeta \tcheese\n";



        String[] splits = query.split("\\s");
        System.err.println(Arrays.toString(splits));
        String[] splits2 = splits[0].split("\\|\\|");


            int start, stop;

//        query = ">hg38chr1||4803203||4803310||rs770335705[S]<4803253|C/T>,rs531498434[S]<4803261|C/T>";
//
//        splits2 = query.split("\\|\\|");
//
//        start = Integer.parseInt(splits2[1]);
//        System.err.println(start+"");
//        end = Integer.parseInt(splits2[2]);
//        System.err.println(end+"");
//
//        System.err.println(Arrays.toString(splits2));
//
//        String text = splits2[3];
//
//
//
//        String bump = "rs770335705[S]<4803253|C/T>";
//        ArrayList<Variant> b = Variant.buildFromText(text);
//        System.err.println(b);
//        b = Variant.buildFromText(bump);
//        System.err.println(b);
//
//        String[] variants;
//
//
//        variants = bump.split(",");
//        System.err.println(Arrays.toString(variants));
//
//
//        variants = text.split(",");
//        System.err.println(Arrays.toString(variants));
//
//        String[] splitBY2 = variants[0].split("\\[");
//        System.err.println(Arrays.toString(splitBY2));
//
//        Character type = splitBY2[1].charAt(0);
//        System.err.println(Character.toString(type));
//        String[] splitBingo = splitBY2[1].substring(3,splitBY2[1].length()-1).split("\\|");
//        System.err.println(Arrays.toString(splitBingo));
//        int pos = Integer.parseInt(splitBingo[0]);
//        System.err.println(pos+"");
//        String insertSequence = splitBingo[1].split("\\/")[1];
//        System.err.println(insertSequence);



        KidDatabaseMemory kd = new KidDatabaseMemory();
        kd.add(new Kid("bob"));
        SuperString sz = new SuperString();
        for (int k=0; k<10;k++) sz.addAndTrim("AAAATAAAAT");
        String reference = "AAAAATCGACA";
        sz.addAndTrim(reference);

        try {
            kd.storeSequence(1, new DnaBitString(sz));
        } catch (DataFormatException e) {
            e.printStackTrace();
        }


        start = 100;
        stop = 111;
        int kid=1;

        String mutts = "rs531498434[S]<105|T/G>,rs53[D]<109|-/C>,rs53[I]<110|-/zzz>";
        String sequence = "AAAAAGCGCzzzA";

        System.err.println(sequence.substring(6,9));
        System.err.println(sequence.substring(6,10));

        System.out.println(
                SimulationVerifier.verify(start, stop, mutts, sequence, kd, kid)
        );

        query = ">hg38chr1||7322906||7323006||rs796802139[I]<7322956|-/TCCCT>,rs758353119[I]<7322957|-/CCCCC/CCCCCC>";
        splits2 = query.split("\\|\\|");

        start = Integer.parseInt(splits2[1]);
        System.err.println(start+"");
        stop = Integer.parseInt(splits2[2]);
        System.err.println(stop+"");

        System.err.println(Arrays.toString(splits2));

        String text = splits2[3];

        SimulationVerifier.verify(start, stop, text, sequence, kd, kid);




    }
}
