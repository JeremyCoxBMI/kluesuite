package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klat2.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 9/13/17.
 */
public class TestSmithWatermanTruncated3 {

    @Test
    public void testConstructor(){
        //constructor uses many member functions


        //CASE 0: PerfectMatch
        //            0         1         2         3         4         5         6         7         8
        //           "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        String ref = "ATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGA";
        String que = "ATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGA";

        Seed a = new Seed(0, 40, 0, 40, false, 40, 40, 1);
        Seed b;

//        SuperSeed tester = null;
//        try {
//            tester = SuperSeed.buildSuperSeed(31,a,b);
//        } catch (DataFormatException e) {
//            e.printStackTrace();
//        }

        SuperSeed tester = new SuperSeed(31,a);
        "DEBUG".equals("BREAK LINE");

        //SmithWatermanTruncated swt = new SmithWatermanTruncated();

        SmithWatermanTruncated3 swt;
        swt = new SmithWatermanTruncated3(que,ref,tester, true);

        Assert.assertTrue(swt.getBox(0).type == BoxType.EXACT);
        Assert.assertEquals(0,swt.getBox(0).srow); //queryStart
        Assert.assertEquals(69,swt.getBox(0).erow); //queryStop
        Assert.assertEquals(0,swt.getBox(0).scol); //refStart
        Assert.assertEquals(69,swt.getBox(0).ecol); //refStop

        "DEBUG".equals("BREAK LINE");




        //CASE 1:: IMPERFECT MATCH

        //            0         1         2         3         4         5         6         7         8
        //           "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //           "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXXMMMXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM"
        ref =        "ATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGA";
        que =        "ATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGA";

        a = new Seed(0, 1, 0, 1, false, 1, 1, 1);
        b = new Seed(37, 40, 37, 40, false, 3, 3, 1);

//        SuperSeed tester = null;
//        try {
//            tester = SuperSeed.buildSuperSeed(31,a,b);
//        } catch (DataFormatException e) {
//            e.printStackTrace();
//        }

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);
        "DEBUG".equals("BREAK LINE");

        //SmithWatermanTruncated swt = new SmithWatermanTruncated();
        swt = new SmithWatermanTruncated3(que,ref,tester, true);

//        0 30  INCLUSIVE INCLUSIVE	10 40
//        30 37						40 47
//        37 69						47 79
//        69 72						79 83

        //Note that regions between seeds include the start and finish coordinates of said seeds

        Assert.assertTrue(swt.getBox(0).type == BoxType.EXACT);
        Assert.assertEquals(0,swt.getBox(0).srow); //queryStart INCLUSIVE
        Assert.assertEquals(30,swt.getBox(0).erow); //queryStop INCLUSIVE
        Assert.assertEquals(0,swt.getBox(0).scol); //refStart INCLUSIVE
        Assert.assertEquals(30,swt.getBox(0).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(1).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(1)).mytype == InitType.MID);
        Assert.assertEquals(30,swt.getBox(1).srow); //queryStart INCLUSIVE
        Assert.assertEquals(37,swt.getBox(1).erow); //queryStop INCLUSIVE
        Assert.assertEquals(30,swt.getBox(1).scol); //refStart INCLUSIVE
        Assert.assertEquals(37,swt.getBox(1).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(2).type == BoxType.EXACT);
        Assert.assertEquals(37,swt.getBox(2).srow); //queryStart INCLUSIVE
        Assert.assertEquals(69,swt.getBox(2).erow); //queryStop INCLUSIVE
        Assert.assertEquals(37,swt.getBox(2).scol); //refStart INCLUSIVE
        Assert.assertEquals(69,swt.getBox(2).ecol); //refStop INCLUSIVE


        "DEBUG".equals("BREAK LINE");

        //CASE 2:: IMPERFECT MATCH, with right side IMPERFECT match

        //Dangling rectangle
        //     0         1         2         3         4         5         6         7         8
        //    "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        ref = "ATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATC";
        que = "ATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";
        //     012345678901234567890123456789012345678901234567890123456789012345678901234
        //     ranges: 0 30   INCLUSIVE/INCLUSIVE
        //     30 37  //OVERLAP of 1
        //     37 69
        //     69 72  // How should end of edge be handled?
        //SEED coordinates are INCLUSIVE / EXCLUSIVE
        a = new Seed(0, 1, 0, 1, false, 1, 1, 1);
        b = new Seed(37, 40, 37, 40, false, 3, 3, 1);

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);


        swt = new SmithWatermanTruncated3(que,ref,tester, true);
//        "DEBUG".equals("BREAK LINE");
//        Assert.assertEquals(133,swt.getFastKlatScore());

        //Note that regions between seeds include the start and finish coordinates of said seeds
        Assert.assertTrue(swt.getBox(0).type == BoxType.EXACT);
        Assert.assertEquals(0,swt.getBox(0).srow); //queryStart INCLUSIVE
        Assert.assertEquals(30,swt.getBox(0).erow); //queryStop INCLUSIVE
        Assert.assertEquals(0,swt.getBox(0).scol); //refStart INCLUSIVE
        Assert.assertEquals(30,swt.getBox(0).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(1).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(1)).mytype == InitType.MID);
        Assert.assertEquals(30,swt.getBox(1).srow); //queryStart INCLUSIVE
        Assert.assertEquals(37,swt.getBox(1).erow); //queryStop INCLUSIVE
        Assert.assertEquals(30,swt.getBox(1).scol); //refStart INCLUSIVE
        Assert.assertEquals(37,swt.getBox(1).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(2).type == BoxType.EXACT);
        Assert.assertEquals(37,swt.getBox(2).srow); //queryStart INCLUSIVE
        Assert.assertEquals(69,swt.getBox(2).erow); //queryStop INCLUSIVE
        Assert.assertEquals(37,swt.getBox(2).scol); //refStart INCLUSIVE
        Assert.assertEquals(69,swt.getBox(2).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(3).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(3)).mytype == InitType.RIGHT);
        Assert.assertEquals(69,swt.getBox(3).srow); //queryStart INCLUSIVE
        Assert.assertEquals(72,swt.getBox(3).erow); //queryStop INCLUSIVE
        Assert.assertEquals(69,swt.getBox(3).scol); //refStart INCLUSIVE
        Assert.assertEquals(73,swt.getBox(3).ecol); //refStop INCLUSIVE







        //CASE 3:: IMPERFECT MATCH, with right side IMPERFECT match, refSequence has offset
        //Add dangling ref sequence of 10, query dangling
        //     0         1         2         3         4         5         6         7         8         9
        //    "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123
        ref = "TTTTTTTTTTATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATCTTTTTTTTTT";
        que =           "ATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";
        //              "0123456789012345678901234567890123456789012345678901234567890123456789012
        //               0         1         2         3         4         5         6         7

        // SEED coordinates are INCLUSIVE / EXCLUSIVE
        a = new Seed(0, 1, 10, 11, false, 1, 1, 1);
        b = new Seed(37, 40, 47, 50, false, 3, 3, 1);

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);

        swt = new SmithWatermanTruncated3(que,ref,tester, true);

        //Note that regions between seeds include the start and finish coordinates of said seeds
        Assert.assertTrue(swt.getBox(0).type == BoxType.EXACT);
        Assert.assertEquals(0,swt.getBox(0).srow); //queryStart INCLUSIVE
        Assert.assertEquals(30,swt.getBox(0).erow); //queryStop INCLUSIVE
        Assert.assertEquals(10,swt.getBox(0).scol); //refStart INCLUSIVE
        Assert.assertEquals(40,swt.getBox(0).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(1).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(1)).mytype == InitType.MID);
        Assert.assertEquals(30,swt.getBox(1).srow); //queryStart INCLUSIVE
        Assert.assertEquals(37,swt.getBox(1).erow); //queryStop INCLUSIVE
        Assert.assertEquals(40,swt.getBox(1).scol); //refStart INCLUSIVE
        Assert.assertEquals(47,swt.getBox(1).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(2).type == BoxType.EXACT);
        Assert.assertEquals(37,swt.getBox(2).srow); //queryStart INCLUSIVE
        Assert.assertEquals(69,swt.getBox(2).erow); //queryStop INCLUSIVE
        Assert.assertEquals(47,swt.getBox(2).scol); //refStart INCLUSIVE
        Assert.assertEquals(79,swt.getBox(2).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(3).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(3)).mytype == InitType.RIGHT);
        Assert.assertEquals(69,swt.getBox(3).srow); //queryStart INCLUSIVE
        Assert.assertEquals(72,swt.getBox(3).erow); //queryStop INCLUSIVE
        Assert.assertEquals(79,swt.getBox(3).scol); //refStart INCLUSIVE
        Assert.assertEquals(93,swt.getBox(3).ecol); //refStop INCLUSIVE



        //CASE 3:: IMPERFECT MATCH, with left and right side IMPERFECT match, refSequence has offset
        //Add dangling ref sequence of 10, query dangling
        //     012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        ref = "TTTTTTTTTTATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATCTTTTTTTTTT";
        que =         "AGATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";
        //             012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        //     ranges: 0 30   INCLUSIVE/INCLUSIVE
        //     30 37  //OVERLAP of 1
        //     37 69
        //     69 72  // How should end of edge be handled?
        //SEED coordinates are INCLUSIVE / EXCLUSIVE
        a = new Seed(2, 3, 10, 11, false, 1, 1, 1);
        b = new Seed(39, 42, 47, 50, false, 3, 3, 1);

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);

        swt = new SmithWatermanTruncated3(que,ref,tester, true);

//        0  2                        0 10
//        2  32  INCLUSIVE INCLUSIVE	10 40
//        32 39						40 47
//        39 71						47 81
//        71 74						81 83

        Assert.assertTrue(swt.getBox(0).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(0)).mytype == InitType.LEFT);
        Assert.assertEquals(0,swt.getBox(0).srow); //queryStart INCLUSIVE
        Assert.assertEquals(2,swt.getBox(0).erow); //queryStop INCLUSIVE
        Assert.assertEquals(0,swt.getBox(0).scol); //refStart INCLUSIVE
        Assert.assertEquals(10,swt.getBox(0).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(1).type == BoxType.EXACT);
        Assert.assertEquals(2,swt.getBox(1).srow); //queryStart INCLUSIVE
        Assert.assertEquals(32,swt.getBox(1).erow); //queryStop INCLUSIVE
        Assert.assertEquals(10,swt.getBox(1).scol); //refStart INCLUSIVE
        Assert.assertEquals(40,swt.getBox(1).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(2).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(2)).mytype == InitType.MID);
        Assert.assertEquals(32,swt.getBox(2).srow); //queryStart INCLUSIVE
        Assert.assertEquals(39,swt.getBox(2).erow); //queryStop INCLUSIVE
        Assert.assertEquals(40,swt.getBox(2).scol); //refStart INCLUSIVE
        Assert.assertEquals(47,swt.getBox(2).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(3).type == BoxType.EXACT);
        Assert.assertEquals(39,swt.getBox(3).srow); //queryStart INCLUSIVE
        Assert.assertEquals(71,swt.getBox(3).erow); //queryStop INCLUSIVE
        Assert.assertEquals(47,swt.getBox(3).scol); //refStart INCLUSIVE
        Assert.assertEquals(79,swt.getBox(3).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(4).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(4)).mytype == InitType.RIGHT);
        Assert.assertEquals(71,swt.getBox(4).srow); //queryStart INCLUSIVE
        Assert.assertEquals(74,swt.getBox(4).erow); //queryStop INCLUSIVE
        Assert.assertEquals(79,swt.getBox(4).scol); //refStart INCLUSIVE
        Assert.assertEquals(93,swt.getBox(4).ecol); //refStop INCLUSIVE


    }

    @Test
    /**
     * (not updated) P15-4404\\C:\Users\cox1kb\Documents\KLAT2_development\TestSmithWatermanTruncated3.testCumScores.xlsx
     * NEW C:\Users\cox1kb\OneDrive\Documents\Dissertation\TestSmithWatermanTruncated3.testCumScores.xlsx
     */
    public void testCumScores(){
        //constructor uses many member functions

        String ref;
        String que;

        Seed a;
        Seed b;



        SuperSeed tester;
        SmithWatermanTruncated3 swt;
        "DEBUG".equals("BREAK LINE");


        //CASE 3:: IMPERFECT MATCH, with left and right side IMPERFECT match, refSequence has offset
        //Add dangling ref sequence of 10, query dangling
        //     012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        ref = "TTTTTTTTTTATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATCTTTTTTTTTT";
        que =         "AGATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";
        //             012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        //     ranges: 0 30   INCLUSIVE/INCLUSIVE
        //     30 37  //OVERLAP of 1
        //     37 69
        //     69 72  // How should end of edge be handled?
        //SEED coordinates are INCLUSIVE / EXCLUSIVE
        a = new Seed(2, 3, 10, 11, false, 1, 1, 1);
        b = new Seed(39, 42, 47, 50, false, 3, 3, 1);

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);

        swt = new SmithWatermanTruncated3(que,ref,tester, true);


//        0  2                        0 10
//        2  32  INCLUSIVE INCLUSIVE	10 40
//        32 39						40 47
//        39 71						47 81
//        71 74						81 83

        Assert.assertTrue(swt.getBox(0).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(0)).mytype == InitType.LEFT);
        Assert.assertEquals(0,swt.getBox(0).srow); //queryStart INCLUSIVE
        Assert.assertEquals(2,swt.getBox(0).erow); //queryStop INCLUSIVE
        Assert.assertEquals(0,swt.getBox(0).scol); //refStart INCLUSIVE
        Assert.assertEquals(10,swt.getBox(0).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(1).type == BoxType.EXACT);
        Assert.assertEquals(2,swt.getBox(1).srow); //queryStart INCLUSIVE
        Assert.assertEquals(32,swt.getBox(1).erow); //queryStop INCLUSIVE
        Assert.assertEquals(10,swt.getBox(1).scol); //refStart INCLUSIVE
        Assert.assertEquals(40,swt.getBox(1).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(2).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(2)).mytype == InitType.MID);
        Assert.assertEquals(32,swt.getBox(2).srow); //queryStart INCLUSIVE
        Assert.assertEquals(39,swt.getBox(2).erow); //queryStop INCLUSIVE
        Assert.assertEquals(40,swt.getBox(2).scol); //refStart INCLUSIVE
        Assert.assertEquals(47,swt.getBox(2).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(3).type == BoxType.EXACT);
        Assert.assertEquals(39,swt.getBox(3).srow); //queryStart INCLUSIVE
        Assert.assertEquals(71,swt.getBox(3).erow); //queryStop INCLUSIVE
        Assert.assertEquals(47,swt.getBox(3).scol); //refStart INCLUSIVE
        Assert.assertEquals(79,swt.getBox(3).ecol); //refStop INCLUSIVE

        Assert.assertTrue(swt.getBox(4).type == BoxType.CALCULATED);
        Assert.assertTrue(((CalculatedMatches2) swt.getBox(4)).mytype == InitType.RIGHT);
        Assert.assertEquals(71,swt.getBox(4).srow); //queryStart INCLUSIVE
        Assert.assertEquals(74,swt.getBox(4).erow); //queryStop INCLUSIVE
        Assert.assertEquals(79,swt.getBox(4).scol); //refStart INCLUSIVE
        Assert.assertEquals(93,swt.getBox(4).ecol); //refStop INCLUSIVE

        swt.getFastKlatScore(); //force calculations
        System.out.println("0: cumActualFastKlat : "+swt.getBox(0).cumulativeActualFastKlatScore);
        System.out.println("0: cumSmitWater : "+swt.getBox(0).cumulativeSmithWatermanScore);

        System.out.println("1: cumActualFastKlat : "+swt.getBox(1).cumulativeActualFastKlatScore);
        System.out.println("1: cumSmitWater : "+swt.getBox(1).cumulativeSmithWatermanScore);

        System.out.println("2: cumActualFastKlat : "+swt.getBox(2).cumulativeActualFastKlatScore);
        System.out.println("2: cumSmitWater : "+swt.getBox(2).cumulativeSmithWatermanScore);

        System.out.println("3: cumActualFastKlat : "+swt.getBox(3).cumulativeActualFastKlatScore);
        System.out.println("3: cumSmitWater : "+swt.getBox(3).cumulativeSmithWatermanScore);

        System.out.println("4: cumActualFastKlat : "+swt.getBox(4).cumulativeActualFastKlatScore);
        System.out.println("4: cumSmitWater : "+swt.getBox(4).cumulativeSmithWatermanScore);



        //Caclulates alignment scores here
        Assert.assertEquals(69,swt.getFastKlatScore());
        Assert.assertEquals(131,swt.getSmithWatermanScore());


            //TODO return later
//        Assert.assertEquals(66,swt.getCumulativeMinimumFastKlat());
//        Assert.assertEquals(123,swt.getCumulativeMinimumSW());
//
//        Assert.assertEquals(70,swt.getCumulativeMaximumFastKlat());
//        Assert.assertEquals(135,swt.getCumulativeMaximumSW());
    }

    @Test
    public void testCalculatedMatches2PrintTable(){
        String ref = "TTTTTTTTTTATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATCTTTTTTTTTT";
        String que =         "AGATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";
        //             012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        //     ranges: 0 30   INCLUSIVE/INCLUSIVE
        //     30 37  //OVERLAP of 1
        //     37 69
        //     69 72  // How should end of edge be handled?
        //SEED coordinates are INCLUSIVE / EXCLUSIVE
        Seed a = new Seed(2, 3, 10, 11, false, 1, 1, 1);
        Seed b = new Seed(39, 42, 47, 50, false, 3, 3, 1);

        SuperSeed tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);

        //bug seems to be in creation step :: not adjusting coordinates
        SmithWatermanTruncated3 swt = new SmithWatermanTruncated3(que,ref,tester, true);

        //force alignment
        swt.getFastKlatScore();

        System.out.println(((CalculatedMatches2) swt.getBox(0)).printTable());
        System.out.println();
        System.out.println(((CalculatedMatches2) swt.getBox(2)).printTable());
        System.out.println();
        System.out.println(((CalculatedMatches2) swt.getBox(4)).printTable());
        System.out.println();
    }


    @Test
    public void testCumScores2(){
        //constructor uses many member functions

        String ref;
        String que;

        Seed a;
        Seed b;



        SuperSeed tester;
        SmithWatermanTruncated3 swt;
        "DEBUG".equals("BREAK LINE");


        //CASE 3:: IMPERFECT MATCH, with left and right side IMPERFECT match, refSequence has offset
        //Add dangling ref sequence of 10, query dangling
        //     012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        ref = "TTTTTTTTTTATACGATACGATACGATACGATACGATACGATATCGTATACGATACGATACGATACGATATACGATACGAGATCTTTTTTTTTT";
        que =         "AGATACGATACGATACGATACGATACGATACGAATTCGCATACGATACGATACGATACGATATACGATACGACAC";
        //             012345678_1_2345678_2_2345678_3_2345678_4_2345678_5_2345678_6_2345678_7_2345678_8_2345678_9_234
        //     ranges: 0 30   INCLUSIVE/INCLUSIVE
        //     30 37  //OVERLAP of 1
        //     37 69
        //     69 72  // How should end of edge be handled?
        //SEED coordinates are INCLUSIVE / EXCLUSIVE
        a = new Seed(2, 3, 10, 11, false, 1, 1, 1);
        b = new Seed(39, 42, 47, 50, false, 3, 3, 1);

        tester = new SuperSeed(31,a);
        tester.children.add(b);
        tester.updateValues(b);

        swt = new SmithWatermanTruncated3(que,ref,tester, false);

        swt.getBox(4).calculateScores(0,0);
        ((CalculatedMatches2)swt.getBox(4)).printTableFK();
        ((CalculatedMatches2)swt.getBox(4)).printTable();
    }


}


