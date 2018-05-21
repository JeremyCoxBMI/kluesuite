package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.klat.Seed;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *  PositionList is a container for locations/Position, where a kmer occurs
 *  Saves time and effort by not importing bytes as Class Position if it doesn't have to
 *
 * 2016-08-15   v2.0    Imported from v1.6 without changes.  Some code is commented out -- unsure if it needs removal.
 *                      Added addWithTrim(ArrayList<Long>)
 */
public class PositionList {


    /**
     *
     */
    boolean initialized;

    long[] value;
    ArrayList<Position> posArr;

    public PositionList(){
        value = null;
        initialized = true;
        posArr = new ArrayList<Position>();
    }

    /**
     *
     * @param ll    list of longs as array
     */
    public PositionList(long[] ll){
        //here, we are referencing the object, NOT COPYING IT
        value = ll;
        initialized = false;
        posArr = new ArrayList<Position>();
    }

    /**
     *
     * @param ll    list of longs as array
     */
    public PositionList(ArrayList<Long> ll){
        initialized = true;
        posArr = new ArrayList<Position>();
        if (ll != null) {
            for (long pos : ll) {
                posArr.add(new Position(pos));
            }
        }
    }


    /**
     *
     * @param bytes     array of long presented as array of byte; should be multiple of 8
     */
    public PositionList(byte[] bytes){

        //2016.06.20 FIX
        if (bytes == null ){
            value = new long[0];
            initialized = true;
        } else {

            if (bytes.length % 8 != 0){
                System.err.println("Initializing PositionList with X bytes: "+Integer.toString(bytes.length));
            }
            int numEntries = bytes.length/8;
            value = new long[numEntries];
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            for (int k=0; k<numEntries; k++){
                value[k] = bb.getLong();
            }

            initialized = false;
        }
        posArr = new ArrayList<Position>();
    }


    /**
     * returns index of FIRST instance of a Kid in the list
     * -1 if nothing found
     * @param aKid
     * @return
     */
    public int indexOf(int aKid){
        int result = -1;
        generateArray();
        for (int k=0; k< posArr.size(); k++) {
            if (posArr.get(k).getMyKID() == aKid){
                result = k;
                break;
            }
        }
        return result;
    }


    public int length() {
        if (initialized) { return posArr.size(); }
        int total=0;
        if (value != null) total += value.length;
        if (posArr != null) total += posArr.size();
        return total;
    }

    /**
     * creates array of fully imported Positions for read access
     */
    public void generateArray( ){
        //Note that initializing value to positions
        // DOES NOT remove items already added via addWithTrim() function.

        //put at start of list, they came first
        if (value != null) {
            for (int k = 0; k < value.length; k++) {
                Position testy = new Position(value[k]);
                posArr.add(k, testy); //inserts at top of list, in order
            }
        }
        initialized = true;

        //mark value as no longer needed -- save memory
        value = null;
    }

    /**
     * Appends the position to the Position List
     *
     * @param myPos
     */
    public void add( Position myPos){
        posArr.add(myPos);
    }


    /**
     * Appends the positionS to the Position List
     *
     * @param posz  List<Long> of positions
     */
    public void add( ArrayList<Long> posz){
        //System.out.println("bug\t"+posz);
        if (posz != null)
            for (long item : posz)
                posArr.add( new Position(item));
    }


    /**
     * Appends the positionS to the Position List
     *
     * @param posz  List<Long> of positions
     */
    public void add( PositionList posz) {
        //System.out.println("bug\t"+posz);
        if (posz != null)
            for (int k = 0; k < posz.length(); k++) {
                posArr.add(posz.get(k));
            }
    }

    /**
     * Appends the positionS to the Position List
     *
     * @param posz  long[] of positions
     */
    public void add( long[] posz){
        if (posz != null)
            for (long item : posz)
                posArr.add( new Position(item));
    }


    /**
     * Sorts based on the comparator defined in Position class.
     * v2.0, this sorts by Kid, then by position
     * in this way, equal positions will be adjacent in sorted list
     */
    private void sortPositions(){
        if (!initialized){ generateArray(); }
        Collections.sort( posArr );
    }


    /***
     * sorts array, removes duplicates.
     * to be used when BUILDING database
     * @return	true if any values were removed
     */
    public boolean sortAndRemoveDuplicates(){
        boolean result = false;
        sortPositions();
        if (posArr.size() == 1) return false;

        for (int k=0; k<posArr.size()-1; k++){
            if (posArr.get(k).equals( posArr.get(k+1) ) || (1<= posArr.get(k).getMyKID() && posArr.get(k).getMyKID() <= 3) ){
                posArr.remove(k+1);
                k--;  //stay at this position, look again
                result = true;
            }
        }
        return result;
    }

    /**
     * Convert Position List to long[]
     * @return
     */
    public long[] toLongArray(){
        long[] result;
        if (value == null){
            result = new long[ posArr.size() ];
            for (int k=0; k< posArr.size(); k++){
                result[k] = posArr.get(k).toLong();
            }
        } else {
            result = new long[ value.length + posArr.size() ];
            for (int k=0; k< posArr.size(); k++){
                result[k+value.length] = posArr.get(k).toLong();
            }

            for (int k=0; k< value.length; k++){
                result[k] = value[k];
            }
        }
        return result;
    }

    /**
     * Convert Position List to ArrayList<Long>
     * @return
     */
    public ArrayList<Long> toArrayListLong(){
        ArrayList<Long> result = new ArrayList<Long>();
        if (value == null){
            for (int k=0; k< posArr.size(); k++){
                result.add( posArr.get(k).toLong() );
            }
        } else {
            for (int k=0; k< value.length; k++){
                result.add(value[k]);
            }
            for (int k=0; k< posArr.size(); k++) {
                result.add(posArr.get(k).toLong());
            }
        }
        return result;
    }

    /**
     * Converts to byte[], in order to write to store
     * @return
     */
    public byte[] toBytes() {

        int pos = 0;
        byte[] result;
        if (initialized) {
            result = new byte[  8*posArr.size()   ];
        } else {
            result = new byte[  8*(posArr.size() + value.length) ];
            pos = pos*value.length;
            //copy existing, unitialized data
            for (int k=0; k < value.length;k++){
                //            FROM         TO              LENGTH
                System.arraycopy( ByteBuffer.allocate(8).putLong(value[k]).array(), 0,    result,8*k,    8);
            }
        }

        //for (Position var : posArr){
        for (int k = 0; k < posArr.size(); k++){
            //                                    FROM         TO              LENGTH
            System.arraycopy( posArr.get(k).toBytes(), 0,    result, pos+8*k,    8);
        }

        return result;
    }

    /**
     * toString creates a String representing internal values of PositionList
     * Will state "EMPTY" if contains nothing, for clear debugging.
     */
    public String toString(){
        String result = "Output of PositionList as text\n";
        if (length() == 0){ result += "\t EMPTY!!!";}
        else {
            if (!initialized){ generateArray(); }
            for (Position tmp : posArr ){ result += "\t"+ tmp.toString() + "\n"; }
        }
        return result;
    }

//    /**
//     * Prints to System.out the values of the List, similar to toString(), will declare "EMPTY"
//     * Difference is it will also map arbitrary KID of the position to the RefSeqName
//     *
//     * @param myKidDB	type KidDatabaseMemory	STORE SPECIFIC mapping of the database KID's to more information, such as name
//     */
//    public void PrintWithRefSeqNames( KidDatabaseMemory myKidDB ){
//        System.out.println("Output of PositionList as text, expanded");
//        if (length() == 0){ System.out.println("\t EMPTY!!!"); }
//        else {
//            if (!initialized){ generateArray(); }
//            for (Position tmp : posArr ){ System.out.println(tmp.toString()+"\t"+myKidDB.getName(tmp.myKID)); }
//        }
//    }


    /**
     * return Position stored at location index
     *
     * @param index
     * @return
     */
    public Position get(int index){
        if (!initialized)
        {
            if ( index < value.length ) {
                generateArray();
            } else {
                index = index - value.length;
            }
        }
        return posArr.get(index);
    }


    public boolean contains(Position pos){
        boolean result = false;

        if (!initialized && value != null && !result) {
            for (int k = 0; k < value.length;k++){
                if (pos.isEquivalentCoordinate(new Position(value[k]))){
                    result = true;
                    break;
                }
            }
        }

        for (int k=0; k< posArr.size() && !result; k++){
            if (posArr.get(k).isEquivalentCoordinate(pos)){
                result = true;
                break;
            }
        }
        return result;
    }

    public final class PositionIterator implements Iterator<Position> {

        int curr = 0;
        int size;

        PositionIterator(){
            if (!initialized){ generateArray(); }
            size = posArr.size();
        }

        @Override
        public boolean hasNext() {
            return curr < size;
        }

        @Override
        public Position next() {
            Position r = posArr.get(curr);
            curr++;
            return r;
        }
    }

    public Iterator<Position> iterator(){
        return new PositionIterator();
    }

}
