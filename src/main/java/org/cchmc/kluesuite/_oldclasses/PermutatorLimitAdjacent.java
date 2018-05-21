package org.cchmc.kluesuite._oldclasses;

import org.cchmc.kluesuite.SupplementalPrograms.SimulatorRandomLengthCluster;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.variantklue.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by osboxes on 11/MAY/17.
 *
 * This Class exists to allow permutations to be limited to a set number of adjacent permutations.
 * Note that 4 options at one position is "one adjacent" permutation
 *
 * This can be set to 1 as a default.
 *
 *
 * Global variables MAX_ADJACENT and ONLY_MAX_ADJACENT impact behavior greatly.
 *
 * generateSequenceAllVariants()
 * and processVariants operate very differently
 *
 */
public class PermutatorLimitAdjacent extends Permutator {

    /**
     * Define KMER_SIZE this class uses
     */
    public static int KMER_SIZE = Kmer31.KMER_SIZE;

    /**
     * This function allows returning all StringAndVariant responses from processVariants(), however this is
     * memory inefficient and for debugging only.
     */
    //public static boolean returnAllStringAndVariants = false;

    /**
     * If inifinite limit on adjacency is desired, use Permutator
     */
    public static int MAX_ADJACENT = 1;


    public static Variant lastVariant; //for debug

    /**
     * If true, instead of writing variants of multiple size combinations, only does MAX_ADJACENT combinations.
     * (I.e. No permutations)
     */
    public static boolean ONLY_MAX_ADJACENT = false;


    /**
     *  Controls if debug messages are passed back.
     *
     * Also controls whether return all StringAndVariant responses from processVariants(), however this is
     * memory inefficient and for debugging only.
     */

    public static boolean DEBUG = false;


    public PermutatorLimitAdjacent(KLUE klue, KidDatabaseMemory kdb, VariantDatabase vd) {
        super(klue, kdb, vd);
    }


    public PermutatorLimitAdjacent(KidDatabaseMemory kdb, VariantDatabase vd) {
        super(null, kdb, vd);
    }

    /**
     * Processes one kid, resumes at defined index (of course, 0 starts by resuming at 0)
     *
     * @param kid
     * @param resumepos
     */
    @Override
    public void processVariants(int kid, int resumepos) {
        processVariants(vd.iterator(kid), kid, resumepos);
//        super.processVariants(kid, resumepos);
    }


    /**
     * calculating refStart in one function, so that if it needs to change, it is localized
     * @param curr
     * @return
     */
    public static int calculateRefStop(Variant curr){
        if (curr.type == mutation.DELETION)
            return curr.start + curr.length + (KMER_SIZE); //EXCLUSIVE end
        else
            return curr.start + (KMER_SIZE); //EXCLUSIVE end
    }

    /**
     * calculating refStart in one function, so that if it needs to change, it is localized
     * @param curr
     * @return
     */
    public static int calculateRefStart(Variant curr) {
        return curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
    }

    public static boolean insideRefStopRange(int refStop, Variant curr){
        //BUG 2017-06-19
        //return (curr.start - KMER_SIZE +1) < refStop;
        if (curr == null) return false;
        else              return curr.start < refStop;
    }

    @Override
    public void processVariants(){
        System.err.println("\t\t\tprocessVariants() removed in PermutatorLimitAdjacent: use processVariants(int,int) instead");
    }

    /**
     * @return  returns Container of StringAndVariants written.  Returns empty container iff DEBUG = false;
     *
     * @param it
     * @param kid
     * @param resumepos
     */
    public StructPermutationStrings processVariants(VariantDatabaseIterator<Variant> it, int kid, int resumepos) {

        StructPermutationStrings result = new StructPermutationStrings();


        //Create sorted list of keys (i.e. KID)
        DnaBitString dns = myKidDb.getSequence(kid);

        int countAdjacent = 0;

        int refStart = 0, refStop = 0;

        ArrayList<Variant> variants = null;
//        variants =  new ArrayList<Variant>();

        if (dns == null) {
            System.err.println("PermutatorLimitAdjacent.processVariants called on KID, which is null " + kid);
            System.err.println("Cannot build permutations ERROR 67234");
        } else {

            // dnabitstring coordinates
            refStart = 0;  //INCLUSIVE
            refStop = 0;  //EXCLUSIVE

            int maxOverlap;
            Variant prev = null;


            //initialize curr to fake value to enter processing loop
            Variant curr = Variant.buildDELETION(-1000,0,1,"","");

            //skip over all entries that are fall below the resuming position
            while (it.hasNext() && it.peekPosition().start < resumepos) {
                curr = it.next();

                if (curr.start < resumepos) {
                    //nothing to process
                    curr = null;  //prevent going further
                }
            }

            //it.next() is now the first variant to consider

            System.out.println("%*%*%*%%*%*%*%*%%");
            System.out.println("(Per command line) Resuming on position "+curr.start);
            System.out.println("%*%*%*%%*%*%*%*%%");

            //curr==null is end
            variants = new ArrayList<Variant>();
            while (curr != null) {

                //iterate
                curr = it.next();
                //if null is triggered, abort
                if (curr == null) {
                    break;
                }


                //new cluster or adding to old?
                if (variants.size() > 0 && insideRefStopRange(refStop,curr)){
                 //old cluster exists && within range       ergo            inside old cluster
                    variants.add(curr);
                    refStop = calculateRefStop(curr);
                } else {
                    //Initialize new cluster
//                    variants = new ArrayList<Variant>();  //unnecessary? already asserted as size 0
                    variants.add(curr);
                    refStart = calculateRefStart(curr);
                    if (refStart < 0) refStart = 0;
                    refStop = calculateRefStop(curr);
                }

                Variant peek = it.peekPosition();

                //Increase cluster to MAX_ADJACENT size if possible

                //TODO size here incorrectly does not consider multiple variants at one location

                while (//curr != null && //curr will never be null inside loop
                        peek != null && insideRefStopRange(refStop,peek) && variants.size() < MAX_ADJACENT) {
                    curr = it.next();
                    variants.add(curr);
                    refStop = calculateRefStop(curr);
                    peek = it.peekPosition();
                }

                //write all permutations UNLESS
                // < MAX_ADJACENT and cluster is not ending
                if (variants.size() == MAX_ADJACENT || !insideRefStopRange(refStop,peek)) {
                    //count variants by the first one in cluster
                    tempCount2 += 1;
                    StringAndVariants[] sav = processVariantsHelper(variants, refStart, refStop, dns, kid);
                    if (DEBUG) result.add(sav, variants);

                    //maximum cluster size
                    if (variants.size() == MAX_ADJACENT){
                        variants.remove(0);
                        if (variants.size() > 0)
                            refStart = calculateRefStart(variants.get(0)); //bug 2017-06-19
                        //refStop keeps previous value
                    }
                    //if we will not continue, there is no reason to write permutation of fewer variants, as already written
                    if (!insideRefStopRange(refStop,peek)){

                        //if final cluster write, count the variants in the cluster not counted as first
                        tempCount2 += variants.size() - 1;  //works for size == 1, too
                        variants = new ArrayList<>();
                        refStart = -1000;
                        refStop = -1000;
                    }
                }



            } //end outer while

        } //end else
        return result;
    } // end processVariants


    /**
     * Modified for ONLY_MAX_ADJACENT
     * @param variants
     * @param refStart
     * @param refStop
     * @param dns
     * @param kid
     * @return
     */
    @Override
    protected StringAndVariants[] processVariantsHelper(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns, int kid) {
//        System.err.println(Arrays.toString(variants.toArray()));
//        tempCount2+= variants.size();
        //tempCount2+= 1; //variants.size();

        Integer seqStart = variants.get(0).start;
        String[] pieces = new String[ variants.size()+1];

        pieces[0] = dns.getSequence(refStart,seqStart);

        Integer tempPos = seqStart;

        Integer nextPos = -100; //SENTINEL
        if (1 < variants.size() )         nextPos = variants.get(1).start;

        int p = 1; //pieces index


        if (ONLY_MAX_ADJACENT){
            //LOOP is weird, because it is looking ahead to see if the NEXT variants match the position
            //this means looking ahead 2, as well as iterating all variables before k++ instead of after

            int numVar = 0;

            for (int k = 0; numVar < MAX_ADJACENT && k < variants.size(); k++) {
                //2017.05.01
                if (nextPos != null && nextPos.equals(tempPos)) { //same position for variant
                    //ITERATE
                    tempPos = nextPos;

                    if (k + 2 < variants.size())
                        nextPos = variants.get(k + 2).start;
                    else
                        nextPos = null;
                    //write last if this is last
                    if (k + 1 == (variants.size())) {
                        pieces[p] = dns.getSequence(tempPos, refStop);
                        p++;
                    }
                }
                //end 2017.05.01
                else {
                    if (k + 1 == variants.size()) {
                        pieces[p] = dns.getSequence(tempPos, refStop);
                    } else {
                        pieces[p] = dns.getSequence(tempPos, variants.get(k + 1).start);
                    }
                    //ITERATE
                    //tempPos = variants.get(k).start;
                    p++;
                    numVar++;
                    if (k < variants.size() - 1) {
                        seqStart = variants.get(k + 1).start;
                        tempPos = nextPos;
                        if (k + 2 < variants.size())
                            nextPos = variants.get(k + 2).start;
                        else
                            nextPos = null;

                    } else {
                        seqStart = null;
                        tempPos = null;
                    }
                }
            }
        } else {

            //LOOP is weird, because it is looking ahead to see if the NEXT variants match the position
            //this means looking ahead 2, as well as iterating all variables before k++ instead of after

            for (int k = 0; k < variants.size(); k++) {
                //2017.05.01
                if (nextPos != null && nextPos.equals(tempPos)) { //same position for variant
                    //ITERATE
                    tempPos = nextPos;

                    if (k + 2 < variants.size())
                        nextPos = variants.get(k + 2).start;
                    else
                        nextPos = null;
                    //write last if this is last
                    if (k + 1 == (variants.size())) {
                        pieces[p] = dns.getSequence(tempPos, refStop);
                        p++;
                    }
                }
                //end 2017.05.01
                else {
                    if (k + 1 == variants.size()) {
                        pieces[p] = dns.getSequence(tempPos, refStop);
                    } else {
                        pieces[p] = dns.getSequence(tempPos, variants.get(k + 1).start);
                    }
                    //ITERATE
                    //tempPos = variants.get(k).start;
                    p++;
                    if (k < variants.size() - 1) {
                        seqStart = variants.get(k + 1).start;
                        tempPos = nextPos;
                        if (k + 2 < variants.size())
                            nextPos = variants.get(k + 2).start;
                        else
                            nextPos = null;

                    } else {
                        seqStart = null;
                        tempPos = null;
                    }
                }
            }
        }
        //last one
        //pieces[p] = dns.getSequence(tempPos, refStop);

        // pieces now contains the entire sequence broken up by variant locations
        // sequences to be deleted start the after segment

        //if (DEBUG)      for (String s : pieces)         s += "\t";

        StringAndVariants[] zeroSB = new StringAndVariants[1];
        zeroSB[0] = new StringAndVariants();

        //we start with lead sequence and we will permut from there
        zeroSB[0].s = pieces[0];

        //is this bug correction? :: pieces is empty (size 0)    ?????confused
        if (pieces[0] == null) {
            pieces[0] = "";
        }

        //variants should be size 1 or greater, which is number in adjacency cluster
        StringAndVariants[] writeUs =  recurseVariantCombos(zeroSB,pieces,variants,0,1);  //bug 2017.06.13  p starts at 1



        //Not a problem after all
//        if (ONLY_MAX_ADJACENT) {
//            //NEW PROBLEM:
//            //Sequence may not extend as far as it should, since pieces is broke up by variant
//            //In fact, unless it is last I think it is guranteed to not work
//            int max = -1;
//            int min = Integer.MAX_VALUE;
//            for (StringAndVariants sav : writeUs) {
//                if (sav != null) {
//                    for (int k : sav.var) {
//                        max = Math.max(k, max);
//                        min = Math.min(k, min);
//                    }
//                    int myStop = PermutatorLimitAdjacent.calculateRefStop(variants.get(max));
//                    int myStart = PermutatorLimitAdjacent.calculateRefStart(variants.get(min));
//                    int length = sav.s.length();
//                    if (length < (myStop - myStart)) {
//                        sav.s += dns.getSequence(myStart + length, myStop);
//                    }
//                }
//            }
//        }

        if (writeUs == null){
            System.err.println("WARNING : recurseVariantCombos returned null");
            System.err.println(zeroSB);
            System.err.println(pieces);
            System.err.println(variants);
        } else {
//            int j=0; //debug

            //determine if a variant is being "skipped over" for writing
//            boolean stopDebug = false;
//            for (StringAndVariants s : writeUs ){
//                if (s!=null && s.s != null ) {
//                    stopDebug = true;
//                    break;
//                }
//            }
            if (writeUs[0] == null || writeUs[0].s == null) {
                boolean stopDebug = true;
            }

            for (StringAndVariants s : writeUs) {
                if (s!=null && s.s != null ) {
                    lastVariant = variants.get(variants.size()-1);
                    writeSequenceKlue(kid, refStart, s, variants);
//                    j++; //debug
                }
            }
//            System.err.println("j:\t"+j); //debug
        }
        return writeUs;
    }





    //NEW METHOD!
    public StringAndVariants[] generateSequenceAllVariants(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns, SimulatorRandomLengthCluster srlc) {
//        String[] pieces = generatePieces(variants,refStart,refStop,dns);
//
//        // pieces now contains the entire sequence broken up by variant locations
//        // sequences to be deleted start the after segment
//
//        StringAndVariants[] zeroSB = new StringAndVariants[1];
//        zeroSB[0] = new StringAndVariants();
//        zeroSB[0].s = pieces[0];
//        return recurseVariantCombosForceVariant(zeroSB,pieces,variants,0,1);
        return processVariantsHelper(variants,refStart,refStop,dns,srlc);
    }


    /**
     * Process variants given that there is a distribution of sizes for clusters
     *
     * New function for expanded purpose compared for generateSequenceAllVariants, so it has a different argument signature
     *
     * Not Affected by ONLY_MAX_ADJACENT
     * @param variants
     * @param refStart
     * @param refStop
     * @param dns
     * @param srlc
     * @return
     */

    protected StringAndVariants[] processVariantsHelper(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns, SimulatorRandomLengthCluster srlc) {

        ArrayList<StringAndVariants> result = new ArrayList<>();

        Integer seqStart = variants.get(0).start;
        String[] pieces = new String[variants.size() + 1];

        pieces[0] = dns.getSequence(refStart, seqStart);

        Integer tempPos = seqStart;

        Integer nextPos = -100; //SENTINEL
        if (1 < variants.size()) nextPos = variants.get(1).start;

        int p = 1; //pieces index

        //LOOP is weird, because it is looking ahead to see if the NEXT variants match the position
        //this means looking ahead 2, as well as iterating all variables before k++ instead of after

        for (int k = 0; k < variants.size(); k++) {
            //2017.05.01
            if (nextPos != null && nextPos.equals(tempPos)) { //same position for variant
                //ITERATE
                tempPos = nextPos;

                if (k + 2 < variants.size())
                    nextPos = variants.get(k + 2).start;
                else
                    nextPos = null;
                //write last if this is last
                if (k + 1 == (variants.size())) {
                    pieces[p] = dns.getSequence(tempPos, refStop);
                    p++;
                }
            }
            //end 2017.05.01
            else {
                if (k + 1 == variants.size()) {
                    pieces[p] = dns.getSequence(tempPos, refStop);
                } else {
                    pieces[p] = dns.getSequence(tempPos, variants.get(k + 1).start);
                }
                //ITERATE
                //tempPos = variants.get(k).start;
                p++;
                if (k < variants.size() - 1) {
                    seqStart = variants.get(k + 1).start;
                    tempPos = nextPos;
                    if (k + 2 < variants.size())
                        nextPos = variants.get(k + 2).start;
                    else
                        nextPos = null;

                } else {
                    seqStart = null;
                    tempPos = null;
                }
            }
        }
        //last one
        //pieces[p] = dns.getSequence(tempPos, refStop);

        // pieces now contains the entire sequence broken up by variant locations
        // sequences to be deleted start the after segment

        //if (DEBUG)      for (String s : pieces)         s += "\t";


        //make Variant sub groups
        ArrayList<ArrayList<Variant>> bigguns = new ArrayList<ArrayList<Variant>>();

        int used = 0; //used is the number of variants used

        ArrayList<Variant> temp = new ArrayList<Variant>();

        //TODO ignores impact of Variants at same location

        while(used < variants.size()) {

            int available = variants.size() - used;
            if (available > 0) {   //bug 2017-06-21  was "> 1"
                int r = srlc.getRandomSize();
                if (r>available)    r=available;
                for (int k =0; k < r; k++){
                    temp.add(variants.get(used+k));
                }
                used += r;
                bigguns.add(temp);
                temp = new ArrayList<Variant>();
            }

        }

        //write predefined subclusters
        for (ArrayList<Variant> vars : bigguns){
            StringAndVariants[] zeroSB;
            zeroSB = new StringAndVariants[1];
            zeroSB[0] = new StringAndVariants();

            //we start with lead sequence and we will permut from there
            zeroSB[0].s = pieces[0];

            //is this bug correction? :: pieces is empty (size 0)    ?????confused
            if (pieces[0] == null) {
                pieces[0] = "";
            }


            StringAndVariants[] writeUs = recurseVariantCombos(zeroSB, pieces, vars, 0, 1);  //bug 2017.06.13  p starts at 1
            for (int k = 0; k < writeUs.length; k++) {
                result.add(writeUs[k]);
            }

            if (writeUs == null) {
                System.err.println("WARNING : recurseVariantCombos returned null");
                System.err.println(zeroSB);
                System.err.println(pieces);
                System.err.println(variants);
            }
        }

        //return (StringAndVariants[]) result.toArray();

        StringAndVariants[] result2 = new StringAndVariants[result.size()];
        for (int k=0; k<result.size(); k++)
            result2[k] = result.get(k);

        return result2;
    }




    /**
     * Takes all combinations so far and permutes one more time on next variant
     *  Modified to do ONLY_MAX_ADJACENT as well
     * @param prevPermut
     * @param variants
     * @param pieces    String parts that correspond to all variant splice locations; before and after
     *                  Note that this means pieces has one more entry than variants, and index selection is based before/after
     *                  The first piece is PREFIX, then pieces[1] corresponds to variants[0] and so forth
     * @param k         integer index of current variant being processed in variants; used for recursion end
     * @param p         integer index of current String piece being processed
     * @return
     */
    @Override
    protected StringAndVariants[] recurseVariantCombos(StringAndVariants[] prevPermut, String[] pieces, ArrayList<Variant> variants, int k, int p){
        StringAndVariants[] result = null;

        //2017.06.22  short circuit
        if (variants.size() == 0){
            return prevPermut;
        }

        //In case I ever need it: the first mutation written is the default form
        //So, I suppose, we never have to write result[0] to database

        ArrayList<Variant> localVariants = new ArrayList<>();

        //Stops bug of empty PrevPermut entries (by recursing)
        //band-aid, not real fix
        int remove=0;
        for (StringAndVariants sb : prevPermut){
            if (sb == null){
                remove++;
            }
        }

        if (remove > 0){
            StringAndVariants[] temp = new StringAndVariants[prevPermut.length - remove];
            int z = 0;
            for (StringAndVariants sb : prevPermut){
                if (sb != null){
                    temp[z] = sb;
                    z++;
                }
            }
            prevPermut = temp;
        }



        // ONLY OPERATING ON ONE POSITION AT A TIME, then recurse
        // This loop checks to see if there are multiple variants at a position

        localVariants.add(variants.get(k));
        for (int z = k + 1; z < variants.size(); z++) {
            if (variants.get(z).start == variants.get(k).start) {
                localVariants.add(variants.get(z));
            } else {
                break;
            }
        }


        int multiplier = 1;
        for (int z = 0; z < localVariants.size(); z++) {
            switch (localVariants.get(z).type) {
                case SNP:
                    Set<Character> sc = DNAcodes.substituteSNP.
                            get(variants.get(k).insertSequence.charAt(0));
                    multiplier += sc.size();
                    break;
                case INSERTION:
                case DELETION:
                    multiplier += 1;
                    break;
                default:
                    break;
            }
            //is this issue?   I think whisker placement logic replaces this
            // for a sequence inserted.length() > 120, we need another position flagged --> ignoring
        }


        result = new StringAndVariants[ multiplier * prevPermut.length ];

        int r = 0;

        //Unaltered Variants first
        //write first sequences, unaltered versions
        //We don't want unaltered variants IF ONLY_MAX_ADJACENT

        if (!ONLY_MAX_ADJACENT) {
            for (StringAndVariants sb : prevPermut) {
                result[r] = new StringAndVariants(sb.s + pieces[p], prevPermut[0].var);
                r++;
            }
        }



        for (int z=0; z<localVariants.size(); z++) {
            Variant v = localVariants.get(z);
            switch (v.type) {
                case SNP:
                    Set<Character> snps = new HashSet<Character>(DNAcodes.substituteSNP.get(v.insertSequence.charAt(0)));
                    Character first = pieces[p].charAt(0);
                    String snipped = pieces[p].substring(1);  //remove first character

                    //ASSERT: snps should contain first, otherwise BIG ERROR, probably to do with indexing
                    if (snps.contains(first) && snps.size() > 1) {   //2017.05.10 debug: compensate database errors
                        snps.remove(first);
                    }

                    // non-Permutations using unaltered Strings written above.
                    // unless ONLY_MAX_ADJACENT

                    //Write all other permutations
                    for (Character c : snps) {
                        for (StringAndVariants sb : prevPermut) {
                            result[r] = null; //debug
                            result[r] = new StringAndVariants(sb.s + c + snipped, sb.var, k); //SNPs are now written to variants used (var) by (k)
                            r++;
                        }
                    }
                    break;
                case DELETION:
                    String deleted;
                    if (v.length >= pieces[p].length()){
                        // what if deletion covers next variant!
                        deleted = "";
                        if (DEBUG) {
                            System.err.println("\tWARNING\tDeletion length overlaps next Variant, truncating gap.\tKID\t" + v.KID + "\tpos\t" + v.start);
                            System.err.println("\tWARNING\tNote this acceptable because gaps are not looked up, the sequences on either side are.");
                            System.err.println("\tWARNING\tLikewise, this behavior is undefined.  What if a DELETION eclispes another DELETION?");
                        }

                    } else {
                        //delete characters by skipping past via substring command
                        deleted = pieces[p].substring(v.length);
                    }

                    for (StringAndVariants sb : prevPermut) {
                        result[r] = new StringAndVariants(sb.s + deleted, sb.var, k);
                        r++;
                    }
                    break;
                case INSERTION:
                    String inserted = v.insertSequence;
                    for (StringAndVariants sb : prevPermut) {
                        result[r] = new StringAndVariants(sb.s + inserted + pieces[p], sb.var, k);
                        r++;
                    }
                    break;
                default:
                    System.err.println("    WARNING:  (SHOULD NEVER HAPPEN)  Permutator.recurvsiveVariantCombos() has unknown variant : " + v.type);
                    //This error should never happen, but does not end the show as it were.
                    //result = new StringAndVariants[prevPermut.length];
                    //r = 0;
                    for (StringAndVariants sb : prevPermut) {
                        result[r] = new StringAndVariants(sb.s + pieces[p], sb.var);
                        r++;
                    }
                    break;
            }//end switch
        }//end for

        //STOP CONDITION : all variants processed
        if (k+localVariants.size() < variants.size() ) {
            return recurseVariantCombos(result, pieces, variants, k + localVariants.size(), p + 1);
        }else {
            return result;
        }
    }




}
