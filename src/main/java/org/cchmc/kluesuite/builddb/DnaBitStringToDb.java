package org.cchmc.kluesuite.builddb;

import org.cchmc.kluesuite.klat.*;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.wildklat.PrefixPair;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by osboxes on 24/09/16.
 *
 * 2016.11.22
 * This class was modified to allow ONE METHODOLOGY of handling SNPs, which is abandoned in favor of VariantDatabase1.
 * May contain code related to VariantDatabase1 as well.  This is a mess.
 *
 * 2016.12.16   Fixed.  SNP function marked as deprecated.  New functions.
 *              FUTURE: need to debug writeAlll() suite of functions; modified the code for corrections
 *2017.03.31    Adding Forward only flag for KLUEforward
 *
 * This class allows a DnaBitString to be written to the database in optimized manner.  Note that the database is still
 * hit with random access patterns, as such, this is often used with MemoryKlueHeapFastImportArray, then dumped to disk
 */
public class DnaBitStringToDb extends DnaBitString {

    KLUE klue;
    int kid;
    AlignmentKLAT1 al;
    int debugKmerWritten=0;
    boolean forwardOnly;

    public DnaBitStringToDb( DnaBitString notAcopy, KLUE writeToMe, int KID){
        super(notAcopy);
        klue = writeToMe;
        kid = KID;
        al = null;
        forwardOnly = false;
    }

    public DnaBitStringToDb( DnaBitString notAcopy, KLUE writeToMe, int KID, boolean forwardOnly){
        super(notAcopy);
        klue = writeToMe;
        kid = KID;
        al = null;
        this.forwardOnly = forwardOnly;
    }

    /**
     * Generates a set of positions in string (start at 0, from left)
     * That are invalid kmers, so they can be skipped.
     * @return
     */
    HashSet<Integer> excludedPositions(){
        HashSet<Integer> result = new HashSet<Integer>();
        int start, stop;
        for (int key : exceptions.keySet()){
            start = Math.max(0, key - Kmer31.KMER_SIZE + 1);       //INCLUSIVE
            for (int k = start; k <= key; k++){             //key is inclusive upper limit
                result.add(k);
            }
        }
        return result;
    }

    /**
     * Generates a set of positions in string (start at 0, from left)
     * That are invalid kmers, so they can be skipped.
     * @return
     */
    HashSet<Integer> excludedPositionsSNPallowed(){
        HashSet<Integer> result = new HashSet<Integer>();
        int start, stop;
        for (int key : exceptions.keySet()){
            if ( !DNAcodes.all.contains(exceptions.get(key))) {
                start = Math.max(0, key - Kmer31.KMER_SIZE + 1);       //INCLUSIVE
                for (int k = start; k <= key; k++) {             //key is inclusive upper limit
                    result.add(k);
                }
            }
        }
        return result;
    }




    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP){
        debugKmerWritten++;

//        System.out.println("BUCK  ROGERS");
        //System.err.println("Writing\t"+word);

        Kmer31 revWord = word.reverseStrand();
        Position loc = new Position(KID, pos);
        Position revLoc = new Position(KID, pos + Kmer31.KMER_SIZE_MINUS_ONE );
        revLoc.setFlag(Position.REVERSE, true);
        if (START) {
            loc.setFlag(Position.START, true);
            revLoc.setFlag(Position.STOP, true);
        }
        if (STOP) {
            loc.setFlag(Position.STOP, true);
            revLoc.setFlag(Position.START, true);
        }
        klue.append(word.toLong(), loc.toLong());
        if (!forwardOnly)   klue.append(revWord.toLong(), revLoc.toLong());
    }

    /**
     * Added with variantklue.PermutatorOLD :: includes Indel flag
     * @param word
     * @param KID
     * @param pos
     * @param START
     * @param STOP
     * @param INDEL
     */
    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP, boolean INDEL, boolean SNP){
        debugKmerWritten++;

        Position loc = new Position(KID, pos);
        Position revLoc = new Position(KID, pos + Kmer31.KMER_SIZE -1 );

        if (START) {
            loc.setFlag(Position.START, true);
            revLoc.setFlag(Position.STOP, true);
        }
        if (STOP) {
            loc.setFlag(Position.STOP, true);
            revLoc.setFlag(Position.START, true);
        }
        if (INDEL){
            loc.setFlag(Position.INDEL, true);
            revLoc.setFlag(Position.INDEL, true);
        }
        if (SNP){
            loc.setFlag(Position.SNP, true);
            revLoc.setFlag(Position.SNP, true);
        }
        //System.err.println("\t\t\t\t"+word+"\tloc\t"+loc);
        klue.append(word.toLong(), loc.toLong());

        // Only write reverse if allowed
        if (!forwardOnly) {
            Kmer31 revWord = word.reverseStrand();
            revLoc.setFlag(Position.REVERSE, true);
            klue.append(revWord.toLong(), revLoc.toLong());
        }
    }



    //this is broken
//    public long firstKmer() {
//        if (NUM_BITS == 0){
//            return -1L;
//        } else {
//
//            int offset = NUM_BITS % 64;
//            int indexLongs = (NUM_BITS - 1) / 64; //bug: did not say BITs -1, just NUM_BITS
//            long result;
//
//            if (offset == 62) {
//                result = compressed.getLongAtIndex(indexLongs);
//            } else if (offset == 64) {
//                result = compressed.getLongAtIndex(indexLongs) >> 2;
//            } else {
//                if (indexLongs >= compressed.getLengthLongArray()) {
//                    System.err.println("WARNING :: DnaBitString for kid " + kid + " had unexpected length\t" +
//                            compressed.getLengthLongArray() + "\twith NUM_BITS + \t" + NUM_BITS);
//                    System.err.println("WARNING :: DnaBitString for kid " + kid + " should be considered corrupt ");
//                    indexLongs = compressed.getLengthLongArray() - 1;
//                }
//                result = compressed.getLongAtIndex(indexLongs) << (62 - offset);
//                result += compressed.getLongAtIndex(indexLongs - 1) >> offset;
//            }
//            return result;
//        }
//    }


    public boolean checkAlignment(KidDatabaseMemory myKidDB) {
        String query = getSequence(50,110);
        al = new AlignmentKLAT1( query,"Test_verify",klue);

        al.pollKmersForPositions();
        al.calculateAlignmentTables();
        al.calculateSeeds();

        SmithWatermanAdvanced swa;

        boolean result = false;
        for (Seed s : al.fwdSeeds) {
            if (result) break;
            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
            //swa.printPrettyBestResults(System.out);
            ArrayList<PartialAlignment1> pas = swa.bestAlignments();
            for (PartialAlignment1 p : pas){
                if (p.getPercentIdentity() > 0.99){ //instead of ==1.00, we use > 0.99, cuz 100 possibilities, same thing.  No float errors
                    result = true;
                    break;
                }
            }
        }
        for (Seed s : al.revSeeds) {
            if (result) break;
            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
            //swa.printPrettyBestResults(System.out);
            ArrayList<PartialAlignment1> pas = swa.bestAlignments();
            for (PartialAlignment1 p : pas){
                if (p.getPercentIdentity() > 0.99){ //instead of ==1.00, we use > 0.99, cuz 100 possibilities, same thing.  No float errors
                    result = true;
                    break;
                }
            }
        }


        if (!result) System.err.println("WARNING :: Apparently, sequence "+kid+" alignment to self failed.  Suspect database write failure");
        return result;
    }

    /**
     * Note only more accurate, but faster.
     *
     * @param myKidDB
     * @return
     */
    public boolean checkAlignment2(KidDatabaseMemory myKidDB) {
        int from = 50;
        int to = 110;

        String query = getSequence(from,to);
        int length = query.length();
        int alignTo = length - Kmer31.KMER_SIZE + 1; //should be 30 less because of K=31

        al = new AlignmentKLAT1( query,"Test_verify",klue);
        al.pollKmersForPositions();
        al.calculateAlignmentTables();
        al.calculateSeeds();

        boolean result = false;
        for (Seed s : al.fwdSeeds) {
            if (result) break;
            if (s.queryStart == 0 && s.queryEnd == alignTo) {
                result = true;
                break;
            }
        }

        if (!result) System.err.println("WARNING :: Apparently, sequence "+kid+" alignment to self failed.  Suspect database write failure");
        return result;
    }



    public void alignmentStringDump() {
        System.out.println("\tQUERY\t"+al.queryName);
        System.out.println("\t"+al.query);
        System.out.println("FORWARD SEEDS\t");
        for (Seed s : al.fwdSeeds) {
            System.out.println("\t"+s);
        }
        System.out.println("\tREVERSE SEEDS");
        for (Seed s : al.revSeeds) {
            System.out.println("\t"+s);
        }
//        System.out.println("\t");
//        System.out.println("\t");
//        System.out.println("\t");
//        System.out.println("\t");
//        System.out.println("\t");
    }

//    public static void main(String[] args) {
//
//        if (args.length != 2) {
//            System.err.println("Proper syntax is ' java -cp classpath/kluesuite.jar -Xmx[arg3]m [program] [arg1 : input queries filename] [arg2 : DB location]'");
//            exit(0);
//        }
//    }



    public void writeAllPositions(){
    //        if (NUM_BITS != null) System.out.println("NUM_BITS is null");
    //        if (compressed != null) System.out.println("compressed is null");
    //        if (exceptions != null) System.out.println("exceptions is null");

        HashSet<Integer> skips = excludedPositions();
        int nextIndex;
        int k;
        long val;
        //we are going to parse to the next position in this loop, hence NOT (NUM_BITS/2)-Kmer31.KMER_SIZE - 1//long val = firstKmer();

//        System.err.println("PARSING \t"+NUM_BITS/2);

        val = 0;
        for (k=0; k < (NUM_BITS /2); k++){
            val = val % KlueDataImport.twoE60;
            val = val << 2;
            if (compressed.get(bitA(k))) val += 2;  //bug was k+1
            if (compressed.get(bitB(k))) val += 1;  //bug was k+1

//            System.out.println("k:\t"+k+"\tkmer\t"+new Kmer31(val));
            if ( k >= (Kmer31.KMER_SIZE-1) &&       // if k == 30, we have 31 letters added
                    k < (NUM_BITS /2) -1 &&              //save last write for end
                    !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){
//                System.err.println("adding\t"+k+"\t"+new Kmer31(val));
                recordForwardAndReverseToStore(new Kmer31(val),kid,k-(Kmer31.KMER_SIZE-1), (k-(Kmer31.KMER_SIZE-1)==0), false);
            }

            if (k % 1000000 == 999999){
                System.out.println("DnaBitStringToDb : processing "+(k+1)/1000000+" millions");
            }
        }


        //Record last position
        k--;
        if ( !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){
            recordForwardAndReverseToStore(new Kmer31(val), kid, k-(Kmer31.KMER_SIZE-1), (k-(Kmer31.KMER_SIZE-1)==0), true);
        }
    }


    /**
     * same algorithm, but writes to stdout
     */
    public void simulateWriteAllPositions(){

        HashSet<Integer> skips = excludedPositions();
        int nextIndex;
        int k;
        long val;
        //we are going to parse to the next position in this loop, hence NOT (NUM_BITS/2)-Kmer31.KMER_SIZE - 1//long val = firstKmer();

        val = 0;
        String skippers = "";
        int stop = 110;
        int start = 50;

        System.out.println(getSequence(0,stop));
        //skippers += " ";    //errror

        for (k=0; k < (NUM_BITS /2); k++){
            val = val % KlueDataImport.twoE60;
            val = val << 2;
            if (compressed.get(bitA(k))) val += 2;  //bug was k+1
            if (compressed.get(bitB(k))) val += 1;  //bug was k+1

            if ( k >= (Kmer31.KMER_SIZE-1) &&       // if k == 30, we have 31 letters added
                    k < (NUM_BITS /2) -1 &&              //save last write for end
                    !skips.contains(k-(Kmer31.KMER_SIZE-1)) &&
                    start <= k && k < stop){
                Kmer31 tmp = new Kmer31(val);
                System.out.println(skippers+tmp);
                skippers += " ";
            }
        }


        //Record last position
        k--;
        if ( !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){
            Kmer31 tmp = new Kmer31(val);
            System.out.println(skippers+tmp);
        }
    }


    /**
     * Where SNP are present (as represented in klue.DNAcodes)
     * Write multiple copies of Kmers.  Only do this for up to SNP of width One
     * Thus,  if codes is ATCNNCGC
     * ATCN will be written and NCGC will be written
     * but not CNNC (with two consecutive SNP)
     *
     * CURRENTLY DEPRECATED
     */
//    public void writeAllPositionsIncludingSnp(){
//        HashSet<Integer> skips = excludedPositionsSNPallowed();
//        int nextIndex;
//        int k;
//        long val;
//        //we are going to parse to the next position in this loop, hence NOT (NUM_BITS/2)-Kmer31.KMER_SIZE - 1//long val = firstKmer();
//        val = 0;
//
//        int numberSNPs = 0;
//        int numberBadChar = 0;
//        ArrayList<Integer> SNPpositions = new ArrayList<Integer>();
//        ArrayList<Integer> badPositions = new ArrayList<Integer>();
//
//        for (k=0; k < (NUM_BITS/2); k++) {
//
//            //ADD letter coming into reading frame
//            val = val % KlueDataImport.twoE60;
//            val = val << 2;
//            if (compressed.get(bitA(k))) val += 2;  //bug was k+1
//            if (compressed.get(bitB(k))) val += 1;  //bug was k+1
//            // NOTE: if the character is in exceptions, it encodes as 00 here and thus adds 0,
//            // enabling the writeSNPs to work
//
//            //reading frame of current Kmer
//            int first = k - Kmer31.KMER_SIZE + 1;   // if k == 30, we have 31 letters added, and the position is 0
//            int last = k;   //INCLUSIVE
//
//            if (exceptions.containsKey(k)){
//                if (   DNAcodes.all.contains(exceptions.get(k))   ){
//                    numberSNPs++;
//                    SNPpositions.addAndTrim(k);
//                } else {
//                    numberBadChar++;
//                    badPositions.addAndTrim(k);
//                }
//            }
//
//            //REmove letter passing out of reading frame
//            //Did SNP or bad char pass out of reading frame?
//
//            if (SNPpositions.size() > 0 &&              //short circuit
//                    SNPpositions.get(0) == (first-1) ){
//                //int c = SNPpositions.get(0);
//                //BUG: cannot distinguish between remove(E) and remove(int), where E = Integer
//                SNPpositions.remove( 0 );
//                numberSNPs--;
//            }
//            if (badPositions.size() > 0 &&              //short circuit
//                    badPositions.get(0) == (first-1) ){
//                badPositions.remove( 0 );
//                numberBadChar--;
//            }
//
//            if (k %100 == 0){
//                int breakpoint;
//                breakpoint = k /1000;
//            }
//
//            if ( first >= 0 && !skips.contains(first) ) {
//                //If we have a full size KMER, we can write it, unless we are forbidden by skips
//
//                if(numberBadChar == 0){
//                    boolean start = (first == 0);
//                    boolean end = (last == (NUM_BITS/2-1));
//                    if(numberSNPs == 0){
//                        recordForwardAndReverseToStore(new Kmer31(val),kid,first, start, end);
//                    } else if (numberSNPs == 1){
//                        writeSNPs(val, SNPpositions.get(0), SNPpositions.get(0)-first, kid, first, start, end);
//                    }
//
//                    //else  SKIP, do not write Kmers
//                }
//            }
//        }
//    } // END writeAllPositionsIncludingSnp


//    /**
//     * What is this?
//     * @param val
//     * @param SNPindex
//     * @param relativeOffset
//     * @param kid
//     * @param pos
//     * @param start
//     * @param finish
//     */
//    void writeSNPs(long val, int SNPindex, int relativeOffset, int kid, int pos, boolean start, boolean finish){
//        //QUESTION do we want to include a SNP flag in Position?
//            //IF so, addAndTrim that code here
//
//        int offset = (Kmer31.KMER_SIZE - 1 - relativeOffset) * 2;
//        HashSet<Character> subs = DNAcodes.equivalency.get( exceptions.get(SNPindex) );
//        if (subs.contains('A')){
//            recordForwardAndReverseToStore(new Kmer31(val + (3L << offset)), kid, pos, start, finish);
//        }
//        if (subs.contains('G')){
//            recordForwardAndReverseToStore(new Kmer31(val + (2L << offset)), kid, pos, start, finish);
//        }
//        if (subs.contains('C')){
//            recordForwardAndReverseToStore(new Kmer31(val + (1L << offset)), kid, pos, start, finish);
//        }
//        if (subs.contains('T')){
//            recordForwardAndReverseToStore(new Kmer31(val), kid, pos, start, finish);
//        }
//        return;
//    }

    public int numSNPs() {
        return excludedPositionsSNPallowed().size();
    }

    /**
     * Convert all N between two other N's to an X, so it won't be a SNP
     */
    public void eliminateRunsOfN(){
        for( int el : exceptions.keySet() ){
            if ( exceptions.containsKey(el-1) && exceptions.containsKey(el+1) ){
                if (    (exceptions.get(el-1) == 'N' || exceptions.get(el-1) == 'X')
                    &&
                        (exceptions.get(el+1) == 'N' || exceptions.get(el+1) == 'X')){
                    exceptions.put(el,'X');
                }
            }
        }
    }

    public void setKlue(KLUE k){
        klue = k;
    }


    /**
     * Companion function to match variantklue.PermutatorOLD
     *
     * TODO : verify offset is used properly here.  I think the idea is that we skip several coordinates due to first position
     *
     * @param offset
     * @param endsFinal
     * @param indels
     * @param snps
     */
    public void writeAllPositionsVariants(int offset, boolean endsFinal, boolean[] indels, boolean[] snps) {
        HashSet<Integer> skips = excludedPositions();
        int nextIndex;
        int k;
        long val;
        //we are going to parse to the next position in this loop, hence NOT (NUM_BITS/2)-Kmer31.KMER_SIZE - 1//long val = firstKmer();

        val = 0;
        for (k=0; k < (NUM_BITS /2); k++){
            val = val % KlueDataImport.twoE60;
            val = val << 2;
            if (compressed.get(bitA(k))) val += 2;  //bug was k+1
            if (compressed.get(bitB(k))) val += 1;  //bug was k+1

            //This is goofy -- we addAndTrim every single base (of course), but we only write 31-mers
            if ( k >= (Kmer31.KMER_SIZE-1) &&       // if k == 30, we have 31 letters added
                    k < (NUM_BITS /2) -1 &&              //save last write for end
                    !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){
                recordForwardAndReverseToStore(new Kmer31(val),                         //sequence
                                                    kid,                                //kid
                                                    offset+k-(Kmer31.KMER_SIZE-1),      //start position
                                                    (offset+k-(Kmer31.KMER_SIZE-1))==0, //includes start of sequence
                                                    false,                              //includes end of sequence
                                                    indels[k-(Kmer31.KMER_SIZE-1)],     //includes indel
                                                    snps[k-(Kmer31.KMER_SIZE-1)]        //includes snp
                                              );
            }
//    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int var, boolean START, boolean STOP, boolean INDEL, boolean SNP){
        }

        k--;
        //Record last position
        //We know that the sequence is long enough to have an end --> because we are handling variant windows of +/- 30 min


        if ( k >= Kmer31.KMER_SIZE && !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){
            recordForwardAndReverseToStore(
                    new Kmer31(val),
                    kid,
                    offset+k-(Kmer31.KMER_SIZE-1),
                    (offset+k-(Kmer31.KMER_SIZE-1))==0, endsFinal,
                    indels[k-(Kmer31.KMER_SIZE-1)],
                    snps[k-(Kmer31.KMER_SIZE-1)]);
        }

    }

    public void writeAllPositions(KLUE startEndKlue) {
        if (NUM_BITS < 31) return;

        HashSet<Integer> skips = excludedPositions();
        int nextIndex;
        int k;
        long val;
        //we are going to parse to the next position in this loop, hence NOT (NUM_BITS/2)-Kmer31.KMER_SIZE - 1//long val = firstKmer();

        val = 0;
        for (k=0; k < (NUM_BITS /2); k++){
            val = val % KlueDataImport.twoE60;
            val = val << 2;
            if (compressed.get(bitA(k))) val += 2;  //bug was k+1
            if (compressed.get(bitB(k))) val += 1;  //bug was k+1

            if ( k >= (Kmer31.KMER_SIZE-1) &&       // if k == 30, we have 31 letters added
                    k < (NUM_BITS /2) -1 &&              //save last write for end
                    !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){

                int pos=k-(Kmer31.KMER_SIZE_MINUS_ONE);
                Kmer31 word = new Kmer31(val);
                Kmer31 revWord = word.reverseStrand();
                Position loc = new Position(kid, k-(Kmer31.KMER_SIZE-1));
                Position revLoc = new Position(kid, pos + Kmer31.KMER_SIZE_MINUS_ONE );
                revLoc.setFlag(Position.REVERSE, true);

                boolean START = pos==0;
                boolean STOP = pos == (NUM_BITS/2)-1;

                if (START) {
                    loc.setFlag(Position.START, true);
                    revLoc.setFlag(Position.STOP, true);
                    startEndKlue.append(word.toLong(),loc.toLong());
                    startEndKlue.append(revWord.toLong(),revLoc.toLong());
                }
                if (STOP) {
                    loc.setFlag(Position.STOP, true);
                    revLoc.setFlag(Position.START, true);
                    startEndKlue.append(word.toLong(),loc.toLong());
                    startEndKlue.append(revWord.toLong(),revLoc.toLong());
                }
                klue.append(word.toLong(), loc.toLong());
                klue.append(revWord.toLong(), revLoc.toLong());
                long debug = word.toLong();
            }

            if (k % 1000000 == 999999){
                System.out.println("DnaBitStringToDb : processing "+(k+1)/1000000+" millions");
            }
        }


        //Record last position
        k--;
        if ( !skips.contains(k-(Kmer31.KMER_SIZE-1)) ){
            recordForwardAndReverseToStore(new Kmer31(val), kid, k-(Kmer31.KMER_SIZE-1), (k-(Kmer31.KMER_SIZE-1)==0), true);
            Position loc = new Position(kid, k-(Kmer31.KMER_SIZE-1));
            loc.setFlag(Position.STOP,true);
            startEndKlue.append(val,loc.toLong());
        }

    }
}
