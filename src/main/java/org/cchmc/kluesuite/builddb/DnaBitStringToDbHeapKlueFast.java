package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.klue.DnaBitString;

/**
 * Created by osboxes on 24/09/16.
 */
public class DnaBitStringToDbHeapKlueFast extends DnaBitStringToDb {

    public MemoryKlueHeapFastImportArray hklue;

    public DnaBitStringToDbHeapKlueFast(DnaBitString notAcopy, MemoryKlueHeapFastImportArray writeToMe, int KID){
        super(notAcopy, writeToMe, KID);
        hklue = writeToMe;
        System.err.println("\t\tBuilding DnaBitStringToDbHeapKlueFast complete");
        this.forwardOnly = false;
    }

    public DnaBitStringToDbHeapKlueFast(DnaBitString notAcopy, MemoryKlueHeapFastImportArray writeToMe, int KID, boolean forwardOnly){
        super(notAcopy, writeToMe, KID);
        hklue = writeToMe;
        System.err.println("\t\tBuilding DnaBitStringToDbHeapKlueFast complete");
        this.forwardOnly = forwardOnly;
    }

}
