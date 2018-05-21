package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.binaryfiledirect.UnsafeSerializable;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

/**
 * Created by jwc on 8/1/17.
 *
 * This allows you to skip all KID up to a certain point, great for massive parallelization of KID importing database.
 *
 * Implementation may be incomplete.
 */
public class KidDatabaseMemorySneaky extends KidDatabaseMemory implements UnsafeSerializable{
    int offset;

    public KidDatabaseMemorySneaky(int i) {
        super();
        offset = i;
    }

    public int getMaxKid() {
        return last+offset;
    }

    public int indexOf(String seqName) {
        if (seqName.charAt(0) == '>'){
            return nameIndex.indexOf(seqName.substring(1))+offset;
        }
        return nameIndex.indexOf(seqName)+offset;
    }

    public int getSequenceLength(int index) {
        index -= offset;
        if (0 <= index && index <= last && sequences.get(index) != null)
            return sequences.get(index).getLength();
        else
            return -1;  //1 << 30;
    }

}
