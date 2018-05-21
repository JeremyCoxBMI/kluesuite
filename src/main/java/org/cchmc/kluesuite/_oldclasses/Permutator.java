package org.cchmc.kluesuite._oldclasses;

import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.variantklue.StringAndVariants;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabase;
import org.cchmc.kluesuite.variantklue.VariantDatabaseIterator;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.cchmc.kluesuite.klue.Kmer31.KMER_SIZE;
import static org.cchmc.kluesuite.variantklue.mutation.*;

/**
 * Permutator to work with VariantDatabaseOLD Interface
 *
 * 2017-05-01 Redesign: can have multiple indels at a single position
 * Redesign again : works with VariantDatabase interface
 *
 * Will become DEPRECATED once the variant caller is completed
 *
 */
public class Permutator {

    public static boolean DEBUG = false;

    /**
     * counter used for debugging
     * number Kmers written
     *
     */
    public static int tempCount = 0;


    /**
     * counter used for debugging
     * number processVariantHelperCalls
     *
     */
    public static int tempCount2 = 0;


    //RocksDB dbsKlue; //part of RocksKidDatabase
    KLUE kmerDB;
    KidDatabaseMemory myKidDb;
    VariantDatabase vd;

    public Permutator() {
//        dbsKlue = null;
        kmerDB = null;
        myKidDb = null;
        vd = null;
    }

    /**
     * @param klue Kmer Database to update.  Must be opened in writeable mode
     *             Can be null, if not planning to use writeAllPositions(), such as _generating_simulations_
     * @param kdb  RocksKidDatabase, so that we can look up KID and DnaBitString
     * @param vd   Database of Variants to process
     */
    public Permutator(KLUE klue, KidDatabaseMemory kdb, VariantDatabase vd) {
        this.kmerDB = klue;
        this.myKidDb = kdb;
        this.vd = vd;
    }


    /**
     * Processes one kid, resumes at defined index (of course, 0 starts by resuming at 0)
     *
     * @param kid
     * @param resumepos
     */
    public void processVariants(int kid, int resumepos) {

        //Create sorted list of keys (i.e. KID)
        VariantDatabaseIterator<Variant> it = vd.iterator(kid);
        DnaBitString dns = myKidDb.getSequence(kid);

        Variant curr = Variant.buildDELETION(0, kid, 1, "junk", "junk");
        while (it.hasNext() && curr.start < resumepos) {
            curr = it.next();
        }

        System.out.println("%*%*%*%%*%*%*%*%%");
        System.out.println("Resuming on position "+curr.start);
        System.out.println("%*%*%*%%*%*%*%*%%");


        if (curr.start == 0) curr = it.next();   //if resuming at 0, curr will be the fake reading


        int refStart = 0, refStop = 0;

        //curr is now the first sequence to consider

        ArrayList<Variant> variants = null;

        if (dns == null) {
            System.err.println("Permutator.processVariants called on KID, which is null " + kid);
            System.err.println("Cannot build permutations ERROR 77234");
        } else {

            variants = new ArrayList<Variant>();

            refStart = 0;
            refStop = 0;  //absolute dnabitstring coordinates, before things get screwy

            int maxOverlap;
            Variant prev;

            // iterates at the end
            while (curr != null) {
                prev = curr;  //debug line

                //Initialize positions
                if (curr != null) {
                    refStart = curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
                    if (refStart < 0) refStart = 0;
                    refStop = curr.start + curr.length + (KMER_SIZE - 1); //EXCLUSIVE end
                } else {
                    prev = prev;
                }


                while (curr != null && (curr.start < refStop)) {
                    prev = curr;  //debug line

                    if (curr != null) {
                        variants.add(curr);

                        //refStop is the position EXCLUSIVE where the sequence stops being eclipsed by the SNPs and indels
                        //in case of insert, it does not go further.
                        //in the case of delete, it DOES extend based on length

                        refStop = curr.start + (KMER_SIZE - 1); //EXCLUSIVE end
                        if (curr.type == DELETION) {
                            refStop += curr.length;
                        }
                    }//if curr not null

                    //Iterate or end
//                    if (it.hasNext() && it.peekPosition().start < refStop ) {
                    if (it.hasNext()) { //always iterate; loop starts having iterated
                        curr = (Variant) it.next(); //iterate
                    } else {
                        curr = null;  //terminate loop
                    }
                } // end while inner loop


                if (variants != null && variants.size() > 0)
                    processVariantsHelper(variants, refStart, refStop, dns, kid);
                else
                    System.out.println("WARNING : empty variant list produced : (hasNext) : " + it.hasNext());

                //reset loop
                if (it.hasNext()) {
                    curr = it.next();
                    variants = new ArrayList<Variant>();
                } else {
                    curr = null;    //EXIT and keep variants in case it needs to process
                }

            } //end while master

            //IF LAST VARIANT was not processes, process it
            if (variants != null && variants.size() > 0) {
                if (variants.size() == 1) {
                    curr = variants.get(0);
                    refStart = curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
                    if (refStart < 0) refStart = 0;
                    refStop = curr.start + curr.length + (KMER_SIZE - 1); //EXCLUSIVE end
                }
                processVariantsHelper(variants, refStart, refStop, dns, kid);
            }
        } //end if/else
    } //end processVariants(KID, resume)



    public void processVariantsLimitedPermutationCount(int kid, int resumepos, int maxSimultaenousVariants ){

        //Create sorted list of keys (i.e. KID)
        VariantDatabaseIterator<Variant> it = vd.iterator(kid);
        DnaBitString dns = myKidDb.getSequence(kid);

        Variant curr = Variant.buildDELETION(0, kid, 1, "junk", "junk");
        while (it.hasNext() && curr.start < resumepos) {
            curr = it.next();
        }

        System.out.println("%*%*%*%%*%*%*%*%%");
        System.out.println("Resuming on position "+curr.start);
        System.out.println("%*%*%*%%*%*%*%*%%");


        if (curr.start == 0) curr = it.next();   //if resuming at 0, curr will be the fake reading


        int refStart = 0, refStop = 0;

        //curr is now the first sequence to consider

        ArrayList<Variant> variants = null;

        if (dns == null) {
            System.err.println("Permutator.processVariantsHelper called on KID, which is null " + kid);
            System.err.println("Cannot build permutations ERROR 67234");
        } else {

            variants = new ArrayList<Variant>();

            refStart = 0;
            refStop = 0;  //absolute dnabitstring coordinates, before things get screwy

            int maxOverlap;
            Variant prev;

            // iterates at the end
            while (curr != null) {
                prev = curr;  //debug line

                //Initialize positions
                if (curr != null) {
                    refStart = curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
                    if (refStart < 0) refStart = 0;
                    refStop = curr.start + curr.length + (KMER_SIZE - 1); //EXCLUSIVE end
                } else {
                    prev = prev;
                }


                while (curr != null && (curr.start < refStop)) {
                    prev = curr;  //debug line

                    if (curr != null) {
                        variants.add(curr);

                        //refStop is the position EXCLUSIVE where the sequence stops being eclipsed by the SNPs and indels
                        //in case of insert, it does not go further.
                        //in the case of delete, it DOES extend based on length



                        refStop = curr.start + (KMER_SIZE - 1); //EXCLUSIVE end
                        if (curr.type == DELETION) {
                            refStop += curr.length;
                        }

                        if (variants.size() == maxSimultaenousVariants){
                            processVariantsHelper(variants, refStart, refStop, dns, kid);
                            variants.remove(0);
//                            int difference = variants.get(0).start - refStart;
                            refStart = variants.get(0).start;
                        }


                    }//if curr not null

                    //Iterate or end
//                    if (it.hasNext() && it.peekPosition().start < refStop ) {
                    if (it.hasNext()) { //always iterate; loop starts having iterated
                        curr = (Variant) it.next(); //iterate
                    } else {
                        curr = null;  //terminate loop
                    }
                } // end while inner loop


                if (variants != null && variants.size() > 0)
                    processVariantsHelper(variants, refStart, refStop, dns, kid);
                else
                    System.out.println("WARNING : empty variant list produced : (hasNext) : " + it.hasNext());

                //reset loop
                if (it.hasNext()) {
                    curr = it.next();
                    variants = new ArrayList<Variant>();
                } else {
                    curr = null;    //EXIT and keep variants in case it needs to process
                }

            } //end while master

            //IF LAST VARIANT was not processes, process it
            if (variants != null && variants.size() > 0) {
                if (variants.size() == 1) {
                    curr = variants.get(0);
                    refStart = curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
                    if (refStart < 0) refStart = 0;
                    refStop = curr.start + curr.length + (KMER_SIZE - 1); //EXCLUSIVE end
                }
                processVariantsHelper(variants, refStart, refStop, dns, kid);
            }
        } //end if/else
    }




    public void processVariantsOneAtATime(int kid, int resumepos) {

        //Create sorted list of keys (i.e. KID)
        VariantDatabaseIterator<Variant> it = vd.iterator(kid);
        DnaBitString dns = myKidDb.getSequence(kid);

        Variant curr = Variant.buildDELETION(0, kid, 1, "junk", "junk");
        while (it.hasNext() && curr.start < resumepos) {
            curr = it.next();
        }

        System.out.println("%*%*%*%%*%*%*%*%%");
        System.out.println("Resuming on position "+curr.start);
        System.out.println("%*%*%*%%*%*%*%*%%");


        if (curr.start == 0) curr = it.next();   //if resuming at 0, curr will be the fake reading


        int refStart = 0, refStop = 0;

        //curr is now the first sequence to consider

        ArrayList<Variant> variants = null;

        while (curr != null) {

            variants = new ArrayList<Variant>();

            if (dns == null) {
                System.err.println("Permutator.processVariantsHelper called on KID, which is null " + kid);
                System.err.println("Cannot build permutations ERROR 67234");
            } else {

                refStart = 0;
                refStop = 0;  //absolute dnabitstring coordinates, before things get screwy
                //Initialize positions
                if (curr != null) {
                    refStart = curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
                    if (refStart < 0) refStart = 0;
                    refStop = curr.start + curr.length + (KMER_SIZE - 1); //EXCLUSIVE end
                }
                variants.add(curr);

                StringAndVariants[] zeroSB = new StringAndVariants[1];
                zeroSB[0] = new StringAndVariants();

                String[] pieces = new String[2];
                pieces[0] = dns.getSequence(refStart, curr.start);
                pieces[1] = dns.getSequence(curr.start, refStop);


                //we start with lead sequence and we will permut from there
                zeroSB[0].s = pieces[0];

                StringAndVariants[] writeUs = recurseVariantCombos(zeroSB, pieces, variants, 0, 1);

                if (writeUs == null) {
                    System.err.println("WARNING : recurseVariantCombos returned null");
                    System.err.println(zeroSB);
                    System.err.println(pieces);
                    System.err.println(variants);
                } else {
                    for (int k = 1; k < writeUs.length; k++) {  //skip the normal unaltered version
                        StringAndVariants s = writeUs[k];
                        if (s != null && s.s != null)
                            writeSequenceKlue(kid, refStart, s, variants);
                    }
                }


                if (it.hasNext())
                    curr = it.next();
                else
                    curr = null;
            }
        }//end while
    }



    public void processVariants(){

        //Create sorted list of keys (i.e. KID)
        HashSet<Integer> kz = new HashSet(vd.getSNPKeys());
        kz.addAll(vd.getIndelKeys());
        ArrayList<Integer> keys = new ArrayList<Integer>(kz);
//        for ( Integer k : kz) {
//            keys.addAndTrim(k);
//        }
        Collections.sort(keys);
//        int refStart;
//        int refStop;


//        //DEBUG BLOCK
//        ArrayList<Integer> keys2 = keys;
//        keys = new ArrayList<Integer>();
//        for (int k=0; k<4; k++){
//            keys.addAndTrim(keys2.get(k));
//        }
//        //  end  DEBUG BLOCK




        for (int kid : keys){

            VariantDatabaseIterator<Variant> it = vd.iterator(kid);
            DnaBitString dns = myKidDb.getSequence(kid);

            if (dns == null) {
                System.err.println("Permutator.processVariantsHelper called on KID, which is null "+kid);
                System.err.println("Cannot build permutations ERROR 67234");
            } else {

                ArrayList<Variant> variants = new ArrayList<Variant>();

                int refStart=0, refStop=0;  //absolute dnabitstring coordinates, before things get screwy
                int maxOverlap;

                Variant curr = null, prev;

                //start a new variant process
                while (it.hasNext()) {
                    prev = curr;  //debug line
                    curr = (Variant) it.next();

//                    //2017-05-01
//                    if (prev.start == curr.start) {
//
//
//                    }

                    if (curr != null) {

                        variants.add(curr);
                        refStart = curr.start - (KMER_SIZE - 1);   //INCLUSIVE start
                        if (refStart < 0) refStart = 0;
                        refStop = curr.start + curr.length + (KMER_SIZE - 1); //EXCLUSIVE end
                    } else {
                        prev=prev;
                    }

                    while (   it.hasNext()  && curr != null &&  ( it.peekPosition().start < refStop )    ) {
                        prev = curr;  //debug line
                        curr = (Variant) it.next();

                        if (curr.start == 10127){
                            int debug = 1;
                        }

                        if (curr != null) {
                            variants.add(curr);

                            //refStop is the position EXCLUSIVE where the sequence stops being eclipsed by the SNPs and indels
                            //in case of insert, it does not go further.
                            //in the case of delete, it DOES extend based on length

//                            if ((curr == null)) {
//                                System.err.println("DEBUG error SHRUNKY DUNK");
//                                System.err.println("Last variant corretly read " + prev);
//                                System.err.println("Current variant read " + curr);
//                                exit(1);
//                            }

                            refStop = curr.start + (KMER_SIZE - 1); //EXCLUSIVE end
                            if (curr.type == DELETION) {
                                refStop += curr.length;
                            }
                        }//if curr not null
                    } // end while inner loop

                    processVariantsHelper(variants, refStart, refStop, dns, kid);

                    //reset loop
                    variants = new ArrayList<Variant>();
                } //end while master
            }//end  start a new variant process
        }
    }

    /**
     * This is complicated, because we need to generate all possible permutation combinations, THEN
     * write them to database
     *
     * @return Primarily for debugging, he combinatorial strings written
     *
     * @param variants
     * @param refStart
     * @param refStop
     * @param dns
     */

    protected StringAndVariants[] processVariantsHelper(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns, int kid) {

//        System.err.println("Variant list before :: "+variants);
//        variants = combineSamePositions(variants);
//        System.err.println("Variant list after :: "+variants);


        Integer seqStart = variants.get(0).start;
        String[] pieces = new String[ variants.size()+1];

        pieces[0] = dns.getSequence(refStart,seqStart);

        Integer tempPos = seqStart;

        Integer nextPos = -100; //SENTINEL
        if (1 < variants.size() )         nextPos = variants.get(1).start;

        int p = 1; //pieces index

        //LOOP is weird, because it is looking ahead to see if the NEXT variants match the position
        //this means looking ahead 2, as well as iterating all variables before k++ instead of after

        for(int k=0; k<variants.size(); k++){
            //2017.05.01
            if (nextPos != null && nextPos.equals(tempPos)) { //same position for variant
                //ITERATE
                tempPos = nextPos;

                if (k+2 < variants.size() )
                    nextPos = variants.get(k+2).start;
                else
                    nextPos = null;
                //write last if this is last
                if (k+1 == (variants.size())){
                    pieces[p] = dns.getSequence(tempPos, refStop);
                    p++;
                }
            }
            //end 2017.05.01
            else {
                if (k+1 == variants.size()) {
                    pieces[p] = dns.getSequence(tempPos, refStop);
                } else {
                    pieces[p] = dns.getSequence(tempPos, variants.get(k+1).start);
                }
                //ITERATE
                //tempPos = variants.get(k).start;
                p++;
                if (k<variants.size()-1){
                    seqStart = variants.get(k+1).start;
                    tempPos = nextPos;
                    if (k+2 < variants.size())
                        nextPos = variants.get(k+2).start;
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

        StringAndVariants[] zeroSB = new StringAndVariants[1];
        zeroSB[0] = new StringAndVariants();

        //we start with lead sequence and we will permut from there
        zeroSB[0].s = pieces[0];

        //is this bug correction? :: pieces is empty (size 0)    ?????confused
        if (pieces[0] == null) {
            pieces[0] = "";
        }


        StringAndVariants[] writeUs =  recurseVariantCombos(zeroSB,pieces,variants,0,1);  //bug 2017.06.13  p starts at 1

        if (writeUs == null){
            System.err.println("WARNING : recurseVariantCombos returned null");
            System.err.println(zeroSB);
            System.err.println(pieces);
            System.err.println(variants);
        } else {
//            int j=0; //debug
            for (StringAndVariants s : writeUs) {
                if (s!=null && s.s != null ) {
                    writeSequenceKlue(kid, refStart, s, variants);
//                    j++; //debug
                }
            }
//            System.err.println("j:\t"+j); //debug
        }
        return writeUs;
    }

    /**
     * Creates permutations of existing sequences and
     * @param kid
     * @param refStart
     * @param sb
     * @param variants
     */

    void writeSequenceKlue(int kid, int refStart, StringAndVariants sb, ArrayList<Variant> variants) {

        if (DEBUG) System.err.println(sb.toString());
        int numWrites = sb.s.length() - KMER_SIZE + 1;
        if (numWrites > 0) tempCount += numWrites;  //debug
        else tempCount = tempCount;

        boolean[] indelFlag = new boolean[sb.s.length()];
        boolean[] snpFlag = new boolean[sb.s.length()];
        for (int k=0; k<sb.s.length(); k++){
            indelFlag[k] = false;
            snpFlag[k] = false;
        }

        Collections.sort(sb.var);   //sorted, so we don't right true over an over again
        int offset = 0, x, l, u;
        int last = 0;

        for (int p : sb.var){
            x = variants.get(p).start - refStart;
            l = x - KMER_SIZE + 1 + offset;

            boolean isSNP = false;
            if (variants.get(p).type == DELETION){
                offset += -1 * variants.get(p).length;
            } else if (variants.get(p).type == INSERTION){
                offset += variants.get(p).length;
            } else if (variants.get(p).type == SNP){
                //offset = offset;    //do nothing
                isSNP = true;
            }
            u = x + offset;

            for (int k = max(l, last);  k < min(u,sb.s.length());    k++){
                if (isSNP){
                    snpFlag[k]= true;
                } else {
                    indelFlag[k] = true;
                }
                last = k;   //don't right true over an over again
            }
        }

        DnaBitString dbs = new DnaBitString(sb.s);

        DnaBitStringToDb dbstd = new DnaBitStringToDb(dbs, kmerDB, kid);
        boolean reachesEnd = (refStart+sb.s.length()) >= myKidDb.getSequenceLength(kid);  //.get(kid).getLength();


        if (DEBUG) System.err.println("writeAllPositions: kid \t"+kid+"\tstart\t"+refStart+"\t: end\t"+reachesEnd+"\t: seq\t"+sb.s);
        dbstd.writeAllPositionsVariants(refStart, reachesEnd, indelFlag, snpFlag);
    }



    /**
     * Takes all combinations so far and permutes one more time on next variant
     * @param prevPermut
     * @param variants
     * @param pieces    String parts that correspond to all variant splice locations; before and after
     *                  Note that this means pieces has one more entry than variants, and index selection is based before/after
     *                  The first piece is PREFIX, then pieces[1] corresponds to variants[0] and so forth
     * @param k         integer index of current variant being processed in variants; used for recursion end
     * @param p         integer index of current String piece being processed
     * @return
     */
    protected StringAndVariants[] recurseVariantCombos(StringAndVariants[] prevPermut, String[] pieces, ArrayList<Variant> variants, int k, int p){
        StringAndVariants[] result = null;

        //2017.06.22  short circuit
        if (variants.size() == 0){
            return prevPermut;
        }

        //In case I ever need it: the first mutation written is the default form
        //So, I suppose, we never have to write result[0] to database

//        if (variants.get(k).start == 10127){
//            int debug = 1;
//        }

        //2017.05.01
        //build multiple variants in one location
        ArrayList<Variant> localVariants = new ArrayList<>();

        localVariants.add(variants.get(k));


//        for (int z = k+1; z < variants.size(); z++){
        // variant k is already written, start at next
        for (int z = k+1; z < variants.size(); z++){
            if (variants.get(z).start == variants.get(k).start){
                localVariants.add(variants.get(z));
            } else {
                break;
            }
        }

        int multiplier = 1;




        for (int z=0; z<localVariants.size(); z++){
            switch (localVariants.get(z).type){
                case SNP:
                    Set<Character> sc = DNAcodes.substituteSNP.
                            get( variants.get(k).insertSequence.charAt(0));
                    multiplier += sc.size();
//                            DNAcodes.substituteSNP.
//                                    get( variants.get(k).insertSequence.charAt(0))
//                                    .size();
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

//        if (pieces[p].equals("CT") || localVariants.get(0).start == 54713|| localVariants.get(0).start == 54711){
//            int debug = 1;
//        }



        int r = 0;
        //Unaltered Variants first
        //write first sequences, unaltered versions
        for (StringAndVariants sb : prevPermut) {
            result[r] = new StringAndVariants(sb.s + pieces[p], prevPermut[0].var);
            r++;
        }



        for (int z=0; z<localVariants.size(); z++) {
            Variant v = localVariants.get(z);
            switch (v.type) {
                case SNP:
                    Set<Character> snps = new HashSet<Character>(DNAcodes.substituteSNP.get(v.insertSequence.charAt(0)));
                    Character first = pieces[p].charAt(0);
                    String snipped = pieces[p].substring(1);  //remove first character


//                    //Write the normal permutation FIRST
//                    for (StringAndVariants sb : prevPermut) {
//                        result[r] = new StringAndVariants(sb.s + pieces[p], prevPermut[0].var);
//                        r++;
//                    }

                    //ASSERT: snps should contain first, otherwise BIG ERROR, probably to do with indexing
                    if (snps.contains(first) && snps.size() > 1) {   //2017.05.10 debug: compensate database errors
                        snps.remove(first);
                    }

                    //non-Permutations using unaltered Strings written above.

                    //Write all other permutations
                    for (Character c : snps) {
                        for (StringAndVariants sb : prevPermut) {
                            result[r] = new StringAndVariants(sb.s + c + snipped, sb.var, k); //SNPs are now written to variants used (var) by (k)
                            r++;
                        }
                    }
                    break;
                case DELETION:
//                    System.err.println("DEBUG: indel is " + v + "\tpiece\t" + pieces[p]);

//                    if (k == -1 || v.length == -1) {
//                        int debug = 1;
//                    }


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

//                    System.err.println("DEBUG: indel is " + v + "\tpiece\t" + pieces[k + 1]);
                    String inserted = v.insertSequence;
                    //result = new StringAndVariants[2 * prevPermut.length];
                    //r = 0;
                    for (StringAndVariants sb : prevPermut) {
//                        result[r] = new StringAndVariants(sb.s + pieces[p], sb.var);
//                        r++;
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
//            System.err.println("k\t"+k);
//            System.err.println("local variants\t"+Arrays.toString(localVariants.toArray()));
//            System.err.println("      variants\t"+Arrays.toString(variants.toArray()));
            return recurseVariantCombos(result, pieces, variants, k + localVariants.size(), p + 1);

        }else
            return result;
    }

    public StringAndVariants[] generateVariants(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns) {

        String[] pieces = new String[ variants.size()+1];
        int tempPos = refStart;
        for(int k=0; k<variants.size(); k++){
            pieces[k] = dns.getSequence(tempPos,variants.get(k).start);
            tempPos = variants.get(k).start;
        }
        //last one
        pieces[variants.size()] = dns.getSequence(tempPos, refStop);

        // pieces now contains the entire sequence broken up by variant locations
        // sequences to be deleted start the after segment

        StringAndVariants[] zeroSB = new StringAndVariants[1];
        zeroSB[0] = new StringAndVariants();
        zeroSB[0].s = pieces[0];
        return recurseVariantCombos(zeroSB,pieces,variants,0,0);
    }

    public StringAndVariants[] generateVariants(Variant[] variants, int refStart, int refStop, DnaBitString dns) {

        ArrayList<Variant> result = new ArrayList<Variant>(Arrays.asList(variants));

        return  generateVariants(result, refStart, refStop, dns);
    }




    protected static String[] generatePieces(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns){
        Integer seqStart = variants.get(0).start;
        String[] pieces = new String[ variants.size()+1];

        pieces[0] = dns.getSequence(refStart,seqStart);

        Integer tempPos = seqStart;

        Integer nextPos = -100; //SENTINEL
        if (1 < variants.size() )         nextPos = variants.get(1).start;

        int p = 1; //pieces index

        //LOOP is weird, because it is looking ahead to see if the NEXT variants match the position
        //this means looking ahead 2, as well as iterating all variables before k++ instead of after
        for(int k=0; k<variants.size(); k++){
            //2017.05.01
            if (nextPos != null && nextPos.equals(tempPos)) { //same position for variant
                //ITERATE

                tempPos = nextPos;
                if (k+2 < variants.size() )
                    nextPos = variants.get(k+2).start;
                else
                    nextPos = null;
                //write last if this is last
                if (k+1 == (variants.size())){
                    pieces[p] = dns.getSequence(tempPos, refStop);
                    p++;
                }
            }
            //end 2017.05.01
            else {
                if (k+1 == variants.size()) {
                    pieces[p] = dns.getSequence(tempPos, refStop);
                } else {
                    pieces[p] = dns.getSequence(tempPos, variants.get(k+1).start);
                }
                //ITERATE
                //tempPos = variants.get(k).start;
                p++;
                if (k<variants.size()-1){
                    seqStart = variants.get(k+1).start;
                    tempPos = nextPos;
                    if (k+2 < variants.size())
                        nextPos = variants.get(k+2).start;
                    else
                        nextPos = null;

                } else {
                    seqStart = null;
                    tempPos = null;
                }
            }
        }
        return pieces;
    }



    /**
     * //TODO
     * This function would generate only the sequence that contains all variants included
     * @param variants
     * @param refStart
     * @param refStop
     * @param dns
     * @return
     */
    public static StringAndVariants[] generateSequenceAllVariants(ArrayList<Variant> variants, int refStart, int refStop, DnaBitString dns) {
        if (refStart == 83743){
            int debug = 1;
        }

        String[] pieces = generatePieces(variants,refStart,refStop,dns);

        // pieces now contains the entire sequence broken up by variant locations
        // sequences to be deleted start the after segment

        StringAndVariants[] zeroSB = new StringAndVariants[1];
        zeroSB[0] = new StringAndVariants();
        zeroSB[0].s = pieces[0];
        return recurseVariantCombosForceVariant(zeroSB,pieces,variants,0,1);
    }

    /**
     * 2017-05-01 took previous recurseVariantCombos and modified to only include the variants
     * @param prevPermut
     * @param pieces
     * @param variants
     * @param k
     * @param p
     * @return
     */
    protected static StringAndVariants[] recurseVariantCombosForceVariant(StringAndVariants[] prevPermut, String[] pieces, ArrayList<Variant> variants, int k, int p) {
        StringAndVariants[] result = null;

        //In case I ever need it: the first mutation written is the default form
        //So, I suppose, we never have to write result[0] to database

        if (variants.get(k).start == 10127) {
            int debug = 1;
        }


        ArrayList<Variant> localVariants = new ArrayList<>();
        localVariants.add(variants.get(k));

        for (int z = k + 1; z < variants.size(); z++) {
            if (variants.get(z).start == variants.get(k).start) {
                localVariants.add(variants.get(z));
            } else {
                break;
            }
        }

        int multiplier = 0; //no unmodified sequence
        for (int z = 0; z < localVariants.size(); z++) {
            switch (localVariants.get(z).type) {
                case SNP:
                    Set<Character> subs = DNAcodes.substituteSNP.get(variants.get(k).insertSequence.charAt(0));
                    subs.remove(pieces[p].charAt(0));
                    multiplier += subs.size();
                    break;
                case INSERTION:
                case DELETION:
                    multiplier += 1;
                    break;
                default:
                    break;
            }
            //for a sequence inserted.length() > 120, we need another position flagged --> ignoring
        }


//        if (localVariants.get(0).start == 54713) {
//            int debug = 1;
//        }

        result = new StringAndVariants[multiplier * prevPermut.length];

        int r = 0;
        //write first sequences  (
        for (int m = 0;
             m < multiplier;
             m++) { //must repeat to fill array
            for (int z=0; z < prevPermut.length; z++){
            //for (StringAndVariants sb : prevPermut) {
                result[r] = new StringAndVariants(prevPermut[z].s, prevPermut[z].var);
                r++;
            }
        }

        r=0;
        for(int z=0; z<localVariants.size(); z++) {
            Variant v = localVariants.get(z);
            switch (v.type) {
                case SNP:
                    Set<Character> snps = new HashSet<Character>(DNAcodes.substituteSNP.get(v.insertSequence.charAt(0)));
                    Character first = pieces[p].charAt(0);
                    String snipped = pieces[p].substring(1);  //remove first character

                    //SNPS code could include orginal position or not
                    if (snps.contains(first)) {
                        snps.remove(first);
                    }

                    //Write all other permutations
                    for(int m=0; m < prevPermut.length; m++) { //write to results created for this positional variant
                        for (Character c : snps) {
                            //SNPs are now written to variants used (var)
                            result[r] = new StringAndVariants(result[r].s + c + snipped, result[r].var,
                                    variants.indexOf(v)); // BUG 2017.05.05 write index of variants, not localVariants
                            r++;
                        }
                    }
                    break;
                case DELETION:
                    if (k == -1 || v.length == -1) {
                        int debug = 1;
                    }

                    String deleted;



                    if (v.length >= pieces[p].length()) {
                        // what if deletion covers next variant!
                        deleted = "";
                        System.err.println("\tWARNING\tDeletion length overlaps next Variant, truncating gap.\tposition:\t"+v.start+"\tlength:\t"+v.length);
                        System.err.println("\tWARNING\tNote this acceptable because gaps are not looked up, the sequences on either side are.");
                        System.err.println("\tWARNING\tLikewise, this behavior is undefined.  What if a DELETION eclispes another DELETION?");
                    } else {
                        deleted = pieces[p].substring(v.length); //delete characters

                    }
//                    for (StringAndVariants sb : result) { //write to all results
                    for (int m=0; m < prevPermut.length; m++){
                        result[r] = new StringAndVariants(
                                prevPermut[m].s + deleted, prevPermut[m].var,
                                variants.indexOf(v)); // BUG 2017.05.05 write index of variants, not localVariants
                        r++;
                    }
                    break;
                case INSERTION:
                    String inserted = v.insertSequence;
//                    for(int m=0; m < prevPermut.length; m++) { //write to results created for this positional variant
                    for (int m=0; m < prevPermut.length; m++){
                        result[r] = new StringAndVariants(prevPermut[m].s+ inserted + pieces[p], prevPermut[m].var,
                                variants.indexOf(v)); // BUG 2017.05.05 write index of variants, not localVariants
                        r++;
                    }
                    break;
                default:
                    System.err.println("    WARNING:  (SHOULD NEVER HAPPEN)  Permutator.recurvsiveVariantCombos() has unknown variant : " + v.type);
                    for(int m=0; m < prevPermut.length; m++) { //write to results created for this positional variant
                        result[r] = new StringAndVariants(prevPermut[m].s + pieces[p], prevPermut[m].var,z);
                        r++;
                    }
                    break;
            }//end switch
        }//end for

        if (k+localVariants.size() < variants.size() )
            return recurseVariantCombosForceVariant(result, pieces, variants, k+localVariants.size(), p+1);
        else
            return result;


    }


    /**
     * Variants such as multiple SNPs are found in the databsae at the same position
     * This function rectifies the problem.
     *
     * ONLY HANDLES SNP at the moment
     *
     * @param av
     * @return
     */
    public static ArrayList<Variant> combineSamePositions(ArrayList<Variant> av) throws UnsupportedOperationException{
        //ArrayList<Variant> result = new ArrayList<Variant> ();
        Set<Character> snps;
        ArrayList<Variant> result = av;
        for (int k=0; k < (av.size() - 1); k++){  //if something is removed, then size changes

            if (av.get(k).type == SNP) {
                snps = new HashSet<Character>();
                snps.addAll(DNAcodes.substituteSNP.get(av.get(k).insertSequence.charAt(0)));
                int j = k + 1;
                //snps.addAll(DNAcodes.substituteSNP.get(av.get(k)));
                while (j < av.size() && av.get(k).start == av.get(j).start ) {

                    if (av.get(j).type == SNP) {

                        snps.addAll(DNAcodes.substituteSNP.get(av.get(j).insertSequence.charAt(0)));

                    } else {
                        System.err.println("Unexpected multiple variants other than snps at single location " + av.get(k).start);
                        System.err.println("Variants " + av);
                        throw new UnsupportedOperationException("Multiple variants other than SNP at a single location.");
                    }
                    j++;
                }

                if (j > k+1){  //something founds
                    System.out.println("j:\t"+j+"\tk\t"+k+"\tz\t"+(j-1)+"\tsize\t"+av.size());
                    av.get(k).insertSequence = Character.toString(DNAcodes.findMatch(snps));
                    //most moronic remove bug ever
//                    for (int z = j-1; z > k; k--){  //reverse so indexes dont change
//                        av.remove((Variant) av.get(z));
//                    }

                    result = new ArrayList<Variant>();
                    for(int z=0; z<av.size(); z++){
                        //if (z < j && z > k) //skip
                        if (z >= j || z <= k)
                            result.add(av.get(z));
                    }

                    av = result;
                }

            }//end if SNP
        } //end for k

        return result;
    }


}
