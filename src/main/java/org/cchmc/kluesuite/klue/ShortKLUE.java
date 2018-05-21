package org.cchmc.kluesuite.klue;

/**
 * Created by COX1KB on 4/5/2018.
 *
 * Defines interface functionality that RocksDB can provide
 */
public interface ShortKLUE {

    public PositionList getShortKmerMatches(long shorty, int prefixLength);

}
