package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 2/7/18.
 */
public class TestSeedClass {



    @Test
    public void testConstructorA() {

    }

    @Test
    public void testConstructorB() {

    }

    @Test
    public void testConstructorCopy() {

    }

    @Test
    public void testConstructorD() {

    }

    @Test
    public void testConstructorE() {

    }

    @Test
    public void testGetPosition() {

    }

   @Test
    public void testMergeIfAble() {

    }

    @Test
    public void testMayMergeAdjacent() {
        Seed a = new Seed(9,23,false,1);
        Seed b = new Seed(10,24,false,1);

        Seed c = new Seed(9,23,true,1);
        Seed d = new Seed(10,22,true,1);
        Seed e = new Seed(10,21,true,1);
        Seed f = new Seed(11,22,true,1);

        Assert.assertTrue(Seed.mayMergeAdjacentSeeds(a,b));
        Assert.assertTrue(Seed.mayMergeAdjacentSeeds(b,a)); //order doesn't matter

        Assert.assertTrue(Seed.mayMergeAdjacentSeeds(c,d));
        Assert.assertTrue(Seed.mayMergeAdjacentSeeds(d,c));//order doesn't matter

        Assert.assertFalse(Seed.mayMergeAdjacentSeeds(a,d)); // can't mix forward, reverse
        Assert.assertFalse(Seed.mayMergeAdjacentSeeds(b,c)); // can't mix forward, reverse even if coordinates forward match
        Assert.assertFalse(Seed.mayMergeAdjacentSeeds(e,c)); // can't merge distance too big
        Assert.assertFalse(Seed.mayMergeAdjacentSeeds(f,c)); // can't merge distance too big

    }

    @Test
    public void testMayMergeConsecutive() {
        Seed a = new Seed(9,23,false,1);
        Seed b = new Seed(10,25,false,1);
        Seed c = new Seed(11,25,false,1);

        Seed f = new Seed(9,24,false,1);


        int WHISK = KLATsettings.WHISKERS_LENGTH_ALIGNMENT;
        int FAIL = WHISK + 1;
        Seed d = new Seed(9+FAIL,23+FAIL,false,1);
        Seed e = new Seed(9+WHISK,23+WHISK,false,1);


        Assert.assertFalse(Seed.mayMergeConsecutiveSeeds(a,b,1,KLATsettings.WHISKERS_LENGTH_ALIGNMENT)); //too close
        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(a,b,0, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(a,c,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(c,a,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT)); //order does not matter

        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(e,a,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
        Assert.assertFalse(Seed.mayMergeConsecutiveSeeds(d,a,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));

        Assert.assertFalse(Seed.mayMergeConsecutiveSeeds(f,a,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));

        //REVERSE
        a = new Seed(9,23,true,1);
        b = new Seed(10,22,true,1);
        c = new Seed(11,21,true,1);

        f = new Seed(9,22,true,1);
        d = new Seed(9+FAIL,123-FAIL,true,1);
        e = new Seed(9+WHISK,123-WHISK,true,1);
        Seed g = new Seed(9,123,true,1);


        Assert.assertFalse(Seed.mayMergeConsecutiveSeeds(a,b,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT)); //too close
        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(a,b,0, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(a,c,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(c,a,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT)); //order does not matter

        Assert.assertTrue(Seed.mayMergeConsecutiveSeeds(e,g,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
        Assert.assertFalse(Seed.mayMergeConsecutiveSeeds(d,g,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));

        Assert.assertFalse(Seed.mayMergeConsecutiveSeeds(f,g,1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT));
    }




    @Test
    public void testMayMergeReverse() {
        //May merge with reverse seeds
        Seed a = new Seed();
        Seed b = new Seed();
        //TODO


    }

  @Test
    public void testMerge() {
      Seed a = new Seed(9,23,false,1);
      Seed b = new Seed(10,25,false,1);
      Seed c = new Seed(11,25,false,1);

      Seed f = new Seed(9,24,false,1);


      int WHISK = KLATsettings.WHISKERS_LENGTH_ALIGNMENT;
      int FAIL = WHISK + 1;
      Seed d = new Seed(9+FAIL,23+FAIL,false,1);
      Seed e = new Seed(9+WHISK,23+WHISK,false,1);

      Seed r = Seed.merge(a,b);
      Seed r2 = new Seed(9,11,23,26,false,2,1,1,false, false, 32);

      Assert.assertEquals(r.queryStart,r2.queryStart);
      Assert.assertEquals(r.queryEnd,r2.queryEnd);
      Assert.assertEquals(r.start,r2.start);
      Assert.assertEquals(r.end,r2.end);
      Assert.assertEquals(r.hits,r2.hits);
      Assert.assertEquals(r.adjacency,r2.adjacency);
      Assert.assertEquals(r.snp,r2.snp);
      Assert.assertEquals(r.indel,r2.indel);
      Assert.assertEquals(r.fastKLATscore,r2.fastKLATscore);

      Assert.assertTrue(r2.equals(r));

      Seed s = Seed.merge(a,c);
      Seed s2 = new Seed(9,12,23,26,false,2,1,1,false, false, 33);
      Assert.assertTrue(s2.equals(s));


      Seed t = Seed.merge(a,d);
      Seed t2 = new Seed(9,9+FAIL+1,23,23+FAIL+1,false,2,1,1,false, false, 62);
      Assert.assertTrue(t2.equals(t));

      b = new Seed(10,24,false,1);
      Seed u = Seed.merge(a,b);
      Seed u2 = new Seed(9,11,23,25,false,2,2,1,false, false, 32);

      //REVERSE
      a = new Seed(9,23,true,1);
      b = new Seed(10,22,true,1);
      c = new Seed(11,21,true,1);

      f = new Seed(9,22,true,1);
      d = new Seed(9+FAIL,123-FAIL,true,1);
      e = new Seed(9+WHISK,123-WHISK,true,1);
      Seed g = new Seed(9,123,true,1);

      Seed v = Seed.merge(a,b);
      Seed v2 = new Seed(9,11,23,21,true,2,2,1, false, false, 32);
      Assert.assertTrue(v2.equals(v));

      Seed w = Seed.merge(g,d);
      Seed w2 = new Seed(9,9+FAIL+1,123,123-FAIL-1,true,2,1,1,false, false, 62);
      Assert.assertTrue(w2.equals(w2));
  }

   @Test
    public void testFastKLATscoreFromSeeds() {

    }

   @Test
    public void testIsAdjacent() {

    }

   @Test
    public void testCombineSeeds() {

    }

   @Test
    public void testCombineConsecutiveSeeds() {

    }

   @Test
    public void testIsAdjacencyStreak() {

    }

    @Test
    public void testCompareTo() {

    }

    @Test
    public void testEliminateDuplicates() {

    }

    @Test
    public void testSeedsOverlap() {

    }




    @Test
    public void testCalculateFastKlatScoreAdjacentSeed() {

    }

//    @Test
//    public void test() {
//
//    }
//
//    @Test
//    public void test() {
//
//    }
//
//    @Test
//    public void test() {
//
//    }
//
//    @Test
//    public void test() {
//
//    }

}
