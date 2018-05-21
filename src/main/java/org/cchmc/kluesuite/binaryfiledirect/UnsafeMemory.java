package org.cchmc.kluesuite.binaryfiledirect;



/**
 * Created by osboxes on 5/30/17.
 *
 * EXAMPLE CODE -- EXPANDED HERE
 * https://mechanical-sympathy.blogspot.jp/2012/07/native-cc-like-performance-for-java.html
 *
 * UnsafeMemory uses unsafe class to serialize objects rapidly, in C++ style to achieve much higher speed.
 * Paired with UnsafeFileReader and UnsafeFileWriter, I think these are buffered (?not certain)
 *
 */
import org.apache.commons.lang3.reflect.TypeUtils;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.mutation;
import sun.misc.Unsafe;

import javax.print.DocFlavor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;

import static org.cchmc.kluesuite.klue.Position.SNP;
import static org.cchmc.kluesuite.variantklue.mutation.DELETION;
import static org.cchmc.kluesuite.variantklue.mutation.INSERTION;
import static org.cchmc.kluesuite.variantklue.mutation.NONE;


/**
 * UnsafeMemory uses unsafe memory operations to write objects to a byte array, which can then be read or
 * exported quickly to a file.
 * For SMALL to MEDIUM objects, because all data is copied in memory to a new byte[].
 *
 * KEY DESIGN NOTE:  all objects (except long, int, char, String, boolean)
 * begin with (int) indicating total size in bytes and (long) for the object's SerialUID.
 *
 *
 * Note that Strings of length 0 are treated as null
 *
 * This Class includes many object types ready for read/write
 *
 * THIS CLASS can be used with its own primitives
 * OR
 * you can write member functions of your classes using UnsafeSerializable or UnsafeFileIO interfaces.
 * OR
 * The bold may also expand the data types herein included, please create pull requests.
 *
 * 2017-06-2017  TestCases for basic functions written  TestUnsafeMemory
 */
public class UnsafeMemory {
    private static final Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //TYPES for internal use // EXISTING CODE
    public static final long STRING_UID = 91001001L;
    public static final long INT_UID = 91001002L;
    public static final long ARRAYLIST_UID = 91001003L;
    public static final long HASHMAP_UID = 91001004L;
    public static final long CHAR_UID = 91001005L;
    public static final long KID_UID = Kid.getSerialVersionUID();
    public static final long DNABITSTRING_UID = DnaBitString.serialVersionUID;
    public static final long MYFIXEDBITSET_UID = MyFixedBitSet.serialVersionUID;
    public static final long KIDDATABASE_NO_DNABITSTRING_TYPE = 91001006L;
    public static final long HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID = 91001008L;
    public static final long TREEMAP_INTEGER_ARRAY_VARIANT_UID = 91001009L;
    public static final long VARIANT_UID = 91001010L;


    /* *****************************************************
    // TYPES for use with read/write/write size functions
    // for programmer
    // These are the types you can read/write
    ***************************************************** */

    public static final long[] STRING_TYPE = new long[]{STRING_UID};
    public static final long[] KID_TYPE = new long[]{KID_UID};
    public static final long[] CHAR_TYPE = new long[]{CHAR_UID};
    public static final long[] HASHMAP_INTEGER_CHARACTER_TYPE = new long[]{HASHMAP_UID, INT_UID, CHAR_UID};
    public static final long[] ARRAYLIST_DNABITSTRING_TYPE = new long[]{ARRAYLIST_UID,DnaBitString.serialVersionUID};
    public static final long[] ARRAYLIST_STRING_TYPE = new long[]{ARRAYLIST_UID, STRING_UID};
    public static final long[] ARRAYLIST_KID_TYPE = new long[]{ARRAYLIST_UID,KID_UID};
    public static final long[] ARRAYLIST_INT_TYPE = new long[]{ARRAYLIST_UID,INT_UID};
    public static final long[] MYFIXEDBITSET_TYPE = new long[]{MyFixedBitSet.serialVersionUID};
    public static final long[] DNABITSTRING_TYPE = new long[]{DnaBitString.serialVersionUID};
    public static final long[] HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_TYPE = new long[]{HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID};
    public static final long[] TREEMAP_INTEGER_ARRAY_VARIANT_TYPE = new long[]{TREEMAP_INTEGER_ARRAY_VARIANT_UID};
    public static final long[] VARIANT_TYPE = new long[]{VARIANT_UID};


    private static final long byteArrayOffset = unsafe.arrayBaseOffset(byte[].class);
    private static final long longArrayOffset = unsafe.arrayBaseOffset(long[].class);
    private static final long doubleArrayOffset = unsafe.arrayBaseOffset(double[].class);

    public static final int SIZE_OF_BOOLEAN = 1;
    public static final int SIZE_OF_INT = 4;
    public static final int SIZE_OF_CHAR = 1;
    public static final int SIZE_OF_LONG = 8;

    private int pos;
    private final byte[] buffer;

    public UnsafeMemory( int size){
        this.buffer = new byte[size];
        pos = 0;
    }

    public UnsafeMemory(final byte[] buffer) {
        if (null == buffer) {
            throw new NullPointerException("buffer cannot be null");
        }

        this.buffer = buffer;
        pos = 0;
    }

    /**
     * Resets the memory block to beginning, so that a written block can be read; mostly for debugging
     */
    public void reset() {
        this.pos = 0;
    }

    /**
     * Moves the memory reading position
     * reset(4) skips the reading size header, so that read functions work
     */
    public void reset(int pos) {
        this.pos = pos;
    }


    private boolean willOverflow(int bytes){
        return (this.pos + bytes) > buffer.length;
    }

    public void putBoolean(final boolean value) throws ArrayIndexOutOfBoundsException {
        if(willOverflow(SIZE_OF_BOOLEAN)){
            throw new ArrayIndexOutOfBoundsException("attempting to write thru to index "+this.pos+SIZE_OF_BOOLEAN+" past "+buffer.length);
        }
        unsafe.putBoolean(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_BOOLEAN;
    }

    public boolean getBoolean() throws ArrayIndexOutOfBoundsException{
        if(willOverflow(SIZE_OF_BOOLEAN)){
            throw new ArrayIndexOutOfBoundsException("attempting to read thru to index "+this.pos+SIZE_OF_BOOLEAN+" past "+buffer.length);
        }
        boolean value = unsafe.getBoolean(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_BOOLEAN;

        return value;
    }

    public void putInt(final int value) throws ArrayIndexOutOfBoundsException{
        if(willOverflow(SIZE_OF_INT)){
            throw new ArrayIndexOutOfBoundsException("attempting to write thru to index "+this.pos+SIZE_OF_INT+" past "+buffer.length);
        }
        unsafe.putInt(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_INT;
    }

    public int getInt() throws ArrayIndexOutOfBoundsException{
        if(willOverflow(SIZE_OF_INT)){
            throw new ArrayIndexOutOfBoundsException("attempting to read thru to index "+this.pos+SIZE_OF_INT+" past "+buffer.length);
        }
        int value = unsafe.getInt(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_INT;

        return value;
    }

    public void putLong(final long value) throws ArrayIndexOutOfBoundsException{
        if(willOverflow(SIZE_OF_LONG)){
            throw new ArrayIndexOutOfBoundsException("attempting to write thru to index "+this.pos+SIZE_OF_LONG+" past "+buffer.length);
        }
        unsafe.putLong(buffer, byteArrayOffset + pos, value);
        pos += SIZE_OF_LONG;
    }

    public long getLong() throws ArrayIndexOutOfBoundsException{
        if(willOverflow(SIZE_OF_LONG)){
            throw new ArrayIndexOutOfBoundsException("attempting to read thru to index "+this.pos+SIZE_OF_LONG+" past "+buffer.length);
        }
        long value = unsafe.getLong(buffer, byteArrayOffset + pos);
        pos += SIZE_OF_LONG;

        return value;
    }

    /**
     * @param character
     */
    public void putChar(char character) throws ArrayIndexOutOfBoundsException{
        if(willOverflow(SIZE_OF_CHAR)){
            throw new ArrayIndexOutOfBoundsException("attempting to write thru to index "+this.pos+SIZE_OF_CHAR+" past "+buffer.length);
        }
//        unsafe.putChar(buffer, byteArrayOffset + pos, character);
        unsafe.putByte(buffer, byteArrayOffset + pos, (byte) character);
        pos += SIZE_OF_CHAR;

    }

    /**
     * no boundary checking -- String already checked
     * @param character
     * @throws ArrayIndexOutOfBoundsException
     */
    private void putCharForString(char character) throws ArrayIndexOutOfBoundsException{
        unsafe.putByte(buffer, byteArrayOffset + pos, (byte) character);
        pos += SIZE_OF_CHAR;

    }


    public char getChar() throws ArrayIndexOutOfBoundsException{
//        char value = unsafe.getChar(buffer, byteArrayOffset + pos);
        if(willOverflow(SIZE_OF_CHAR)){
            throw new ArrayIndexOutOfBoundsException("attempting to read thru to index "+this.pos+SIZE_OF_CHAR+" past "+buffer.length);
        }
        char value = (char) unsafe.getByte(buffer, byteArrayOffset + pos);

        pos += SIZE_OF_CHAR;

        return value;
    }

    /**
     * no boundary checking, as not needed for each char (already done)
     * @return
     * @throws ArrayIndexOutOfBoundsException
     */
    private char getCharForString() throws ArrayIndexOutOfBoundsException{
//        char value = unsafe.getChar(buffer, byteArrayOffset + pos);

        char value = (char) unsafe.getByte(buffer, byteArrayOffset + pos);

        pos += SIZE_OF_CHAR;

        return value;
    }


    public void putString(String s) throws ArrayIndexOutOfBoundsException {

        if (s == null){
            putInt(0);
        }else {
            if (willOverflow(s.length() + SIZE_OF_INT)) {
                throw new ArrayIndexOutOfBoundsException("attempting to write thru to index " + this.pos + s.length() + SIZE_OF_INT + " past " + buffer.length);
            }
            putInt(s.length());
            for (int k = 0; k < s.length(); k++) {
                putCharForString(s.charAt(k));
            }
        }
    }

    public String getString() throws ArrayIndexOutOfBoundsException {
        int length = getInt();
        if (willOverflow(length)) {
            throw new ArrayIndexOutOfBoundsException("attempting to read thru to index " + this.pos + SIZE_OF_INT + length + " past " + buffer.length);
        }

        char[] c = new char[length];
        for (int k = 0; k < length; k++) {
            c[k] = getCharForString();
        }
        return new String(c);
    }

    public byte[] getClone() {
        return buffer.clone();
    }

    public byte[] toBytes() {
        return buffer;
    }

    /**
     * Primarily for debugging
     *
     * @param bytes
     * @param from
     * @param to
     * @return
     */
    public static String bytesToString(byte[] bytes, int from, int to) {
//        byte[] bob = new byte[number];
        byte[] bob = Arrays.copyOfRange(bytes, from, to);
        return Arrays.toString(bob);
    }

    public byte[] peekBytes(int number) {
        int to = number + this.pos;
        if (to > buffer.length) {
            to = buffer.length;
        }
        return Arrays.copyOfRange(buffer, this.pos, to);
    }

    public byte[] reversePeekBytes(int number) {
        int from = this.pos - number;
        if (from < 0) from = 0;
        return Arrays.copyOfRange(buffer, from, this.pos);
    }



    /**
     * This function makes writing objects use one function on UnsafeMemory object for convenience
     *
     * @param obj
     */
    public static int getWriteUnsafeSize(Object obj, long[] serial) {
        int result = 0;

        //header
        result += SIZE_OF_INT + SIZE_OF_LONG;

        if (serial[0] == DNABITSTRING_UID) {
            result = ((DnaBitString) obj).getWriteUnsafeSize();
            result=result; //debug
        } else if (serial[0] == HASHMAP_UID) {
            if (serial[1] == INT_UID && serial[2] == CHAR_UID) {
                Set<Integer> keys = ((HashMap<Integer, Character>) obj).keySet();
                result += SIZE_OF_LONG + SIZE_OF_LONG  //additional serialUID
                         + SIZE_OF_INT; //size of map
                result += (SIZE_OF_CHAR + SIZE_OF_INT) * (keys.size());
                result += 0; //debug
            } else {
                System.out.println("Unsafe.put() :: Unknown HashMap class ::\t" + serial[1] + "\t" + serial[2]);
            }

        } else if (serial[0] == MYFIXEDBITSET_UID) {
            //includes header in calculation
            result = ((MyFixedBitSet) obj).getWriteUnsafeSize();

        } else if(serial[0] == STRING_UID){
            if (obj == null){
                result = SIZE_OF_INT;
            } else {
                result = SIZE_OF_INT + (SIZE_OF_CHAR * (((String) obj).length()));
            }
        } else if(serial[0] == ARRAYLIST_UID){
            result += SIZE_OF_LONG //second serialUID
                    + SIZE_OF_INT; //size of list

            if (serial[1] == DnaBitString.serialVersionUID) {
                ArrayList<DnaBitString> arr = (ArrayList<DnaBitString>) obj;
                for (int k=0; k< arr.size(); k++){
                    result += ((DnaBitString) arr.get(k)).getWriteUnsafeSize();
                }
            } else if (serial[1] == STRING_UID) {
                ArrayList<String> arr = (ArrayList<String>) obj;
                for (int k=0; k< arr.size(); k++){
                    result += getWriteUnsafeSize((String) arr.get(k), STRING_TYPE);
                }
            } else if(serial[1] == KID_UID) {
                ArrayList<Kid> arr = (ArrayList<Kid>) obj;
                for (int k = 0; k < arr.size(); k++) {
                    result += getWriteUnsafeSize((Kid) arr.get(k), KID_TYPE);
                }
            } else if(serial[1] == INT_UID) {
                ArrayList<Integer> arr = (ArrayList<Integer>) obj;
                result += arr.size() * SIZE_OF_INT;
            } else {
                System.out.println("Unsafe.getWriteUnsafeSize (ArrayList) :: Unknown class UID ::\t" + serial);
            }
        } else if (serial[0] == CHAR_UID) {
            result = SIZE_OF_CHAR;
        } else if (serial[0] == INT_UID) {
            result = SIZE_OF_INT;
        } else if (serial[0] == KID_UID) {
            result = ((Kid) obj).getWriteUnsafeSize();
        } else if (serial[0] == KIDDATABASE_NO_DNABITSTRING_TYPE){
//            result+=UnsafeMemory.getWriteUnsafeSize(fileName, UnsafeMemory.STRING_TYPE);
//            result+=UnsafeMemory.SIZE_OF_INT;
//            result+=UnsafeMemory.getWriteUnsafeSize(nameIndex,UnsafeMemory.ARRAYLIST_STRING_TYPE);
//            result+=UnsafeMemory.getWriteUnsafeSize(entries,UnsafeMemory.ARRAYLIST_KID_TYPE);
        }else if (serial[0] == HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID){
            HashMap<Integer, TreeMap<Integer,Variant[]>> hm = ((HashMap<Integer, TreeMap<Integer,Variant[]>>) obj);

            Set<Integer> hashKeys = hm.keySet();
            //keyset size
            result+=SIZE_OF_INT;

            //the TreeMaps are stored using their own Int size reads
//            for (int key : hashKeys){
//                //key
//                result += SIZE_OF_INT;
//                result += getWriteUnsafeSize(hm.get(key), TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
//            }


        } else if (serial[0] == TREEMAP_INTEGER_ARRAY_VARIANT_UID){
            TreeMap<Integer,Variant[]> tree= ((TreeMap<Integer,Variant[]>) obj);
            Set<Integer> hashKeys = tree.keySet();
            //keyset size
            result+=SIZE_OF_INT;

            for (int key : hashKeys){
                Variant[] va = tree.get(key);
                //length of array and key
                result+=2*SIZE_OF_INT;
//                for (int k=0; k<va.length)

                //This loop malfunctions
//                for (Variant v : va) {
                for (int k=0; k<va.length;k++){
                    result += getWriteUnsafeSize(va[k], VARIANT_TYPE);
                }
            }
        } else if (serial[0] == VARIANT_UID){

            Variant v = ((Variant) obj);

            //insertSequence,detailedName, name
            result+=getWriteUnsafeSize(v.insertSequence, STRING_TYPE);
            result+=getWriteUnsafeSize(v.detailedName, STRING_TYPE);
            result+=getWriteUnsafeSize(v.name, STRING_TYPE);

            //KID
            //length
            //start
            //mutation
            result+=4*SIZE_OF_INT;
            //isReverse
            result += SIZE_OF_BOOLEAN;
//            System.out.println("DEBUG\tVariant size is "+result);
        }
        else {
            System.out.println("Unsafe.getWriteUnsafeSize :: Unknown class UID ::\t" + serial);
        }


        return result;
    }

    /**
     * This function makes writing objects use one function on UnsafeMemory object for convenience
     *
     * @param obj   object to write to this
     * @param serial array of SERIAL_UID (including those made up here) of class, see commented "XXX_TYPE" const above
     */
    public void put(Object obj, long[] serial) throws ClassCastException {

        //header
        if (serial[0] == DNABITSTRING_UID) {
            ((DnaBitString) obj).writeUnsafe(this);
        } else if (serial[0] == HASHMAP_UID) {
            if (serial[1] == INT_UID && serial[2] == CHAR_UID) {
                putInt(getWriteUnsafeSize(obj, serial));
                putLong(serial[0]);
                putLong(serial[1]);
                putLong(serial[2]);
                Set<Integer> keys = ((HashMap<Integer, Character>) obj).keySet();
                putInt(keys.size());
                //NOTE: no serialVersionUID
                for (Integer k : keys) {
                    putInt(k);
                    putChar(((HashMap<Integer, Character>) obj).get(k));
                }
            } else {
                System.out.println("Unsafe.put() :: Unknown HashMap class ::\t" + serial[1] + "\t" + serial[2]);
                throw new ClassCastException("Unsafe.put() :: Unknown HashMap class ::\t" + serial[1] + "\t" + serial[2]);
            }
        } else if (serial[0] == MyFixedBitSet.serialVersionUID) {
            ((MyFixedBitSet) obj).writeUnsafe(this);
//        } else if(serial[0] == MyFixedBitSet.serialVersionUID) {
//            ((MyFixedBitSet) obj).writeUnsafe(this);
        } else if (serial[0] == STRING_UID) {
            putString((String) obj);
        } else if (serial[0] == ARRAYLIST_UID) {
            if (serial[1] == DnaBitString.serialVersionUID) {
                putArrayList((ArrayList<DnaBitString>) obj, serial);
            } else if (serial[1] == STRING_UID) {
                putArrayList((ArrayList<String>) obj, serial);
            } else if (serial[1] == KID_UID) {
                putArrayList((ArrayList<Kid>) obj, serial);
            } else if (serial[1] == INT_UID) {
                putArrayList((ArrayList<Kid>) obj, serial);
            } else {
                System.out.println("Unsafe.put() :: Unknown ArrayList< class UID> ::\t" + serial[1]);
                throw new ClassCastException("Unsafe.put() :: Unknown ArrayList< class UID> ::\t" + serial[1]);
            }
        } else if (serial[0] == CHAR_UID) {
            putChar((char) obj);
        } else if (serial[0] == INT_UID) {
            putInt((int) obj);
        } else if (serial[0] == KID_UID) {
            ((Kid) obj).writeUnsafe(this);
//        } else if (serial[0] == HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID){
//            //header
//            //putInt(getWriteUnsafeSize(obj,HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_TYPE));
//            putInt(SIZE_OF_LONG+SIZE_OF_INT);
//            putLong(HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID);
//
//            HashMap<Integer, TreeMap<Integer,Variant[]>> hm = ((HashMap<Integer, TreeMap<Integer,Variant[]>>) obj);
//
//            Set<Integer> hashKeys = hm.keySet();
//
//            putInt(hashKeys.size());
//
//
//            for (int key : hashKeys){
//                putInt(key);
//                put(hm.get(key), TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
//            }
//

        } else if (serial[0] == TREEMAP_INTEGER_ARRAY_VARIANT_UID){
            TreeMap<Integer,Variant[]> tree= ((TreeMap<Integer,Variant[]>) obj);
            Set<Integer> hashKeys = tree.keySet();

            //header
            int size = getWriteUnsafeSize(obj,TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
            putInt(size);

            putLong(TREEMAP_INTEGER_ARRAY_VARIANT_UID);
            //keyset size
            putInt(hashKeys.size());

            for (int key : hashKeys){
//                if (key == 12381){
//                    boolean bad = false;
//                }
                Variant[] va = tree.get(key);
                //length of array
                putInt(key);
                putInt(va.length);
//                for (Variant v : va)
                for (int k=0; k<va.length;k++) {
                    put(va[k], VARIANT_TYPE);
                }
//                System.out.println("Debug : last key was "+key);
            }

        } else if (serial[0] == VARIANT_UID){

            Variant v = ((Variant) obj);

            int DEBUG=pos;

            putInt(getWriteUnsafeSize(v,VARIANT_TYPE));
            putLong(VARIANT_UID);

            //insertSequence,detailedName, name
            putString(v.insertSequence);
            putString(v.detailedName);
            putString(v.name);


            //KID
            //length
            //start
            //mutation type
            putInt(v.KID);
            putInt(v.length);
            putInt(v.start);
            putInt(v.type.getValue());
            putBoolean(v.isReverse);
            //System.out.println("DEBUG\tVariant size is (position)\t"+(this.pos-DEBUG)+"\t"+this.toBytes().length+"\tabsolute position\t"+pos);
        }
        else {
            System.out.println("Unsafe.put() :: Unknown class UID ::\t" + serial[0]);
            throw new ClassCastException("Unsafe.put() :: Unknown class UID ::\t" + serial[0]);
        }

    }

    /**
     * Reads an object from byte array of TYPE serial
     * This function makes reading objects use one function on UnsafeMemory object for convenience
     *
     * NOTE: the first 4 bytes is the number of bytes.
     * Removing these is built into UnsafeFileReader.
     * HOWEVER, FOR operating directly to memory, these must be skipped
     *      unsafeMemory.put (something, SOMETHING_TYPE)
     *      unsafeMemory.reset()
     *      int memorySize = unsafeMemory.getInt()    //don't forget
     *      SomethingClass something = (SomethingClass) unsafeMemory.get(SOMETHING_TYPE)
     *
     * @param serial
     * @return
     */
    public Object get(long[] serial){


        Object result = null;

        if (serial[0] == DNABITSTRING_UID) {
//            int size = getInt();
            result = DnaBitString.unsafeMemoryBuilder(this);

        } else if (serial[0] == HASHMAP_UID) {
            if (getLong() != serial[0] || getLong() != serial[1] || getLong() != serial[2]){
                System.out.println("Unsafe.get() :: Expecting HashMap class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]+"\t"+ serial[1] + "\t" + serial[2]);
                throw new ClassCastException("Unsafe.get() :: Expecting HashMap class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]+"\t"+ serial[1] + "\t" + serial[2]);
            }

            if (serial[1] == INT_UID && serial[2] == CHAR_UID) {
                int size = getInt();
                HashMap<Integer,Character> temp = new HashMap<Integer,Character>();
                int x;
                char c;
                for (int k =0; k < size; k++){
                    x = getInt();
                    c = getChar();
                    temp.put(x,c);
                }
                result = temp;
            } else {
                System.out.println("Unsafe.get() :: Unknown HashMap class ::\t" + serial[1] + "\tto\t" + serial[2]);
                throw new ClassCastException("Unsafe.get() :: Unknown HashMap class ::\t" + serial[1] + "\tto\t" + serial[2]);
            }

        } else if (serial[0] == MyFixedBitSet.serialVersionUID) {
            MyFixedBitSet mfbs = new MyFixedBitSet(0);
            mfbs.readUnsafe(this);
            result=mfbs;
        } else if(serial[0] == STRING_UID){
            result = getString();
        } else if(serial[0] == ARRAYLIST_UID){
            if (serial[1] == DNABITSTRING_UID) {
                ArrayList<DnaBitString> temp = null;
                result = UnsafeMemory.<DnaBitString>getArrayList(this, serial);
                //bug :: temp 'returns' null; now proper return
            } else if (serial[1] == STRING_UID) {
                ArrayList<String> temp = null;
                result = UnsafeMemory.<String>getArrayList(this, serial);

            } else if (serial[1] == KID_UID) {
                ArrayList<Kid> temp = null;
                result = UnsafeMemory.<Kid>getArrayList(this, serial);
            } else if (serial[1] == INT_UID) {
                ArrayList<Integer> temp = null;
                result = UnsafeMemory.<Integer>getArrayList(this, serial);
            }        else {
                System.out.println("Unsafe.put() :: Unknown ArrayList< class UID> ::\t" + serial[1]);
                throw new ClassCastException("Unsafe.put() :: Unknown ArrayList< class UID> ::\t" + serial[1]);
            }
        } else if (serial[0] == CHAR_UID) {
            result = getChar();
        } else if (serial[0] == INT_UID) {
            result = getInt();
        } else if (serial[0] == KID_UID) {
            Kid temp = new Kid("");
            temp.readUnsafe(this);
            result=temp;
//        } else if (serial[0] == HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID){
//            //header
//            if (getLong() != serial[0]){
//                System.out.println("Unsafe.get() :: Expecting HashMap<TreeMap> class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]);
//                throw new ClassCastException("Unsafe.get() :: Expecting HashMap<TreeMap> class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]);
//            }
//
//            HashMap<Integer, TreeMap<Integer,Variant[]>> hm = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
//
//            int size = getInt();
//            int key;
//            TreeMap<Integer,Variant[]> tree;
//            for (int z=0; z<size;z++){
//                key = getInt();
//                tree = (TreeMap<Integer, Variant[]>) get(TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
//                hm.put(key,tree);
//            }
//            result = hm;

        } else if (serial[0] == TREEMAP_INTEGER_ARRAY_VARIANT_UID){
            TreeMap<Integer,Variant[]> tree = new TreeMap<Integer,Variant[]>();



            //header
            //variant byte size -- Do Not Read In
            //int temp = getInt();
            long myserial = getLong();

            if ( myserial != serial[0] ){
                System.out.println("Unsafe.get() :: Expecting TreeMap class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]+"\tactual\t"+myserial);
                throw new ClassCastException("Unsafe.get() :: Expecting TreeMap class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]+"\tactual\t"+myserial);
            }

            //keyset size
            int keysize = getInt();
            int size;
            Variant[] va; // = new Variant[size];

            for (int k=0; k<keysize; k++){
                int key = getInt();
                int arrSize = getInt();
                va = new Variant[arrSize];
                //length of array
//                key = getInt();
//                arrSize = getInt();
                for (int z=0; z<arrSize;z++)   {
                    size = getInt();  //burn size marker
                    va[z] = (Variant) get(VARIANT_TYPE);
                }
                tree.put(key,va);

            }

            result = tree;
        } else if (serial[0] == VARIANT_UID){



            int t, length, start, KID;
            String insert, name, detailedName;

            //header
            //int size = getInt();  //burn size

            long myserial = getLong();
            if (myserial != serial[0] ){
                System.out.println("Unsafe.get() :: Expecting Variant class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]+"\tsaw\t"+myserial);
                throw new ClassCastException("Unsafe.get() :: Expecting Variant class, but SerialUID do not match :: unknown :: expecting\t"+serial[0]+"\tsaw\t"+myserial);
            }

            insert = getString();
            if (insert.length() == 0) insert = null;
            detailedName = getString();
            if (detailedName.length() == 0) detailedName = null;
            name = getString();
            if (name.length() == 0) name = null;

            //KID
            //length
            //start
            //mutation type
            KID = getInt();
            length = getInt();
            start = getInt();
            t = getInt();
            boolean isReverse = getBoolean();   //unused value currently (see Class)

            result = new Variant(mutation.getEnum(t), length, start, KID, insert, name, detailedName);

        } else {
            System.out.println("Unsafe.put() :: Unknown class UID ::\t" + serial[0]);
            throw new ClassCastException("Unsafe.put() :: Unknown class UID ::\t" + serial[0]);
        }
        return result;
    }

    /**
     * helper function to put, as there is a lot of code here
     * Specifically, puts Arrays to this
     * @param arr
     * @param serial
     * @param <E>
     */
    private <E> void putArrayList(ArrayList<E> arr, long[] serial)
    {
        //String x = type.getClass().getSimpleName();
        int total = getWriteUnsafeSize(arr, serial);
        //header

        putInt(total);
        putLong(ARRAYLIST_UID);
        putLong(serial[1]);
        putInt(arr.size());

        if (serial[1] == DNABITSTRING_UID) {
            for (int k=0; k<arr.size(); k++){
                put(arr.get(k), UnsafeMemory.DNABITSTRING_TYPE);
            }
        } else if (serial[1] == STRING_UID) {
            for (int k=0; k<arr.size(); k++){
                putString((String) arr.get(k));
            }
        } else if(serial[1] == KID_UID) {
            for (int k=0; k<arr.size(); k++){
                put(arr.get(k),UnsafeMemory.KID_TYPE);
            }
        } else if(serial[1] == INT_UID) {
            for (int k=0; k<arr.size(); k++){
                putInt((Integer) arr.get(k));
            }
        }
        else {
            System.out.println("Unsafe.putArrayList() :: Unknown class UID ::\t" + serial);
        }


    }


    /**
     * Helper function to get, as there is a large amount of code here
     * Specifically, gets Arrays from this
     * @param um
     * @param serial
     * @param <E>
     * @return
     * @throws ClassCastException
     */
    private static <E> ArrayList<E> getArrayList(UnsafeMemory um, long[] serial) throws ClassCastException
    {
        //String x = type.getClass().getSimpleName();
        int total = 0;

        ArrayList<E> result = new ArrayList<>();

        //header
        long serial0 = um.getLong();
        long serial1 = um.getLong();
        int arrSize = um.getInt();

        if (serial0 != ARRAYLIST_UID){
            System.err.println("UnsafeMemory.getArrayList expected Arraylist class, instead\t"+Arrays.toString(serial));
            throw new ClassCastException("UnsafeMemory.getArrayList expected Arraylist class, instead\t"+Arrays.toString(serial));
        }


        if (serial1 == DNABITSTRING_UID) {

            for (int k=0; k<arrSize; k++){
                int size = um.getInt(); //burn counter of bytes
                result.add((E) DnaBitString.unsafeMemoryBuilder(um));
            }
        } else if (serial1 == STRING_UID) {
            for (int k=0; k<arrSize; k++){
                //um.getInt(); //burn counter of bytes
                result.add((E) um.getString());
            }

        } else if(serial1 == KID_UID) {
            for (int k=0; k<arrSize; k++){
                um.getInt(); //burn counter of bytes
                result.add((E) Kid.unsafeMemoryBuilder(um));
            }
        } else if(serial1 == INT_UID) {
            for (int k=0; k<arrSize; k++){
                result.add((E) (Integer) um.getInt());
            }
        }
        else {
            System.out.println("Unsafe.putArrayList() :: Unknown subclass UID ::\t" + serial[0]+"\tExpecting\t"+serial[1]);
        }

        return result;
    }





    public static void main(String[] args) {
        DnaBitString dns = new DnaBitString("ATG");
        Kmer31 kmer = new Kmer31(45L);
        HashMap<Integer,Character> exceptions = new HashMap<Integer,Character>();
        MyFixedBitSet mfbs = new MyFixedBitSet(0);

//        getWriteUnsafeSize(new ArrayList<String>());
//        getWriteUnsafeSize(dns);
//        getWriteUnsafeSize(kmer);
//        getWriteUnsafeSize(exceptions);
//        getWriteUnsafeSize(mfbs);



    }

    /**
     * Position in byte array is mostly needed for debugging.  This indicates the next byte to be written or read
     * @return
     */
    public int getPos() {
        return pos;
    }

    public void putMutation(mutation type) {
        switch (type) {
            case NONE:
                putInt(0);
                break;

            case SNP:
                putInt(1);
                break;

            case INSERTION:
                putInt(2);
                break;

            case DELETION:
                putInt(3);
                break;
        }
    }

    public mutation getMutation(){
        switch (getInt()){
            case 0:
            default:
                return NONE;
            case 1:
                return mutation.SNP;
            case 2:
                return INSERTION;
            case 3:
                return DELETION;
        }
    }
}
