package org.cchmc.kluesuite.memoryklue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by jwc on 8/24/17.
 */
public class KVpair  {

    public long key;
    public ArrayList<Long> value;

    public KVpair(long k, long v){
        value = new ArrayList<Long>();
        value.add(v);
        key = k;
    }

    public KVpair(long k, ArrayList<Long> v){
        value = v;
        key = k;
    }

    public void add(long v){
        value.add(v);
    }

    public String toString(){
        return "{"+key+",\t"+ Arrays.toString(value.toArray())+"}";
    }
}


final class KVpairComparator implements Comparator<KVpair> {
    @Override
    public int compare(KVpair lookUp, KVpair t1) {
        int result;
        long chump = lookUp.key - t1.key;
        if (chump > (long) Integer.MAX_VALUE){
            result = Integer.MAX_VALUE;
        } else if (chump < (long) Integer.MIN_VALUE){
            result = Integer.MIN_VALUE;
        } else {
            result = (int) chump;
        }
        return result;
    }
}

