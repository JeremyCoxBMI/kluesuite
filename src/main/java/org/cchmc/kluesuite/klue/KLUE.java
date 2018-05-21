package org.cchmc.kluesuite.klue;

import java.util.ArrayList;

/**
 * IN GENERAL, KLUE is a system of storing (key, value) pairs to represent a DNA
 * _Kmer _Look-_Up _Table ==> K.L.U.E.
 * KLUE is independent of the implementation of the store, so KLUE is its own package.
 * Versions of KLUE are then implemented for whatever store type you have.
 *
 * Implement this interface for a given store type.
 *
 * 2016-08-12   v2.0    Imported without changes from V1.6; new get ShortKmer function
 */

public interface KLUE {

    /**
     * writes the entire positions List to the store
     * REPLACES the record present if it exists
     *
     * @param key   this is the databse key representing the Kmer31Slow
     * @param positions     this is a List of positions, will OVERWRITE existing record
     */
    public void put(long key, ArrayList<Long> positions);


    /**
     * Adds a position to an existing key.
     * This is like an append operation, it adds a value to the position list corresponding to the key.
     *
     * @param key
     * @param pos
     */
    public void append(long key, long pos);

    /**
     * Retrieves a record from store
     *
     * Returns null if no record
     *
     * @param key
     * @return
     */
    public ArrayList<Long> get(long key);

    /**
     * Retrieves multiple records from store
     * The records are IN SAME ORDER as the ordering of keys as INPUT.
     * They result contains a null value if record does not exist for a single key.
     *
     * @param keys
     * @return
     */
    public ArrayList<ArrayList<Long>> getAll( long[] keys);

    /**
     * Depending on implementation of KLUE, getAll that returns PositionLists instead of List<Long>
     * may be faster
     *
     * @param keys
     * @return
     */
    public ArrayList<PositionList> getAllPL( long[] keys);

    /**
     * Allows lookup of kmers with partial match only.
     * Low significant bits that are wild cards are left as false; thus, shorty is lower limit of matches
     *
     * @param shorty    number representing lowest numeric match
     * @param prefixLength   length of prefix to match against; must be < Kmer31Slow.KMER_SIZE
     * @return
     */
    public PositionList getShortKmerMatches( long shorty, int prefixLength );

    /**
     * In the case of the database or store needs special handling when closing the store, implement here
     * Note a proper program using KLUE will call shutDown() before exitting
     */
    public void shutDown();
}
