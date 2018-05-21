package org.cchmc.kluesuite.SupplementalPrograms;

import org.junit.Assert;

import java.util.Random;

/**
 * Created by jwc on 6/19/17.
 *
 * This simulations a distribution from 1 to length of Probability array (inclusive)
 * so that P(n) = probabilityArr[n-1]  i.e. with an offset
 *
 */
public class SimulatorRandomLengthCluster {

    /**
     * Random number generated used for calling random size
     */
    Random rand;

    /**
     * index represents the probability of drawing index+1
     */
    Double[] probability;

    /**
     * internal table for looking up probability (a cumulative sum table)
     */
    Integer[] lookupMax;

    /**
     *  because floats simulated by integers, must set a depth for integer counting (i.e. 10,000 goes to 4th decimal place)
     */
    int seedDepth;

    //int  n = rand.nextInt(50) + 1;

    public SimulatorRandomLengthCluster(int seed,Double[] probability, int seedDepth){
        rand = new Random(seed);
        this.probability = probability;
        this.seedDepth = seedDepth;

        lookupMax = new Integer[probability.length+1];
        int total = 0;

        int s = 0;
        for (int k=0; k<probability.length; k++){
            s += probability[k]*seedDepth;
        }
        this.probability[0] += new Double(seedDepth - s)/seedDepth;   //any unused probability added to first

        for (int k=0; k<probability.length; k++){
            lookupMax[k]=total;
            total += seedDepth * this.probability[k];
        }
        //Assert.assertEquals(total,seedDepth);

        lookupMax[probability.length] = seedDepth+1;  //so it will always end in getRandomSize(), not go off array
    }

    public int getRandomSize(){
        int r = rand.nextInt(seedDepth);
        int k;
        for(k=0; r > lookupMax[k]; k++);    //k is always one higher than it should be as index, and size is 1-shifted
        return k;
    }
}
