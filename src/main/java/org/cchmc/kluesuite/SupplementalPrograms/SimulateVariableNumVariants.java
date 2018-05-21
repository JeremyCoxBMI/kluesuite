package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.DNAcodes;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.variantklue.*;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static java.lang.System.exit;
import static org.cchmc.kluesuite.variantklue.mutation.DELETION;
import static org.cchmc.kluesuite.variantklue.mutation.SNP;

/**
 * Created by jwc on 6/19/17.
 *
 * Simulate Variant Reads using PLA
 * Note this uses a new class
 *
 */
public class SimulateVariableNumVariants {

    public static final class VariantsStruct{
        public StringAndVariants[] writeUs;
        public int refStart;
        public int refStop;

        public VariantsStruct() {}
    }


    public static VariantsStruct processVariants(ArrayList<Variant> variants, DnaBitString dns){
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

        refStop = endPos + offset + WHISKER_SIZE +1; //exclusive
        StringAndVariants[] writeUs = SimulateVariableNumVariants.generateSequenceAllVariants(variants, refStart, refStop, dns, 4);
        VariantsStruct result = new VariantsStruct();
        result.writeUs = writeUs;
        result.refStart = refStart;
        result.refStop = refStop;

        return result;
    }


    //guarantees that before and after  first and last variant  there are this many bases.`
    public static int WHISKER_SIZE = 50;

    public static int STOP_COUNT = 1000* 1000 * 1000;


    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 4) {
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
        SimulatorRandomLengthCluster srlc = new SimulatorRandomLengthCluster(12345,prob,10000);


        Date timer = new Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseMemory kd = new KidDatabaseMemory();
        VariantDatabaseMemory vdm = new VariantDatabaseMemory();

        try {
            System.err.println("Loading KidDatabaseMemory (unsafe)\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
            kd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
            System.err.println("Loading VariantDatabase (unsafe) \t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
            vdm = new VariantDatabaseMemory();
            vdm.loadFromFileUnsafe(args[1]);
            System.err.println("Loading complete \t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        } catch (IOException e) {
            e.printStackTrace();
            exit(0);
        }


//        //TEST CODE
//
//        vdm.addSNP(1, Variant.buildSNP(500,1,"a","b","c"));
//        vdm.addSNP(1, Variant.buildSNP(700,1,"a","b","c"));
//        vdm.addSNP(1, Variant.buildSNP(900,1,"a","b","c"));



        Double d =  Double.parseDouble(args[3]) * 1000 * 1000;
        int num_random  = d.intValue();
        //DEBUG
//        num_random = 3;

        int numVariants = vdm.size();  //how to determine?


//        //debug
//        num_random = 1000 * 1000;
//        numVariants = 150 * 1000 * 1000;


        int period = numVariants/num_random;

        Random rand = new Random(59111187654L);
        Set<Integer> keeps = new HashSet<>();
        int fourth = period / 4;
        int offset = period - fourth;
        //so offset allows rand numbers spacing from 75% to 125% length

        int lastIdx = rand.nextInt(period/2);
        keeps.add(lastIdx);


        int completed = 1;




        while (completed < num_random){
            //so offset allows rand numbers spacing from 75% to 125% length
            lastIdx += offset + rand.nextInt(2*fourth -1 ) + 1; //+1 guaranttes moves at least 1
            keeps.add(lastIdx);
            completed++;
        }

        System.err.println("\nlength of keeps is "+keeps.size()+"\n");
//        exit(0);

//        System.err.println(Arrays.toString(new ArrayList(keeps).toArray()));

//        PermutatorLimitAdjacent p = new PermutatorLimitAdjacent(kd,vdm);

        FileWriter fw = null, fwClean = null;
        BufferedWriter writer = null, writerClean = null;

        int countClean = 0;
        int countVar = 0;
        int vNum = -1; //start at 0, but always iterated
        boolean randomlySelected = false;

        try {

            fw = new FileWriter(args[2]);
            writer = new BufferedWriter(fw);

            fwClean = new FileWriter(args[2]+".clean.fa");
            writerClean = new BufferedWriter(fwClean);


            ArrayList<Variant> currentList;

            //for testing: limit keys to a single KID
            Set<Integer> s = vdm.getKeys();

            Variant curr = null, next = null, last = null, prev = null;

            //int count = 0;
            for (Integer kid : s) {

                VariantDatabaseIterator<Variant> it = vdm.iterator(kid);
                DnaBitString dns = kd.getSequence(kid);

                if (dns == null) {
                    System.err.println("Permutator.processVariantsHelper called on KID, which is null " + kid);
                    System.err.println("Cannot build permutations ERROR 67234");
                } else {

                    ArrayList<Variant> variants = new ArrayList<Variant>();

                    curr = null;

                    //start a new variant process
                    while (it.hasNext()) {
                        prev = curr;  //debug line
                        curr = (Variant) it.next();
                        vNum++;
                        if (keeps.contains(vNum)) randomlySelected = true;

                        //pick random size to use
                        int numVar = srlc.getRandomSize();


                        //only wish to pass in correct number of variants
                        //start new variant list only when a hot number is found
                        if (curr != null &&
                                randomlySelected && variants.size() < numVar) {
                            variants.add(curr);
                        }

                        //only wish to pass in correct number of variants
                        //continue building list if adjacent variants needed
                        while ( randomlySelected  &&
                                it.hasNext() &&
                                curr != null &&
                                it.peekPosition() != null &&
                                (it.peekPosition().start < curr.start+WHISKER_SIZE) &&
                                 variants.size() < numVar) {
                            prev = curr;  //debug line
                            curr = (Variant) it.next();
                            vNum++;
                            if (keeps.contains(vNum)) randomlySelected = true;

                            if (curr != null) {
                                variants.add(curr);

//                                refStop = curr.start + WHISKER_SIZE;

                                //peekPosition
//                                next = it.peekPosition();
////                                if (curr.type == DELETION && next != null &&
////                                        next.start <= curr.start+curr.length) {
////                                    curr.length = next.start - curr.start;
////                                }

                            }//if curr not null
                        } // end while inner loop


//                        if (variants.size() > 0 && variants.get(0).type == DELETION) {
//                            //refStop is the look up length, impacts length of reference sequences, others do no
//                            // refStop += variants.get(0).length;
//                            if (variants.size() >= 2  &&
//                                    variants.get(1).start <= variants.get(0).start+variants.get(0).length) {
//                                variants.get(0).length = variants.get(1).start - variants.get(0).start;
//                            }
//                            deletionOffset += variants.get(0).length;
//
//                        }
//                        refStop = endPos + offset + WHISKER_SIZE;


                        if (randomlySelected && variants.size() > 0) {
                            System.err.println("DEBUG :: Variants selected : "+ Arrays.toString(variants.toArray()) );

                            //StringAndVariants[] writeUs = SimulateVariableNumVariants.generateSequenceAllVariants(variants, refStart, refStop, dns, numVar);
                             VariantsStruct vs = processVariants(variants,dns);
                             StringAndVariants[] writeUs = vs.writeUs;
                             randomlySelected = false;
                             int refStart = vs.refStart;
                             int refStop = vs.refStop;

                            String name;



                            //generate named line
                            //from-to indexing EXCLUSIVE to INCLUSIVE(i.e.  to -= 1), 0-indexing to 1-indexing (both +1)
                            String nameBase = ">"+kd.getName(kid)+"||"+(refStart+1)+"||"+refStop;
                            writerClean.write(nameBase+"\n");
                            writerClean.write(dns.getSequence(refStart,refStop)+"\n");
                            countClean++;

                            try {
                                for (StringAndVariants sav : writeUs ){
                                    String varNames = Variant.variantNameList(variants,sav.var);
                                    name = nameBase+"||"+varNames;

                                    if (varNames.length() > 0) {  //if zero, then a repeat of the clean sequence written above
                                        writer.write(name + "\n");
                                        writer.write(sav.s + "\n");
                                        countVar++;
                                    }
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
//                        else {
//                            System.err.println("DEBUG :: Variants NOT selected : "+ Arrays.toString(variants.toArray()) );
//                        }



                        //reset loop
                        variants = new ArrayList<Variant>();
                        if (countClean >= STOP_COUNT)
                            break;
                    } //end while it.hasNext()
                    if (countClean >= STOP_COUNT)
                        break;
                }//end  if(start a new sequence)
                if (countClean >= STOP_COUNT)
                    break;

            }//end for

            //end function body
        }catch(IOException x){
            System.err.format("IOException: %s%n", x);
        }finally {

            try {

                if (writer != null)
                    writer.close();
                if (writerClean != null)
                    writerClean.close();

                if (fw != null)
                    fw.close();
                if (fwClean != null)
                    fwClean.close();

            } catch (IOException ex) {

                ex.printStackTrace();
            }
        }//end try
        System.err.println("Number of Clean written\t"+countClean);
        System.err.println("Number of Variant written\t"+countVar);
    }

    public static StringAndVariants[] generateSequenceAllVariants(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns, int numVariants) {
        StringAndVariants[] result = new StringAndVariants[1];
//        String[] mystrings = new String[1];
        //int numVariants = srlc.getRandomSize();

        if (variants.size() < numVariants){
            System.err.println("\tWARNING\ttarget was "+numVariants+" but insufficienct adjacent Variants ("+variants.size()+")");
            numVariants = variants.size();
        }

        result[0] = new StringAndVariants(dns.getSequence(refStart,refStop),new ArrayList<Integer>());

        //String currStr = dns.getSequence(refStart,refStop);

        List<StringAndVariants> temp = new ArrayList<StringAndVariants>();

        //ArrayList<String> temp = new ArrayList<>();



        //inserts and deletions affect start point
        int offset =0;
        //int pos = 0;
        for (int k=0; k< numVariants; k++){

            //SNPS may have multiple values.  In this case, return multiples.  This requires extra loop control to return multiple results.
            //result stores sequences as they get altered 1 variant at a time; begins unaltered
            for (int z=0; z<result.length;z++) {
                Variant v = variants.get(k);

                String begin = result[z].s.substring(0, v.start+offset-refStart);
                String end = result[z].s.substring(v.start+offset-refStart);
                String mid = "";
                Set<Character> snps = null;
                if (v.type == SNP) {
                    snps = DNAcodes.substituteSNP.get(v.insertSequence.charAt(0));
//                    snps = new HashSet<Character>();
                }

                switch (v.type) {
                    case SNP:
                        if (snps == null){
                            System.err.println("\tWARNING:\tfor snp\t"+v+"Cannot find equivalency char set");
                        } else {
                            Iterator<Character> it = snps.iterator();
                            //Write all other permutations
                            end = end.substring(1);
                            while (it.hasNext()) {
                                Character c = it.next();
                                mid = c.toString();
                                temp.add(new StringAndVariants(begin + mid + end, result[z].var, k));
                            }
                        }
                        break;
                    case DELETION:
                        int delete = v.length;
                        //bug fix
                        if (k+1 < numVariants){
                            if (variants.get(k+1).start - v.start < delete){
                                delete =  variants.get(k+1).start- v.start - 1; //delete thru the position before next
                            }
                        }
                        //why delete - 1?
                        end = end.substring(delete);  //does not handle deletion too long gracefully; should be handled by refStop
                        offset -= delete;
                        temp.add(new StringAndVariants(begin+end, result[z].var, k));
                        break;

                    case INSERTION:
                        mid = v.insertSequence;
                        offset += v.length;
                        temp.add(new StringAndVariants(begin+mid+end, result[z].var, k));
                        break;

                    case NONE:
                    default:
                        System.err.println("    WARNING:  (SHOULD NEVER HAPPEN)  generateSequenceAllVariants : " + v.type);
                        break;
                }



            }

            //reset loop
            result = new StringAndVariants[temp.size()];
            for (int a=0; a < temp.size(); a++){
                result[a] = temp.get(a);
            }
            temp = new ArrayList<StringAndVariants>();
        }

        return result;
    }

}
