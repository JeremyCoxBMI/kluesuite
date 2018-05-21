package org.cchmc.kluesuite.klue;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by osboxes on 13/09/16.
 */
public interface DnaBitStringGeneric {

    /**
     * * Fast conversion to Kmer31 family, by same bit representation
     * @param from
     * @return
     */
    public Kmer31 getKmer31(int from);

    /**
     * Fast conversion to Kmer31 family, by same bit representation
     * @param from  index of the starting sequence, 0-indexed text left-to-right
     * @param k kmer size
     * @return
     */
    public ShortKmer31 getShortKmer31(int from, int k);

    /**
     * Returns subsequence in the given range.
     * Indexed left to right, 0-index
     *
     * @param from  integer index INCLUSIVE
     * @param to    integer index EXCLUSIVE
     * @return
     */
    public String getSequence( int from, int to);

    /**
     * Returns reverseStrand (reverse and inverse) subsequence in the given range.
     * Indexed left to right, 0-index
     *
     * @param from  integer index INCLUSIVE
     * @param to    integer index EXCLUSIVE
     * @return
     */
    public String getSequenceReverseStrand( int from, int to);
    /**
     * returns the character at the supplied index (0-indexing, left to right)
     * @param index
     * @return
     */
    public char charAt(int index);

    /**
     * Returns the length of the sequence; has nothing to do with internal mechanics.
     * @return
     */
    public int getLength();

//    private void writeObject(java.io.ObjectOutputStream stream) throws IOException;
//
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException;

    public Iterator<Kmer31> iterator();

    public String toString();

    /**
     * Serializes object to bytes, so we can use a store that accepts byte[]
     * @return
     */
    public byte[] toByteArray();

    /**
     * Java syle iterator<Kmer31>
     *     Additionally, has length() function.
     */
    public static abstract class myIterator implements Iterator<Kmer31> {
        @Override
        public Kmer31 next() {
            return null;
        }

        @Override
        public void remove() {

        }
    }


}
