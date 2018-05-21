package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.binaryfiledirect.SillyTestCode;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeSerializable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Totally uncompressed Fixed Size BitSet for O(1) reads and writes
 */

public class MyFixedBitSet extends FixedBitSet implements UnsafeSerializable {

    static boolean DEBUG = false;

    /**
     * bits is like a two-dimensional array
     */
    public long[] bits;

//    public FixedBitSet(int setLength) {
//        fixedLength = setLength;
//        fixedBytes = ((setLength+7)/8);
//        fixedSize = 8*fixedBytes;
//    }

    MyFixedBitSet() {
        super(0);
        bits = new long[0];
    }

    MyFixedBitSet(int setLength) {
        super(setLength);
        bits = new long[(fixedBytes+7)/8];
        for (long l : bits) l = 0L;
    }

    MyFixedBitSet(FixedBitSet copy) {
        super(copy);
        bits = copy.toLongArray();
    }

    public MyFixedBitSet(int setLength, BitSet mybits) {
        super(setLength, mybits);
        bits = new long[(fixedBytes+7)/8];
        long[] temp = mybits.toLongArray();
        for (int k=0; k < (setLength+63)/64 && k < temp.length; k++)    bits[k] = temp[k];
    }

    public MyFixedBitSet(int setLength, long[] arr) {
        super(setLength);
        int valLength = (fixedBytes+7)/8;
        if (arr.length == valLength){
            bits = arr;
        } else {
            if (valLength < arr.length && DEBUG){
                System.err.println("  WARNING  MyFixedBitSet constructor called with long[] longer than specificed length.  ("
                        +arr.length+" > "+valLength+")");
            }
            //this pads with zeroes or truncates
            bits = Arrays.copyOf(arr, valLength);
        }
    }

    public MyFixedBitSet(long bits64) {
        super(bits64);
        bits = new long[]{bits64};
    }

    public MyFixedBitSet(int length, long bits64) {
        super(length, bits64);
//        System.out.println("MyFixcedBitSet constuctor: was length size = "+length);
        int numLong = (length+63)/64;
        bits = new long[numLong];
        bits[0] = bits64;
        if (numLong ==0)    clean();
        else                for (int k=1; k< numLong; k++ ) bits[k] = 0L;
    }

    @Override
    public boolean get(int index) {
        int row = index / 64;
        int col = index % 64;
        return !( (bits[row] & (1L << col)) == 0);
    }

    public MyFixedBitSet get(int from, int to) {
        MyFixedBitSet result = new MyFixedBitSet( (int) (to-from) );

        //this is very inefficient if multiple bytes being copied
        //perhaps could use code similar to flip()
        //as it stands, only ever need this to get 62 bits, so this code cannot be improved

        for (int k = 0; k <(to-from); k++){
            result.set(k, get(from+k));
        }

        return result;
    }


    @Override
    public void set(int index, boolean setvalue) {
        int row = index / 64;
        int col = index % 64;
        long trixie = 1L << col;
        if (setvalue){
            bits[row] = bits[row] | trixie;
        } else {
            bits[row] = bits[row] & ~trixie;
        }
    }

    @Override
    public void flip(int index) {
        int row = index / 64;
        int col = index % 64;
        long trixie = 1L << col;
        bits[row] = bits[row] ^ trixie;
//        //if false
//        if ( (bits[row] & (1L << col)) == 0) {
//            bits[row] = bits[row] & ~trixie;
//        } else {
//            bits[row] = bits[row] | trixie;
//        }
    }

    @Override
    public void flip(int fromInclusive, int toExclusive) {
        int rowStart = fromInclusive / 64;
        int colStart = fromInclusive % 64;
        int rowFinish = (toExclusive-1) / 64;
        int colFinish = (toExclusive-1) % 64 + 1;

        if (rowStart == rowFinish){
            bits[rowStart] = flipLong(bits[rowStart], colStart, colFinish);
        } else {
            bits[rowStart] = flipLong(bits[rowStart], colStart, 64);
            for (int k = rowStart + 1; rowStart < rowFinish; k++) {
                bits[k] = bits[k] ^ -1L;
            }
            bits[rowFinish] = flipLong(bits[rowFinish], 0, colFinish);
        }
    }

    private long flipLong(long l, int fromInclusive, int toExclusive){
        int diff = toExclusive - fromInclusive;
        //Assumed that index range is correct
        long trixie = ((1L << diff) - 1) << fromInclusive;
        return l ^ trixie;
    }

    @Override
    public void clean() {
        int row = fixedLength / 64;
        int col = fixedLength % 64;
        //set bits[row] highest bits to 0
        int diff = 64-col;
        long trixie = ((1L << diff) - 1) << col;
        bits[row] = bits[row] & ~trixie;
        for(int k=row+1; row < (fixedSize+63) / 64; k++) bits[row] = 0L;
    }

    @Override
    public int getFixedLength() {
        return fixedLength;
    }

    @Override
    public int getFixedSize() {
        return fixedSize;
    }

    @Override
    public boolean isInRange(int index) {
        return ( 0 <= index && index < fixedLength );
    }

    @Override
    public int cardinality() {
        return getFixedLength();
    }

    @Override
    public String toBinaryString() {
        String result ="";
        for (int k=(fixedLength-1); k>=0; k--) {
            if (get(k)) result += "1";
            else        result += "0";
        }
        return result;
    }

    @Override
    public String toBinaryString(int fromInclusive, int toExclusive) {
        String result ="";
        for (int k=(toExclusive-1); k>=fromInclusive; k--) {
            if (get(k)) result += "1";
            else        result += "0";
        }
        return result;
    }

    @Override
    public String toBinaryStringSpaces() {
        int period = 5;
        String result ="";
        for (int k=(fixedLength-1); k>=0; k--) {
            if (get(k)) result += "1";
            else        result += "0";
            if (k % 5 == 0 && k != 0) result += " ";
        }
        return result;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer bb = ByteBuffer.allocate(8 * bits.length);
        for (long l : bits) {
            bb.putLong(l);
        }
        return bb.array();
    }

    @Override
//    public long[] toLongArray() {
//        return Arrays.copyOf(bits, bits.length);
//    }
    public long[] toLongArray() {
        return bits;
    }


    @Override
    public long getLastLong() {
        return new Long(bits[0]);
    }

    public long getLongAtIndex( int index ){
        return new Long(bits[index]);
    }


    public int getLengthLongArray(){
        return bits.length;
    }

    public void setLongArray(int index, long l){
        bits[index] = l;
    }


    public static final long serialVersionUID = 1022001L;


//    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
//
//        Integer nextOffset = getFixedLength();
//        stream.writeObject(nextOffset);
//
//        nextOffset=fixedBytes;
//        stream.writeObject(nextOffset);
//
//        nextOffset=fixedSize;
//        stream.writeObject(nextOffset);
//        stream.writeObject(bits);
//    }
//
//
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        Integer nextOffset = (Integer) stream.readObject();
//        fixedLength = nextOffset;
//        nextOffset = (Integer) stream.readObject();
//        fixedBytes = nextOffset;
//        nextOffset = (Integer) stream.readObject();
//        fixedSize = nextOffset;
//        bits = (long[]) stream.readObject();
//    }
//
//
//    @Override
//    public void write (Kryo kryo, Output output) {
//        kryo.writeObject( output, this.bits.length);
//        kryo.writeObject( output, this.fixedLength);
//        kryo.writeObject( output, this.fixedSize);
//        kryo.writeObject( output, this.fixedBytes);
//        for (int k=0; k < this.bits.length; k++){
//            kryo.writeObject( output, this.bits[k]);
//        }
//    }
//
//    @Override
//    public void read (Kryo kryo, Input input) {
//        int nextOffset;
//
//        nextOffset = input.readInt();
//        long[] bits = new long[nextOffset];
//
//        this.fixedLength = input.readInt();
//        this.fixedSize = input.readInt();
//        this.fixedBytes = input.readInt();
//
//        for (int k=0; k<nextOffset; k++){
//            bits[k] = input.readLong();
//        }
//
//    }


    @Override
    public int getWriteUnsafeSize() {
        int total = 0;


        /**
         * header
         */
        total+= UnsafeMemory.SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG;

//        public int fixedLength;
//        public int fixedBytes;
//        public int fixedSize;
//        array size
        total+= 4 * UnsafeMemory.SIZE_OF_INT;

        //Array
        total += bits.length * UnsafeMemory.SIZE_OF_LONG;
//        total += this.fixedSize;

        return total;
    }

    @Override
    public void writeUnsafe(UnsafeMemory um) {
        um.putInt(getWriteUnsafeSize());
        um.putLong(serialVersionUID);
//        System.err.println("peek serial\t"+Arrays.toString( um.reversePeekBytes(8))); //debug
        um.putInt(fixedLength);
        um.putInt(fixedBytes);
        um.putInt(fixedSize);
        um.putInt(bits.length);

        for(int k=0;k<bits.length;k++)  {
            um.putLong(bits[k]);
        }
//        System.err.println("peek\t"+Arrays.toString( um.reversePeekBytes(8))); //debug
    }

    @Override
    public void readUnsafe(UnsafeMemory um) throws ClassCastException{
        long serial = um.getLong();
//        System.err.println("peek serial\t"+Arrays.toString( um.peekBytes(8))); //debug
        if (serial!= serialVersionUID){
            throw new ClassCastException("Reading UNSAFE MyFixedBitSet, but found wrong serialVersionUID =\t"+serial+"\texpecting\t"+serialVersionUID);
        }
        fixedLength=um.getInt();
        fixedBytes=um.getInt();
        fixedSize=um.getInt();
        int loopcontrol = um.getInt();
        bits = new long[loopcontrol];

//        System.err.println("peek\t"+Arrays.toString( um.peekBytes(8))); //debug
//        byte[] test = um.peekBytes(8); //debug

        for (int k=0; k < loopcontrol; k++)
            bits[k] = um.getLong();
    }
}
