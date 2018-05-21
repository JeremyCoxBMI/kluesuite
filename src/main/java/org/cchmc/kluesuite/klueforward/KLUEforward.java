package org.cchmc.kluesuite.klueforward;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.PositionList;

import java.util.ArrayList;

/**
 * Created by osboxes on 27/09/16.
 *
 * KLUEforward stores only forward direction of DNA strand.
 * The major change for version 3 is that reverseStrand information will not be written into database, to save on space.
 * This means all kmers stored will be in ForwardDirection
 *
 * For the user, this means that reverse strand lookups are a SECOND operation.
 * So trade 1/2 memory need for 2x computation need.
 * Additionally, look-ups MAY have fewer entries, meaning smaller (n) for downstream calculations.
 * This may or may not be of benefit -- whether you are interested in reverse direction, your complexity
 * Is complexity for size n  less than complexity for running size n/2 twice?  (Probably is)
 * It also means that all forward strand lookups are neatly stored separately from reverse strand lookups
 *
 */
public interface KLUEforward extends KLUE {
    @Override
    void put(long key, ArrayList<Long> positions);

    @Override
    void append(long key, long pos);

    @Override
    ArrayList<Long> get(long key);

    @Override
    ArrayList<ArrayList<Long>> getAll(long[] keys);

    @Override
    ArrayList<PositionList> getAllPL(long[] keys);

    ArrayList<Long> getRevStrand(long key);

    ArrayList<ArrayList<Long>> getAllRevStrand(long[] keys);

    ArrayList<PositionList> getAllPLRevStrand(long[] keys);

    @Override
    PositionList getShortKmerMatches(long shorty, int prefixLength);

    //PositionList getShortKmerMatchesRevStrand(long shorty, int prefixLength);
    //TODO idea: need a new database of short strands at end; position will indicate short k-mer length

    @Override
    void shutDown();
}
