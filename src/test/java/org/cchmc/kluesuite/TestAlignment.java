package org.cchmc.kluesuite;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;

import java.util.zip.DataFormatException;

/**
 * Created by jwc on 8/4/17.
 */
public class TestAlignment {

    public static void main(String[] args) {
        MemoryKlueTreeMap klue = new MemoryKlueTreeMap();

        KidDatabaseMemory kd = new KidDatabaseMemory();

        kd.add(new Kid("bob"));
        SuperString sz = new SuperString();
        for (int k=0; k<10;k++) sz.addAndTrim("AAAATAAAAT");
        String reference = "AAAAATCGACA";
        sz.addAndTrim(reference);

        DnaBitString dbs = null;
        String query = null;
        try {
            dbs = new DnaBitString(sz);
            kd.storeSequence(1, dbs);
            query = kd.getSequence(1,1,35,false);
        } catch (DataFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DnaBitStringToDb dbstb = new DnaBitStringToDb(dbs,klue,1);
        dbstb.writeAllPositions();

        System.out.println("\n\n\tIn memory test version KLUE initialized");


        AlignmentKLAT1 alig;
        alig = new AlignmentKLAT1(query, "manyhits", klue);
        String result;
        result = alig.results(kd);
//
//        String query;
//        AlignmentKLAT1 alig;
//        String source;
//        SmithWatermanAdvanced swa;
//        SmithWatermanOriginal sw;
//        String ref;
//        ArrayList<Seed> fwdSeeds, revSeeds;
//
//        Kmer31Slow.SUPPRESS_WARNINGS=true;
//
//
//        System.out.println("\nVerifying KLUE is initialized");
//
//        System.out.println(klue.get(new Kmer31Slow("CGACTACTATTATTTTGAAGGACAATCCAGT").toLong()));
//
//
//        System.out.println("\n * # * # * # * # * # * # * # * # * # * # * # * # * # * # * # * #\n");
//        //First half of this sequence occurs at least twice in one alignment
//        System.out.println("\nTesting sequence in multiple locations in sequence");
//        query="GTCGTTCCATTCGGAGGGTATGGATATCATAACGACTACTATTATTTTGAAGGACAATCCAGT";
//        alig = new AlignmentKLAT1(query,"multiple locations", klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        alig.pollKmersForPositions();
//        alig.calculateAlignmentTables();
//        System.out.println("\t***\tTest AlignmentKLAT1 Table IS:");
//        System.out.println(alig.toString());
//
////        System.out.println("\n\tForward Seed Matches");
////        ref = mapKidString.get(4).substring(490,523+Kmer31Slow.KMER_SIZE-1);
////        swa = new SmithWatermanAdvanced(query, ref);
////        swa.printPrettyBestResults(System.out);
////
////        ref = mapKidString.get(4).substring(1090,1098+Kmer31Slow.KMER_SIZE-1);
////        swa = new SmithWatermanAdvanced(query, ref);
////        swa.printPrettyBestResults(System.out);
//
//        System.out.println("\n\tReverse Seed Matches");
//        System.out.println("None");
//
//        System.out.println("\nTesting finding alignment seeds");
//        System.out.println( Arrays.toString(alig.getForwardSeeds().toArray()) );
//        System.out.println( Arrays.toString(alig.getReverseSeeds().toArray()) );
//
//        System.out.println("\nTesting sequence in multiple locations in sequence (AGAIN)");
//        query = "ATCCATTCCGTCATACACGCTAACCGGGAACAAAATCAATCTATCATGCACCAGATGTCCCGGACAAGAT";
//        alig = new AlignmentKLAT1(query,"multiple locations 2",klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        alig.pollKmersForPositions();
//        alig.calculateAlignmentTables();
//        System.out.println("\t***\tTest AlignmentKLAT1 Table IS:");
//        System.out.println(alig.toString());
//
//
//
//        System.out.println("\nTesting gap in middle");
//        query="GTCGTTCCATTCGGAGGGTATGGATATCATA_gap_7_ACGACTACTATTATTTTGAAGGACAATCCAGT";
//        alig = new AlignmentKLAT1(query,"gap in middle", klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
//        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
//        alig.testAll(myKidDB);
//
//
//        System.out.println("\nTesting gap at start");
//        query="TCGTTCCATTCGGAGGGTATGGATATCATA_gap_7_ACGACTACTATTATTTTGAAGGACAATCCAGT";
//        alig = new AlignmentKLAT1(query,"gap at start", klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
//        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
//        alig.testAll(myKidDB);
//
//
//        System.out.println("\nTesting gap at end middle");
//        query="GTCGTTCCATTCGGAGGGTATGGATATCATA_gap_7_ACGACTACTATTATTTTGAAGGACAATCC";
//        alig = new AlignmentKLAT1(query,"gap at end",klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
//        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
//        alig.testAll(myKidDB);
//
//        System.out.println("\nTesting many gaps, Reverse strand");
//        //Added to fake database, so this contains a reverse sequence also
//        query = "WCAGCAACAATTGTAATCAAGAGTGCGATATCAAGTGTTATGTAGTATGTAATTTAAGAATTAAGGAATAA" +
//                "WATTGTTGCCGAAGGTCTGTTATTTGAATGTTGAGATAAGGAAAGGGGCGGCGAAGCATGTGTGTATAATW";
//        source = "CAGCAACAATTGTAATCAAGAGTGCGATATCAAGTGTTATGTAGTATGTAATTTAAGAATTAAGGAATAA" +
//                "WATTGTTGCCGAAGGTCTGTTATTTGAATGTTGAGATAAGGAAAGGGGCGGCGAAGCATGTGTGTATAAT" +
//                "AACATAT";
//        alig = new AlignmentKLAT1(query,"gaps with reverse",klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
//        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
//        alig.testAll(myKidDB);
//
//        System.out.println("\nSuperHard test Case");
//        System.out.println("There are fewer hits in middle for second alignment, so adjacent hits are not in same row.");
//
//        query = "GAGTTTTTTGGAGACGTCGAGGAAGACAATTTGACCCCCGTGTGAACTCACAAAGGTCGAATAGAGGTCA";
//        alig = new AlignmentKLAT1(query,"superhard test",klue);
//        System.out.println("Checking query isValid() :: "+alig.isValid());
//        alig.testAll(myKidDB);
    }


}
