package org.cchmc.kluesuite;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabase;

/**
 * Interface to enable passing all databases as one object, save time on functions like adding sequence
 */
public abstract class VirtualDatabases {

    public KLUE klue;
    public KidDatabase kd;
    DnaBitString dbs;
    DnaBitStringToDb dbstb;

    public VirtualDatabases(){
        kd = null;
        klue=null;
    }

    public void addSequence(String name, String seq){
        kd.add(new Kid(name));
        dbs = new DnaBitString(seq);
        kd.storeSequence(1, dbs);
        dbstb = new DnaBitStringToDb(dbs,klue,1);
        dbstb.writeAllPositions();
    }

}
