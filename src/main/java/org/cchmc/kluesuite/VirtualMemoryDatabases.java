package org.cchmc.kluesuite;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;

public class VirtualMemoryDatabases extends VirtualDatabases {
    public VirtualMemoryDatabases() {
        klue = new MemoryKlueTreeMap();
        kd = new KidDatabaseMemory();
    }
}
