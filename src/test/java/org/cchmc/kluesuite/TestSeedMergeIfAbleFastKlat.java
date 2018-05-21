package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.Seed;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jwc on 8/17/17.
 */
public class TestSeedMergeIfAbleFastKlat {


    // CASE 4: other  (not adjacent, ref sequences significant distance)
    @Test
    public void testCaseFour(){
        Seed s1 = new Seed(1,5,49,56,false,4,4,1,false, false,4+30);
        Seed s2 = new Seed(6,14,70,80,false,8,5,1,false,false,5+30);

        Seed m = Seed.mergeIfAble(s1,s2);

        Assert.assertEquals(m.fastKLATscore, 4+35+14);
    }


    //CASE 6: seeds are adjacent by query, but not reference
    @Test
    public void testCaseSix(){
        Seed s1 = new Seed(1,5,49,56,false,4,4,1,false, false,4+30);
        Seed s2 = new Seed(5,14,70,80,false,8,5,1,false,false,5+30);

        Seed m = Seed.mergeIfAble(s1,s2);

        Assert.assertEquals(m.fastKLATscore, 4+35+14);
    }


    // CASE 1: the seeds are adjacent as both reference and query sequences
    @Test
    public void testCaseOne(){
        Seed s1 = new Seed(1,5,49,56,false,4,4,1,false, false,4+30);
        Seed s2 = new Seed(5,14,56,72,false,9,6,1,false,false,9+30);

        Seed m = Seed.mergeIfAble(s1,s2);

        Assert.assertEquals(m.fastKLATscore, 4+9+30+0);
    }

    // CASE 2: the seeds are adjacent as reference sequences
    @Test
    public void testCaseTwo(){
        Seed s1 = new Seed(1,5,49,56,false,4,4,1,false, false,4+30);
        Seed s2 = new Seed(13,22,56,72,false,9,6,1,false,false,9+30);

        Seed m = Seed.mergeIfAble(s1,s2);

        Assert.assertEquals(m.fastKLATscore, 4+9+30+0);
    }

    //CASE 3: the seeds are not adjacent, reference sequences closer than 31 to each other
    @Test
    public void testCaseThree(){
        Seed s1 = new Seed(1,5,49,56,false,4,4,1,false, false,4+30);
        Seed s2 = new Seed(13,22,60,74,false,9,6,1,false,false,9+30);

        Seed m = Seed.mergeIfAble(s1,s2);

        Assert.assertEquals(m.fastKLATscore, 4+9+30+4);
    }

    //CASE 5: overlapping reference sequences

}
