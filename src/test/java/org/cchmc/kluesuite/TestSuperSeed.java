package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klat2.SuperSeed;
import org.cchmc.kluesuite.klue.Kmer31;
import org.junit.Assert;
import org.junit.Test;

import java.util.zip.DataFormatException;

import static java.lang.System.exit;

/**
 * Created by jwc on 9/7/17.
 *
 * PROPER TEST CASES TO KEEP
 *
 *
 * RECALL:  queryEnd and (reference) end  indicate LAST k-mer _start_ position, so unless adjacent, a gap is needed Issue #64, Issue #65
 */
public class TestSuperSeed {

    // Seeds should not combine, reference range too close
    @Test
    public void testSeedsTooClose() {
        //public Seed(int qStart, int qStop, int start, int end, boolean reverse, int hits, int adjacency, int myKid){
        Seed a = new Seed(100, 150, 900, 950, false, 50, 50, 1);
        Seed b = new Seed(160, 210, 960, 1010, false, 50, 50, 1);

        //too close to combine!

        SuperSeed c = null;
        try {
            c = SuperSeed.buildSuperSeed(Kmer31.KMER_SIZE, a,b);
        } catch (DataFormatException e) {
            e.printStackTrace();
            exit(0);
        }

        Assert.assertNull(c);

    }

    // Seeds should combine
    @Test
    public void testSeedsClose() {
        //public Seed(int qStart, int qStop, int start, int end, boolean reverse, int hits, int adjacency, int myKid){
        Seed a = new Seed(100, 150, 900, 950, false, 50, 50, 1);
        //exclusive to inclusive distance is 31+1
        Seed b = new Seed(185, 235, 980, 1030, false, 50, 50, 1);

        //too close to combine!

        SuperSeed c = null;
        try {
            c = SuperSeed.buildSuperSeed(Kmer31.KMER_SIZE, a, b);
        } catch (DataFormatException e) {
            e.printStackTrace();
            exit(0);
        }

//        Assert.assertNull(c);

        Assert.assertTrue(c != null);
        Assert.assertTrue(c.children.size() == 2);
        Assert.assertTrue(c.isReverse == false);
        Assert.assertTrue(c.rightEdge==1060);
        Assert.assertTrue(c.children.get(0).end == 950);

        Assert.assertTrue(c.fastKLATscore == 100);
        Assert.assertTrue(c.queryStart == 100);
        Assert.assertTrue(c.queryEnd == 235);
        Assert.assertTrue(c.start == 900);
        Assert.assertTrue(c.end == 1030);
        Assert.assertFalse(c.isAdjacencyStreak());
        Assert.assertTrue(c.adjacency == 50);
        Assert.assertTrue(c.hits == 100);
        Assert.assertFalse(c.snp);
        Assert.assertFalse(c.indel);



    }

    @Test
    public void testAdjacentClose() {
        //public Seed(int qStart, int qStop, int start, int end, boolean reverse, int hits, int adjacency, int myKid){
        Seed a = new Seed(100, 150, 900, 950, false, 50, 50, 1);
        //exclusive to inclusive distance is 31+1
        Seed b = new Seed(150, 200, 950, 1050, false, 50, 50, 1);

        b.indel = true;
        //too close to combine!

        SuperSeed c = null;
        try {
            c = SuperSeed.buildSuperSeed(Kmer31.KMER_SIZE, a, b);
        } catch (DataFormatException e) {
            e.printStackTrace();
            exit(0);
        }

//        Assert.assertNull(c);

        Assert.assertTrue(c != null);
        Assert.assertTrue(c.children.size() == 2);
        Assert.assertTrue(c.isReverse == false);
        Assert.assertTrue(c.rightEdge==1080);
        Assert.assertTrue(c.children.get(0).end == 950);

        Assert.assertTrue(c.fastKLATscore == 100);
        Assert.assertTrue(c.queryStart == 100);
        Assert.assertTrue(c.queryEnd == 200);
        Assert.assertTrue(c.start == 900);
        Assert.assertTrue(c.end == 1050);
        Assert.assertTrue(c.isAdjacencyStreak());
        Assert.assertTrue(c.adjacency == 100);
        Assert.assertTrue(c.hits == 100);
        Assert.assertFalse(c.snp);
        Assert.assertTrue(c.indel);



    }


    @Test
    public void testMayMerge(){
        Seed a = new Seed(100, 150, 900, 950, false, 50, 50, 1);
        Seed b = new Seed(160, 210, 960, 1010, false, 50, 50, 1);
        Seed c = new Seed(185, 235, 980, 1030, false, 50, 50, 1);

        Assert.assertTrue(SuperSeed.mayMerge(a,c,31));
        Assert.assertFalse(SuperSeed.mayMerge(a,b,31));
        Assert.assertFalse(SuperSeed.mayMerge(b,c,31));

        SuperSeed z = null;
        try {
            z = SuperSeed.buildSuperSeed(Kmer31.KMER_SIZE, a, c);
        } catch (DataFormatException e) {
            e.printStackTrace();
            exit(0);
        }

        Assert.assertFalse(SuperSeed.mayMerge(a,b,31));
    }

    @Test
    public void testConstructor(){
        //tested above
    }

}
