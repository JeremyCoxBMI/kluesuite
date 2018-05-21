package org.cchmc.kluesuite.klue;

import java.util.HashMap;

/**
 * A class for using Amino Acid codes.
 * In theory, a text letter can be converted to an integer and then converted to bits.
 *
 * 2016-08-15   v2.0    Straight import from v1.6.  NOTE: Pkmer10 was not brought along herewith.
 */
public class AminoAcids {

    static HashMap<Integer,String> intToAAname;
    static HashMap<Integer,Character> intToAAletter;
    static HashMap<Character, Integer> AAtoInt;

    static int ALANINE = 0;     // A
    static int CYSTEINE = 1;    // C
    static int ASPARTATE = 2;   // D
    static int GLUTAMATE = 3;   // E
    static int PHENYLALANINE = 4;   // F
    static int GLYCINE = 5;     // G
    static int HISTIDINE = 6;   // H
    static int ISOLEUCINE = 7;  // I
    static int LYSINE = 8;      // K
    static int LEUCINE = 9;     // L
    static int METHIONINE = 10; // M
    static int ASPARAGINE = 11; // N
    static int PROLINE = 12;    // P
    static int GLUTAMINE = 13;  // Q
    static int ARGININE = 14;   // R
    static int SERINE = 15;     // S
    static int THREONINE = 16;  // T
    static int VALINE = 17;     // V
    static int TRYPTOPHAN = 18; // W
    static int TYROSINE = 19;   // Y

    static {
        intToAAname = new HashMap<Integer, String>();
        intToAAletter = new HashMap<Integer, Character>();
        AAtoInt = new HashMap<Character, Integer>();

        intToAAname.put(ALANINE, "ALANINE");
        intToAAname.put(CYSTEINE, "CYSTEINE");
        intToAAname.put(ASPARTATE, "ASPARTATE");
        intToAAname.put(GLUTAMATE, "GLUTAMATE");
        intToAAname.put(PHENYLALANINE, "PHENYLALANINE");
        intToAAname.put(GLYCINE, "GLYCINE");
        intToAAname.put(HISTIDINE, "HISTIDINE");
        intToAAname.put(ISOLEUCINE, "ISOLEUCINE");
        intToAAname.put(LYSINE, "LYSINE");
        intToAAname.put(LEUCINE, "LEUCINE");
        intToAAname.put(METHIONINE, "METHIONINE");
        intToAAname.put(ASPARAGINE, "ASPARAGINE");
        intToAAname.put(PROLINE, "PROLINE");
        intToAAname.put(GLUTAMINE, "GLUTAMINE");
        intToAAname.put(ARGININE, "ARGININE");
        intToAAname.put(SERINE, "SERINE");
        intToAAname.put(THREONINE, "THREONINE");
        intToAAname.put(VALINE, "VALINE");
        intToAAname.put(TRYPTOPHAN, "TRYPTOPHAN");
        intToAAname.put(TYROSINE, "TYROSINE");

        intToAAletter.put(ALANINE, 'A');
        intToAAletter.put(CYSTEINE, 'C');
        intToAAletter.put(ASPARTATE, 'D');
        intToAAletter.put(GLUTAMATE, 'E');
        intToAAletter.put(PHENYLALANINE, 'F');
        intToAAletter.put(GLYCINE, 'G');
        intToAAletter.put(HISTIDINE, 'H');
        intToAAletter.put(ISOLEUCINE, 'I');
        intToAAletter.put(LYSINE, 'K');
        intToAAletter.put(LEUCINE, 'L');
        intToAAletter.put(METHIONINE, 'M');
        intToAAletter.put(ASPARAGINE, 'N');
        intToAAletter.put(PROLINE, 'P');
        intToAAletter.put(GLUTAMINE, 'Q');
        intToAAletter.put(ARGININE, 'R');
        intToAAletter.put(SERINE, 'S');
        intToAAletter.put(THREONINE, 'T');
        intToAAletter.put(VALINE, 'V');
        intToAAletter.put(TRYPTOPHAN, 'W');
        intToAAletter.put(TYROSINE, 'Y');

        AAtoInt.put('A', ALANINE);
        AAtoInt.put('C', CYSTEINE);
        AAtoInt.put('D', ASPARTATE);
        AAtoInt.put('E', GLUTAMATE);
        AAtoInt.put('F', PHENYLALANINE);
        AAtoInt.put('G', GLYCINE);
        AAtoInt.put('H', HISTIDINE);
        AAtoInt.put('I', ISOLEUCINE);
        AAtoInt.put('K', LYSINE);
        AAtoInt.put('L', LEUCINE);
        AAtoInt.put('M', METHIONINE);
        AAtoInt.put('N', ASPARAGINE);
        AAtoInt.put('P', PROLINE);
        AAtoInt.put('Q', GLUTAMINE);
        AAtoInt.put('R', ARGININE);
        AAtoInt.put('S', SERINE);
        AAtoInt.put('T', THREONINE);
        AAtoInt.put('V', VALINE);
        AAtoInt.put('W', TRYPTOPHAN);
        AAtoInt.put('Y', TYROSINE);

        AAtoInt.put('a', ALANINE);
        AAtoInt.put('c', CYSTEINE);
        AAtoInt.put('d', ASPARTATE);
        AAtoInt.put('e', GLUTAMATE);
        AAtoInt.put('f', PHENYLALANINE);
        AAtoInt.put('g', GLYCINE);
        AAtoInt.put('h', HISTIDINE);
        AAtoInt.put('i', ISOLEUCINE);
        AAtoInt.put('k', LYSINE);
        AAtoInt.put('l', LEUCINE);
        AAtoInt.put('m', METHIONINE);
        AAtoInt.put('n', ASPARAGINE);
        AAtoInt.put('p', PROLINE);
        AAtoInt.put('q', GLUTAMINE);
        AAtoInt.put('r', ARGININE);
        AAtoInt.put('s', SERINE);
        AAtoInt.put('t', THREONINE);
        AAtoInt.put('v', VALINE);
        AAtoInt.put('w', TRYPTOPHAN);
        AAtoInt.put('y', TYROSINE);
    }

    public static int AAtoInt(char c){
        return AAtoInt.get(c);
    }

    public static char intToAAletter (int k){
        return intToAAletter.get(k);
    }
}
