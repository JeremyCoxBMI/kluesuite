package org.cchmc.kluesuite.klue;

/**
 * Created by Jeremy on 9/12/2016.
 */

import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeSerializable;
import org.cchmc.kluesuite.klue.kiddatabase.GetExceptionsArrKey;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import sun.misc.Unsafe;

import java.io.*;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;


/**
 * Ported from CompressedDNAString
 *
 *      Replacing FixedJavaBitSet with my own MyFixedBitSet, for speed
 *      This is optimized for read/writes O(1), whereas most bitsets are compressed,
 *      leading to a O(n) read/write time or something > O(1)
 *
 *
 *
 *      	bitB
 * bitA	    1	0
 * 1	    A	G
 * 0	    C	T
 * bitB is rightmost bit, but read right to left for sequence

 */

public class DnaBitString implements DnaBitStringGeneric, UnsafeSerializable{


//    private static final long serialVersionUID = 12601706L;


    protected int NUM_BITS;
    protected MyFixedBitSet compressed;
    protected HashMap<Integer,Character> exceptions;

    public static String SENTINEL = "EMPTY";

    static public boolean SUPPRESS_WARNINGS = false;


    protected DnaBitString(){}

    /**
     * for debugging
     * TODO - go private
     * @param val
     * @param size
     */
    public DnaBitString(long val, int size){
        NUM_BITS = 2*size;
        compressed = new MyFixedBitSet(NUM_BITS,val);
        exceptions = new HashMap<Integer,Character>();
    }

    public DnaBitString(long[] val, int size){
        NUM_BITS = 2*size;
        compressed = new MyFixedBitSet(NUM_BITS,val);
        exceptions = new HashMap<Integer,Character>();
    }

    public DnaBitString(long[] val, int size, HashMap<Integer,Character> ex){
        NUM_BITS = 2*size;
        compressed = new MyFixedBitSet(NUM_BITS,val);
        exceptions = ex;
    }

    protected DnaBitString(DnaBitString notAcopy){
        if (notAcopy == null){
            System.err.println("\tWARNING\t" +
                    "This DnaBitString is emtpy, cannot construct");
            NUM_BITS=0;
        } else {
            NUM_BITS = notAcopy.NUM_BITS;
            compressed = notAcopy.compressed;
            exceptions = notAcopy.exceptions;
        }
    }

    public DnaBitString(String sequence){

        NUM_BITS = 2 * sequence.length();
        compressed = new MyFixedBitSet(NUM_BITS);
        exceptions = new HashMap<Integer,Character>();
        for (int k=0; k< (sequence.length()); k++){
            if (sequence.charAt(k) == 'A' || sequence.charAt(k) == 'a'){
                compressed.set(bitA(k), true);
                compressed.set(bitB(k), true);
            } else if (sequence.charAt(k) == 'G' || sequence.charAt(k) == 'g'){
                compressed.set(bitA(k), true);
                compressed.set(bitB(k), false);
            } else if (sequence.charAt(k) == 'C' || sequence.charAt(k) == 'c'){
                compressed.set(bitA(k), false);
                compressed.set(bitB(k), true);
            } else if (sequence.charAt(k) == 'T' || sequence.charAt(k) == 't' || sequence.charAt(k) == 'U' || sequence.charAt(k) == 'u'){
                compressed.set(bitA(k), false);
                compressed.set(bitB(k), false);
            } else {
                //Default value is (false, false) -- implementation specific detail
                //compressed.set(2*k, false);
                //compressed.set(2*k+1, false);
                exceptions.put(k, sequence.charAt(k));
                if ( !DNAcodes.complement.containsKey(sequence.charAt(k)) )
                    if (!SUPPRESS_WARNINGS) System.err.println("\tWARNING: constructing compressed DNA String with non-standard char :: "+sequence.charAt(k));
            }
        }
    }

    /**
     * Creates a string of spaces that is 'spaces' spaces long.
     *
     * @param spaces The number of spaces to addAndTrim to the string.
     * http://stackoverflow.com/questions/2804827/create-a-string-with-n-characters
     */
    public String blankString( int spaces ) {
        return CharBuffer.allocate( spaces ).toString();
    }


    /**
     *
     * @param seq
     * @param from
     * @param to
     * @return
     */
    private long stringToLong(SuperString seq, int from, int to){
        if (( to-from) > 32){
            System.err.println("WARNING DnaBitString.stringToLong was fed string too long, reading 32 chars from beginning");
        }

        long temp = 0;
        int loops = from + Math.min(32, (to-from));
        for(int k=from; k<loops; k++){
            temp = temp << 2;
            switch(seq.charAt(k)){
                case 'A':
                case 'a':
                    temp += 3;
                    break;

                case 'C':
                case 'c':
                    temp += 1;
                    break;
                case 'G':
                case 'g':
                    temp += 2;
                    break;
                case 'T':
                case 't':
                    //temp += 0;  //adding 0 does nothing
                    break;
                default:
                    exceptions.put(k, seq.charAt(k));
                    //temp += 0;  //adding 0 does nothing
                    if ( !DNAcodes.complement.containsKey(seq.charAt(k)) )
                        if (!SUPPRESS_WARNINGS) System.err.println("\tWARNING: constructing compressed DNA String with non-standard char :: "+seq.charAt(k));
                    break;
            }
        }
        return temp;

    }

    public DnaBitString(SuperString sequence) throws DataFormatException{

        int length = sequence.length();

        if (length == 0){
            //System.err.println("DnaBitString SuperString constructor fed SuperString length 0.  This should never happen.");
            throw new DataFormatException("DnaBitString SuperString constructor fed SuperString length 0.  This should never happen.");
        }

        int strides = (length-1)/32+1;
        int offset = length%32;

        NUM_BITS = 2 * sequence.length();
        compressed = new MyFixedBitSet(NUM_BITS);
        exceptions = new HashMap<Integer,Character>();

        //BIG FOUND HERE  added if  2016.09.25
        if (offset == 0) offset=32;
        compressed.setLongArray(strides - 1, stringToLong(sequence, 0, offset));
        for( int s=1; s < strides; s++){
            int from = offset + 32*(s-1);
            compressed.setLongArray( (strides-1-s), stringToLong(sequence, from, (from+32)));
        }

    }


    /**
     * Constructs from serialized bytes.  Essentially, used to import from data store that stores byte[].
     * @param bytes
     */
    public DnaBitString(byte[] bytes){
        //http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
//        System.err.println("\t\t\tInitializing DnaBitString(byte[] bytes");
        if (bytes == null){
            NUM_BITS = 0;
            compressed = new MyFixedBitSet(NUM_BITS);
            exceptions = new HashMap<Integer,Character>();
        } else {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream in = null;
            try {
//                System.err.println("\t\t\tTrying to create InputStream");
                in = new ObjectInputStream(bis);

                // the serializer is different now
//                System.err.println("\t\t\tthis.readObject(in)");
                this.readObject(in);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null)
                        bis.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
        }
    }

    /**
     * * Fast conversion to Kmer31 family, by same bit representation
     * @param from
     * @return
     */
    public Kmer31 getKmer31(int from){
        if (from >= NUM_BITS /2- Kmer31.KMER_SIZE+1){
            return new ShortKmer31(-1L, 0);
        }
        //coordinates are reversed, see comments at top
        int fromB = bitB(from + Kmer31.KMER_SIZE -1);
        int toB = bitA(from)+1;  //+1, EXCLUSIVE
        if( toB > NUM_BITS /2) {
            System.err.println("DnaBitString.getShortKmer31() is out of bounds, may be an error");
            toB = NUM_BITS /2;
        }

        return new Kmer31( compressed.get(fromB, toB).toLongArray()[0]);
    }


    /**
     * Fast conversion to Kmer31 family, by same bit representation
     * @param from  index of the starting sequence, 0-indexed text left-to-right
     * @param k kmer size
     * @return
     */
    public ShortKmer31 getShortKmer31(int from, int k) {

        if (from >= NUM_BITS /2-k+1){
            return new ShortKmer31(-1L, 0);
        }
//        int to = from+k;
//        if( to > NUM_BITS/2) {
//            System.err.println("DnaBitString.getShortKmer31() is out of bounds, may be an error");
//            to = NUM_BITS/2;
//        }

        //coordinates are reversed
        int fromB = bitB(from + k -1);  // INCLUSIVE
        int toB = bitA(from)+1;  //+1, EXCLUSIVE
        if( toB > NUM_BITS /2) {
            System.err.println("DnaBitString.getShortKmer31() is out of bounds, may be an error");
            toB = NUM_BITS /2;
        }

        return new ShortKmer31( compressed.get(fromB, toB).toLongArray()[0], k);
    }

    /**
     * Returns subsequence in the given range.
     * Indexed left to right, 0-index
     *
     * @param from  integer index INCLUSIVE
     * @param to    integer index EXCLUSIVE
     * @return
     */


    public String getSequence( int from, int to){
        //Don't read off array
        to = Math.min(to, NUM_BITS /2);
        StringBuilder result = new StringBuilder( (to-from) );

        if (from >= NUM_BITS /2){
            //do nothing
            return SENTINEL;
        } else {

            for (int k = from; k < to; k++) {
                if (exceptions.containsKey(k)) {
//                    result += exceptions.get(k);
//                    result.setCharAt(k-from, exceptions.get(k));
                    result.append(exceptions.get(k));
                } else {
                    if (compressed.get(bitA(k))) {
                        if (compressed.get(bitB(k))) {
//                            result += 'A';
//                            result.setCharAt(k-from, 'A');
                            result.append('A');
                        } else {
//                            result += 'G';
//                            result.setCharAt(k-from, 'G');
                            result.append('G');
                        }
                    } else {
                        if (compressed.get(bitB(k))) {
//                            result += 'C';
//                            result.setCharAt(k-from, 'C');
                            result.append('C');
                        } else {
//                            result += 'T';
//                            result.setCharAt(k-from, 'T');
                            result.append('T');
                        }
                    }
                }
            }
        }
        return result.toString();
    }



    //SLOW
//    public String getSequence( int from, int to){
//        //Don't read off array
////        to = Math.min(to, NUM_BITS /2);
////        StringBuilder result = new StringBuilder( (to-from) );
//
//        String result ="";
//
//        if (from >= NUM_BITS /2){
//            //do nothing
//            return SENTINEL;
//        } else {
//
//            for (int k = from; k < to; k++) {
//                if (exceptions.containsKey(k)) {
//                    result += exceptions.get(k);
////                    result.setCharAt(k-from, exceptions.get(k));
////                    result.append(exceptions.get(k));
//                } else {
//                    if (compressed.get(bitA(k))) {
//                        if (compressed.get(bitB(k))) {
//                            result += 'A';
////                            result.setCharAt(k-from, 'A');
////                            result.append('A');
//                        } else {
//                            result += 'G';
////                            result.setCharAt(k-from, 'G');
////                            result.append('G');
//                        }
//                    } else {
//                        if (compressed.get(bitB(k))) {
//                            result += 'C';
////                            result.setCharAt(k-from, 'C');
////                            result.append('C');
//                        } else {
//                            result += 'T';
////                            result.setCharAt(k-from, 'T');
////                            result.append('T');
//                        }
//                    }
//                }
//            }
//        }
//        return result;
//    }





    /**
     * Returns reverseStrand (reverse and inverse) subsequence in the given range.
     * Indexed left to right, 0-index
     *
     * @param from  integer index INCLUSIVE
     * @param to    integer index EXCLUSIVE
     * @return
     */
    public String getSequenceReverseStrand( int from, int to){
        String result = "";
        if (from > getLength()){
            return new String(SENTINEL);
        } else {
            //Don't read off array
            to = Math.min(to, getLength());
            for (int k = from; k < to; k++) {
                if (exceptions.containsKey(k)) {
                    result += exceptions.get(k);
                } else {
                    if (compressed.get(bitA(k))) {
                        //TAKE INVERSE here
                        if (compressed.get(bitB(k))) {
                            result += 'T';
                        } else {
                            result += 'C';
                        }
                    } else {
                        if (compressed.get(bitB(k))) {
                            result += 'G';
                        } else {
                            result += 'A';
                        }
                    }
                }
            }
        }
        return new StringBuilder(result).reverse().toString();
    }

    /**
     * returns the character at the supplied index (0-indexing, left to right)
     * @param index
     * @return
     */
    public char charAt(int index) {
        char result = '0';

        if (index >= getLength()) return result;

        if (exceptions.containsKey(index)){
            return exceptions.get(index);
        } else {
            if (compressed.get(2 * index)) {
                if (compressed.get(2 * index + 1)) {
                    result += 'A';
                } else {
                    result += 'G';
                }
            } else {
                if (compressed.get(2 * index + 1)) {
                    result += 'C';
                } else {
                    result += 'T';
                }
            }
        }
        return result;
    }

    /**
     * Returns the length of the sequence; has nothing to do with internal mechanics.
     * @return
     */
    public int getLength(){
        return compressed.getFixedLength()/2;
    }

    public int getNumBytes(){
        return compressed.getFixedSize() / 8;
    }


    public static final long serialVersionUID = 1013001L;


    /**
     * While Java serializer is deprecated, this is still used by some members
     * @param stream
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(NUM_BITS);
        stream.writeObject(compressed);
        stream.writeObject(exceptions);

        int debug = 1;
//        Integer size = compressed.fixedLength();
//        stream.writeObject(size);
//        stream.writeObject(compressed.bits);


    }

    /**
     * While Java serializer is deprecated, this is still used by some members
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        NUM_BITS = (int) stream.readObject();
        compressed = (MyFixedBitSet) stream.readObject();

        exceptions = (HashMap<Integer,Character>) stream.readObject();
        int debug = 1;

//        //code breaks here
//        //compressed = (MyFixedBitSet) stream.readObject();
//        //compressed.readObject(stream);
//        compressed = new MyFixedBitSet(NUM_BITS);
//        //        compressed.fixedLength = NUM_BITS;
//    //        compressed.fixedBytes = ((NUM_BITS+7)/8);
////        compressed.fixedSize = 8*compressed.fixedBytes;
//
//        compressed.bits = (long[]) stream.readObject();

//        Integer size = (Integer) stream.readObject();
//        long[] temp = (long[]) stream.readObject();
//        compressed = new MyFixedBitSet(size, temp);


    }


    /**
     * Converts ascending string index to first bit (two's place) of
     * BitSet index
     *
     * Remember, bit ordering reversed:
     * bitA represents TO bit coordinate, INCLUSIVE
     *
     * @param index
     * @return
     */
    protected int bitA(int index){
        return (NUM_BITS -1)-(2*index);
    }

    /**
     * Converts ascending string index to second bit (ones's place) of
     * BitSet index
     *
     * Remember, bit ordering reversed:
     * bitB represents FROM bit coordinate, INCLUSIVE
     *
     * @param index
     * @return
     */
    protected int bitB(int index){
        return (NUM_BITS -1)-(2*index+1);
    }

    public myIterator iterator(){
        return new myIterator(this);
    }

    public String toString(){
        return getSequence(0,getLength());
    }

    public String toBinaryString(){
        return compressed.toBinaryString();
    }

    /**
     * Serializes object to bytes, so we can use a store that accepts byte[]
     * @return
     */
    public byte[] toByteArray(){
        //http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
        byte[] value = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            this.writeObject(out);
            value = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing compressed DNA to database");
            exit(1);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return value;
    }

    public int numExceptions() {
        return exceptions.size();
    }

    public long[] toLongArray(){
        return compressed.toLongArray();
    }

//    @Override
//    public void write(Kryo kryo, Output output) {
//        int NumBits = this.NUM_BITS;
//
//        if (this.NUM_BITS < 0) {
//            NumBits = 0;
//        }
//        kryo.writeObject( output, NumBits);
//        //kryo.writeClassAndObject(output, this.exceptions);
//
//        Set<Integer> s = this.exceptions.keySet();
//        kryo.writeObject(output, s.size());
//        for (Integer z : s){
//            kryo.writeObject(output, z);
//            kryo.writeObject(output, this.exceptions.get(z));
//        }
//        kryo.register(MyFixedBitSet.class);
//        kryo.writeObject(output, this.compressed);
//    }
//
//    @Override
//    public void read(Kryo kryo, Input input) {
//        this.NUM_BITS = input.readInt();
////        this.exceptions= (HashMap<Integer, Character>) kryo.readClassAndObject(input);
//
//        int size = input.readInt();
//        this.exceptions = new HashMap<Integer, Character>();
//        for (int k=0; k<size; k++){
//            Integer key = kryo.readObject(input, Integer.class);
//            Character ch = kryo.readObject(input, Character.class);
//            exceptions.put(key, ch);
//        }
//
//        kryo.register(MyFixedBitSet.class);
//        this.compressed = kryo.readObject(input, MyFixedBitSet.class);
//
//    }

    public static DnaBitString unsafeMemoryBuilder(UnsafeMemory um) {
        DnaBitString result = new DnaBitString();
        result.readUnsafe(um);
        return result;
    }

    public void putExceptions(RocksDB rocks, GetExceptionsArrKey geak) throws RocksDBException {
        UnsafeMemory um = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(exceptions, UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE));
        um.put(exceptions,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        rocks.put(geak.toBytes(),um.toBytes());
    }

    /**
     * Java syle iterator<Kmer31>
     *     Additionally, has length() function.
     */
    public static final class myIterator implements Iterator<Kmer31> {
        DnaBitString dbs;
        int length;
        int curr;

        myIterator(DnaBitString pointer) {
            dbs = pointer;
            length = dbs.NUM_BITS / 2 - Kmer31.KMER_SIZE + 1;
            curr = 0;
        }

        public boolean hasNext() {
            return curr < length;
        }

        public Kmer31 next() {
            Kmer31 result;
            boolean inValid = false;
            // how to make sure ranges do not cross?
            for (int key : dbs.exceptions.keySet()) {
                if (curr <= key && key < curr + Kmer31.KMER_SIZE) {
                    inValid = true;
                }
            }

            if (inValid)
                result = null;
            else {
                int from = dbs.bitB(curr + Kmer31.KMER_SIZE - 1);
                int to = dbs.bitA(curr) + 1;  //+1, EXCLUSIVE
                result = new Kmer31();
//                result.seq = new FixedJavaBitSet(64,
//                        dbs.compressed.get(from, to).toLongArray()[0]);
                result.seq = new MyFixedBitSet(64,
                        dbs.compressed.get(from, to).toLongArray()[0]);
            }
            curr++;
            return result;
        }

        public int length() {
            return length;
        }

        public void remove() {
            curr++;
        }


    }



    public int getWriteUnsafeSize(){
        int total = 0;
        Set<Integer> s = this.exceptions.keySet();

        /**
         * header
         */
        total+= UnsafeMemory.SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG;

        //NUM_BITS and exceptions.size()
        total += 2 * UnsafeMemory.SIZE_OF_INT;

        total+= (UnsafeMemory.SIZE_OF_CHAR+UnsafeMemory.SIZE_OF_INT) * s.size();
        total+=compressed.getWriteUnsafeSize();

        return total;
    }


    public void writeUnsafe(UnsafeMemory um){

        int x = getWriteUnsafeSize();

        /**
         * Customary opening fields
         */
        um.putInt(x);
        um.putLong(serialVersionUID);

        //DEBUG
        //System.err.println("Write Serial ID\t"+ Arrays.toString(um.toBytes()));
//        um.reset();
//        um.getInt();
//        long debug = um.getLong();
//        System.err.println("Writ Serial ID\t"+ UnsafeMemory.bytesToString(RocksDbKlue.longToBytes(debug),8));


        Set<Integer> s = this.exceptions.keySet();



        um.putInt(this.NUM_BITS);
        um.putInt(s.size());
        for (Integer z : s) {
            um.putInt((int) z);
            um.putChar(this.exceptions.get(z));
        }

        compressed.writeUnsafe(um);
//        System.err.println("Write Object\t"+Arrays.toString(um.toBytes()));
        int debugPause =1;
    }


    @Override
    //public void readUnsafe(UnsafeMemory um) throws ClassCastException;
    public void readUnsafe(UnsafeMemory um) throws ClassCastException {
//        System.err.println("Read  Object\t"+Arrays.toString(um.toBytes()));

        long serial = um.getLong();

//        System.err.println("Read Serial ID\t"+ UnsafeMemory.bytesToString(um.toBytes(),4,12));

        if (((long)serial) != ((long)serialVersionUID)){
            throw new ClassCastException(
                    "Reading UNSAFE DnaBitString, but found wrong serialVersionUID =\t"+serial+"\texpecting\t"+serialVersionUID
            );
        }

        this.NUM_BITS = um.getInt();
        int loopCount = um.getInt();
        exceptions = new HashMap<Integer,Character>();
        int x;
        char c;
        for (int k=0; k<loopCount;k++){
            x = um.getInt();
            c = um.getChar();
            exceptions.put(x,c);
        }

        int myFixedBitSetSize = um.getInt(); //no need to keep

        compressed = new MyFixedBitSet(0);
        try {
            (compressed = new MyFixedBitSet()).readUnsafe(um);
        }catch (ClassCastException e){
            throw new ClassCastException("Reading UNSAFE DnaBitString\t"+e.toString());
        }
//        int debugPause =1;
    }


}
