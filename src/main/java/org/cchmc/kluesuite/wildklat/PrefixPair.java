package org.cchmc.kluesuite.wildklat;

/**
 * Created by jwc on 3/13/18.
 */
public class PrefixPair {

    /**
     * the key, including zeroes
     */
    long key;

    /**
     * Number of bases stored in the key
     */
    int bases;

    //use PrefixPairBuilder
//    public PrefixPair(int depth, String s){
//
//    }

    public PrefixPair(long l, int i) {
        key = l;
        bases =i;
    }

    public boolean isValid(){
        return bases == 31;
    }
}
