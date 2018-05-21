package org.cchmc.kluesuite;

import org.cchmc.kluesuite.variantklue.Variant;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by osboxes on 30/04/17.
 */
public class TestVariantEquals {

    @Test
    public void diffInsertSequence(){
        Variant v1 = Variant.buildSNP(1,14,"ATC","roger","multithread");
        Variant v2 = Variant.buildSNP(1,14,"","roger12","bacon12");
        Assert.assertEquals(v1.equals(v2),true);
    }

}
