package org.cchmc.kluesuite.klue;

import java.util.Arrays;
import java.util.BitSet;

import static java.lang.Long.parseLong;

/**
* Makes a BitSet of exact length and keeps that length.
* Rounds up to multiples of 8 bits for writing output bytes.
*
* 2016-08-12   v2.0    Imported without changes from V1.6; added a new constructor
*/


public class FixedJavaBitSet extends BitSet {

    /**
     * Fixed number of bits of information stored in the FixedJavaBitSet
     */
    public int fixedLength;

    /**
     * fixed number of bytes the FixedJavaBitSet converts to
     */
    public int fixedBytes;


    /**
     * Number of bits used to store the FixedJavaBitSet (must be multiple of 8)
     */
    public int fixedSize;

    public FixedJavaBitSet(int setLength ) {
        super();
        fixedLength = setLength;
        fixedBytes = ((setLength+7)/8);
        fixedSize = 8*fixedBytes;
    }

    /**
     * This is a copy constructor
     * @param mybits
     */
    public FixedJavaBitSet(FixedJavaBitSet mybits){
        fixedLength = mybits.fixedLength;
        fixedBytes  = mybits.fixedBytes;
        fixedSize   = mybits.fixedSize;
        set (0, fixedLength, true);
        and( mybits );
    }

//    /**
//     * This is a copy constructor
//     * @param mybits
//     */
//    public FixedJavaBitSet ( BitSet mybits, int setLength){
//        super();
//        fixedLength = setLength;
//        fixedBytes = ((setLength+7)/8);
//        fixedSize = 8*fixedBytes;
//        //set (0, Math.min(fixedLength,mybits.length()), true);
//        //copy only number = fixedLength bits over
//        set (0, fixedLength, true);   //excludes last index
//        and( mybits );
//    }


    /**
     * This is a copy constructor
     *
     * @param setLength
     * @param mybits
     */
    public FixedJavaBitSet(int setLength, BitSet mybits){
        super();
        fixedLength = setLength;
        fixedBytes = ((setLength+7)/8);
        fixedSize = 8*fixedBytes;

        //copy only number = fixedLength bits over
        set (0, fixedLength, true);   //excludes last index
        and( mybits );

    }

    public FixedJavaBitSet(long bits64 ){
        super();
        fixedLength = 64;
        fixedBytes = 8;
        fixedSize = 64;
        set (0, fixedLength, true);
        and( BitSet.valueOf( new long[] {bits64}) );
    }

    public FixedJavaBitSet(int length, long bits64 ){
        super();
        fixedLength = length;
        fixedBytes = (length+7)/8;
        fixedSize = 8*fixedBytes;
        set (0, fixedLength, true);
        and( BitSet.valueOf( new long[] {bits64}) );
    }


    /**
     * Sets all bits above fixedSize to special values
     */
    private void clean(){
        set (fixedLength+1, size()-1, false);
    }

    /**
     * This function tells you the static size chosen with constructor.
     * @return
     */
    public int fixedLength() {
        return fixedLength;
    }

    public boolean isInRange( int index ){
        if (index >= fixedLength || index < 0){
            return false;
        } else {
            return true;
        }
    }

    public int	cardinality(){
        return super.cardinality() - 1;
    }

    public String toBinaryString(){
        byte[] bytes = toByteArray();
        String result = "";
        for (int k = bytes.length-1; k >= 0; k--){
            result += Integer.toBinaryString(bytes[k] & 255 | 256).substring(1);
            //        Integer.toBinaryString(test2.toByteArray()[0] & 255 | 256).substring(1)
        }
        return result;
    }

    public String toBinaryString(int fromInclusive, int toExclusive){
        byte[] bytes = toByteArray();
        String result = "";
        for (int k = bytes.length-1; k >= 0; k--){
            result += Integer.toBinaryString(bytes[k] & 255 | 256).substring(1);
            //        Integer.toBinaryString(test2.toByteArray()[0] & 255 | 256).substring(1)
        }

        //the coordinates are reversed?
        // had to check the math (0:6) is( max-6 : max) in reverse
        return result.substring(result.length() - toExclusive, result.length() - fromInclusive);
    }

    public String toBinaryStringSpaces(){
        byte[] bytes = toByteArray();
        String result = "";
        for (int k = bytes.length-1; k >= 0; k--){
            result += Integer.toBinaryString(bytes[k] & 255 | 256).substring(1);
            //        Integer.toBinaryString(test2.toByteArray()[0] & 255 | 256).substring(1)
            result += " ";
        }
        return result;
    }



    // ########################################
    // OVERRIDES
    // ########################################

    @Override
    public byte[]	toByteArray() {

        byte[] result = Arrays.copyOf(super.toByteArray(), fixedBytes);
        return result;
    }

    @Override
    public long[]	toLongArray() {

        long[] result = Arrays.copyOf(super.toLongArray(), (fixedBytes+7)/8 );
        //JUN 17, 2016  bug found -- value of 0 still returns 0 bytes
        if (result.length == 0){ result = new long[] {-1L};}
        return result;
    }

    /**
     * Essentially, this function truncates FixedJavaBitSet to lowest 64 bits
     */
    public long getLastLong(){
        //JWC was calling super function: hence the bug
        return toLongArray()[0];
    }

    //Arrays.copyOf(bitSet.toByteArray(), desiredLength)

    /* These functions ideally would have error bounds checking
    static BitSet	valueOf(byte[] bytes)
    static BitSet	valueOf(ByteBuffer bb)
    static BitSet	valueOf(long[] longs)
    static BitSet	valueOf(LongBuffer lb)
    void	set(int bitIndex)
    void	set(int bitIndex, boolean value)
    void	set(int fromIndex, int toIndex)
    void	set(int fromIndex, int toIndex, boolean value)
    boolean	get(int bitIndex)
    BitSet	get(int fromIndex, int toIndex)
    void	clear(int bitIndex)
    void	clear(int fromIndex, int toIndex)
    */

    /* Logic overrides need to clean() after operations
    void	and(BitSet set)
    void	andNot(BitSet set)
    void	xor(BitSet set)
    */

    public static void main(String[] args) {
        //test cases
        System.out.println("\nTest importing a string, base 2");
        BitSet test2 = BitSet.valueOf(new long[] { parseLong("1101", 2) });
        System.out.println( Integer.toBinaryString(test2.toByteArray()[0] & 255 | 256).substring(1) );

        //Test copy constructor and toBinaryString()
        System.out.println("\nTest Copy Constructor and toBinaryString");
        FixedJavaBitSet test3 = new FixedJavaBitSet(4, test2);
        System.out.println(test3.toBinaryString());

        test3 = new FixedJavaBitSet(9, test2);
        System.out.println("\nShould be 16 bits long (two groups 8 bits)");
        System.out.println(test3.toBinaryStringSpaces());

        System.out.println("\nTesting set(int, boolean) function");
        FixedJavaBitSet bacon = new FixedJavaBitSet(12);
        bacon.set(9,true);
        bacon.set(1,true);
        bacon.set(13,true);  //illegal reference allowed, not checked -- you must check with isInRange(int)
        System.out.println(bacon.toBinaryString());
        System.out.println(bacon.toBinaryStringSpaces());
        System.out.println("Testing toBinaryString(from, to)");
        System.out.println(bacon.toBinaryString());
        System.out.println(bacon.toBinaryString(0, 6));
        System.out.println(bacon.toBinaryString(6, 12));


        System.out.println("\nTesting emptybyte");
        FixedJavaBitSet emptyByte = new FixedJavaBitSet(8);
        System.out.println(emptyByte.toBinaryString());

        System.out.println("\nTesting bitshifts");
        long five = 5;
        long four = 4;
        long negsix = -6;
        System.out.println("This long "+Long.toString(five)+" bitshift >> 2 => "+Long.toString(five>>2));
        System.out.println("This long "+Long.toString(five)+" bitshift << 2 => "+Long.toString(five<<2));
        System.out.println("This long "+Long.toString(four)+" bitshift >> 2 => "+Long.toString(four>>2));
        System.out.println("This long "+Long.toString(four)+" bitshift << 2 => "+Long.toString(four<<2));
        System.out.println("This long "+Long.toString(negsix)+" bitshift >> 2 => "+Long.toString(negsix>>2));
        System.out.println("This long ");
        System.out.println("3210987654321098765432109876543210987654321098765432109876543210");
        System.out.println(Long.toBinaryString(negsix));
        System.out.println("bitshift >> 2");
        System.out.println(Long.toBinaryString(negsix>>2));

        System.out.println("This long "+Long.toString(negsix)+" bitshift << 2 => "+Long.toString(negsix<<2));
        System.out.println("This long ");
        System.out.println(Long.toBinaryString(negsix));
        System.out.println("bitshift << 2");
        System.out.println(Long.toBinaryString(negsix<<2));


    }

//    private static long parseLong(String s, int i) {
//    }

}