package org.cchmc.kluesuite.klue;

import java.io.IOException;
import java.util.BitSet;

/**
 * FixedBitSet allows setting a fixed size.
 * Also, an important feature, bits are numbered from (max size) on left to (min size) on right.
 * So Bit 0 is in the 0 power position in position 0 of integer array.
 * All bits default to false;
 *
 *  NEED TO MAKE SURE I AM CLEAR ON THIS
 */
public abstract class FixedBitSet{

    /**
     * Fixed number of bits of information stored in the FixedJavaBitSet
     */
    public int fixedLength;

    /**
     * fixed number of bytes required to store bits (multiple of 1 bytes)
     */
    public int fixedBytes;

    /**
     * Number of bits used to store the FixedJavaBitSet (must be multiple of 8)
     * Thus, this is the MINIMUM size of the number of bits to allocate, and all leading bits are 0.
     */
    public int fixedSize;

    public FixedBitSet(){
        fixedLength = 0;
        fixedBytes =0;
        fixedSize=0;
    }

    //Constructor requires a size, because it is a fixed size BitSet
    public FixedBitSet(int setLength) {
        fixedLength = setLength;
        fixedBytes = ((setLength+7)/8);
        fixedSize = 8*fixedBytes;
    }

    public FixedBitSet(FixedBitSet copy){
        fixedLength = copy.fixedLength;
        fixedBytes = copy.fixedBytes;
        fixedSize = copy.fixedSize;
    }

    public FixedBitSet(int setLength, BitSet mybits){
        fixedLength = setLength;
        fixedBytes = ((setLength+7)/8);
        fixedSize = 8*fixedBytes;
    }

    public FixedBitSet(long bits64 ){
        fixedLength = 64;
        fixedBytes = 8;
        fixedSize = 64;
    }

    /**
     * Construct bitset from a long integer.  Ignore bits equal to higher than length
     * @param length
     * @param bits64
     */
    public FixedBitSet(int length, long bits64 ){
        fixedLength = length;
        fixedBytes = ((length+7)/8);
        fixedSize = 8*fixedBytes;
    }

    abstract public boolean get(int index);

    abstract public void set(int index, boolean setvalue);

    abstract public void flip(int index);

    abstract public void flip(int fromInclusive, int toExclusive);

    /**
     * Sets any leading bits in the implementation above fixedSize to false
     */
    abstract public void clean();

    /**
     * This function tells you the static size chosen with constructor.
     * Number of bits STORED
     * @return
     */
    abstract public int getFixedLength();


    /**
     * This function tells you the static size chosen with constructor.
     * Number of bits ALLOCATED
     * @return
     */
    abstract public int getFixedSize();

    /**
     * Based on fixed size, is the index valid
     * @param index
     * @return
     */
    abstract public boolean isInRange( int index );

    /*
     * Number of bits stored
     */
    abstract public int	cardinality();

    /*
     *  Outputs binary string; bit 0 is rightmost and highest bit is leftmost
     */
    abstract public String toBinaryString();

    /**
     *  Binary string representing the range of bits.
     */
    abstract public String toBinaryString(int fromInclusive, int toExclusive);



    /**
     * As toBinaryString, but includes visually pleasing spaces between chunks of 5 bits.
     * @return
     */
    abstract public String toBinaryStringSpaces();

    /**
     * convert stored bits to array of bytes -- not the same as serialization
     * @return
     */
    abstract public byte[]	toByteArray();

    /**
     * convert stored bits to array of long integers
     * @return
     */
    abstract public long[]	toLongArray();

    /**
     * Essentially, this function truncates FixedJavaBitSet to lowest 64 bits
     */
    abstract public long getLastLong();


//    private abstract void writeObject(java.io.ObjectOutputStream stream) throws IOException;
//
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException;
}
