package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.klue.FixedJavaBitSet;

/**
 * 8 byte key for KLUE  (interconverts to Long)
 *
 * 2016-08-12   v2.0    Imported without changes from V1.6
 *
 * IS THIS NOW DEPRECATED?  Since we have all types of FixedBitSet?
 */
public abstract class Key8 {


    /**
     * Warnings are displayed when constructor finds invalid sequence.
     * Squelch your minimize SPAM in System.err stream.
     */
    public static boolean SUPPRESS_WARNINGS = false;

    /**
     * Stores representation as bits
     */
    protected FixedJavaBitSet seq;
//    public FixedJavaBitSet seq;

    public static int KMER_SIZE = 0;
    static int BITS_PER_BASE = 0;
    static int COPY_BITS = BITS_PER_BASE * KMER_SIZE;  // 0 is bits per kmer
    //Key8 is 8 bytes
    static int NUM_BITS = 8 * 8;


    public Key8() {seq = new FixedJavaBitSet( 0L );}

    public Key8(long value){
        seq = new FixedJavaBitSet(value);
    }

    public Key8 (Key8 input){
        // makes a copy -- this constructor copies
        seq = new FixedJavaBitSet(input.seq);
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
     * Note that if absolute value of the invalid entry is the valid counterpart
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
