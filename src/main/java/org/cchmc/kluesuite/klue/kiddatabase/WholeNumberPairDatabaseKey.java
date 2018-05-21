package org.cchmc.kluesuite.klue.kiddatabase;

import java.nio.ByteBuffer;

/**
 * Created by jwc on 4/12/18.
 */
public class WholeNumberPairDatabaseKey {

    private int a;

    /**
     * location is position in the reference sequence
     * or coordinate 0 to X, where X is length of reference sequence
     */
    private int b;

//    long key;

    /**
     * non-negative integers
     * @param x
     * @param y
     */
    public WholeNumberPairDatabaseKey(int x, int y) {
        this.a = x;
        this.b = y;
//        key = toLong();
    }

//    private long toLong() {
//        long nextOffset = (long) a;
//        nextOffset = nextOffset << 32;
//        return (nextOffset += b);
//    }
//
//    public long getLong() {
//        return key;
//    }

    public WholeNumberPairDatabaseKey(long number) {
        long shifter = number >> 32;
        a = (int) shifter;
        shifter = shifter << 32;
        number -= shifter;
        b = (int) number;
    }

    protected int getA() {
        return a;
    }

    protected int getB() {
        return b;
    }

    public byte[] toBytes(){
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(a);
        bb.putInt(b);
        return bb.array();
    }


    /*
     * Note associated keys in lexicographic order
     * 8 byte long integer (WholeNumberPairedDatabaseKey) Kid and -2000    ==> to length
     * 8 byte long integer (WholeNumberPairedDatabaseKey) kid and position ==> DnaBitString fragment
     * 8 byte long integer (WholeNumberPairedDatabaseKey) kid and -145     ==> to exceptions HashMap
     * 8 byte long integer (WholeNumberPairedDatabaseKey) kid and -1576    ==> to String
     * String of longer than 8 bytes ==> kid & length
     */
    public static Class identifyClass(WholeNumberPairDatabaseKey w){
        switch (w.getB()){
            case GetExceptionsArrKey.EXCEPTIONS_HASHMAP_POSITION:
                return GetExceptionsArrKey.class;
            case GetSequenceNameKey.STRING_KEY:
                return GetSequenceNameKey.class;
            case GetLengthKey.LENGTH_KEY:
                return GetLengthKey.class;
            default:
                return GetDnaBitStringFragmentKey.class;
        }
    }

}
