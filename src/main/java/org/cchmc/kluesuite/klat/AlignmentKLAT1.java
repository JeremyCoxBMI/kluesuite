package org.cchmc.kluesuite.klat;


import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.masterklue.KLATsettings;

import org.cchmc.kluesuite.memoryklue.MemoryKlueTreeMap;
import org.cchmc.kluesuite.multithread.KidDatabaseThreadSafe;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseDisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
 * AlignmentKLAT1 class will take a query, then lookup all kmers using the store.
 * Then, make an alignment calculation.
 *
 * Design here requires comment.
 * Query is stored as KmerSequence, which makes a sparse sequence object storing only Kmers needing to be looked up.
 * Then, only those kmers that are valid are looked up.
 * Then, using the gap information, we know which ones are contiguous.
 *
 * Major issue: there may be multiple hits for a single kmer in the same reference sequence.
 * Solution: addAndTrim extra rows to the alignment table
 *
 *  2016-07-13  v1.6    Major development started today
 *  2016-07-22  v1.6.1  For simplicity, need two tables: forward and reverse
 *                      Caught major bug: was not looking up reverseStrand() kmers from kmersequence
 *
 *  2016-11-08          Coming back to code to proof-read, try to improve and comment seed agglomeration
 *
 * @author osboxes
 *
 * Overview of how this class works in term of data flow/calculations
 * 1) Constructor()
 *  Initializes variables.
 *  Of note, this parses the input sequence, creates several helper objects (Non-trivial complexity)
 *  isValid() can be used if there is doubt about validity of query
 *
 * 2) pollKmersForPositions()
 *  This looks up the kmers in the query.
 *
 * 3) calculateAlignmentTables()
 *
 * 2017-05-13
 * FASTKLAT score: new score defined as
 * (Seed.adjacency + Seed.hits)/2
 *
 *
 * SEED agglomeration has turned out to be a very tricky business.  Multiple functions are called in succession to combine seeds
 * (1) AlignmentKLAT1.combineAdjacentSeeds(table, r, numCols, reverse, myKid, false);
 *     This looks for seeds in a row that are literally adjacent to one another
 *     This is done by row first to O(n) complexity in the first pass, as next step is 0(m^2), where m is the number found in this step
 *     MIN_SEED_ADJACENCY and MIN_SEED_HITS applied
 * (2) Seed.combineConsecutiveSeeds();
 *     This looks across row to combine seeds that are adjacent and same KID, but may not be in the same row.
 * (3) Seed.combineSeeds();
 *     Finally, all seeds that are nearby in a KID are considered in permutations to make possibly multiple pairings.
 * (4) Seed.eliminateDuplicateSeeds
 *     Especially in low complexity regions, there are duplicate seeds, some with lower scores. We retain maximum only.
 *
 *
 * Reverse strand coordinates are such that the k-mer positions should count down for a good alignment.
 * E.G. query 0 through 4 matches reference 104 thru 100
 *
 */

public class AlignmentKLAT1 {

    public static final boolean CALL_VARIANTS = true;
    /**
     * for debug messages
     */
    public static boolean DEBUG = false;

    /**
     * Constant needed for conversions
     */
    protected static long twoE60 = 1L << 60;


    /**
     * String representing the DNA sequencing for the alignment
     */
    public final String query;

    /**
     * fasta sequence name
     */
    public final String queryName;

    /**
     * Kmer31 decomposition of query.
     * This list is truncated to remove gaps for lookups.
     */
    public KmerSequence kmers;

    /**
     * Row labels for alignmentTable
     */
    protected ArrayList<Integer> listKidRows;

    /**
     * Row labels for alignmentTableRev
     */
    protected ArrayList<Integer> listKidRowsRev;


    /**
     * Positions after looking up (KmerSequence kmers) in the database.
     * Order is important, the list of list is ordered by position in query (0 thru length - k + 1), then lists positions
     */
    protected ArrayList<PositionList> posz;

    /**
     * Positions after looking up (KmerSequence kmers) in the database, reverseStrand kmer
     */
    protected ArrayList<PositionList> poszRev;

    /**
     * Intermediate data structure used to build listKidRows
     * This maps KID to max number of occurrences in the positions of each kmer.
     * This is required in order to give multiple rows to a kmer.
     * NOTE that we cannot simply take the longest list of positions and use that as number of rows,
     * because each row is forced to be tied to a specific KID
     * FORWARD
     */
    protected HashMap<Integer, Integer> KIDtoCount;

    /**
     * Seeds are stored here after being calculated
     */
    public ArrayList<Seed> fwdSeeds, revSeeds;

    /**
     * Intermediate data structure used to build listKidRows
     * This maps KID to max number of occurrences in the positions of each kmer.
     * This is required in order to give multiple rows to a kmer.
     * REVERSE
     */
    protected HashMap<Integer, Integer> KIDtoCountRev;

    /**
     * This contains positions by KID nextOffset kmer lookup index
     * Note the kmer lookup index matches the indexing of KmerSequence, which excludes gaps.
     * Forward Direction
     */
    protected Position[][] alignmentTable;


    /**
     * This contains positions by KID nextOffset kmer lookup index
     * Note the kmer lookup index matches the indexing of KmerSequence, which excludes gaps.
     * Reverse Direction
     */
    protected Position[][] alignmentTableRev;

    /**
     * Tracks whether the alignmentTable has been created and written.
     */
    protected boolean calculated;

    /**
     * KLUE look up engine used to lookup sequences.
     */
    protected KLUE klue;
    protected ArrayList<PartialAlignment1> bestAlignments;

    /**
     * query is required to construct the object
     * All functions depend on non-empty query
     * easy to parallelize constructor on an executor block, as it accesses database extensively
     * Note that pollKmersForPositions() and calculateAlignmentTables() are not part of constructor, so that
     * lookups can be parallelized as needed appropriately (i.e. programmer may separate them if needed).
     *
     * @param query String	DNA sequence we are seeking alignments for
     */
    public AlignmentKLAT1(String query, String queryName, KLUE klue) {
        this.queryName = queryName;
        this.query = query;
        this.klue = klue;
        kmers = new KmerSequence(query);
        listKidRows = new ArrayList<Integer>();
        KIDtoCount = new HashMap<Integer, Integer>();
        listKidRowsRev = new ArrayList<Integer>();
        KIDtoCountRev = new HashMap<Integer, Integer>();

        //2016.11.16    Major wasteful oversite in code: looking up kmers in constructor
        //ArrayList<ArrayList<Long>> temp = klue.getAll(kmers.toKmerKeys());

        fwdSeeds = null;
        revSeeds = null;

        posz = new ArrayList<PositionList>();
        poszRev = new ArrayList<PositionList>();

        calculated = false;
        // we do not want to do calculations in the constructor
        // we may want to do these in a different form of parrallelism
        // i.e. this task does not access the database
        // pollKmersForPositions();
        // calculateAlignmentTables();
        bestAlignments = new ArrayList<PartialAlignment1>();
    }

    /**
     * Are underlying kmers valid?
     *
     * @return
     */
    public boolean isValid() {
        return kmers.isValid();
    }

    /**
     * print to System.out human readable versions of seeds
     *
     * @param fwdSeeds
     * @param revSeeds
     */
    public static void printSeeds(ArrayList<Seed> fwdSeeds, ArrayList<Seed> revSeeds) {
        System.out.println("\nFinal seeds");
        for (Seed s : fwdSeeds) {
            System.out.println(s);
        }
        for (Seed s : revSeeds) {
            System.out.println(s);
        }
    }

    /**
     * lookup all constituent Kmers of the Query
     */
    public void pollKmersForPositions() {
        //FIXED:  exhaustive lookup of reverse sequencews as well
        posz = klue.getAllPL(kmers.getAllForward());
    }

    /***
     * First in several functions to process alignment
     * This builds the alignment tables of looked up kmer values
     */
    public void calculateAlignmentTables() {

        if (!kmers.isValid()) {
            System.err.println("\tWarning: trying to do alignment on an empty sequence");
        } else {
            //count number of gaps

            //hits always come before and after gaps
            // this is critical about saving space / compute time:
            //     kmers does not return the invalid kmer31 subsequences that are invalid
            // so every step has to loop over every "run" between gaps
            int numberRuns = kmers.gaps.size() + 1;


            //we need to calculate and store statistics on alignments for each KID reported
            //we also need to track possible multiple alignments

            //  3.1)	Sort positions into forward and reverse kmers, counting them


            //BUG: was resetting this inside the loop



            //Establish the number of rows in the alignment table
            for (int r = 0; r < numberRuns; r++) {
                int runStop = runStop(r);
                int runStart = runStart(r);
                PositionList temp;

                //for each lookup position
                for (int k = runStart(r); k < runStop; k++) {

                    HashMap<Integer, Integer> tempKIDtoCount = new HashMap<Integer, Integer>();
                    HashMap<Integer, Integer> tempKIDtoCountRev = new HashMap<Integer, Integer>();

                    temp = posz.get(kmers.indexToInternalIndex[k]);
                    if (temp == null) temp = new PositionList();

                    //for each alignment a position
                    for (int m = 0; m < temp.length(); m++) {
                        int x = temp.get(m).getMyKID();
                        if (temp.get(m).getFlag(Position.REVERSE)) {
                            //If reverse kmer
                            if (tempKIDtoCountRev.containsKey(x)) {
                                tempKIDtoCountRev.put(x, tempKIDtoCountRev.get(x) + 1);
                            } else {
                                tempKIDtoCountRev.put(x, 1);
                            }
                        } else {
                            if (tempKIDtoCount.containsKey(x)) {
                                tempKIDtoCount.put(x, tempKIDtoCount.get(x) + 1);
                            } else {
                                tempKIDtoCount.put(x, 1);
                            }
                        }
                    }

//                    // MAJOR BUG - do not want to lookup reverse kmers; we want to FIND matching kmers in reverseStrand
//                    temp = poszRev.get(kmers.indexToInternalIndex[k]);
//                    for (int m=0; m < temp.length();m++){
//                        int nextOffset = temp.get(m).getMyKID();
//                        if ( temp.get(m).getFlag(Position.REVERSE) ){
//                            //If reverse kmer
//                            if( tempKIDtoCountRev.containsKey( nextOffset )){
//                                tempKIDtoCountRev.put(nextOffset, tempKIDtoCountRev.get(nextOffset)+1);
//                            } else {
//                                tempKIDtoCountRev.put(nextOffset, 1);
//                            }
//                        } else {
//                            if( tempKIDtoCount.containsKey( nextOffset )){
//                                tempKIDtoCount.put(nextOffset, tempKIDtoCount.get(nextOffset)+1);
//                            } else {
//                                tempKIDtoCount.put(nextOffset, 1);
//                            }
//                        }
//                    }


                    //for each lookup position

                    //Update number of rows needed.  IN FORWARD
                    for (int m : tempKIDtoCount.keySet()) {
                        if (KIDtoCount.containsKey(m)) {
                            KIDtoCount.put(m, Math.max(KIDtoCount.get(m), tempKIDtoCount.get(m)));
                        } else {
                            KIDtoCount.put(m, tempKIDtoCount.get(m));
                        }
                    }

                    //Update number of rows needed.  IN REVERSE
                    for (int m : tempKIDtoCountRev.keySet()) {
                        if (KIDtoCountRev.containsKey(m)) {
                            KIDtoCountRev.put(m, Math.max(KIDtoCountRev.get(m), tempKIDtoCountRev.get(m)));
                        } else {
                            KIDtoCountRev.put(m, tempKIDtoCountRev.get(m));
                        }
                    }

                } //end for k
            } // end for r

            // 3.2)	Summarize number of rows needed & label appropriately
            calculateKidFound();

            //now listKidRows.length() is the number of rows of our table
//            System.err.println("Making table of dimensions "+listKidRows.size()+" X "+kmers.indexToInternalIndex.length);

            alignmentTable = new Position[listKidRows.size()][kmers.indexToInternalIndex.length];
            alignmentTableRev = new Position[listKidRowsRev.size()][kmers.indexToInternalIndex.length];

//            for (int k=0; k < listKidRows.size(); k++) for(int j=0;j<kmers.indexToInternalIndex.length;j++) alignmentTable[k][j] = null;

            for (int r = 0; r < numberRuns; r++) {
                for (int k = runStart(r); k < runStop(r); k++) {
                    PositionList temp = posz.get(kmers.indexToInternalIndex[k]);
                    if (temp == null) temp = new PositionList();
                    for (int m = 0; m < temp.length(); m++) {
                        Position here = new Position(temp.get(m));
                        //Sort on FORWARD/REVERSE
                        if (here.getFlag(Position.REVERSE)) {
                            int x = listKidRowsRev.indexOf(here.getMyKID());
                            int maxX = (x + KIDtoCountRev.get(here.getMyKID()));
                            for (; x < maxX; x++) {
                                //write object to correct column and row
                                //
                                if (alignmentTableRev[x][kmers.indexToInternalIndex[k]] == null) {
                                    alignmentTableRev[x][kmers.indexToInternalIndex[k]] = here;
                                    break;
                                } else if (x == maxX - 1) {
                                    System.err.println(" (This should never happen) :: Building alignment table, no room for : " + here);
                                }
                            }
                        } else {
                            int x = listKidRows.indexOf(here.getMyKID());
                            int maxX = (x + KIDtoCount.get(here.getMyKID()));
                            for (; x < maxX; x++) {
                                //write object to correct column and row
                                //
                                if (alignmentTable[x][kmers.indexToInternalIndex[k]] == null) {
                                    alignmentTable[x][kmers.indexToInternalIndex[k]] = here;
                                    break;
                                } else if (x == maxX - 1) {
                                    System.err.println(" (This should never happen) :: Building alignment table, no room for : " + here);
                                }
                            }
                        }
                    } // end for

                } //end for k
            } //end for r

            for (int key : KIDtoCount.keySet()) {
                int number = KIDtoCount.get(key);
                if (number > 1) {
                    //must check every entry to see if they are sorted
                    int row = listKidRows.indexOf(key);
                    for (int c = 0; c < kmers.internalIndexToIndex.length; c++) {
                        sortMultipleKid(row, c, number);
                    }
                }
            } // rows of duplicate entries now sorted

        } // end else isValid() == true
        calculated = true;
    } //end calculateAlignmentTables


    /**
     * function for multi-step analysis
     * Once calculateAlignmentTables has constructed alignment tables, seeds can be found
     */
    public void calculateSeeds() {
        fwdSeeds = getForwardSeeds();
        revSeeds = getReverseSeeds();
    }

    /**
     * This takes possibility of multiple maps to same KID in alignmentTable and sorts by position number.
     *
     * @param row    first row where KID occurs
     * @param col    column to sort
     * @param number number of entries to sort
     */
    private void sortMultipleKid(int row, int col, int number) {
        int lastExclusive = -1;
        for (int k = row; k < row + number; k++) {
            k = k;
            if (alignmentTable[k][col] != null) lastExclusive = k + 1;
        }

        //bubble sort
        Position swap;
        for (int r = row; r < lastExclusive; r++) {
            for (int rr = r + 1; rr < lastExclusive; rr++) {

                //TODO this line identified as null pointer exception  ?resolved?
                if ( alignmentTable[rr][col] != null && alignmentTable[r][col] != null &&
                        alignmentTable[rr][col].getPosition() < alignmentTable[r][col].getPosition()) {
                    swap = alignmentTable[rr][col];
                    alignmentTable[rr][col] = alignmentTable[r][col];
                    alignmentTable[r][col] = swap;
                }
            }

        }
    }

    private void calculateKidFound() {
        for (int m : KIDtoCount.keySet()) {
            for (int x = 0; x < KIDtoCount.get(m); x++) {
                listKidRows.add(m);
            }
        }
        Collections.sort(listKidRows);


        for (int m : KIDtoCountRev.keySet()) {
            for (int x = 0; x < KIDtoCountRev.get(m); x++) {
                listKidRowsRev.add(m);
            }
        }
        Collections.sort(listKidRowsRev);
    }

    private int runStart(int run) {
        //hits always come before and after gaps

        if (run == 0) {
            return 0;
        } else {
            return kmers.gaps.get(run - 1).nextIndexAfterGap();
        }
    }


    /**
     * End of the run, EXCLUSIVE
     * RUNS are inbetween gaps
     *
     * @param run
     * @return
     */
    private int runStop(int run) {
        //hits always come before and after gaps
        if (run == kmers.gaps.size()) {
            return kmers.length;
        } else {
            return kmers.gaps.get(run).pos; //gap starts
        }
    }

    /**
     * human readable
     *
     * @return
     */
    public String toString() {
        if (!kmers.isValid()) {
            System.err.println("\tWarning: trying to do alignment on an empty sequence");
            return "EMPTY_SEQUENCE";
        } else {
            String result = "\tFULL printout of AlignmentKLAT1 class for\n\t\t" + query + "\n";

            result += "\tFORWARD direction\n";
            result += "\t\tKidCount :: [";
            for (int k : KIDtoCount.keySet()) {
                result += Integer.toString(k) + " : " + KIDtoCount.get(k) + ", ";
            }
            result += "]\n";
            result += tableToString();


            result += "\tREVERSE direction\n";
            result += "\t\tKidCountRev :: [";

            for (int k : KIDtoCountRev.keySet()) {
                result += Integer.toString(k) + " : " + KIDtoCount.get(k) + ", ";
            }
            result += "]\n";
            result += tableRevToString();

            return result;
        }
    }


    /**
     * Creates string representing the table.  helper function
     *
     * @return
     */
    private String tableToString() {
        String result = "";
        result += "\t";
        //result += String.format("%1$8s","")+"\t\t";
        int z;
        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
            result += "|GAP|\t";
            z = 0;    //start on first entry
        } else {
            result += Integer.toString(kmers.internalIndexToIndex[0]) + "\t";
            z = 1;    //start on second entry
            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
        }
        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
            result += Integer.toString(kmers.internalIndexToIndex[k]) + "\t";
            //check for gap; short circuit to not read off array
            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
                result += "|GAP|\t";
        }
        //addAndTrim last
        result += "\n";


        //KMER column headers
        result += "\t";
        //result += String.format("%1$8s","kid")+"\t\t";
        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
            result += "|GAP|\t";
            z = 0;    //start on first entry
        } else {
            result += kmers.get(0) + "\t";
            z = 1;    //start on second entry
            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
        }
        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
            result += kmers.get(kmers.internalIndexToIndex[k]) + "\t";
            //check for gap; short circuit to not read off array
            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
                result += "|GAP|\t";
        }
        //addAndTrim last
        result += "\n";


        if (listKidRows.size() == 0) result += "\tNONE FOUND\n";

        for (int r = 0; r < listKidRows.size(); r++) {
            //result += String.format("%1$8s","kid")+"\t"+listKidRows.get(r) + "\t";
            result += listKidRows.get(r) + "\t";
            if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
                result += "|GAP|\t";
                z = 0;
            } else {
                //System.err.println("Accessing row "+r+" position 0");
                Position t = alignmentTable[r][0];
                if (t != null) {
                    result += t.toString() + "\t";
                } else {
                    result += "null\t";
                }
                z = 1;
                if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
            }

            for (int c = z; c < kmers.internalIndexToIndex.length; c++) {
                if (alignmentTable[r][c] == null) result += "null\t";
                else result += alignmentTable[r][c].toString() + "\t";
                //check for gap; short circuit to not read off array
                if ((c + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[c + 1] - kmers.internalIndexToIndex[c] > 1)
                    result += "|GAP|\t";
            }
            result += "\n";
        }

        return result;
    }


    /**
     * String version of reverse direction table of matching Kmers looked up in REVERSE direction
     * helper function
     *
     * @return
     */
    public String tableRevToString() {
        String result = "";

        //table
        result += "\t";

        int z;
        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
            result += "|GAP|\t";
            z = 0;    //start on first entry
        } else {
            result += Integer.toString(kmers.internalIndexToIndex[0]) + "\t";
            z = 1;    //start on second entry
            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
        }
        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
            result += Integer.toString(kmers.internalIndexToIndex[k]) + "\t";
            //check for gap; short circuit to not read off array
            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
                result += "|GAP|\t";
        }
        //addAndTrim last
        result += "\n";

        result += "\t";
        //KMER column headers
        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
            result += "|GAP|\t";
            z = 0;    //start on first entry
        } else {
            result += kmers.get(0).reverseStrand() + "\t";
            z = 1;    //start on second entry
            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
        }
        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
            result += kmers.get(kmers.internalIndexToIndex[k]).reverseStrand() + "\t";
            //check for gap; short circuit to not read off array
            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
                result += "|GAP|\t";
        }
        //addAndTrim last
        result += "\n";

        if (listKidRowsRev.size() == 0) result += "\tNONE FOUND\n";

        for (int r = 0; r < listKidRowsRev.size(); r++) {
            result += listKidRowsRev.get(r) + "\t";
            if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
                result += "|GAP|\t";
                z = 0;
            } else {
                if (alignmentTableRev[r][0] == null) result += "null\t";
                else result += alignmentTableRev[r][0].toString() + "\t";
                z = 1;
                if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
            }

            for (int c = z; c < kmers.internalIndexToIndex.length; c++) {
                if (alignmentTableRev[r][c] == null) result += "null\t";
                else result += alignmentTableRev[r][c].toString() + "\t";
                //check for gap; short circuit to not read off array
                if ((c + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[c + 1] - kmers.internalIndexToIndex[c] > 1)
                    result += "|GAP|\t";
            }
            result += "\n";
        }
        return result;
    }


//    /**
//     * Function used for testing.  Predates KidDatabaseMemory.getSequence() which is used instead.
//     * deprecated
//     *
//     * @param filename
//     * @param myKidDB
//     * @return
//     */
//    static private HashMap<Integer, String> readFNAsequencesToMemory(String filename, KidDatabaseMemory myKidDB){
//        HashMap<Integer, String> result = new HashMap<Integer, String>();
//        boolean ignore = true; //do not write empty sequence to database
//        boolean skipping = false;
//        boolean debug = false;
//        String skip = "";
//        int currentKID = myKidDB.getMaxKid();
//        String currentSeq = "";
//
//        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
//
//            for(String line; (line = br.readLine()) != null; ) {
//
//                if(debug){System.err.println("Single line:: "+line);}
//
//                // if blank line, it does not count as new sequence
//                if (line.trim().length() == 0){
//                    if(debug){System.err.println("           :: blank line detected  ");}
//                    if (!skipping) {
//                        if (!ignore) {
//                            System.err.println("putting this sequence onto store :" + currentSeq.length() + ": " + currentSeq.substring(0, Math.min(currentSeq.length(), 100)));
//                            result.put(currentKID, currentSeq);
//                        }
//                    }
//                    ignore = true;
//
//                    // if line starts with ">", then it is start of a new reference sequence
//                } else if( line.charAt(0) == '>'){
//                    if(debug){System.err.println("           :: new entry detected  ");}
//                    // save previous iteration to database
//                    if (skipping && skip.equals(line.trim())){
//                        System.out.println("\tFound the skipto line :: "+line);
//                        skipping = false;
//                    }
//
//                    if (!skipping){
//                        if (!ignore){
//                            System.err.println("putting this sequence onto store :"+currentSeq.length()+": "+currentSeq.substring(0,Math.min(currentSeq.length(), 100)));
//                            result.put(currentKID, currentSeq);
//
//                        }
//
//                        // initialize next iteration
//                        currentKID = myKidDB.indexOf(line.trim());
//                        System.err.println();
//                        currentSeq ="";
//                        ignore = false;
//                    }
//
//                    System.out.println("\tCurrent KID : "+currentKID+"\tProcessing Ref Seq :: "+line);
//
//                } else {
//                    if (!skipping) { currentSeq += line.trim(); }
//                }
//
//            } //end for
//
//            if (!ignore){
//                //if (debug){ System.err.println("putting this sequence onto store :"+currentSeq.length()+": "+currentSeq.substring(0,100));}
//                System.err.println("putting this sequence onto store :"+currentSeq.length()+": "+currentSeq.substring(0,Math.min(currentSeq.length(), 100)));
//                result.put(currentKID, currentSeq);
//
//            }
//            br.close();
//        } catch (java.io.IOException e){
//
//        }
//
//        return result;
//    }

    /**
     * Generate alignment seeds on demand in FORWARD direction
     *
     * @return
     */
    public ArrayList<Seed> getForwardSeeds() {
        return generateSeeds(alignmentTable, listKidRows, kmers.indexToInternalIndex.length, false);
    }

    /**
     * Generate alignment seeds on demand in REVERSE direction
     *
     * @return
     */
    public ArrayList<Seed> getReverseSeeds() {
        return generateSeeds(alignmentTableRev, listKidRowsRev, kmers.indexToInternalIndex.length, true);
    }


    protected void addSeedIfMinimumsMet(ArrayList<Seed> tempSeeds, Seed streakerStart, Seed prev, int myKid) {
        int l = (prev.queryStart - streakerStart.queryStart) + 1;
//        if (l >= KLATSettingsOLD.MIN_SEED_ADJACENCY && l >= KLATSettingsOLD.MIN_SEED_HITS) {
        if (l >= KLATsettings.MIN_SEED_ADJACENCY && l >= KLATsettings.MIN_SEED_HITS) {
            boolean snp = streakerStart.snp || prev.snp;
            boolean indel = streakerStart.indel || prev.indel;
            boolean reverse = streakerStart.isReverse;
            if (streakerStart.isReverse != prev.isReverse) {
                System.err.println("\tWARNING\t two adjacent seeds; one reverse one forward.  SHOULD NOT HAPPEN");
            }
            tempSeeds.add(new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
                    prev.end, reverse, l, l, myKid, snp, indel));
            if (DEBUG)
                System.err.println("New Seed \t" + new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
                        prev.end, reverse, l, l, myKid, snp, indel));
        }
        return;
    }

    protected ArrayList<Seed> addSeed(ArrayList<Seed> tempSeeds, Seed streakerStart, Seed prev, int myKid) {

        if (prev == null){
            tempSeeds.add(streakerStart);
        } else {
            int l = (prev.queryStart - streakerStart.queryStart) + 1;

            boolean snp = streakerStart.snp || prev.snp;
            boolean indel = streakerStart.indel || prev.indel;
            boolean reverse = streakerStart.isReverse;
            if (streakerStart.isReverse != prev.isReverse) {
                System.err.println("\tWARNING\t two adjacent seeds; one reverse one forward.  SHOULD NOT HAPPEN");
            }
            tempSeeds.add(new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
                    prev.end, reverse, l, l, myKid, snp, indel));
            if (DEBUG)
                System.err.println("New Seed \t" + new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
                        prev.end, reverse, l, l, myKid, snp, indel));
        }

        return tempSeeds;
    }



    /**
     * Helper function
     *
     * @return  SORTED list of seeds that have been agglomerated to summarize best possible alignment combinations
     */
    protected ArrayList<Seed> generateSeeds(Position[][] table, ArrayList<Integer> rows, int numCols, boolean reverse) {
        int numRows = rows.size();
        ArrayList<Seed> result = new ArrayList<Seed>();

        //go through rows, generate seeds for each KID

        //4.1)	For each row, calculate streaks, where seeds are obviously adjacent to each other WITHIN the ROW
        int lastKid = -1;       //SENTINEL
        int myKid = 0;          //SENTINEL

        ArrayList<Seed> tempSeeds = new ArrayList<Seed>();
        ArrayList<Seed> tempSeeds2 = new ArrayList<Seed>();

        //Need this step to intialize first row properly?
        if (rows.size() > 0) {
            myKid = rows.get(0);
            lastKid = myKid;
            tempSeeds = combineAdjacentSeeds(table, 0, numCols, reverse, myKid);
        }
        int kidStreakStart = 0;
        int r;
        for (r = 1; r < rows.size(); r++) {
            myKid = rows.get(r);

            // if starting a block of rows with a new KID
            // then record all seeds so far out, reset for new row(s)
            if (myKid != lastKid) {

                //write seeds from temporary to permanent
                //only combine if multiple rows
                if (r - kidStreakStart > 0) {
                    // ***** this is where we combine across rows  ******
                    tempSeeds = Seed.combineConsecutiveSeeds(tempSeeds);
                    tempSeeds = Seed.combineSeeds(tempSeeds);

                } // ELSE DO NOTHING
                result.addAll(tempSeeds);

                //reset for next set
                tempSeeds = combineAdjacentSeeds(table, r, numCols, reverse, myKid);
                //consecutive seeds already combined across rows
                //tempSeeds = Seed.combineConsecutiveSeeds(tempSeeds);
                lastKid = myKid;
                kidStreakStart = r;

            } else {
                //same KID, so addAndTrim to list
                tempSeeds2 = combineAdjacentSeeds(table, r, numCols, reverse, myKid);
                //consecutive seeds already combined across rows
                //tempSeeds2 = Seed.combineConsecutiveSeeds(tempSeeds2);
                tempSeeds.addAll(tempSeeds2);
            }
        }

        if (r - kidStreakStart > 0) {
            // ***** this is where we combine across rows  ******
            tempSeeds = Seed.combineConsecutiveSeeds(tempSeeds);
            tempSeeds = Seed.combineSeeds(tempSeeds);
        } // ELSE DO NOTHING
        result.addAll(tempSeeds);

//        //Process last chunk (chunks written when kid changes, but can't change on last entry)
//        tempSeeds = Seed.combineConsecutiveSeeds(tempSeeds);
//        //so far, only adjacent seeds combined into streaks, look across rows
//        tempSeeds = Seed.combineSeeds(tempSeeds);
//        for (Seed s : tempSeeds) result.addAndTrim(s);

        //there may be some new consecutive streaks
        //it is not clear why second run through is needed?  2017-07-18 JWC
//        result = Seed.combineConsecutiveSeeds(result);
        result = Seed.combineSeeds(result);

        //reduce duplications of same positions  this happens when there are low complexity regions
        //note also the result is now SORTED
        result = Seed.eliminateDuplicates(result);

//        //why is this step here?  -- filtered later
//        result = Seed.eliminateBelowFastKlatScore(result);

        return result;
    }




//            for (Seed s : combineAdjacentSeeds(table, r, numCols, reverse, myKid, false)) {
//                tempSeeds.addAndTrim(s);
//            }
//        }

//        for (int r = 0; r < rows.size(); r++) {
//
//            System.err.println("Row is \t" + Arrays.toString(table[r]));
//
//            myKid = rows.get(r);
//
//            // if starting a block of rows with a new KID
//            // then record all seeds so far out, reset for new row(s)
//            if (myKid != lastKid) {
//
//                //write seeds from temporary to permanent
//                //unless starting loop (lastKid == -1)
//                if (lastKid != -1) {
//                    //Seed.combineAdjacentSeeds(tempSeeds);
//                    Seed.combineSeeds(tempSeeds);
//                    for (Seed s : tempSeeds) result.addAndTrim(s);
//                } // ELSE first line DO NOTHING
//
//                lastKid = myKid;
//                //reset for next set
//                tempSeeds = new ArrayList<Seed>();
//            }
//
////            //Initialize seeds
////            for (int c = 0; c < numCols; c++) {
////                if (table[r][c] != null) {
////                    int var = table[r][c].getPosition();
////                    tempSeeds.addAndTrim(new Seed(c,c+1,var, var + 1, table[r][c].getFlag(Position.REVERSE), 1,1));
////                }
////            }
//
//
//            //Initialize adjacent seeds
//            Seed streakerStart = new Seed(), temp = new Seed(), prev = new Seed();
//            int streakStart = -1;
//            Position pos;
//
//
//            //streakStart = -1 means no streak currently
//            for (int c = 0; c < numCols; c++) {
//                //If there is a position to consider as a seed
//                if (table[r][c] != null) {
//                    //pos = table[r][c].getPosition();
//                    pos = table[r][c];
//
//                    // If first streak defined / first hit in table
//                    if (streakStart == -1) {
//                        streakStart = c;
//                        streakerStart = new Seed(c, c + 1, pos, myKid);
////                        temp = new Seed(c,c+1,pos, myKid);
//                        prev = new Seed(c, c + 1, pos, myKid);
//                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//                    } else {
////                        prev = temp;
//                        temp = new Seed(c, c + 1, pos, myKid);
//
//                        //If Seed is adjacent, update last seed; do not addAndTrim another
//                        //we want to split streaks if their SNP/INDEL profile does not match
//                        if (!Seed.isAdjacent(prev, temp) ||     //bug 2017-05-13  used AND instead of OR
//                                prev.indel != temp.indel ||
//                                prev.snp != temp.snp) {
//                            //create seed ending the streak
//                            // If of sufficient length, write to seed list
//
//                            addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
//                            streakStart = c; //new streak
//                            streakerStart = new Seed(c, c + 1, pos, myKid);
//                            if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
////                            streakStart = -1; //new streak
//                            //already done above
//                            //temp = new Seed(c,c+1,pos, myKid);
//                        }
//                        prev = temp;
//                        // else seed is adjacent, AND indel and snp flags match
//                        // streak continues; do nothing
////                        else {
////                            System.out.println("*****\n\tDebug message 1042. Possible source of error in SEED parsing.");
////                        }
//                    }
//                } else {    // else entry in table is blank
//
//                    //create seed ending the streak
//                    if (streakStart != -1) {
//                        //prev = temp;
//                        addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);
//                        prev = null;
//                    }
//                    streakStart = -1; // no streak; current alignment is null
//                }
//            }
//            //record final streak
//            prev = temp; //bug -- ommitted line -- fixed 08-03
//            int l = (prev.queryStart - streakerStart.queryStart) + 1;
//            if (streakStart != -1) { // && l >= KLATSettingsOLD.MIN_SEED_ADJACENCY && l >= KLATSettingsOLD.MIN_SEED_HITS) {
//                addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);
//            }
//
//        } // for all rows


    /**
     *
     *
     * @param table
     * @param r
     * @param numCols
     * @param reverse
     * @param myKid
     * @return
     */
    protected ArrayList<Seed> combineAdjacentSeeds(Position[][] table, int r, int numCols, boolean reverse, int myKid) {
        //minimums ==> combine based on minimums?

        //Initialize adjacent seeds
        Seed streakerStart = new Seed(), temp = new Seed(), prev = new Seed();
        int streakStart = -1;
        Position pos;
        ArrayList<Seed> tempSeeds = new ArrayList<Seed>();


        //streakStart = -1 means no streak currently
        for (
                int c = 0;
                c < numCols; c++)

        {

            //If there is a position to consider as a seed
            if (table[r][c] != null) {
                pos = table[r][c];

                // If first streak defined / first hit in table
                if (streakStart == -1) {
                    streakStart = c;
                    streakerStart = new Seed(c, c + 1, pos, myKid);

                    prev = new Seed(c, c + 1, pos, myKid);
                    if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
                } else {

                    temp = new Seed(c, c + 1, pos, myKid);


//                    //First seed, start streak
//                    if (prev == null){
//                        streakStart = c; //new streak
//                        streakerStart = new Seed(c, c + 1, pos, myKid);
//                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//                    }
//
//                    else
                        //If Seed is not adjacent, addAndTrim last streak to list, otherwise streak continues
                    if (!Seed.isAdjacent(prev, temp)){


                        // create seed ending the streak
                        //we cannot filter at this point.  2-mer seeds will not meet default criteria

                        //no streak if prev is empty

                        //MIN_SEED_ADJACENCY and //MIN_SEED_HITS enforced
                        addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);
//                        tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak

                        streakStart = c; //new streak
                        streakerStart = new Seed(c, c + 1, pos, myKid);
                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);

                    }
                    prev = temp;

                }
            } else {    // else entry in table is blank

                //create seed ending the streak
                if (streakStart != -1) {
                    tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
                    prev = null;
                }
                streakStart = -1; // no streak; current alignment is null
                //2017-07-26
                //prev = null;
            }
        }
        //record final streak
        prev = temp; //bug -- ommitted line -- fixed 08-03
        int l = (prev.queryStart - streakerStart.queryStart) + 1;
        if (streakStart != -1) {
//            if(streakStart==20) {
//                int debugIT = 1;
//            }
            if (streakerStart.equals(prev) || prev.myKid == 0){
                tempSeeds.add(streakerStart);
            } else {
                tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);
            }
        }

        //tempseeds now contains agglomerated seeds by row

//        //try to combine adjacent seeds across rows
//        for (int k=0; k < tempSeeds.size()-1; k++){
//            for (int j =k+1; j < tempSeeds.size();j++){
//                if (Seed.isAdjacent(tempSeeds.get(j), tempSeeds.get(k))){
//
//
//                    tempSeeds.set(k,Seed.mergeIfAble(tempSeeds.get(j),tempSeeds.get(k)));
//
//                    //Do we want to delete j?  It was combined with k
//                    //Yes -- we do not need seeds that are "subseeds" of other seeds
//                    tempSeeds.remove(j);
//                    j--;  //we removed it, so to not skip, subtract 1
//                }
//            }
//        }



        return tempSeeds;
    }



//    private ArrayList<Seed> combineAdjacentSeeds(ArrayList<Seed> seeds) {
//        //Initialize adjacent seeds
//        Seed streakerStart = new Seed(), temp = new Seed(), prev = new Seed();
//        int streakStart = -1;
//        Seed pos;
//        ArrayList<Seed> tempSeeds = new ArrayList<Seed>();
//
//
//        //streakStart = -1 means no streak currently
//        for ( int k = 0; k < seeds.size(); k++ ){
//            pos = seeds.get(k);
//
//            // If first streak defined / first hit in table
//            if (streakStart == -1) {
//                streakStart = k;
//                streakerStart = new Seed(c, c + 1, pos, myKid);
////                        temp = new Seed(c,c+1,pos, myKid);
//                prev = new Seed(c, c + 1, pos, myKid);
//                if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//            } else {
////                        prev = temp;
//                temp = new Seed(c, c + 1, pos, myKid);
//
//                //If Seed is adjacent, update last seed; do not addAndTrim another
//                //we want to split streaks if their SNP/INDEL profile does not match
//                if (!Seed.isAdjacent(prev, temp) ||     //bug 2017-05-13  used AND instead of OR
//                        prev.indel != temp.indel ||
//                        prev.snp != temp.snp) {
//                    //create seed ending the streak
//                    // If of sufficient length, write to seed list
//                    if (minimums)
//                        addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
//                    else
//                        addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
//                    streakStart = c; //new streak
//                    streakerStart = new Seed(c, c + 1, pos, myKid);
//                    if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
////                            streakStart = -1; //new streak
//                    //already done above
//                    //temp = new Seed(c,c+1,pos, myKid);
//                }
//                prev = temp;
//                // else seed is adjacent, AND indel and snp flags match  ---> not necessarily
//                // streak continues; do nothing
////                        else {
////                            System.out.println("*****\n\tDebug message 1042. Possible source of error in SEED parsing.");
////                        }
//            }
//        } //end for
//
//
//
//    }



    protected ArrayList<Seed> combineAdjacentSeeds(ArrayList<Seed> input,boolean reverse, int myKid, boolean minimums) {
        //minimums ==> combine based on minimums?

        //Initialize adjacent seeds
        Seed streakerStart = new Seed(), temp = new Seed(), prev = new Seed();
        int streakStart = -1;
        Position pos;
        ArrayList<Seed> tempSeeds = new ArrayList<Seed>();


        //streakStart = -1 means no streak currently
        for ( int k=0; k<input.size(); k++)
        {

                Seed s = input.get(k);
                int c = s.queryStart;
                //pos = table[r][c].getPosition();
                //pos = table[r][c];
                pos = s.getPosition();

                // If first streak defined / first hit in table
                if (streakStart == -1) {
                    streakStart = c;
                    streakerStart = new Seed(c, c, pos, myKid);
//                        temp = new Seed(c,c+1,pos, myKid);
                    prev = new Seed(c, c + 1, pos, myKid);
                    if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
                } else {
//                        prev = temp;
                    temp = new Seed(c, c + 1, pos, myKid);

                    //If Seed is adjacent, update last seed; do not addAndTrim another
                    //we want to split streaks if their SNP/INDEL profile does not match   //TODO this seems like a silly goal  why?
                    if (!Seed.isAdjacent(prev, temp) ||     //bug 2017-05-13  used AND instead of OR
                            prev.indel != temp.indel ||
                            prev.snp != temp.snp) {
                        //create seed ending the streak
                        // If of sufficient length, write to seed list
                        if (minimums)
                            addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
                        else
                            tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
                        streakStart = c; //new streak
                        streakerStart = new Seed(c, c + 1, pos, myKid);
                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//                            streakStart = -1; //new streak
                        //already done above
                        //temp = new Seed(c,c+1,pos, myKid);
                        prev = temp;
                    }

                }

        }
        //record final streak
        prev = temp; //bug -- ommitted line -- fixed 08-03
        int l = (prev.queryStart - streakerStart.queryStart) + 1;
        if (streakStart != -1)

            if (minimums)
                addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);
            else
                tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);

        return tempSeeds;
    }






    /**
     * Used to test AlignmentKLAT1 performance.
     * Performs seed based alignments only and outputs to screen.
     * Skips actual aligment for case when the actual sequence is not available
     */
    public void testSeeds() {
        SmithWatermanAdvanced swa;

        pollKmersForPositions();
        calculateAlignmentTables();
        calculateSeeds();

        System.out.println("\t***\tTest AlignmentKLAT1 Table IS:");
        System.out.println(toString());
        System.out.println("\nFinal seeds");
        for (Seed s : fwdSeeds) {
            System.out.println(s);
        }
        for (Seed s : revSeeds) {
            System.out.println(s);
        }
    }

    /**
     * Used to test AlignmentKLAT1 performance.
     * Performs all calculations for alignment and outputs to screen.
     *
     * @param myKidDB
     */
    public void testAll(KidDatabaseMemory myKidDB){
        SmithWatermanAdvanced swa;

        pollKmersForPositions();
        calculateAlignmentTables();
        calculateSeeds();

        System.out.println("\t***\tTest AlignmentKLAT1 Table IS:");
        System.out.println(toString());
        System.out.println("\nFinal seeds");
        for (Seed s : fwdSeeds) {
            System.out.println(s);
        }
        for (Seed s : revSeeds) {
            System.out.println(s);
        }
        System.out.println("\nAlignments");
        System.out.println("\tFORWARD");
        for (Seed s : fwdSeeds) {
            System.out.println("\nSEED "+s);
            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
            swa.printPrettyBestResults(System.out);
            ArrayList<PartialAlignment1> pas = swa.bestAlignments();
            bestAlignments.addAll(pas);

            System.out.println("\tFORWARD Blast Outformat 6");
            System.out.println("\t"+Blast6Header);
            for (PartialAlignment1 pa : pas){
                System.out.println("\t"+toBlast6(pa, t, myKidDB));
            }
        }
        System.out.println("\tREVERSE");
        for (Seed s : revSeeds) {
            System.out.println("\nSEED "+s);
            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
            swa.printPrettyBestResults(System.out);
            ArrayList<PartialAlignment1> pas = swa.bestAlignments();
            bestAlignments.addAll(pas);
            System.out.println("\tREVERSE Blast Outformat 6");
            System.out.println("\t"+Blast6Header);
            for (PartialAlignment1 pa : pas){
                System.out.println("\t"+toBlast6(pa, t, myKidDB));
            }
        }
    }

    public String
    results(KidDatabaseMemory myKidDB) {

        SmithWatermanAdvanced swa;

        pollKmersForPositions();
        calculateAlignmentTables();
        calculateSeeds();

        SuperString result = new SuperString();


        //opportunity to remove identical reference sequence requests
        //this is taken care of in the seeds combineSeeds step

        ArrayList<ReferenceSequenceRequest> refSeqReq = new ArrayList<>();
        //ArrayList<ReferenceSequenceRequest> fwdRefSeqReq = new ArrayList<>();

        for (Seed s : fwdSeeds) {
            if (AlignmentKLAT1.DEBUG == true) {
                System.err.println("\tSEED\t\t" + s);
                System.err.println("\tFASTKLAT\t" + AlignmentKLAT1.calculateFastKlatScore(s));
            }
            if (AlignmentKLAT1.calculateFastKlatScore(s) >= KLATsettings.MIN_FAST_KLAT_SCORE) {
                refSeqReq.add(s.toRefSeqRequest(myKidDB, query.length()));
            }
        }

        for (int k = 0; k < refSeqReq.size() - 1; k++) {
            for (int j = k + 1; j < refSeqReq.size(); j++) {
                if (ReferenceSequenceRequest.areEqual(refSeqReq.get(k), refSeqReq.get(j))) {
                    refSeqReq.remove(j);
                    j--;
                }
            }
        }

        for (ReferenceSequenceRequest r : refSeqReq) {
//            System.out.println("Current RefSequence Request\t"+r.toString());
            String str = r.getReferenceSequence();
            if (DEBUG) System.err.println("Seq \t" + str);

            if (!str.equals(DnaBitString.SENTINEL)) {
                swa = new SmithWatermanAdvanced(query, str);
                ArrayList<PartialAlignment1> pas = swa.bestAlignments();
                bestAlignments.addAll(pas);
                for (PartialAlignment1 pa : pas) {
//                    if (CALL_VARIANTS) {
//                        //Variants must be called to fix any errors in gaps
//                        String vnl = Variant.variantNameList(pa.callVariants());
//                        result.add(toBlast6(pa, r, myKidDB) + "\t" + vnl + "\n");
//                    } else {
//                        result.add(toBlast6(pa, r, myKidDB) + "\n");
//
//                    }

                    //toBlast6 now includes Variants called
                    result.add(toBlast6(pa, r, myKidDB) + "\n");
                }
            }
        }


        refSeqReq = new ArrayList<>();

        for (Seed s : revSeeds) {
            if (AlignmentKLAT1.DEBUG == true) {
                System.err.println("\tSEED\t\t" + s);
                System.err.println("\tFASTKLAT\t" + AlignmentKLAT1.calculateFastKlatScore(s));
            }
            if (AlignmentKLAT1.calculateFastKlatScore(s) >= KLATsettings.MIN_FAST_KLAT_SCORE) {
                refSeqReq.add(s.toRefSeqRequest(myKidDB, query.length()));
            }
        }

        for (int k = 0; k < refSeqReq.size() - 1; k++) {
            for (int j = k + 1; j < refSeqReq.size(); j++) {
                if (ReferenceSequenceRequest.areEqual(refSeqReq.get(k), refSeqReq.get(j))) {
                    refSeqReq.remove(j);
                    j--;
                }
            }
        }

        for (ReferenceSequenceRequest r : refSeqReq) {
//            System.out.println("Current RefSequence Request\t"+r.toString());
            String str = r.getReferenceSequence();
            if (DEBUG) System.err.println("Seq \t" + str);

            if (!str.equals(DnaBitString.SENTINEL)) {
                swa = new SmithWatermanAdvanced(query, str);
                ArrayList<PartialAlignment1> pas = swa.bestAlignments();
                bestAlignments.addAll(pas);
                for (PartialAlignment1 pa : pas) {

                    //Filters for applicable hits
                    if (pa.pident >= KLATsettings.MIN_PERCENT_IDENTITY &&
                            pa.evalue <= KLATsettings.MAX_EVALUE
                            ) {
                        if (CALL_VARIANTS) {
                            //Variants must be called to fix any errors in gaps
                            String vnl = Variant.variantNameList(pa.callVariants());
                            result.add(toBlast6(pa, r, myKidDB) + "\t" + vnl + "\n");
                        } else {
                            result.add(toBlast6(pa, r, myKidDB) + "\n");
                        }
                    }
                }
            }
        }
        return result.toString();
    }


    public String
    resultsVariant(KidDatabaseMemory myKidDB, VariantDatabaseDisk vd){

        SmithWatermanAdvanced swa;

        pollKmersForPositions();
        calculateAlignmentTables();
        calculateSeeds();

        SuperString result = new SuperString();


        //opportunity to remove identical reference sequence requests
        //this is taken care of in the seeds combineSeeds step

        ArrayList<ReferenceSequenceRequest> refSeqReq = new ArrayList<>();
        //ArrayList<ReferenceSequenceRequest> fwdRefSeqReq = new ArrayList<>();

        for (Seed s : fwdSeeds){
            if(AlignmentKLAT1.DEBUG == true){
                System.err.println("\tSEED\t\t"+s);
                System.err.println("\tFASTKLAT\t"+ AlignmentKLAT1.calculateFastKlatScore(s));
            }
            if( AlignmentKLAT1.calculateFastKlatScore(s) >= KLATsettings.MIN_FAST_KLAT_SCORE) {
                refSeqReq.add(s.toRefSeqRequest(myKidDB, query.length()));
            }
        }

        for (int k=0; k<refSeqReq.size()-1; k++){
            for (int j=k+1; j<refSeqReq.size(); j++){
                if (ReferenceSequenceRequest.areEqual(refSeqReq.get(k),refSeqReq.get(j))){
                    refSeqReq.remove(j);
                    j--;
                }
            }
        }

        for (ReferenceSequenceRequest r : refSeqReq){
//            System.out.println("Current RefSequence Request\t"+r.toString());
            String str = r.getReferenceSequence();
            if (DEBUG) System.err.println("Seq \t" + str);

            if (!str.equals(DnaBitString.SENTINEL)) {
                swa = new SmithWatermanAdvanced(query, str);
                ArrayList<PartialAlignment1> pas = swa.bestAlignments();

                for (PartialAlignment1 pa : pas) {

                    ArrayList<Variant> vs = pa.callVariants();
                    for (Variant v: vs){
                        v = Variant.checkVariantInVariantDatabaseAndModify(v,vd);
                    }

                    //Variants must be called to fix any errors in gaps
                    String vnl = Variant.variantNameList(pa.callVariants());
                    result.add(toBlast6(pa, r, myKidDB) + "\t" + vnl + "\n");

                }
                bestAlignments.addAll(pas);
            }
        }


        refSeqReq = new ArrayList<>();

        for (Seed s : revSeeds){
            if(AlignmentKLAT1.DEBUG == true){
                System.err.println("\tSEED\t\t"+s);
                System.err.println("\tFASTKLAT\t"+ AlignmentKLAT1.calculateFastKlatScore(s));
            }
            if( AlignmentKLAT1.calculateFastKlatScore(s) >= KLATsettings.MIN_FAST_KLAT_SCORE) {
                refSeqReq.add(s.toRefSeqRequest(myKidDB, query.length()));
            }
        }

        for (int k=0; k<refSeqReq.size()-1; k++){
            for (int j=k+1; j<refSeqReq.size(); j++){
                if (ReferenceSequenceRequest.areEqual(refSeqReq.get(k),refSeqReq.get(j))){
                    refSeqReq.remove(j);
                    j--;
                }
            }
        }

        for (ReferenceSequenceRequest r : refSeqReq){
//            System.out.println("Current RefSequence Request\t"+r.toString());
            String str = r.getReferenceSequence();
            if (DEBUG) System.err.println("Seq \t" + str);

            if (!str.equals(DnaBitString.SENTINEL)) {
                swa = new SmithWatermanAdvanced(query, str);
                ArrayList<PartialAlignment1> pas = swa.bestAlignments();

                for (PartialAlignment1 pa : pas) {

                    ArrayList<Variant> vs = pa.callVariants();
                    for (Variant v: vs){
                        v = Variant.checkVariantInVariantDatabaseAndModify(v,vd);
                    }

                    //Variants must be called to fix any errors in gaps
                    String vnl = Variant.variantNameList(pa.callVariants());
                    result.add(toBlast6(pa, r, myKidDB) + "\t" + vnl + "\n");

                }
                bestAlignments.addAll(pas);
            }
        }

        return result.toString();
    }





    public void testPrintAlignments(KidDatabaseMemory myKidDB){
        SmithWatermanAdvanced swa;

        pollKmersForPositions();
        calculateAlignmentTables();
        calculateSeeds();

        System.out.println("\nAlignments");
        System.out.println("\tFORWARD");
        for (Seed s : fwdSeeds) {
            System.out.println("SEED "+s);
            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
            swa.printPrettyBestResults(System.out);

        }
        System.out.println("\tREVERSE");
        for (Seed s : revSeeds) {
            System.out.println("SEED "+s);
            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
            swa.printPrettyBestResults(System.out);
        }
    }


    /**
     * column labels for outformat7
     */
    public static String Blast6Header = "qseqid\tKLUE_ID\tpident\tlength\tmismatch\tgapopen\tqstart\tqend\tsstart\tsend\tevalue\tbitscore";

    /**
     * AlignmentKLAT1 results in BLAST outformat 6
     * This function takes one PartialAlignment1 and creates output for this alignment only
     *
     * Note that KLUE is 0-index system, but outformat 6 uses 1-indexing
     *
     * MOVED TO FileExport CLASS -- eventually remove
     * @param pa
     * @param t
     * @param myKidDB
     * @return
     */
    public String toBlast6(PartialAlignment1 pa, ReferenceSequenceRequest t, KidDatabaseMemory myKidDB) {
        int sstart, send;
        SuperString result = new SuperString();

        //String result = "";
        if (t.reverse) {
            //RECALL STOP is EXCLUSIVE, here we report INCLUSIVE
            sstart = t.stop - 1 - pa.sstart;
            send = t.stop - 1 - pa.send;
        } else {
            sstart = t.start + pa.sstart;
            send = t.start + pa.send;
        }

            //0
        //result+=queryName+"\t";

        result.addAndTrim(queryName);
        result.add("\t");
        //result+=t.myKID+"\t";

        //1
        if (KidDatabaseThreadSafe.ON == true){
            result.addAndTrim(t.myKID + "|" + KidDatabaseThreadSafe.getName(t.myKID));
        } else {
            result.addAndTrim(t.myKID + "|" + myKidDB.getName(t.myKID));
        }
        result.add("\t");

        //2
//        result+=pa.pident*100+"\t";
        result.addAndTrim(Double.toString(pa.pident*100));
        result.add("\t");

        //3
//        result+=pa.length+"\t";
        result.addAndTrim(Integer.toString(pa.length));
        result.add("\t");

        //4
//        result+=pa.mismatch+"\t";
        result.addAndTrim(Integer.toString(pa.mismatch));
        result.add("\t");

        //5
//        result+=pa.gapopen+"\t";
        result.addAndTrim(Integer.toString(pa.gapopen));
        result.add("\t");

        //6
//        result+=(pa.qstart+1)+"\t";
        result.addAndTrim(Integer.toString(pa.qstart+1));
        result.add("\t");

        //7
//        result+=(pa.qend+1)+"\t";
        result.addAndTrim(Integer.toString(pa.qend+1));
        result.add("\t");

        //8
//        result+=(sstart+1)+"\t";
        result.addAndTrim(Integer.toString(sstart+1));
        result.add("\t");

        //9
//        result+=(send+1)+"\t";
        result.addAndTrim(Integer.toString(send+1));
        result.add("\t");

        //10
//        result+=pa.evalue+"\t";
        result.addAndTrim(Double.toString(pa.evalue));
        result.add("\t");

        //11
//        result+=pa.bitscore+"\t";
        result.addAndTrim(Integer.toString(pa.bitscore));
        result.add("\t");

        //12
//        result+=pa.fastKLATscore+"";
        result.addAndTrim(Integer.toString(pa.fastKLATscore));
        //no delimiter, last

        //13
        //Called variants (optional)
        if (CALL_VARIANTS){
            result.add("\t");
//            result.addAndTrim(Arrays.toString(pa.callVariants().toArray()));
            result.addAndTrim(Variant.variantNameList(pa.callVariants()));

            //no delimiter, last
        }

        return result.toString();
    }


    public static void main(String[] args) {
        MemoryKlueTreeMap klue = new MemoryKlueTreeMap();

        KidDatabaseMemory myKidDB = new KidDatabaseMemory();
        myKidDB = KidDatabaseMemory.loadFromFileUnsafe(myKidDB.fileName);
        myKidDB.add(new Kid(">Voldemort crushing test"));
        myKidDB.add(new Kid(">gi|167006425|ref|NC_010314.1| 222 Abaca bunchy top virus DNA-N, complete genome"));
        myKidDB.add(new Kid(">IamSoBacon"));
        myKidDB.add(new Kid(">Adjacency Challenge"));


        KlueDataImport kdi = new KlueDataImport(klue, myKidDB);

        String inputfile = "/mnt/Dshare/fasta/test.fna";
        String skipString = "";
        try {
            String currentSeq = "";
            boolean ignore = true; //do not write empty sequence to database
            kdi.readFNAfileSKIPto(inputfile, skipString);
        } catch (IOException e) {

            e.printStackTrace();
            System.exit(0);
        }

        try {
            myKidDB.importFNAold(inputfile);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("\n\n\tIn memory test version KLUE initialized");

        String query;
        AlignmentKLAT1 alig;
        String source;
        SmithWatermanAdvanced swa;
        SmithWatermanOriginal sw;
        String ref;
        ArrayList<Seed> fwdSeeds, revSeeds;

        Kmer31.SUPPRESS_WARNINGS=true;


        System.out.println("\nVerifying KLUE is initialized");

        System.out.println(klue.get(new Kmer31("CGACTACTATTATTTTGAAGGACAATCCAGT").toLong()));


        System.out.println("\n * # * # * # * # * # * # * # * # * # * # * # * # * # * # * # * #\n");
        //First half of this sequence occurs at least twice in one alignment
        System.out.println("\nTesting sequence in multiple locations in sequence");
        query="GTCGTTCCATTCGGAGGGTATGGATATCATAACGACTACTATTATTTTGAAGGACAATCCAGT";
        alig = new AlignmentKLAT1(query,"multiple locations", klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        alig.pollKmersForPositions();
        alig.calculateAlignmentTables();
        System.out.println("\t***\tTest AlignmentKLAT1 Table IS:");
        System.out.println(alig.toString());

        System.out.println("\n\tReverse Seed Matches");
        System.out.println("None");

        System.out.println("\nTesting finding alignment seeds");
        System.out.println( Arrays.toString(alig.getForwardSeeds().toArray()) );
        System.out.println( Arrays.toString(alig.getReverseSeeds().toArray()) );

        System.out.println("\nTesting sequence in multiple locations in sequence (AGAIN)");
        query = "ATCCATTCCGTCATACACGCTAACCGGGAACAAAATCAATCTATCATGCACCAGATGTCCCGGACAAGAT";
        alig = new AlignmentKLAT1(query,"multiple locations 2",klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        alig.pollKmersForPositions();
        alig.calculateAlignmentTables();
        System.out.println("\t***\tTest AlignmentKLAT1 Table IS:");
        System.out.println(alig.toString());

        System.out.println("\nTesting gap in middle");
        query="GTCGTTCCATTCGGAGGGTATGGATATCATA_gap_7_ACGACTACTATTATTTTGAAGGACAATCCAGT";
        alig = new AlignmentKLAT1(query,"gap in middle", klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
        alig.testAll(myKidDB);


        System.out.println("\nTesting gap at start");
        query="TCGTTCCATTCGGAGGGTATGGATATCATA_gap_7_ACGACTACTATTATTTTGAAGGACAATCCAGT";
        alig = new AlignmentKLAT1(query,"gap at start", klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
        alig.testAll(myKidDB);


        System.out.println("\nTesting gap at end middle");
        query="GTCGTTCCATTCGGAGGGTATGGATATCATA_gap_7_ACGACTACTATTATTTTGAAGGACAATCC";
        alig = new AlignmentKLAT1(query,"gap at end",klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
        alig.testAll(myKidDB);

        System.out.println("\nTesting many gaps, Reverse strand");
        //Added to fake database, so this contains a reverse sequence also
        query = "WCAGCAACAATTGTAATCAAGAGTGCGATATCAAGTGTTATGTAGTATGTAATTTAAGAATTAAGGAATAA" +
                "WATTGTTGCCGAAGGTCTGTTATTTGAATGTTGAGATAAGGAAAGGGGCGGCGAAGCATGTGTGTATAATW";
        source = "CAGCAACAATTGTAATCAAGAGTGCGATATCAAGTGTTATGTAGTATGTAATTTAAGAATTAAGGAATAA" +
                "WATTGTTGCCGAAGGTCTGTTATTTGAATGTTGAGATAAGGAAAGGGGCGGCGAAGCATGTGTGTATAAT" +
                "AACATAT";
        alig = new AlignmentKLAT1(query,"gaps with reverse",klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        System.out.println("Checking number gaps :: "+alig.kmers.gaps.size());
        System.out.println("Gap size :: "+alig.kmers.gaps.get(0).length);
        alig.testAll(myKidDB);

        System.out.println("\nSuperHard test Case");
        System.out.println("There are fewer hits in middle for second alignment, so adjacent hits are not in same row.");

        query = "GAGTTTTTTGGAGACGTCGAGGAAGACAATTTGACCCCCGTGTGAACTCACAAAGGTCGAATAGAGGTCA";
        alig = new AlignmentKLAT1(query,"superhard test",klue);
        System.out.println("Checking query isValid() :: "+alig.isValid());
        alig.testAll(myKidDB);
    }


    /**
     * Note the assumption that seed is full adjacent may not be valid
     * @param s
     * @return
     */
    public static Double calculateFastKlatScore(Seed s){
        return Seed.calculateFastKlatScoreAdjacentSeed(s);
//        float result = s.hits + s.adjacency;
//        result /= 2;
//        return result;
    }

    public ArrayList<PartialAlignment1> bestAlignments() {
        return bestAlignments;
    }
}