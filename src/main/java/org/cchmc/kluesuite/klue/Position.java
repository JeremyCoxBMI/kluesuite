package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.klue.FixedJavaBitSet;

import java.util.BitSet;
import java.util.Comparator;

import static java.lang.Long.parseLong;

/**
 * Position class stores the Position information for a key
 * allows importing from 9 bytes, exporting to 9 bytes
 * and modifying the data
 *
 * 2016-08-15   v2.0    Copied from v1.6 with change to allow quick reconfiguration of the bit redistribution
 * 2016-11-18           SNP and INDEL flags added to class; SNP indicates that the Kmer at this position contains an
 *                      alternate base as a SNP.  (I.e. the default value is not flagged as a SNP)  INDEL indicates
 *                      the Kmer contains an insertion or deletion variant from the normal sequence.
 *
 *
 * A position of the reverse strand marks the position of the last member of the forward strand.
 * Thus, a reverse strand version of position 0, points to position 30, and runs "from 30 to 0" (inclusive)
 *
 * Current limitation:   KID :     0 to 2^28 - 1  =  268,435,455
 *                       Position: 0 to 2^28 - 1  =  268,435,455
 *
 */

/* Major redesign of Position Class ISSUE #85

 We want keys to sort by
    - forward/reverse
    - KLUE id
    - position
    ( see to EncodedLong)

    so
        BIT 63      - reverse
        BIT 35-62   - KID
        BIT 07-34   - Position
        BIT 00-06   - more flags

    Transcriptome bit already exists (ISSUE #84)
     But, I propose a new way to use it.
        These are for DNA that are also protein coding. (Exons/mRNA/transcriptome)
 */

/*
    Problem with this class is that a Position is kid with position (or sequence position), so position is not Postion...
 */

public class Position extends Key8 implements Comparable<Position>, Comparator<Position> {

    //coded to be in lexicographic order
    final static int REVERSE_BITS = 1;  //leftmost bits
    final static int KID_BITS = 28;     //second leftmost
    final static int POS_BITS = 28;
    final static int FLAG_BITS = 7;     //(rightmost bits)


    //ISSUE #85 update; more flexible for future
    final static int POS_START = FLAG_BITS;
    final static int KID_START = FLAG_BITS + POS_BITS;
    final static int FLAG_START = 0;
    final static int FLAG_END = FLAG_BITS;
    final static int POS_END = FLAG_BITS + POS_BITS;  //EXCLUSIVE
    final static int KID_END = FLAG_BITS + POS_BITS + KID_BITS;  //EXCLUSIVE



    public static int MAX_POS = (Integer.MAX_VALUE >> (31-POS_BITS));
    public static int MAX_KID = (Integer.MAX_VALUE >> (31-KID_BITS));

    // LIST OF FLAGS from left to right; use these with setters =e.g.=>  setFlag(REVERSE, true);
    // REVERSE == true means the long number is negative.  Just a little programmer humor.  Also, this conserves bits.
    public static int REVERSE = 63;
    public static int START = 06;
    public static int STOP = 04;
    public static int TRANSCRIPTOME = 04;
    public static int SNP = 03;
    public static int INDEL = 02;
    public static int UNDEFINED1 = 01;
    public static int UNDEFINED2 = 00;


    public Position(){
        super();
    }

    public Position(long l){
        super(l);
    }

    public Position(Key8 k){
        super(k);
    }

    /**
     * Undefined flags default to false;
     * In most cases, this will be correct.
     * If not, flags can be set via setters.
     * @param kid
     * @param pos
     */
    public Position( int kid, int pos){
        super();
        COPY_BITS = NUM_BITS;
        BITS_PER_BASE = 2;
        KMER_SIZE = 31;

        extractBitsFromNumber(kid, KID_BITS, KID_START);
        extractBitsFromNumber(pos, POS_BITS, POS_START);
        //flags default to false
    }

    public Position( int kid, int pos, boolean reverse) {
        super();
        COPY_BITS = NUM_BITS;
        BITS_PER_BASE = 2;
        KMER_SIZE = 31;

        extractBitsFromNumber(kid, KID_BITS, KID_START);
        extractBitsFromNumber(pos, POS_BITS, POS_START);
        seq.set(REVERSE, reverse);
    }

    /**
     * Allows setting of flags as a binary string.  Most useful for manual entry/deubbing.
     * In production, flags more likely to be defined using setters.
     * @param kid
     * @param pos
     * @param binStr
     */
    public Position( int kid, int pos, String binStr){
        super();
        COPY_BITS = NUM_BITS;
        BITS_PER_BASE = 2;
        KMER_SIZE = 31;

        extractBitsFromNumber(kid, KID_BITS, KID_START);
        extractBitsFromNumber(pos, POS_BITS, POS_START);

        FixedJavaBitSet tempy = new FixedJavaBitSet(FLAG_BITS, BitSet.valueOf(new long[] { parseLong(binStr, 2) }));
        for( int k=0; k<(FLAG_BITS); k++) {
            seq.set(k, tempy.get(k+FLAG_START));
        }
        seq.set(REVERSE,tempy.get(FLAG_BITS+1));
    }

    /**
     * helper function to convert integers to requisite number of bits and put in correct location in storage
     *
     * @param number
     * @param numBits
     * @param saveBit
     */
    private void extractBitsFromNumber( int number, int numBits, int saveBit){
        FixedJavaBitSet temp = new FixedJavaBitSet( numBits, (long) number );
        for (int k = 0; k < numBits; k++ ){
            seq.set(k+saveBit, temp.get(k));
        }
        int debugLIne = 1;
    }


    public boolean isFlagBit(int index){
        //before ISSUE #85
        //( KID_BITS+POS_BITS <= index && index < KID_BITS+POS_BITS+FLAG_BITS)
        return (FLAG_START <= index && index < FLAG_END)
                || index == REVERSE;
    }


    /**
     * Example usage:
     * myvar.setFlag(Position.REVERSE, true);
     * Does not range check
     * @param index (int)		bit index: accepts 28 thru 31 inclusive)
     * @param val	(boolean)	value to set
     */
    public void setFlag(int index, boolean val){

        if (isFlagBit(index)    ){
           seq.set(index, val);
        }
    }

    public boolean getFlag(int index){
        if (isFlagBit(index)){//
            return seq.get(index);
        } else {
            return Boolean.parseBoolean(null);
        }
    }

    public int getMyKID(){
        return (int) new FixedJavaBitSet( KID_BITS, seq.get(KID_START, KID_END) ).toLongArray()[0];
    }

    public int getPosition(){
        return (int) new FixedJavaBitSet( POS_BITS, seq.get(POS_START,POS_END) ).toLongArray()[0];
    }

    public String toString(){
        return "{KID "+getMyKID()
                +", POS "+getPosition()
                +", FLAGS "+flagsToBinaryString()+"}";
    }


    public String flagsToBinaryString(){
        String result;
        if (getFlag(REVERSE)) {
            result = "1";
        }else{
            result = "0";
        }

        result += seq.toBinaryString(FLAG_START,FLAG_END);

        return result;
    }

    public void setFlagsBinaryString(String s){
        if (s.length() != 8){
            System.err.println("\tWARNING\t No calculations:  Position:setFlagsBinary fed string of length "+s.length());
        } else {
            seq.set(REVERSE, s.charAt(0) == '1');
            for (int k = 1; k < 8; k++){
                seq.set(FLAG_END-k, s.charAt(k) == '1');
            }
        }
    }

    @Override
    //Using toLong() arithmetic takes longer!!!
    //a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
    public int compareTo(Position o) {
        if (getMyKID() - o.getMyKID() == 0)
            return getPosition() - o.getPosition();
        else
            return getMyKID() - o.getMyKID();
    }

    @Override
    /**
     * equality is determined by position, but not flags (excepting reverse)
     */
    public boolean equals(Object o){
//        boolean result = false;
//        Position it = (Position) o;
//        if ( seq == it.seq)
//            result = true;
        return isEquivalentCoordinate((Position)o);
    }

    @Override
    public int compare(Position arg0, Position arg1) {
        return arg0.compareTo(arg1);
    }


    /**
     * Does not check all flags for equality, we just want position (which includes KID)
     */
    public boolean isEquivalentCoordinate(Position p){
//        boolean result = true;
//        if (getMyKID() != p.getMyKID() ||
//                getPosition() != p.getPosition() ||
//                this.getFlag(REVERSE) != p.getFlag(REVERSE) ) {
//            result = false;
//        }
//
//        return result;

        long me = (seq.get(POS_BITS, REVERSE)).toLongArray()[0];
        long him  = (p.seq.get(POS_BITS, REVERSE)).toLongArray()[0];
        return me == him;
    }


    /**
     * Long encoded to be in lexicographic order according to
     * Fixing lexicographic order, now we have no need for encodedLong
     * @return
     */
    public long toEncodedLong(){
//        long k = (long) getMyKID();
//        k = k << NUM_BITS - KID_BITS;
//        long p = (long) getPosition();
//        p = p << NUM_BITS - KID_BITS - POS_BITS;
//
//        long flags = toLong() >> (KID_BITS+POS_BITS);
//
//        return k+p+flags;
        return toLong();
    }


    /**
     * Fixing lexicographic order, now we have no need for encodedLong
     * @param encodedLong
     * @return
     */
    public static Position postionBuilder(long encodedLong){

        //We just need to re-order the bits
        //OK, in endoded long, it goes KID, POS, flags
        //in Position, it goes flags, POS, KID

//        Position result = new Position(0L);
//        result.copyBits(encodedLong,NUM_BITS-KID_BITS,0,KID_BITS);
//        result.copyBits(encodedLong,NUM_BITS-KID_BITS-POS_BITS,KID_BITS,POS_BITS);
//        result.copyBits(encodedLong,0,KID_BITS+POS_BITS,FLAG_BITS);
//        return result;

        return new Position(encodedLong);
    }


    private void copyBits(long source, int srcPos, int tarPos, int numBits){

        //More efficient implementation possible by using bitwise and function?
        // ( 1L << numBits+srcPos) - 1  >> srcPos << srcPos
        //long copy = source &  (( 1L << numBits+srcPos) - 1  >> srcPos << srcPos);
        //how to do bit copy to just set bits?

        FixedJavaBitSet src = new FixedJavaBitSet( source );
        for (int k=0; k<numBits; k++){
            seq.set(tarPos+k, src.get(srcPos+k));
        }
    }
}
