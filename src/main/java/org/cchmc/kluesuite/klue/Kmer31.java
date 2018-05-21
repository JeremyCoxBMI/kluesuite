package org.cchmc.kluesuite.klue;

/**
 * Class representing kmers of length 31
 *
 * 2016-08-12   v2.0    Imported with major changes from V1.6
 * 2016-09-14           Created "Kmer31" to test IFF using MyFixedBitSet would make a speed difference
 */
public class Kmer31 extends Key8Fast {

    /**
     * Integer coding for alphabet
     */
    public static int THYMINE = 0;
    public static int CYTOSINE = 1;
    public static int GUANINE = 2;
    public static int ADENINE = 3;

    private static long twoE60 = (1L << 60);

    public static int KMER_SIZE = 31;
    public static int KMER_SIZE_MINUS_ONE = KMER_SIZE - 1;
    public static int KMER_SIZE_MINUS_TWO = KMER_SIZE - 2;
    static int BITS_PER_BASE = 2;
    static int COPY_BITS = BITS_PER_BASE * KMER_SIZE;

    /**
     * Default value
     */
    public static Kmer31 ZERO = new Kmer31(0L);

    /**
     * SENTINEL is perpetually illegal value, guaranteed not to be used.
     * Negative numbers are not supposed to be used by design.  (This has not been altered so far.)
     */
    public static Kmer31 SENTINEL = new Kmer31(Long.MIN_VALUE);    //PERPETUALLY ILLEGAL VALUE, will not be found in STORE

//    /**
//     * Stores the kmer sequence as NUM_BITS bits.
//     * SHOULD BE PROTECTED and not PUBLIC, but we have weird test going on
//     */
//    public FixedJavaBitSet seq;

    public Kmer31() {
//        seq = new FixedJavaBitSet( 0L );
        super();
    }

    public Kmer31(long value){
//        seq = new FixedJavaBitSet( value );
        super(value);
    }

    public Kmer31(Key8Fast copy){
//        seq = new FixedJavaBitSet( copy.seq );
        super(copy);
    }

    public Kmer31(String sequence){
        seq = new MyFixedBitSet(NUM_BITS);
        boolean illegalChar = false;

        int loops = Math.min(sequence.length(),KMER_SIZE);

        for (int k = 0; k < loops; k++) {
//            int debug1 = COPY_BITS - (2 * k);
//            int debug2 = COPY_BITS - (2 * k) -1;
            if (sequence.charAt(k) == 'A' || sequence.charAt(k) == 'a') {
                seq.set((COPY_BITS-1) - (2 * k), true);
                seq.set((COPY_BITS-1) - (2 * k) - 1, true);
            } else if (sequence.charAt(k) == 'G' || sequence.charAt(k) == 'g') {
                seq.set((COPY_BITS-1) - (2 * k), true);
                seq.set((COPY_BITS-1) - (2 * k) - 1, false);
            } else if (sequence.charAt(k) == 'C' || sequence.charAt(k) == 'c') {
                seq.set((COPY_BITS-1) - (2 * k), false);
                seq.set((COPY_BITS-1) - (2 * k) - 1, true);
            } else if (sequence.charAt(k) == 'T' || sequence.charAt(k) == 't' || sequence.charAt(k) == 'U' || sequence.charAt(k) == 'u') {
                seq.set((COPY_BITS-1) - (2 * k), false);
                seq.set((COPY_BITS-1) - (2 * k) - 1, false);
            } else {
                illegalChar = true;
                break;
            }
        }

        checkIllegalConstruction(illegalChar,sequence);
        return;
    }

    /**
     * Helper function is pulled separate so child classes can overload it.
     * @param illegalChar
     * @param sequence
     */
    protected void checkIllegalConstruction(boolean illegalChar, String sequence){
        if (illegalChar  || sequence.length() < KMER_SIZE) {
            if (!SUPPRESS_WARNINGS) {
                System.err.println("\tWARNING\tConstructor Kmer31 (String) failed.  Kmer31 marked isValid() = false");
                System.err.println("\t\t" + sequence);
            }
            seq.set((NUM_BITS - 1), true);
        }
    }


    /**
     * Flip sequence to its complement
     * Preserves state of "isValid()"
     * @return
     */
    public Kmer31 inverseStrand() {
        Kmer31 result = new Kmer31(this);
        result.seq.flip(0,COPY_BITS);
        return result;
    }

    /**
     * Tests sequence to see if it is a palindrome
     * Palindromes can cause difficulties for lookups, causing false positives as forward and reverse strand are the same.
     *
     * @return
     */
    public boolean isPalindrome() {
        Kmer31 temp = this.reverseStrand();
        return (temp.toLong() == this.toLong());
        //seq.get(0, (KMER_SIZE/2)*2).intersects(seq.get(COPY_BITS-(KMER_SIZE/2)*2), COPY_BITS));  //divide by 2, multiply by 2, round down 31 to 30 bits
    }

//    @Override
//    public boolean isValid() {
//        //checks for sentinel values (negative integer)
//        return !(seq.get(NUM_BITS-1));
//    }

    /**
     * Creates the reverse strand kmer.
     * Note that in DNA, reverse strand is REVERSE and INVERSE
     * If original is inValid(), so is this
     *
     * @return
     */
    public Kmer31 reverseStrand() { //throws Exception{
        //FixedJavaBitSet result = new FixedJavaBitSet(NUM_BITS);
        if (!isValid()) {
            if (!SUPPRESS_WARNINGS) System.err.println("\tWARNING\tTrying to reverseStrand an invalid sequence\t"+toString()+"\t"+toBinaryString());
            //throw new DataFormatException("Kmer31 , marked inValid()==true should not be reversed");
        }

        Kmer31 result = new Kmer31(0);

        for (int k = 0; k < KMER_SIZE; k++) {
            //REVERSE AND INVERSE at same time

            // All letters are flips of each other
            // G represented by 2 (10) and C represented  by 1 (01) are reverses, so DONE
            // A represented by 3 (11) and T represented  by 0 (00) are reverse _inverses_
            //      										 flip bits in reverse _inverses_
            boolean v;
            if ((v = seq.get(2 * k)) == seq.get(2 * k + 1)) {
                //if equal, then flip both when reversing
                result.seq.set((COPY_BITS-1) - (2 * k), !v);
                result.seq.set((COPY_BITS-1) - (2 * k + 1), !v);
            } else {
                //else, just reverse  the bits are opposite each other
                result.seq.set((COPY_BITS-1) - (2 * k), v);
                result.seq.set((COPY_BITS-1) - (2 * k + 1), !v);
            }
        }

        //Preserves INVALID state
        result.seq.set(NUM_BITS - 1, seq.get(NUM_BITS - 1));

        return result;
    }


    public String toString() {
        String result = "";
        for (int k = 0; k < (KMER_SIZE); k++) {
            if (seq.get((COPY_BITS-1) - (2 * k)) == true && seq.get((COPY_BITS-1) - (2 * k) - 1) == true) {
                result += "A";
            } else if (seq.get((COPY_BITS-1) - (2 * k)) == true && seq.get((COPY_BITS-1) - (2 * k) - 1) == false) {
                result += "G";
            } else if (seq.get((COPY_BITS-1) - (2 * k)) == false && seq.get((COPY_BITS-1) - (2 * k) - 1) == true) {
                result += "C";
            } else if (seq.get((COPY_BITS-1) - (2 * k)) == false && seq.get((COPY_BITS-1) - (2 * k) - 1) == false) {
                result += "T";
            }
        }
        return result;
    }


}

