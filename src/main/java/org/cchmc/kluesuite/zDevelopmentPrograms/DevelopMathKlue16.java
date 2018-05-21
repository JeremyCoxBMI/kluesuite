package org.cchmc.kluesuite.zDevelopmentPrograms;

import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.multifilerocksdbklue.Prefix16;

/**
 * Created by osboxes on 30/09/16.
 */
public class DevelopMathKlue16 {


    public static void main(String[] args) {

        Kmer31 chump = new Kmer31("CCGGGGGGGGAAAAAAAAAATTTTTTTTTCC");

        int prefix = (int) (chump.toLong() / (1L << 58));

        System.out.println("CC converts to prefix (expect 5) : "+prefix);
        System.out.println("CCGGGGGGGGAAAAAAAAAATTTTTTTTTCC".substring(0,2));


        System.out.println("intToPrefixString(7) CA  :> "+Prefix16.intToPrefixString(7));
        System.out.println("stringToInt(CA) 7  :> "+Prefix16.prefixToInt("CA"));
        System.out.println("stringToInt(CATT) 7  :> "+Prefix16.prefixToInt("CATT"));
        Kmer31 barry = new Kmer31("ATCGATCGATCGATCGATCGATCGATCGATC");
        System.out.println("kmer31ToPrefixString() AT  :> "+Prefix16.kmer31ToPrefixString(barry));
        System.out.println("kmer31ToPrefixInt() AT-12  :> "+Prefix16.kmer31ToPrefixInt(barry));
    }
}
