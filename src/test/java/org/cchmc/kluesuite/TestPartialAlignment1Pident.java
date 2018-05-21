package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klat.PartialAlignment1;
import org.cchmc.kluesuite.klat.SmithWatermanAdvanced;
import org.cchmc.kluesuite.klat.SmithWatermanOriginal;
import org.junit.Test;

/**
 * Created by jwc on 8/24/17.
 */
public class TestPartialAlignment1Pident {

    @Test
    public void testAlignment(){
        String a = "BACKONCHEESE";
        String b = "EESEBACON";

        System.out.println("\nADVANCED\n");
        SmithWatermanAdvanced swa = new SmithWatermanAdvanced(a,b);
        for (PartialAlignment1 pa : swa.bestAlignments()){
            pa.printAlignment();
            System.out.println(pa.numAligned);
            System.out.println(pa.numMatches);
            System.out.println(pa.pident);
        }

        System.out.println("\n\nORIGINAL\n");
        SmithWatermanOriginal sw = new SmithWatermanOriginal(a,b);
        sw.printPrettyBestResults(System.out);
    }
}
