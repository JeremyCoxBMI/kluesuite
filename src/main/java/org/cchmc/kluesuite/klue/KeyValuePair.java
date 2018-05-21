package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

/**
 * Created by jwc on 3/13/18.
 */
public class KeyValuePair {
    Long key;

    public PositionList positions;

    public KeyValuePair(byte[] key, byte[] value){
        this.key = RocksDbKlue.bytesToLong(key);
        positions = new PositionList(value);
    }
}
