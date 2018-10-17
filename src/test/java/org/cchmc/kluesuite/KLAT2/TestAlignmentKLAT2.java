package org.cchmc.kluesuite.KLAT2;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klat.PartialAlignment;
import org.cchmc.kluesuite.klat2.AlignmentKLAT2;
import org.cchmc.kluesuite.klat2.SmithWatermanTruncated3;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by COX1KB on 4/26/2018.
 */
public class TestAlignmentKLAT2 {

    String chr7seq =
            "cttctccacctgcctcttaagtcatcctcccacttctccagctctgctct" +
            "gggtcctgagaggctggcctctgggagaatcacccaggtcctgttccagg" +
            "atttcagccagagggaggcaccggccatcagaggtggggagggccttggt" +
            "ttcctctgcttccttccttccagtagcgggttttcagaaaggctgggttt" +
            "ctctCTGGTGTTTCTCAACTTCAGCTGCACTAGCATTTCTACTCAACCAT" +
            "TCTCTGTGCTAGGGCTGCCCTGCAAATGAGAGGGTGTCGAGCAGCAGCCC" +
            "TGCAGCAcccccacctgagcccacagtgtcaggcagaatgaccccatgga" +
            "gaaacagccctccagagttctgggagggtccagcagcccattctctcctc" +
            "accctttggccccggggtattaaagggtttctgctgttgctagtctggga" +
            "gcatttcacaggcttttattgtttcttttggctttgcccacacttctgta";

    String query = "CTGGTGTTTCTCAACTTCAGCTGCACTAGCATTTCTACTCAACCATTCTCTGTGCTAGGGCTGCCCTGCAAATGAGAGGGTGTCGAGCAGCAGCCC";
    String reverseStrand = new DnaBitString(query).getSequenceReverseStrand(0,query.length());
    //"GGGCTGCTGCTCGACACCCTCTCATTTGCAGGGCAGCCCTAGCACAGAGAATGGTTGAGTAGAAATGCTAGTGCAGCTGAAGTTGAGAAACACCAG"


    @Test
    public void testBasicAlignment(){
        KidDatabaseMemory kdm = new KidDatabaseMemory();
        kdm.add(new Kid("sub7"), chr7seq);
        KLUE klue = new MemoryKlueTreeMap();
        DnaBitStringToDb dbstd = new DnaBitStringToDb(kdm.getSequence(1),klue, 1);
        dbstd.writeAllPositions();

        SmithWatermanTruncated3.buildStrings = true;

        AlignmentKLAT2 akl = new AlignmentKLAT2(query, "test", klue,31,kdm);
        ArrayList<PartialAlignment> apl = akl.calculateFullAlignments();


        System.out.println(reverseStrand);
        System.out.println(query);
        for (PartialAlignment p : apl){
            System.out.println(p.printAlignment());
        }
    }

}
