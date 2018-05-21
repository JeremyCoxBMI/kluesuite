package org.cchmc.kluesuite.SupplementalPrograms;

import java.util.*;

/**
 * Created by jwc on 6/21/17.
 *
 * Designed to allow repeatable random selections
 */

public class RandomSet {

    int full_set_size;
    List<Integer> list;
    Random rand;

    public RandomSet(int fullSize){

        rand = new Random(91000098765L + fullSize);

        full_set_size = fullSize;
        Set<Integer> integerSet = new HashSet<Integer>();
        for (int i = 0; i < fullSize; i++)
            integerSet.add(new Integer(i));

        List<Integer> list = new LinkedList<Integer>(integerSet);
    }



    /**
     * Returns indexes of random subset
     * @param target_size
     */
    public Set<Integer> randomSubset(int target_size){

        if (target_size > full_set_size)    return new HashSet<Integer>(list);

        Collections.shuffle(list, rand);
        Set<Integer> randomSet = new HashSet<Integer>(list.subList(0, target_size));
        return randomSet;
    }
}
