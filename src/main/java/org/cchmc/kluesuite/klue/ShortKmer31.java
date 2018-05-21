package org.cchmc.kluesuite.klue;

/**
 * This class represents a Kmer of length less than 31, but still enables lookup with KLUE.
 *
 * 2016-08-12   v2.0    New to 2.0
 * 2016-09-21           Changed to extend Kmer31 instead of Kmer31Slow
 *
 */
public class ShortKmer31 extends Kmer31 {

    //long twoE32 = 1L << 32;

    /**
     * INCLUSIVE lower bound of acceptable long values matching this Kmer31Slow
     */
    public long lowerbound;


    /**
     * EXCLUSIVE lower bound of acceptable long values matching this Kmer31Slow
     */
    public long upperbound;

    public int prefixLength;

    /**
     * Prefix length is important, otherwise since the tail end could be all T's, we would not if ended on T or not.
     *
     * @param value
     * @param prefixLength
     */
    public ShortKmer31(long value, int prefixLength){
        super(value);
        this.prefixLength = prefixLength;
        if (prefixLength > KMER_SIZE){
            System.err.println("ShortKmer31 constructor called for (illegal) k = "+prefixLength+"\n  therefore using 0L for upper and lower bound");
            lowerbound = 0L;
            upperbound = 0L;
        } else {
            int gap = KMER_SIZE - prefixLength;
            long multiple = 1L << (2 * gap);
            lowerbound = value / multiple * multiple;
            upperbound = lowerbound + multiple - 1;
        }
    }

    public ShortKmer31(ShortKmer31 copy){
        super(copy);
        int gap = KMER_SIZE - copy.prefixLength;
        long multiple = 1L << (2*gap);
        prefixLength = copy.prefixLength;
        lowerbound = this.toLong()/multiple * multiple;
        upperbound = lowerbound + multiple - 1;
    }

    public ShortKmer31 (String sequence){
        //This will call the overriden checkIllegalConstruction
        super(sequence);
        prefixLength = sequence.length();
        int gap = KMER_SIZE - sequence.length();
        long multiple = 1L << (2*gap);
        lowerbound = this.toLong()/multiple * multiple;
        upperbound = lowerbound + multiple - 1;
    }

    @Override
    protected void checkIllegalConstruction(boolean illegalChar, String sequence) {
        //Overriding this function allows us to construct ShortKmer31, where length < KMER_SIZE
        if (illegalChar  || sequence.length() >= KMER_SIZE) {
            if (!SUPPRESS_WARNINGS) {
                System.err.println("\tWARNING\tConstructor ShortKmer31 (String) failed.  Kmer31Slow marked isValid() = false");
                System.err.println("\t\t" + sequence);
            }
            seq.set((NUM_BITS - 1), true);
        }
    }

    /**
     * This functions tests if a FULL Kmer31Slow matches the k-mer of shorter length (nextOffset) this represents.
     * In other words, do the initial nextOffset letters match between both?
     *
     * @param value
     * @return
     */
    public boolean equal( long value ){
        return (lowerbound <= value && value <= upperbound );
    }

}
