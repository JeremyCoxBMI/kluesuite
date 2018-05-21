package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klat2.SmithWatermanTruncated;
import org.cchmc.kluesuite.klat2.SuperSeed;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 9/13/17.
 */
public class TestSmithWatermanTruncated {

    @Test
    public void testConstructor(){
        //constructor uses many member functions

        //            0         1         2         3         4         5         6         7         8
        //           "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //           "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXXMMMXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
        String ref = "ATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGA";
        String que = "ATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGA";

        Seed a = new Seed(0, 1, 0, 1, false, 1, 1, 1);
        Seed b = new Seed(37, 40, 37, 40, false, 3, 3, 1);

//        SuperSeed tester = null;
//        try {
//            tester = SuperSeed.buildSuperSeed(31,a,b);
//        } catch (DataFormatException e) {
//            e.printStackTrace();
//        }

        SuperSeed tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);
        "DEBUG".equals("BREAK LINE");

        //SmithWatermanTruncated swt = new SmithWatermanTruncated();

        SmithWatermanTruncated swt;
//        swt = new SmithWatermanTruncated(que,ref,tester);


        //start with calculateTable()

        "DEBUG".equals("BREAK LINE");
//        Assert.assertEquals(131,swt.getFastKlatScore());


        //Dangling rectangle
        ref = "ATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATC";
        que = "ATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";

        a = new Seed(0, 1, 0, 1, false, 1, 1, 1);
        b = new Seed(37, 40, 37, 40, false, 3, 3, 1);

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);
        //TODO resume here -- diagonals / rectangles look good; need try other variations

        swt = new SmithWatermanTruncated(que,ref,tester);
        "DEBUG".equals("BREAK LINE");
        Assert.assertEquals(133,swt.getFastKlatScore());
    }

}
