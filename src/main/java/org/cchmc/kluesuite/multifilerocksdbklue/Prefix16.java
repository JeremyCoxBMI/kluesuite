package org.cchmc.kluesuite.multifilerocksdbklue;

import org.cchmc.kluesuite.klue.Kmer31;

import java.util.HashMap;

/**
 * Created by osboxes on 30/09/16.
 *
 * This is a wrapper for a simple concept: we will divide kmers into databases based on first two letters
 * "prefix 16" represents two letters as a number 0 thru 15 inclusive.
 * This is a library of functions.  They are very simple; mostly this is code documentation of the process.
 *
 * Note that bit shifting allows us considerable speed.
 * THIS ONLY WORKS because we know a Key8 / Kmer31 long will *not* be negative.
 */
public class Prefix16 {

    static HashMap<Integer, Character> hm;

    static {
        hm = new HashMap<Integer, Character>();
        hm.put(0,'T');
        hm.put(1,'C');
        hm.put(2,'G');
        hm.put(3,'A');
    }

    public static int prefixToInt(String prefix){
        int temp = 0;
        if (prefix.length() < 2){
            System.err.println("\tWARNING\tPrefix16.prefixToInt called with a String shorter than length 2");
            temp = -1;
        } else {
            for(int k=0; k<2; k++){
                temp = temp << 2;
                switch(prefix.charAt(k)){
                    case 'A':
                    case 'a':
                        temp += 3;
                        break;
                    case 'T':
                    case 't':
                        temp += 0;
                        break;
                    case 'C':
                    case 'c':
                        temp += 1;
                        break;
                    case 'G':
                    case 'g':
                        temp += 2;
                        break;
                }
            }
        }
        return temp;
    }

    public static String intToPrefix(int k){
        String result = "";
        if (0 <= k && k < 16){
            result += hm.get(k / 4);
            result += hm.get(k % 4);
        }
        return result;
    }

    public static String kmer31ToPrefix(Kmer31 kmer){
        return intToPrefix( (int) (kmer.toLong() / (1L << 58)) );
    }

    public static int kmer31ToInt(Kmer31 kmer){
        return  (int) (kmer.toLong() >> 58);
    }

    public static int longToInt(long key) {
        return (int) (key >> 58);
    }
}
