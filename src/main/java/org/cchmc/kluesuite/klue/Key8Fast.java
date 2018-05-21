package org.cchmc.kluesuite.klue;

/**
 * 8 byte key for KLUE  (interconverts to Long)
 *
 * 2016-08-12   v2.0    Imported without changes from V1.6
 * 2016-09-14           Created new Key8Fast, to use MyFixedBitSet
 *                      This is sloppy programming, because the idea of having multiple FixedBitSet classes came as afterthought
 */
public abstract class Key8Fast {


    /**
     * Warnings are displayed when constructor finds invalid sequence.
     * Squelch your minimize SPAM in System.err stream.
     */
    public static boolean SUPPRESS_WARNINGS = false;

    /**
     * Stores representation as bits
     * Note by definition the NEGATIVE bit stores whether the sequence is invalid.  (i.e. Negative integer is invalid)
     */
    public MyFixedBitSet seq;
//    public FixedJavaBitSet seq;

    public static int KMER_SIZE = 0;
    static int BITS_PER_BASE = 0;
    static int COPY_BITS = BITS_PER_BASE * KMER_SIZE;  // 0 is bits per kmer
    //Key8 is 8 bytes
    static int NUM_BITS = 8 * 8;


    public Key8Fast() {seq = new MyFixedBitSet( 0L );}

    public Key8Fast(long value){
        seq = new MyFixedBitSet(value);
    }

    public Key8Fast (Key8Fast input){
        // makes a copy -- this constructor copies
        seq = new MyFixedBitSet(input.seq);
    }

    /**
     *  Converts to a long integer.
     */
    public long toLong() {
        return seq.getLastLong();
    }

    public byte[] toBytes() {
        return seq.toByteArray();
    }

    /**
     * A negative long is an invalid entry.
     * The most typical causes of this error are
     *  (1) the bit array has room for the last bit, and it can be written, even though it is forbidden
     *  (2) constructors flag itself as invalid due to error (rather than throwing an exception)
     * @return
     */
    public boolean isValid() {
        //checks for sentinel values (negative integer)
//        boolean debug = !(seq.get(NUM_BITS-1));
        return !(seq.get(NUM_BITS-1));
    }

    public String toBinaryString(){
        return seq.toBinaryString();
    }


}

