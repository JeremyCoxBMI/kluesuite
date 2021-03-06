package org.cchmc.kluesuite.klue;

import java.util.*;

/**
 * This class contains the equivalency information for DNA letter codes.
 *
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
public class DNAcodes {

    //see    https://en.wikipedia.org/wiki/FASTA_format
    public static    boolean DEBUG = false;

    /**
     * Contains many characters (HashSet) that are equivalent to the first
     */
    public static HashMap<Character, HashSet<Character>> equivalency;

    public static HashMap<Character, HashSet<Character>> substituteSNP;

    /**
     * Maps DNA or RNA letter to its DNA complement
     */
    public static HashMap<Character, Character> complement;


    public static HashSet<Character> all;

    static {
        equivalency = new HashMap<Character, HashSet<Character>>();
        substituteSNP = new HashMap<Character, HashSet<Character>>();
        complement = new HashMap<Character, Character>();

        all = new HashSet<Character>( Arrays.asList('a','A','t','T','u','U','c','C','g','G','n','N',
                'R','r','Y','y','K','k','M','m','S','s','W','w','B','b',
                'D','d','H','h','V','v','N','n','?'
        ) );
        //Gap character '-' matches nothing
        //Singles
        equivalency.put(  'A', new HashSet<Character>( Arrays.asList('A','a') )  );
        equivalency.put(  'a', new HashSet<Character>( Arrays.asList('A','a') )  );
        equivalency.put(  'G', new HashSet<Character>( Arrays.asList('G','g') )  );
        equivalency.put(  'g', new HashSet<Character>( Arrays.asList('G','g') )  );
        equivalency.put(  'T', new HashSet<Character>( Arrays.asList('T','t','u','U') )  );
        equivalency.put(  't', new HashSet<Character>( Arrays.asList('t','T','u','U') )  );
        equivalency.put(  'U', new HashSet<Character>( Arrays.asList('U','u','t','T') )  );
        equivalency.put(  'u', new HashSet<Character>( Arrays.asList('u','U','t','T') )  );
        equivalency.put(  'C', new HashSet<Character>( Arrays.asList('C','c') )  );
        equivalency.put(  'c', new HashSet<Character>( Arrays.asList('C','c') )  );


        //Doubles
        writeCombinations(  'R', 'r', new HashSet<Character>( Arrays.asList('A','a','G','g') )  );
        substituteSNP.put(  'R', new HashSet<Character>(Arrays.asList('A','G')));
        substituteSNP.put(  'r', new HashSet<Character>(Arrays.asList('A','G')));

        writeCombinations(  'Y', 'y', new HashSet<Character>( Arrays.asList('C','T','U','c', 't', 'u') )  );
        substituteSNP.put(  'Y', new HashSet<Character>(Arrays.asList('C','T')));
        substituteSNP.put(  'y', new HashSet<Character>(Arrays.asList('C','T')));

        writeCombinations(  'K', 'k', new HashSet<Character>( Arrays.asList('G','T','U','g', 't', 'u') )  );
        substituteSNP.put(  'K', new HashSet<Character>(Arrays.asList('G','T')));
        substituteSNP.put(  'k', new HashSet<Character>(Arrays.asList('G','T')));

        writeCombinations(  'M', 'm', new HashSet<Character>( Arrays.asList('C','A','c','a') )  );
        substituteSNP.put(  'M', new HashSet<Character>(Arrays.asList('C','A')));
        substituteSNP.put(  'm', new HashSet<Character>(Arrays.asList('C','A')));

        writeCombinations(  'S', 's', new HashSet<Character>( Arrays.asList('C','G','c','g') )  );
        substituteSNP.put(  'S', new HashSet<Character>(Arrays.asList('C','G')));
        substituteSNP.put(  's', new HashSet<Character>(Arrays.asList('C','G')));

        writeCombinations(  'W', 'w', new HashSet<Character>( Arrays.asList('A','T','U','a','t','u') )  );
        substituteSNP.put(  'W', new HashSet<Character>(Arrays.asList('A','T')));
        substituteSNP.put(  'w', new HashSet<Character>(Arrays.asList('A','T')));

        //Triples
        writeCombinations(  'B', 'b', new HashSet<Character>( Arrays.asList('C','T','G','U','c','t','g','u') )  );
        substituteSNP.put('B', new HashSet<Character>(Arrays.asList('C', 'T', 'G')));
        substituteSNP.put(  'b', new HashSet<Character>(Arrays.asList('C','T','G')));

        writeCombinations('D', 'd', new HashSet<Character>(Arrays.asList('A', 'T', 'G', 'U', 'a', 't', 'g', 'u')));
        substituteSNP.put('D', new HashSet<Character>(Arrays.asList('A', 'T', 'G')));
        substituteSNP.put(  'd', new HashSet<Character>(Arrays.asList('A','T','G')));

        writeCombinations('H', 'h', new HashSet<Character>(Arrays.asList('A', 'T', 'C', 'U', 'a', 't', 'c', 'u')));
        substituteSNP.put('H', new HashSet<Character>(Arrays.asList('A', 'T', 'C')));
        substituteSNP.put(  'h', new HashSet<Character>(Arrays.asList('A','T','C')));

        writeCombinations('V', 'v', new HashSet<Character>(Arrays.asList('A', 'G', 'C', 'a', 'g', 'c')));
        substituteSNP.put('V', new HashSet<Character>(Arrays.asList('A', 'G', 'C')));
        substituteSNP.put(  'v', new HashSet<Character>(Arrays.asList('A','G','C')));

        writeCombinations('T', 't', new HashSet<Character>(Arrays.asList('T', 't')));
        substituteSNP.put('T', new HashSet<Character>(Arrays.asList('T')));
        substituteSNP.put(  't', new HashSet<Character>(Arrays.asList('T')));
        writeCombinations('A', 'a', new HashSet<Character>(Arrays.asList('A', 'a')));
        substituteSNP.put('A', new HashSet<Character>(Arrays.asList('A')));
        substituteSNP.put(  'a', new HashSet<Character>(Arrays.asList('a')));

        writeCombinations('C', 'c', new HashSet<Character>(Arrays.asList('C', 'c')));
        substituteSNP.put('C', new HashSet<Character>(Arrays.asList('C')));
        substituteSNP.put(  'C', new HashSet<Character>(Arrays.asList('C')));

        writeCombinations('G', 'g', new HashSet<Character>(Arrays.asList('G', 'g')));
        substituteSNP.put('G', new HashSet<Character>(Arrays.asList('G')));
        substituteSNP.put(  'g', new HashSet<Character>(Arrays.asList('G')));

                //should there be writeCombinations here for 'N'?
        substituteSNP.put(  'N', new HashSet<Character>(Arrays.asList('A', 'T', 'C', 'G')));
        substituteSNP.put(  'n', new HashSet<Character>(Arrays.asList('A', 'T', 'C', 'G')));

        //Wildcards
        equivalency.put(  'N', new HashSet<Character>( all )   );
        equivalency.put(  'n', new HashSet<Character>( all )   );
        equivalency.put(  '?', new HashSet<Character>( all )   );
        for (char c : equivalency.get('N')){
            equivalency.get(c).add('N');
            equivalency.get(c).add('n');
            equivalency.get(c).add('?');
        }


        // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
        // complements
        complement.put('a','T');
        complement.put('A','T');
        complement.put('t','A');
        complement.put('T','A');
        complement.put('u','A');
        complement.put('U','A');
        complement.put('c','G');
        complement.put('C','G');
        complement.put('g','C');
        complement.put('G','C');


        complement.put('r','Y');
        complement.put('R','Y');
        complement.put('y','R');
        complement.put('Y','r');
        complement.put('k','M');
        complement.put('K','M');
        complement.put('m','K');
        complement.put('M','K');
        complement.put('s','S');
        complement.put('S','S');
        complement.put('w','W');
        complement.put('W','W');
        complement.put('b','V');
        complement.put('B','V');
        complement.put('v','B');
        complement.put('V','B');
        complement.put('d','H');
        complement.put('D','H');
        complement.put('h','D');
        complement.put('H','D');
        complement.put('n','N');
        complement.put('N','N');
        complement.put('-','-');
        complement.put('?','N');

    }

    private static void writeCombinations(char letter1, char letter2, HashSet<Character> equals){
        equivalency.put(  letter1, equals  );
        equivalency.put(  letter2, equals  );
        for (char c : equals){
            equivalency.get(c).add(letter1);
            equivalency.get(c).add(letter2);
        }
        equivalency.get(letter1).add(letter1);
        equivalency.get(letter1).add(letter2);
    }


    public static boolean equals(char a, char b){
        if ( equivalency.containsKey(a) && equivalency.containsKey(b))
            return equivalency.get(a).contains(b);
        else
            return false;
    }

    /**
     * lookup complement DNA code
     * @param x
     * @return
     */
    public static char getComplement(char x){
        return complement.get(x);
    }

    /**
     * Returns INVERSE and REVERSE DNA sequence (or, commonly called Reverse Strand)
     * @param sequence
     */
    public static String reverseStrandSequence (String sequence) {
        String result = "";
        int len = sequence.length();
        for( int k = len-1; k >= 0; k--){
            if (complement.containsKey(sequence.charAt(k))) {
                result += getComplement(sequence.charAt(k));
            } else {
                System.err.println("Attempting to reverse strand illegal character : "+sequence.charAt(k)+" :  ABORTING");
                break;
            }
        }
        return result;
    }



    public static void main(String[] args){
        System.out.println("\nThese all should be true");
        System.out.println(DNAcodes.equals('N','n'));
        System.out.println(DNAcodes.equals('n','N'));
        System.out.println(DNAcodes.equals('M','A'));
        System.out.println(DNAcodes.equals('a','M'));
        System.out.println(DNAcodes.equals('W','W'));
        System.out.println(DNAcodes.equals('W','w'));
        System.out.println(DNAcodes.equals('w','W'));

        System.out.println("\nThese all should be false");
        System.out.println(DNAcodes.equals('&','n'));
        System.out.println(DNAcodes.equals('D','C'));
        System.out.println(DNAcodes.equals('c','D'));

        System.out.println("\ntesting reverseStrandSequence");
        String seq = "WATTGTTGCCGAAGGTCTGTTATTTGAATGTTGAGATAAGGAAAGGGGCGGCGAAGCATGTGTGTATAAT" +
                "AACATAT";
        System.out.println("IN  : "+seq);
        System.out.println("OUT : "+reverseStrandSequence(seq));


        System.out.println("\ntesting reverseStrandSequence");
        seq = "ATCCATTCCGTCATACACGCTAACCGGGAACAAAATCAATCTATCATGCACCAGATGTCCCGGACAAGAT";
        System.out.println("IN  : "+seq);
        System.out.println("OUT : "+reverseStrandSequence(seq));

    }

    public static Character findMatch(Set<Character> snps) {
//        if (snps.contains('A') || snps.contains('a')){
//            if (snps.contains('C') || snps.contains('c')){
//                if (snps.contains('G') || snps.contains('g')){
//                    if (snps.contains('T') || snps.contains('t')|| snps.contains('U')|| snps.contains('u')){
//                }
//            }
//
//        } //end if A

        Character result = null;

        boolean a = false, t = false, c = false, g = false;
        int sum = 0;
        for (Character s : snps){
            switch (s) {
                case 'A':
                case 'a':
                    if (!a) sum += 1;
                    a = true;
                    break;
                case 'T':
                case 't':
                case 'U':
                case 'u':
                    if (!t) sum += 2;
                    t = true;
                    break;

                case 'C':
                case 'c':
                    if (!c) sum += 4;
                    c = true;
                    break;

                case 'G':
                case 'g':
                    if (!g) sum += 8;
                    g = true;
                    break;
            }
        }

        switch (sum){
            case 1:
                result = 'A';
                break;
            case 2:
                result = 'T';
                break;
            case 3:
                result = 'W';
                break;
            case 4:
                result = 'C';
                break;
            case 5:
                result = 'M';
                break;
            case 6:
                result = 'Y';
                break;
            case 7:
                result = 'H';
                break;
            case 8:
                result = 'G';
                break;
            case 9:
                result = 'R';
                break;
            case 10:
                result = 'W';
                break;
            case 11:
                result = 'D';
                break;
            case 12:
                result = 'S';
                break;
            case 13:
                result = 'V';
                break;
            case 14:
                result = 'B';
                break;
            case 15:
                result = 'N';
                break;

        }

        if (DEBUG) System.err.println("Set Conversion : "+snps+"\tto\t"+result+"\tto\t"+substituteSNP.get(result));

        return result;
    }

    public static int getInt(Character character) {
        char select = complement.get(character);
        switch (select){
            case 'T':
                return 0;
            case 'C':
                return 1;
            case 'G':
                return 2;
            case 'A':
                return 3;
        }
        return -1;
    }
}

