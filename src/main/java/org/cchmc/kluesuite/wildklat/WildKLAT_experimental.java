package org.cchmc.kluesuite.wildklat;

import org.cchmc.kluesuite.klue.DNAcodes;

import java.util.ArrayList;
import java.util.Set;

/**
 * This class is built to manage wild card searches.  The idea is to not do these in volume, but it is necessary.
 *
 * Other than a catchy name, I've got no ideas on how to best build this yet.
 */

public class WildKLAT_experimental {

    ArrayList<PrefixPairBuilder> building;
    PrefixPair[] pairs;

    public WildKLAT_experimental(String s) {
        //build combinations
        int l = s.length();
        building = new ArrayList<>();
        building.add(new PrefixPairBuilder(0L,0));

        ArrayList<PrefixPairBuilder> temp = new ArrayList<>();
        //iterate over string using the DNA codes
        for (int k = 0; k < l; k++) {
            Set<Character> z = DNAcodes.substituteSNP.get(s.charAt(k));
            Character[] zc = (Character[]) z.toArray();
            //iterate over final entries
            for (int j = 0; j < zc.length; j++) {
                for (PrefixPairBuilder p : building) {
                    PrefixPairBuilder f = new PrefixPairBuilder(p.getKey(), p.getBases());
                    f.addBase((long) DNAcodes.getInt(zc[j]));
                    temp.add(f);
                }
            }
            //iterate and build again 1 letter at a time
            building = temp;
        } //end for k i.e. (char : String s)

        pairs = new PrefixPair[building.size()];
        for (int k=0; k< pairs.length; k++){
            pairs[k] = building.get(k).finish();

        }
        building = null; //erase
    } //end constructor


}
