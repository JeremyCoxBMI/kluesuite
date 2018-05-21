package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.TimeTotals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.System.exit;

/**
 * This class is written to do the job of importing DNA sequences into klue.
 * It is supplied with existing KidDatabaseMemory for sequences to be read.
 * This is an abstract class that must be extended to a specific store type.
 *
 * 2016-08-15   v2.0    Imported from v1.6.  WriteToStore modified to use iterator.
 */

public class KlueDataImport {

    public static long twoE60 = 1073741824L * 1073741824L;  //(i.e. 2^30 * 2^30)
    public static Kmer31 NEGONE = new Kmer31(-1L);
    public boolean debug = false;
    public KLUE klue;
    public KidDatabaseMemory myKidDB;
    public boolean squelchVerify = false;
    public boolean squelchOutput = false;

    /**
     * In hindsight, DNABitString may be very suboptimal for performance.  This turns on alternative, faster mode.
     */
    public boolean fastWriteMode = false;

    //global variable to minimize argument passing
    private long totalLengthRecorded;


    //METHODS FOR CHILD TO IMPLEMENT
    public KlueDataImport( KLUE klue, KidDatabaseMemory myKidDB){
        this.klue = klue;
        this.myKidDB = myKidDB;
    }

    protected KlueDataImport( KidDatabaseMemory myKidDB){
        this.myKidDB = myKidDB;
    }

    //IMPLEMENTED METHODS

    /**
     * writes a string DNA sequence to the store as multiple Kmer31
     * @param currKID
     * @param currSeq
     * @param tt    TimeTotals class that will be paused() and unPaused() in order to track time spent access KLUE
     * @return
     */

    public Kmer31 writeToStore(int currKID, String currSeq, TimeTotals tt){
        TimeTotals trackWrite = new TimeTotals();
        trackWrite.start();
        Kmer31 result = null;

        DnaBitString dna = new DnaBitString(currSeq);

        DnaBitString.myIterator it = (DnaBitString.myIterator) dna.iterator();
        Kmer31 curr;

        for (int k=0; k < it.length(); k++){
            curr = it.next();
            if (curr != null){
                if (result == null){
                    result = new Kmer31(curr);
                }

                if ( k ==0 ){
                    if (k == (it.length()-1) ){
                        recordForwardAndReverseToStore(curr, currKID, k, true, true, tt);  //both start and end, how exciting
                    } else {
                        recordForwardAndReverseToStore(curr, currKID, k, true, false, tt);
                    }
                } else if (k == (it.length()-1) ){
                    recordForwardAndReverseToStore(curr, currKID, k, false, true, tt);
                } else {
                    recordForwardAndReverseToStore(curr, currKID, k, false, false, tt);
                }
            }


        }

        trackWrite.stop();
        long output = trackWrite.timePassed();
        if (!squelchOutput) System.out.format(" Length       \t%d    \tTime(ns)\t%d\tRate(kmer/s)\t%d\n", currSeq.length(), output, (currSeq.length()*1000000000L)/(output) );
        return result;

    } // END writeToStore


    public void recordForwardAndReverseToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP, TimeTotals tt){
        if (debug){ System.out.println("\tputting this kmer onto store :: "+word.toString());}
        Kmer31 revWord = word.reverseStrand();
        Position loc = new Position(KID, pos);
        Position revLoc = new Position(KID, pos + Kmer31.KMER_SIZE -1 );
        revLoc.setFlag(Position.REVERSE, true);
        if (START) {
            loc.setFlag(Position.START, true);
            revLoc.setFlag(Position.STOP, true);
        }
        if (STOP) {
            loc.setFlag(Position.STOP, true);
            revLoc.setFlag(Position.START, true);
        }
        tt.pause();
        klue.append(word.toLong(), loc.toLong());
        klue.append(revWord.toLong(), revLoc.toLong());
        tt.unPause();
    }

    /**
     * This imports an FNA file into the store, presuming of course
     * 		store is initialized correctly and client is talking
     * 		KidDatabaseMemory is already built
     * 		uses TimeTotals class to track time expenditure for efficiency
     *
     * @param filename		file to be imported
     * @param skip			Either "" or name of sequence to skip to in file, for resuming
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void readFNAfileSKIPto( String filename, String skip) throws FileNotFoundException, IOException{
        TimeTotals tt = new TimeTotals();
        tt.start();
        if (!squelchOutput) tt.systemOutPrintln();

        totalLengthRecorded = 0L;
        int currentKID = myKidDB.getMaxKid();
        String currentSeq = "";
        boolean ignore = true; //do not write empty sequence to database
        boolean skipping = true;
        if(skip.equals("")){
            skipping = false;
        }

        Kmer31 firstEntry = null;

        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for(String line; (line = br.readLine()) != null; ) {

                if(debug){System.out.println("Single line:: "+line);}

                // if blank line, it does not count as new sequence
                if (line.trim().length() == 0){
                    if(debug){System.out.println("           :: blank line detected  ");}
                    if (!skipping) {
                        if (!ignore) {
                            processSequence(tt, currentKID, currentSeq);
                        }
                    }
                    ignore = true;

                    // if line starts with ">", then it is start of a new reference sequence
                } else if( line.charAt(0) == '>'){
                    if(debug){System.out.println("           :: new entry detected  ");}
                    // save previous iteration to database
                    if (skipping && skip.equals(line.trim())){
                        if (!squelchOutput) System.out.println("\tFound the skipto line :: "+line);
                        skipping = false;
                        //reset timer
                        tt.start();
                    }

                    if (!skipping){
                        if (!ignore){
                            processSequence(tt, currentKID, currentSeq);
                        }

                        // initialize next iteration
                        currentKID = myKidDB.indexOf(line.trim());

                        //BUG  solved 2016-11-17
                        if (currentKID == -1){
                            myKidDB.add( new Kid(line.trim()));
                            currentKID = myKidDB.indexOf(line.trim());
                        }
                        currentSeq ="";
                        ignore = false;
                    }

                    if (!squelchOutput) System.out.println("\tProcessing Ref Seq :: "+line);
                    if (!squelchOutput) tt.systemOutPrintln();

                } else {
                    if (!skipping) { currentSeq += line.trim(); }
                }

            } //end for

            br.close();

            if (!ignore){
                processSequence(tt, currentKID, currentSeq);
            }
        }
    }


    /**
     * This function is a helper funtion to readFNAfileSKIPto
     * This allows overriding this method rather than the whole function.
     * @param tt
     * @param currentKID
     * @param currentSeq
     */
    private void processSequence(TimeTotals tt, int currentKID, String currentSeq){
        if (!squelchOutput) System.out.println("putting this sequence onto store :"+currentSeq.length()+": "+currentSeq.substring(0,Math.min(currentSeq.length(), 100)));

        Kmer31 firstEntry = null;
        if ( fastWriteMode ) {
            firstEntry = writeToStoreFast(currentKID, currentSeq, tt);
        } else {
            firstEntry = writeToStore(currentKID, currentSeq, tt);
        }
        totalLengthRecorded += currentSeq.length();
        long output = tt.timePassedFromStart();
        if (!squelchOutput) System.out.format(" Running Total\t%d  \tTime(ns)\t%d\tRate(kmer/s)\t%d\n", totalLengthRecorded, output, (totalLengthRecorded*1000000000L)/(output) );
        if (firstEntry == null){
            System.err.println("ERROR: no Kmer31 was found in sequence.  ABORT");
            exit(1);
        } else {
            if (!squelchVerify) System.out.println("\nVerifying that Kmer31 "+firstEntry+"\n\t\t was written to DB as KID: "+currentKID+"\n"+new PositionList(klue.get(firstEntry.toLong())) );
        }
    }

    public void recordTranscriptomeForwardOnlyToStore(Kmer31 word, int KID, int pos, boolean START, boolean STOP, TimeTotals tt){
        if (debug){ System.out.println("\tputting this kmer (forward only) onto store :: "+word.toString());}
        Position loc = new Position(KID, pos);

        loc.setFlag(Position.TRANSCRIPTOME, true);
        if (START) {
            loc.setFlag(Position.START, true);
        }
        if (STOP) {
            loc.setFlag(Position.STOP, true);
        }
        tt.pause();
        klue.append(word.toLong(), loc.toLong());
        tt.unPause();
    }

    /**
     * Placeholder function for needed functionality
     * @param filename
     * @param skip
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void readTranscriptomeFNAfileSKIPto( String filename, String skip) throws FileNotFoundException, IOException{}


    /**
     * It is suggested that DnaBitStringSlow is the source of the speed problems with the program.
     * Therefore, we can use the old approach to calculate kmers online instead of doing whole bitString conversion first.
     *
     * @param currKID
     * @param currSeq
     * @param tt
     * @return
     */
    public Kmer31 writeToStoreFast(int currKID, String currSeq, TimeTotals tt){
        /* this code has been converted to reverse coordinate system
         * test class: TestKDIWriteToStoreFast
         * Double check: G and C switched values of 1 and 2
         */
        Kmer31 tempy = new Kmer31(0L);
        long nextVal;
        boolean returned = false;
        Kmer31 result = null;

        TimeTotals trackWrite = new TimeTotals();
        trackWrite.start();

        if (currSeq.length() < Kmer31.KMER_SIZE){
            System.out.println("Cannot write a sequence shorter than "+Integer.toString(currSeq.length())+" to store.");
        } else if (currKID == 0) {
            System.out.println("Cannot write a sequence assigned to KID == 0 to store.");
        } else {
            // *************************************
            // Process first kmer
            int pos = 0;

            tempy = new Kmer31(currSeq);
            if (!tempy.isValid()) {
                System.out.println("This kmer fed to constructor was invalid :: "+ currSeq.substring(0, Kmer31.KMER_SIZE));
                tempy = Kmer31.SENTINEL;
            }   // stores last value, important in loop control

            //If valid, then record to store
            if (tempy.isValid()){
                result = tempy;
                returned = true;
                if(currSeq.length() == Kmer31.KMER_SIZE) {
                    recordForwardAndReverseToStore(tempy, currKID, pos, true, true, tt);  //both start and end, how exciting
                } else {
                    recordForwardAndReverseToStore(tempy, currKID, pos, true, false, tt);
                }
                pos++;
            }
            //else var stays at 0, tempy is invalid, and we jump into the search at end of switch statement in loop below

            // *************************************
            // Process middle kmers (not first and not last)

            nextVal = tempy.toLong();
            //INTENTIONALLY STOP WITH 1 REMAINING
            for(/*var=var*/; pos < (currSeq.length() - Kmer31.KMER_SIZE); pos++){
                char nextSeq;

                //IF last kmer was invalid,
                if(tempy.isValid()){
                    //nextVal = nextVal / 4;  //OLD method
                    nextSeq = currSeq.charAt(pos + (Kmer31.KMER_SIZE-1) );
                    nextVal = calculateNextVal(nextVal, nextSeq);
                } else {
                    nextSeq = 'X'; //forces search
                }

                tempy = new Kmer31(nextVal);
                if (tempy.toLong() == -1){ System.out.println("\t\tXXXX\t Conversion problem at position "+Integer.toString(pos)); }
                if (!returned) {
                    result = tempy;
                    returned = true;
                }
                recordForwardAndReverseToStore(tempy, currKID, pos, false, false, tt);

                if (!tempy.isValid()){
                    //bad value invalidates a whole section of kmer subsequences
                    //find next good value

                    // *************************************
                    // Search for next valid kmer (except last of course)
                    // admittedly inefficient, but how often will this happen?

                    //mark current as inValid, in case we can't get into this loop

                    tempy = new Kmer31("X");
                    if (!tempy.isValid()) {
                        //error expected
                        tempy = Kmer31.SENTINEL;
                    }

                    for (pos+= Kmer31.KMER_SIZE; pos < currSeq.length()- Kmer31.KMER_SIZE; pos++){

                        tempy = new Kmer31( currSeq.substring( pos, pos + Kmer31.KMER_SIZE ));
                        if (!tempy.isValid()){
                            //e.printStackTrace();
                            System.out.println("This kmer fed to constructor was invlaid :: "+ currSeq.substring(pos, pos+ Kmer31.KMER_SIZE));
                            tempy = Kmer31.SENTINEL;
                        } else {
                            if (!returned) {
                                result = tempy;
                                returned = true;
                            }
                            recordForwardAndReverseToStore(tempy, currKID, pos, false, false, tt);
                            nextVal = tempy.toLong();
                            break;
                        }
                    } //end inner for
                } //end switch statement
            } // end for (middle kmers)

            // *************************************
            // Process last kmer

            //ASSERT   var == currSeq.length()-Kmer31.KMER_SIZE
            if (pos == currSeq.length()- Kmer31.KMER_SIZE){
                //	IF previous kmer is valid
                if (tempy.isValid()){

                    char nextSeq = currSeq.charAt(pos + (Kmer31.KMER_SIZE-1) );
                    nextVal = calculateNextVal(nextVal, nextSeq);

                    tempy = new Kmer31(nextVal);

                } else {

                    tempy = new Kmer31( currSeq.substring( pos, pos + Kmer31.KMER_SIZE ));
                    if (!tempy.isValid()){
                        tempy = Kmer31.SENTINEL;
                        //e.printStackTrace();
                        System.out.println("This kmer fed to constructor was invlaid :: "+ currSeq.substring(pos, pos+ Kmer31.KMER_SIZE));
                    }
                }

                if (tempy.isValid()) {
                    recordForwardAndReverseToStore(tempy, currKID, pos, false, true, tt);
                }
            } //END process last

        } //end if (errors) else (DO)

        trackWrite.stop();
        long output = trackWrite.timePassed();
        System.out.format(" Length       \t%d\tTime(ns)\t%d\tRate(kmer/s)\t%d\n", currSeq.length(), output, (currSeq.length()*1000000000L)/(output) );
        return result;
    } // END writeToStore

    long calculateNextVal(long prevVal, char nextSeq) {
        long nextVal =  prevVal % twoE60; //NEW method
        nextVal = nextVal << 2;

        if (nextSeq == 'A' || nextSeq == 'a'){
            nextVal += 3;

        } else if (nextSeq == 'G' || nextSeq == 'g'){
            nextVal += 2;

        } else if (nextSeq == 'C' || nextSeq == 'c'){
            nextVal += 1;

        } else if (nextSeq == 'T' || nextSeq == 't' || nextSeq == 'U' || nextSeq == 'u'){
            nextVal += 0;
        } else {
            nextVal = -1; //marks tempy as invalid
        }

        return nextVal;
    }
}
