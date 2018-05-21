package org.cchmc.kluesuite.wildklat;

import org.cchmc.kluesuite.klue.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by COX1KB on 4/4/2018.
 *
 * This is a function library to create all possible variants given a FastA wild card string.
 */
public class PermuteDnaString {


        /*
     * This is the FastA standardized alphabet
     * R    :   A, G
     * Y    :   C, T, (U)
     * K    :   G, T, (U)
     * M    :   C, A
     * S    :   C, G
     * W    :   A, T, (U)
     * B    :   C, G, T, (U)
     * D    :   A, G, T, (U)
     * H    :   A, C, T, (U)
     * V    :   A, C, G
     * N    :   A, G, G, T (U)
     * ?    :   A, G, G, T (U)
     */

    public static ArrayList<String> createPermutationsWithoutGaps(String s){
        ArrayList<String> result = new ArrayList<>();
        result.add(""); //starts with one empty string to add possibilities



        char[] sarr = s.toCharArray();
        for (int k=0; k<sarr.length; k++){
            ArrayList<Character> p = new ArrayList<>();
            p.addAll( DNAcodes.substituteSNP.get(sarr[k]));
            int previousNumberSequences = result.size();

            for (int z=0; z < previousNumberSequences; z++) {

                //write last letters as new permutations first
                for (int j=1; j < p.size(); j++ ) {
                    result.add(result.get(z) + p.get(j));
                }
                //modify the original permutation
                result.set(z, result.get(z) + p.get(0));

            } // end for each previous sequence

        } // end for character permutation
        return result;
    }

    public static ArrayList<ShortKmer31> permutationsToShortKmers(ArrayList<String> strings){
        ArrayList<ShortKmer31> result = new ArrayList<>();
        for (String s : strings){
            result.add(new ShortKmer31(s));
        }
        return result;
    }

    public static PositionList shortKmerLookUpNoGaps(String s, ShortKLUE sk){
        PositionList result = new PositionList();
        for( ShortKmer31 shorty : permutationsToShortKmers(createPermutationsWithoutGaps(s))) {
            result.add( sk.getShortKmerMatches(shorty.toLong(), s.length()));
        }
        return result;
    }


    public static void printArrayAsLines(ArrayList<String> as, String prefix, PrintStream ps){
        for (String s : as)  ps.println(prefix+s);
    }

}
