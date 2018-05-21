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


    /**
     * Generate a DNA string based on integer representation
     * @param k
     * @return
     */

    public static String intToPrefixString(int k){
        String result = "";
        if (0 <= k && k < 16){
            result += hm.get(k / 4);
            result += hm.get(k % 4);
        }
        return result;
    }


    // THREE KINDS OF DATA
    //  long or Kmer31 (same)
    //  prefix integer
    //  prefix string
    //  prefix byte, as in the first byte of a long byte array

    public static String kmer31ToPrefixString(Kmer31 kmer){
        return intToPrefixString( (int) (kmer.toLong() / (1L << 58)) );
    }

    public static int kmer31ToPrefixInt(Kmer31 kmer){
        return  (int) (kmer.toLong() >> 58);
    }

    public static int longToInt(long key) {
        return (int) (key >> 58);
    }

    public static Kmer31 prefixIntToKmer31(int p){
        long p2 = (long) p;
        p2 = p2 << 58;
        return new Kmer31(p2);
    }

    /**
     * Convert a prefix integer to the long representation of the corresponding K-mer at lower bound of prefix
     * (That is, append all zeroes to prefix)
     * @param p
     * @return
     */

    public static long prefixIntToKmer31Long(int p) {
        return (long) p << 58;
    }

    /**
     * Using byte representation saves time when using raw bytes from RocksIterator
     * @param key   this is the 8 byte array representation of a long integer or Kmer31
     * @return
     */
    public static int bytesToPrefixInt(byte[] key) {
        return byteToPrefixInt(key[0]);
    }


    /**
     * @param key   a byte, aka 8-bit signed int;  in java a byte is an integer
     * @return
     */
    public static int byteToPrefixInt(byte key) {
//        int r = ((int)key);
//        r = r & 63;
//        r = r >> 2;
        return (((int)key) & 63) >> 2;   //63 is 00111111, skips the first two bits
    }


}
