package org.cchmc.kluesuite;

import org.cchmc.kluesuite.SupplementalPrograms.SimulateVariableNumVariants;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.variantklue.StringAndVariants;
import org.cchmc.kluesuite.variantklue.Variant;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by jwc on 8/15/17.
 */
public class TestSimulator {

    @Test
    public void testSimulatorSequences(){
        String ref1 =    "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        String result1 = "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        Variant v = Variant.buildDELETION(50,1,1,"-/A", "-/A");

        DnaBitString dns = new DnaBitString(ref1);

        ArrayList<Variant> av = new ArrayList<>();
        av.add(v);

        StringAndVariants[] sv = SimulateVariableNumVariants.generateSequenceAllVariants(av, 0, ref1.length(),  //exclusive to inclusive
                dns, 1);
        Assert.assertEquals(sv[0].s,result1);


        String ref2 =    "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        String result2 = "CCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCGAACCCCTAACCCTAACCCTAAACCCTAAACCCTAACCCTAACCCTAACCC";
        v = Variant.buildSNP(50,1,"G","G/T", "G/T");

        dns = new DnaBitString(ref2);

        av = new ArrayList<>();
        av.add(v);

        sv = SimulateVariableNumVariants.generateSequenceAllVariants(av, 0, ref2.length(), dns, 1);
        Assert.assertEquals(sv[0].s,result2);

        String ref3 =    "CAAACTAACTGAATGTTAGAACCAACTCCTGATAAGTCTTGAACAAAAGATAGGATCCTCTATAAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGT";
        String result3 = "CAAACTAACTGAATGTTAGAACCAACTCCTGATAAGTCTTGAACAAAAGACCCTAGGATCCTCTATAAACAGGTTAATCGCCACGACATAGTAGTATTTAGAGT";
        v = Variant.buildINSERTION(50,1,"CCC","","");
        dns = new DnaBitString(ref3);

        av = new ArrayList<>();
        av.add(v);

        sv = SimulateVariableNumVariants.generateSequenceAllVariants(av, 0, ref3.length(), dns, 1);
        Assert.assertEquals(sv[0].s,result3);
    }
}
