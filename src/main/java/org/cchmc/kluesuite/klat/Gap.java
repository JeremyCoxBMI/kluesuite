package org.cchmc.kluesuite.klat;

/**
 * This code keeps track of a gap's location and size.  Useful for building collections.
 * This is used by KmerSequence to track where the gaps due to invalid input
 */

public class Gap {

    /**
     * var records the position in the query where the gap BEGINS, so at that position, the Kmer31Slow is invalid
     */
    public int pos;
    public int length;
    public Gap( int position, int size ){
        pos = position;
        length = size;
    }

    public Gap( int position ){
        pos = position;
        length = 1;
    }

    public Gap() {
        pos = 0;
        length = 1;
    }

    /**
     * increase the gap length by one
     */
    public void increment(){
        length++;
    }

    /**
     * returns the number of spaces the gap covers
     * @return
     */
    public int length(){
        return length;
    }

    /**
     * returns the first index after the gap
     * @return
     */
    public int nextIndexAfterGap(){
        return pos+length;
    }

}