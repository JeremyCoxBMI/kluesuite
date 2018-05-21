package org.cchmc.kluesuite.wildklat;

import org.cchmc.kluesuite.klue.DNAcodes;
import org.cchmc.kluesuite.klue.Kmer31;

/**
 * Created by jwc on 3/13/18.
 */
public class PrefixPairBuilder {

    PrefixPair result;

    public PrefixPairBuilder(){
        result = new PrefixPair(0L,0);
    }

    public PrefixPairBuilder(long k, int b){
        result = new PrefixPair(k,b);
    }

    public PrefixPairBuilder(PrefixPair p){
        result = new PrefixPair(p.key,p.bases);
    }

    public PrefixPairBuilder(PrefixPairBuilder p){
        result = new PrefixPair(p.getKey(), p.getBases());
    }

    public void addBases(String s){
        int t = s.length();
        for (int k=0; k<t;k++){
            addBase(DNAcodes.getInt(s.charAt(k)));
        }
    }

    /**
     * range from 0-3, representing the base encoded
     * @param k
     */
    public void addBase(long k){
        //k = k << (2*result.bases);
        result.key = result.key << 2 + k;
        result.bases++;
    }

    public PrefixPair finish(){
        int shifty = 62 - 2*result.bases;
        result.key = result.key << shifty;
        result.bases = 31;
        return result;
    }

    public PrefixPair getPrefixPair(){return result;}

    public int getBases(){ return result.bases;}

    public long getKey(){return result.key;}
}
