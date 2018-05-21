package org.cchmc.kluesuite;

import org.cchmc.kluesuite.SupplementalPrograms.SimulateVariableNumVariants;
import org.cchmc.kluesuite.SupplementalPrograms.SimulatorRandomLengthCluster;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.variantklue.StringAndVariants;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.*;

import static java.lang.System.exit;
import static org.cchmc.kluesuite.SupplementalPrograms.SimulateVariableNumVariants.WHISKER_SIZE;
import static org.cchmc.kluesuite.variantklue.mutation.DELETION;
import static org.cchmc.kluesuite.variantklue.mutation.SNP;

/**
 * Created by jwc on 8/7/17.
 */
public class RecreateCrashSimulationGenerator {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.out.println("Takes a KidDatabaseMemory and VariantDatabaseMemory file and generates variants as FastA.");
            System.out.println("Variants are generated using length (not maximum) determined by observed distribution ");
            System.out.println("ARG 0 : location KidDatabaseMemory to read (unsafe)");
            System.out.println("ARG 1 : location variant database (unsafe) to read");
            System.out.println("ARG 2 : FastA to output");
            System.out.println("ARG 3 : number Variants to randomly select (millions / decimal)");

            exit(0);
        }

        // ***** Test Example
//        Double[] prob = new Double[]{0.55, 0.25, 0.15, 0.05};
//        SimulatorRandomLengthCluster srlc = new SimulatorRandomLengthCluster(12345,prob,1000);
//
//        Integer[] counts = new Integer[]{null,0,0,0,0};
//
//        for (int k=0; k< 100; k++){
//            int r = srlc.getRandomSize();
//            counts[r]++;
//            System.err.println(r);
//        }
//
//        System.err.println("\nCounts\n");
//        for (int k=1; k< counts.length; k++){
//            System.err.println(k+"\t"+counts[k]);
//        }


        //based on 116 cell lines
        Double[] prob = new Double[]{0.9066, 0.0780, 0.0116, 0.00251};
        SimulatorRandomLengthCluster srlc = new SimulatorRandomLengthCluster(12345, prob, 10000);


        Date timer = new Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseMemory kd = new KidDatabaseMemory();
        VariantDatabaseMemory vdm = new VariantDatabaseMemory();


        System.err.println("Loading KidDatabaseMemory (unsafe)\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        kd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
//            System.err.println("Loading VariantDatabase (unsafe) \t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
//            vdm = new VariantDatabaseMemory();
//            vdm.loadFromFileUnsafe(args[1]);
        System.err.println("Loading complete \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());


        //Double d =  Double.parseDouble(args[3]) * 1000 * 1000;
        int num_random = 3;
        //DEBUG
//        num_random = 3;

//        int numVariants = vdm.size();  //how to determine?


//        //debug
//        num_random = 1000 * 1000;
//        numVariants = 150 * 1000 * 1000;




//        System.err.println(Arrays.toString(new ArrayList(keeps).toArray()));

//        PermutatorLimitAdjacent p = new PermutatorLimitAdjacent(kd,vdm);




        int kid = 12;


        DnaBitString dns = kd.getSequence(kid);
        ArrayList<Variant> variants = new ArrayList<Variant>();


//        variants.addAndTrim(Variant.buildDELETION(134607264, 12, 1255, "hugz01", "luvz01"));
//        variants.addAndTrim(Variant.buildSNP(134607343, 12, "G", "hugz03", "luvz03"));
//        variants.addAndTrim(Variant.buildDELETION(134607353, 12, 3, "hugz02", "luvz02"));
//        variants.addAndTrim(Variant.buildSNP(134607381, 12, "G", "hugz04", "luvz04"));


        //DEBUG :: Variants selected : [DELETION          k 1     s 7204820       l 1      null, DELETION         k 1     s 7204820       l 9      null

//        variants.addAndTrim(Variant.buildDELETION(7205820, 1, 1, "hugz01", "luvz01"));
//        variants.addAndTrim(Variant.buildDELETION(7205820, 1, 9, "hugz02", "luvz02"));


//        variants.addAndTrim(Variant.buildINSERTION(16389482,1,"T","luv19","hug19"));
//        variants.addAndTrim(Variant.buildSNP(16389531, 1, "G", "hugz04", "luvz04"));


//        variants.addAndTrim(Variant.buildDELETION(10227, 1, 1, "hugz02", "luvz02"));

        variants.add(Variant.buildDELETION(1699263, 2, 1223, "hugz02", "luvz02"));



        //remove duplicate mutations at same position
        for (int k=0; k<variants.size()-1; k++){
            for (int j=k+1; j < variants.size(); j++){
                Variant vk=variants.get(k);
                Variant vj=variants.get(j);
                if (vk.start == vj.start){
                    //case of a run of all SNPs, all get considered; note if a run has 1 that is not SNP, removes still occur
                    if (!(vk.type == SNP && vj.type == SNP)) {
                        //keep longest
                        if (vk.length > vj.start) {
                            variants.remove(j);
                            j--;  //move back 1 to stay on current position, so next moves into place
                        } else {
                            variants.remove(k);
                            j--;  //move back 1 to stay on current position, so next moves into place
                            //k--; //k does not move up, next moves up into position; k does not k++ after this body/scope
                        }
                    }
                }
            }
        }

        int refStart = variants.get(0).start - WHISKER_SIZE;   //INCLUSIVE start
        if (refStart < 0) refStart = 0;
        int refStop = 0; //variants.get(0).start + WHISKER_SIZE;

        //We need to keep track of how multiple variants affect the end by position,  and also by offset
        int endPos = variants.get(0).start;
        int offset = 0;


        if (variants.get(0).type == DELETION) {
            //refStop is the look up length, impacts length of reference sequences, others do no
            // refStop += variants.get(0).length;
            if (variants.size() >= 2  &&
                variants.get(1).start <= variants.get(0).start+variants.get(0).length) {
                    variants.get(0).length = variants.get(1).start - variants.get(0).start;
            }
            offset += variants.get(0).length;

        }

        for (int k = 1; k < variants.size(); k++) {
            if (variants.get(k).type == DELETION) {
                //refStop is the look up length, impacts length of reference sequences, others do not
                if (variants.size() > k+1  &&
                        variants.get(k+1).start <= variants.get(k).start+variants.get(k).length) {
                    variants.get(k+1).length = variants.get(k+1).start - variants.get(k).start;
                }
                offset  += variants.get(k).length;
            }
            endPos = variants.get(k).start;
        }

        refStop = endPos + offset + WHISKER_SIZE;
        StringAndVariants[] writeUs = SimulateVariableNumVariants.generateSequenceAllVariants(variants, refStart, refStop, dns, 4);







    }
}
