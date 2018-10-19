package org.cchmc.kluesuite.KLAT2;

import org.cchmc.kluesuite.VirtualMemoryDatabases;
import org.cchmc.kluesuite.klue.SuperString;
import org.junit.Test;

public class TestBasicAlignments {

    @Test
    public void testRepeats() {
        SuperString sz = new SuperString();
        for (int k = 0; k < 10; k++) sz.addAndTrim("AAAATAAAAT");
        String reference = "AAAAATCGACA";
        sz.addAndTrim(reference);

        String ref = sz.toString();
        VirtualMemoryDatabases vmd = new VirtualMemoryDatabases();
        vmd.addSequence("bob", ref);

        String testQuery = ref.substring(ref.length()-20);




    }


}
