package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.*;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabase;
import org.cchmc.kluesuite.masterklue.KLATsettings;

import java.util.*;
import java.util.zip.DataFormatException;

/**
 * Created by jwc on 8/30/17.
 *
 *  2018.04.20
 *  AlignmentKLAT2 uses KLAT2 classes, but follows the footsteps of AlignmentKLAT1; much code is borrowed
 *
 * Seed agglomeration
 *  Adjacent seeds ==> Seed that is "long seed" (all adjacent)          CombineAdjacentSeeds
 *  "long seeds" get combined to SuperSeeds (destructively)             AgglomerateSeeds
 *  remaining seeds combined combinatorically
 *
 *
 * FUNCTIONS IN ORDER TO USE
 *
 * alignments = calculateFullAlignments();
 * for each in alignments: ...
 *
 */
public class AlignmentKLAT2 implements KLAT {
    protected int KMER_SIZE;
    KidDatabase kd;

    public AlignmentKLAT2(String query, String queryName, KLUE klue, int kmer_size, KidDatabase kd) {
        this.kd = kd;
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
        // calculateSeeds
        bestAlignments = new ArrayList<PartialAlignment>();
        this.KMER_SIZE = kmer_size;
    }

    /**
     * Calculate everything and report
     */
    public ArrayList<PartialAlignment> calculateFullAlignments(){
        pollKmersForPositions();
        generateSeeds();
        calculateBestAlignments();
        return bestAlignments;
    }

    /**
     * lookup all constituent Kmers of the Query
     */
    protected void pollKmersForPositions() {
        posz = klue.getAllPL(kmers.getAllForward());
        poszRev = klue.getAllPL(kmers.getAllReverse());
    }

    /**
     * Alternate, faster implementation?
     * #ISSUE 99
     */
    protected void generateSeeds(){
        ArrayList<Seed> fSeeds = new ArrayList<>();
        ArrayList<Seed> rSeeds = new ArrayList<>();
        ArrayList<SuperSeed> superSeeds = new ArrayList<>();


        PositionList curr;
        Position p;
        Seed s;
        int queryCoor = 0;

        // #############
        //   Process Combining Adjacent Seeds
        // #############


            // #############
            //   Build map of found positions
            // #############

        HashMap<SeedCoordinate, Boolean> forward, reverse;
        forward = new HashMap<SeedCoordinate, Boolean>();
        reverse = new HashMap<SeedCoordinate, Boolean>();

        Iterator<PositionList> it = posz.iterator();

        while (it.hasNext()) {
            curr = it.next();
            if (curr != null) {
                Iterator<Position> itP = curr.iterator();
                while (itP.hasNext()) {
                    p = itP.next();
                    //public Seed(int qStart, int qEnd, Position pos, int myKid) {
                    //s = new Seed (queryCoor, queryCoor+1, p, p.getMyKID());
                    if (p.getFlag(Position.REVERSE)){
                        reverse.put(new SeedCoordinate(p.getMyKID(),queryCoor,p.getPosition()),true);
                    } else {
                        forward.put(new SeedCoordinate(p.getMyKID(),queryCoor,p.getPosition()),true);
                    }
                }
            }
            queryCoor++;
        }


            // #############
            //   Iterate over keys to construct matching consecutive keys
            // #############

        fSeeds = combineAdjacentSeeds(forward, false);
        //by accident, middle of seed reading frame could be called
        //checking validity
//        fSeeds = combineAdjacentLongSeeds(fSeeds);

//        for (SeedCoordinate key : reverse.keySet()){
//            exists = reverse.get(key);
//            if((exists != null && exists)) {
//                starter = key;
//                next = 1;
//                ender = new SeedCoordinate(starter, next, true);
//                exists = forward.get(ender);
//                while (exists != null && exists) {
//                    forward.put(ender, true); //marked so not used again
//                    next++;
//                    ender = new SeedCoordinate(starter, next, true);
//                    exists = forward.get(ender);
//                }
//                //write out seed
//                rSeeds.add(new Seed(starter.nextOffset, ender.nextOffset, starter.y, ender.y, true, next, next, key.kid));
//            }
//        }

        //by accident, middle of seed reading frame could be called
        //checking validity
        rSeeds = combineAdjacentSeeds(reverse, true);
//        rSeeds = combineAdjacentLongSeeds(rSeeds);

        fwdSeeds = agglomerateLongSeeds(fSeeds, KMER_SIZE);
        revSeeds = agglomerateLongSeeds(rSeeds, KMER_SIZE);
    }

    private ArrayList<Seed> combineAdjacentSeeds(HashMap<SeedCoordinate, Boolean> map, boolean reverse) {
        ArrayList<Seed> result = new ArrayList<>();

        int next, prev;
        Boolean exists;
        SeedCoordinate starter, ender, previous, finish;


        //loop over keys, check if next position is in the list (by hashing)
        for (SeedCoordinate key : map.keySet()){
            exists = map.get(key);
            if((exists != null && exists)) {
                starter = key;
                next = 1;
                ender = new SeedCoordinate(starter, next, false);
                exists = map.get(ender);
                while (exists != null && exists) {
                    map.put(ender, false); //marked so not used again
                    next++;
                    ender = new SeedCoordinate(starter, next, false);
                    exists = map.get(ender);
                }
                prev = -1;
                previous = new SeedCoordinate(starter, prev, false);
                exists = map.get(previous);
                while (exists != null && exists) {
                    //write out seed
                    map.put(previous, false); //marked so not used again
                    prev--;
                    previous = new SeedCoordinate(starter, prev, false);
                    exists = map.get(previous);
                }
                //move back one
                prev++;

                result.add(new Seed(starter.queryPos +prev, ender.queryPos, starter.refPos+prev, ender.refPos, false, next-prev, next-prev, key.kid));
            }
        }
        return result;
    }

//    private ArrayList<Seed> combineAdjacentLongSeeds(ArrayList<Seed> seeds) {
//        for(int k=0; k < seeds.size()-1; k++){
//            for (int j=k+1; j < seeds.size(); j++){
//                Seed a = seeds.get(k);
//                Seed b = seeds.get(j);
//                if (Seed.isAdjacent(a,b)){
//                    seeds.set(k, Seed.merge(a, b));
//                }
//                seeds.remove(j);
//                j--;
//            }
//        }
//
//        return seeds;
//    }


//    /**
//     * Helper function
//     *  modified from AlignmentKlat1
//     * @return  SORTED list of SuperSeeds that have been agglomerated to summarize best possible alignment combinations
//     */
//
//    protected ArrayList<SuperSeed> generateSeeds(Position[][] table, ArrayList<Integer> rows, int numCols, boolean reverse) {
//        int numRows = rows.size();
////        ArrayList<SuperSeed> result = new ArrayList<SuperSeed>();
//
//        //go through rows, generate seeds for each KID
//
//        //4.1)	For each row, calculate streaks, where seeds are obviously adjacent to each other WITHIN the ROW
//        int lastKid = -1;       //SENTINEL
//        int myKid = 0;          //SENTINEL
//
//        ArrayList<Seed> longSeeds = new ArrayList<Seed>();
//        ArrayList<SuperSeed> superSeeds = new ArrayList<SuperSeed>();
//
//
//        // Build list of long seeds (agglomerated adjacent seeds within a row meeting minimums)
//        if (rows.size() > 0) {
//            myKid = rows.get(0);
//            lastKid = myKid;
//            longSeeds.addAll(combineAdjacentSeeds(table, 0, numCols, reverse, myKid));
//        }
//
//        int kidStreakStart = 0;
//        int r;
//        for (r = 1; r < rows.size(); r++) {
//            myKid = rows.get(r);
//
//            // if starting a block of rows with a new KID
//            // then record all seeds so far out, reset for new row(s)
//            if (myKid != lastKid) {
//                // ***** this is where we combine across rows  ******
//                superSeeds.addAll(agglomerateLongSeeds(longSeeds, KMER_SIZE));
//
//                //reset for next set
//                longSeeds = combineAdjacentSeeds(table, r, numCols, reverse, myKid);
//
//                //consecutive seeds already combined across rows
//                //tempSeeds = Seed.combineConsecutiveSeeds(tempSeeds);
//                lastKid = myKid;
//                kidStreakStart = r;
//
//            } else {
//                // Build list of long seeds (agglomerated adjacent seeds within a row meeting minimums)
//                longSeeds.addAll(combineAdjacentSeeds(table, 0, numCols, reverse, myKid));
//            }
//        }
//
//        //process last
//        superSeeds.addAll(agglomerateLongSeeds(longSeeds, KMER_SIZE));
//
//        return superSeeds;
//    }
//
//    /**
//     *
//     *
//     * @param table
//     * @param r
//     * @param numCols
//     * @param reverse
//     * @param myKid
//     * @return  List of "long seeds", i.e. seeds of adjacency streaks
//     */
//    protected ArrayList<Seed> combineAdjacentSeeds(Position[][] table, int r, int numCols, boolean reverse, int myKid) {
//        //minimums ==> combine based on minimums?
//
//        //Initialize adjacent seeds
//        Seed streakerStart = new Seed(), temp = new Seed(), prev = new Seed();
//        int streakStart = -1;
//        Position pos;
//        ArrayList<Seed> tempSeeds = new ArrayList<Seed>();
//
//
//        //streakStart = -1 means no streak currently
//        for (
//                int c = 0;
//                c < numCols; c++)
//
//        {
//            //If there is a position to consider as a seed
//            if (table[r][c] != null) {
//                pos = table[r][c];
//
//                // If first streak defined / first hit in table
//                if (streakStart == -1) {
//                    streakStart = c;
//                    streakerStart = new Seed(c, c + 1, pos, myKid);
//
//                    prev = new Seed(c, c + 1, pos, myKid);
//                    if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//                } else {
//
//                    temp = new Seed(c, c + 1, pos, myKid);
//
//                    //If Seed is not adjacent, addAndTrim last streak to list, otherwise streak continues
//                    if (!Seed.isAdjacent(prev, temp)){
//                        // create seed ending the streak
//                        //MIN_SEED_ADJACENCY and //MIN_SEED_HITS enforced
//                        addSeedIfMinimumsMet(tempSeeds, streakerStart, prev, myKid);
//                        streakStart = c; //new streak
//                        streakerStart = new Seed(c, c + 1, pos, myKid);
//                        if (DEBUG) System.err.println("Streaker start\t" + streakerStart);
//
//                    }
//                    prev = temp;
//
//                }
//            } else {    // else entry in table is blank
//                //create seed ending the streak
//                if (streakStart != -1) {
//                    tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);  //prev, because temp is not in streak
//                    prev = null;
//                }
//                streakStart = -1; // no streak; current alignment is null
//            }
//        }
//        //record final streak
//        prev = temp; //bug -- ommitted line -- fixed 08-03
//        int l = (prev.queryStart - streakerStart.queryStart) + 1;
//        if (streakStart != -1) {
//            if (streakerStart.equals(prev) || prev.myKid == 0){
//                tempSeeds.add(streakerStart);
//            } else {
//                tempSeeds = addSeed(tempSeeds, streakerStart, prev, myKid);
//            }
//        }
//
//        //tempseeds now contains agglomerated seeds for this row by adjacency
//        return tempSeeds;
//    }

    /**
     * All seeds are "long seeds" meaning they have already been combined by adjacency.
     * @param inSeeds
     * @return
     */
    public static ArrayList<SuperSeed> agglomerateLongSeeds(ArrayList<Seed> inSeeds, int kmersize){


        ArrayList<SuperSeed> results = new ArrayList<>();


        ArrayList<SuperSeed> temp1;
        ArrayList<SuperSeed> tempSeeds = new ArrayList<>();

        for (Seed s : inSeeds){
            tempSeeds.add(new SuperSeed(kmersize, s));
        }


        //complete: AGGLOMERATE ADJACENT SEEDS



        // #we know distance >1, but do not need to force this, it is already true
        // this process destructively removes them

        //EXPERIMENTAL PARAMATER
        KLATsettings.EXCLUSIVE_SEED_AGGLOMERATION_DISTANCE = 10;

        //loop until no more changes are made
        //TODO does this need to be done only once?
        int sz=0;
        while (tempSeeds.size() != sz) {
            sz = tempSeeds.size();

            //contains list of new seeds


            for (int k = 0; k < tempSeeds.size()  - 1; k++) {
                for (int m = k + 1; m < tempSeeds.size(); m++) {
                    if (SuperSeed.mayMergeConsecutiveSeeds(inSeeds.get(k), inSeeds.get(m),
                            0, //adjacency allowed
                            KLATsettings.EXCLUSIVE_SEED_AGGLOMERATION_DISTANCE) //saves computation by merging nearby seeds first
                            )
                    {
                        try {
                            tempSeeds.set(k,SuperSeed.mergeIfAble(inSeeds.get(k), inSeeds.get(m), kmersize));
                            tempSeeds.remove(m);
                            //continue on from m; counteract the increment
                            m--;
                        } catch (DataFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        sz = 0;  //else cannot enter loop

        //combine seeds with greater distance, combinatorically
        //does not do so destructively
        // TODO -- fix  THIS IS NOT COMBINATORIAL
        // TODO NEED TO CONSIDER chaining?
        while (tempSeeds.size() != sz) {
            sz = tempSeeds.size();

            for (int k = 0; k < sz - 1; k++) {
                for (int m = k + 1; m < sz; k++) {
                    if (Seed.mayMergeConsecutiveSeeds(tempSeeds.get(k), tempSeeds.get(m), 1, KLATsettings.WHISKERS_LENGTH_ALIGNMENT)) {
                        try {
                            //results.add(SuperSeed.mergeIfAble(tempSeeds.get(k), tempSeeds.get(m), kmersize));
                            results.add(SuperSeed.mergeIfAble(inSeeds.get(k), inSeeds.get(m), kmersize));
                        } catch (DataFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


//        return results;
        return results;
    }


//    /**
//     * Generate alignment seeds on demand in FORWARD direction
//     *
//     * @return
//     */
//    public ArrayList<SuperSeed> getForwardSeeds() {
//        return generateSeeds(alignmentTable, listKidRows, kmers.indexToInternalIndex.length, false);
//    }
//
//    /**
//     * Generate alignment seeds on demand in REVERSE direction
//     *
//     * @return
//     */
//    public ArrayList<SuperSeed> getReverseSeeds() {
//        return generateSeeds(alignmentTableRev, listKidRowsRev, kmers.indexToInternalIndex.length, true);
//    }


    //PREVIOUS CODE
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
    public ArrayList<SuperSeed> fwdSeeds, revSeeds;

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
    protected ArrayList<PartialAlignment> bestAlignments;

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
     */
    public void printSeeds() {
        System.out.println("\nFinal seeds");
        for (Seed s : fwdSeeds) {
            System.out.println(s);
        }
        for (Seed s : revSeeds) {
            System.out.println(s);
        }
    }
//
//    /***
//     * First in several functions to process alignment
//     * This builds the alignment tables of looked up kmer values
//     */
//    public void calculateAlignmentTables() {
//
//        if (!kmers.isValid()) {
//            System.err.println("\tWarning: trying to do alignment on an empty sequence");
//        } else {
//            //count number of gaps
//
//            //hits always come before and after gaps
//            // this is critical about saving space / compute time:
//            //     kmers does not return the invalid kmer31 subsequences that are invalid
//            // so every step has to loop over every "run" between gaps
//            int numberRuns = kmers.gaps.size() + 1;
//
//
//            //we need to calculate and store statistics on alignments for each KID reported
//            //we also need to track possible multiple alignments
//
//            //  3.1)	Sort positions into forward and reverse kmers, counting them
//
//
//            //BUG: was resetting this inside the loop
//
//
//
//            //Establish the number of rows in the alignment table
//            for (int r = 0; r < numberRuns; r++) {
//                int runStop = runStop(r);
//                int runStart = runStart(r);
//                PositionList temp;
//
//                //for each lookup position
//                for (int k = runStart(r); k < runStop; k++) {
//
//                    HashMap<Integer, Integer> tempKIDtoCount = new HashMap<Integer, Integer>();
//                    HashMap<Integer, Integer> tempKIDtoCountRev = new HashMap<Integer, Integer>();
//
//                    temp = posz.get(kmers.indexToInternalIndex[k]);
//                    if (temp == null) temp = new PositionList();
//
//                    //for each alignment a position
//                    for (int m = 0; m < temp.length(); m++) {
//                        int nextOffset = temp.get(m).getMyKID();
//                        if (temp.get(m).getFlag(Position.REVERSE)) {
//                            //If reverse kmer
//                            if (tempKIDtoCountRev.containsKey(nextOffset)) {
//                                tempKIDtoCountRev.put(nextOffset, tempKIDtoCountRev.get(nextOffset) + 1);
//                            } else {
//                                tempKIDtoCountRev.put(nextOffset, 1);
//                            }
//                        } else {
//                            if (tempKIDtoCount.containsKey(nextOffset)) {
//                                tempKIDtoCount.put(nextOffset, tempKIDtoCount.get(nextOffset) + 1);
//                            } else {
//                                tempKIDtoCount.put(nextOffset, 1);
//                            }
//                        }
//                    }
//
//                    //for each lookup position
//
//                    //Update number of rows needed.  IN FORWARD
//                    for (int m : tempKIDtoCount.keySet()) {
//                        if (KIDtoCount.containsKey(m)) {
//                            KIDtoCount.put(m, Math.max(KIDtoCount.get(m), tempKIDtoCount.get(m)));
//                        } else {
//                            KIDtoCount.put(m, tempKIDtoCount.get(m));
//                        }
//                    }
//
//                    //Update number of rows needed.  IN REVERSE
//                    for (int m : tempKIDtoCountRev.keySet()) {
//                        if (KIDtoCountRev.containsKey(m)) {
//                            KIDtoCountRev.put(m, Math.max(KIDtoCountRev.get(m), tempKIDtoCountRev.get(m)));
//                        } else {
//                            KIDtoCountRev.put(m, tempKIDtoCountRev.get(m));
//                        }
//                    }
//
//                } //end for k
//            } // end for r
//
//            // 3.2)	Summarize number of rows needed & label appropriately
//            calculateKidFound();
//
//            //now listKidRows.length() is the number of rows of our table
//
//            alignmentTable = new Position[listKidRows.size()][kmers.indexToInternalIndex.length];
//            alignmentTableRev = new Position[listKidRowsRev.size()][kmers.indexToInternalIndex.length];
//
//            for (int r = 0; r < numberRuns; r++) {
//                for (int k = runStart(r); k < runStop(r); k++) {
//                    PositionList temp = posz.get(kmers.indexToInternalIndex[k]);
//                    if (temp == null) temp = new PositionList();
//                    for (int m = 0; m < temp.length(); m++) {
//                        Position here = new Position(temp.get(m));
//                        //Sort on FORWARD/REVERSE
//                        if (here.getFlag(Position.REVERSE)) {
//                            int nextOffset = listKidRowsRev.indexOf(here.getMyKID());
//                            int maxX = (nextOffset + KIDtoCountRev.get(here.getMyKID()));
//                            for (; nextOffset < maxX; nextOffset++) {
//                                //write object to correct column and row
//                                //
//                                if (alignmentTableRev[nextOffset][kmers.indexToInternalIndex[k]] == null) {
//                                    alignmentTableRev[nextOffset][kmers.indexToInternalIndex[k]] = here;
//                                    break;
//                                } else if (nextOffset == maxX - 1) {
//                                    System.err.println(" (This should never happen) :: Building alignment table, no room for : " + here);
//                                }
//                            }
//                        } else {
//                            int nextOffset = listKidRows.indexOf(here.getMyKID());
//                            int maxX = (nextOffset + KIDtoCount.get(here.getMyKID()));
//                            for (; nextOffset < maxX; nextOffset++) {
//                                //write object to correct column and row
//                                //
//                                if (alignmentTable[nextOffset][kmers.indexToInternalIndex[k]] == null) {
//                                    alignmentTable[nextOffset][kmers.indexToInternalIndex[k]] = here;
//                                    break;
//                                } else if (nextOffset == maxX - 1) {
//                                    System.err.println(" (This should never happen) :: Building alignment table, no room for : " + here);
//                                }
//                            }
//                        }
//                    } // end for
//
//                } //end for k
//            } //end for r
//
//            for (int key : KIDtoCount.keySet()) {
//                int number = KIDtoCount.get(key);
//                if (number > 1) {
//                    //must check every entry to see if they are sorted
//                    int row = listKidRows.indexOf(key);
//                    for (int c = 0; c < kmers.internalIndexToIndex.length; c++) {
//                        sortMultipleKid(row, c, number);
//                    }
//                }
//            } // rows of duplicate entries now sorted
//
//        } // end else isValid() == true
//        calculated = true;
//    } //end calculateAlignmentTables


//    /**
//     * function for multi-step analysis
//     * Once calculateAlignmentTables has constructed alignment tables, seeds can be found
//     */
//    public void calculateSeeds() {
//        fwdSeeds = getForwardSeeds();
//        revSeeds = getReverseSeeds();
//    }

//    /**
//     * This takes possibility of multiple maps to same KID in alignmentTable and sorts by position number.
//     *
//     * @param row    first row where KID occurs
//     * @param col    column to sort
//     * @param number number of entries to sort
//     */
//    private void sortMultipleKid(int row, int col, int number) {
//        int lastExclusive = -1;
//        for (int k = row; k < row + number; k++) {
//            k = k;
//            if (alignmentTable[k][col] != null) lastExclusive = k + 1;
//        }
//
//        //bubble sort
//        Position swap;
//        for (int r = row; r < lastExclusive; r++) {
//            for (int rr = r + 1; rr < lastExclusive; rr++) {
//                if ( alignmentTable[rr][col] != null && alignmentTable[r][col] != null &&
//                        alignmentTable[rr][col].getPosition() < alignmentTable[r][col].getPosition()) {
//                    swap = alignmentTable[rr][col];
//                    alignmentTable[rr][col] = alignmentTable[r][col];
//                    alignmentTable[r][col] = swap;
//                }
//            }
//        }
//    }
//
//    private void calculateKidFound() {
//        for (int m : KIDtoCount.keySet()) {
//            for (int nextOffset = 0; nextOffset < KIDtoCount.get(m); nextOffset++) {
//                listKidRows.add(m);
//            }
//        }
//        Collections.sort(listKidRows);
//
//        for (int m : KIDtoCountRev.keySet()) {
//            for (int nextOffset = 0; nextOffset < KIDtoCountRev.get(m); nextOffset++) {
//                listKidRowsRev.add(m);
//            }
//        }
//        Collections.sort(listKidRowsRev);
//    }

//    private int runStart(int run) {
//        //hits always come before and after gaps
//
//        if (run == 0) {
//            return 0;
//        } else {
//            return kmers.gaps.get(run - 1).nextIndexAfterGap();
//        }
//    }


//    /**
//     * End of the run, EXCLUSIVE
//     * RUNS are inbetween gaps
//     *
//     * @param run
//     * @return
//     */
//    private int runStop(int run) {
//        //hits always come before and after gaps
//        if (run == kmers.gaps.size()) {
//            return kmers.getLength();
//        } else {
//            return kmers.gaps.get(run).pos; //gap starts
//        }
//    }

//    /**
//     * human readable
//     *
//     * @return
//     */
//    public String toString() {
//        if (!kmers.isValid()) {
//            System.err.println("\tWarning: trying to do alignment on an empty sequence");
//            return "EMPTY_SEQUENCE";
//        } else {
//            String result = "\tFULL printout of AlignmentKLAT1 class for\n\t\t" + query + "\n";
//
//            result += "\tFORWARD direction\n";
//            result += "\t\tKidCount :: [";
//            for (int k : KIDtoCount.keySet()) {
//                result += Integer.toString(k) + " : " + KIDtoCount.get(k) + ", ";
//            }
//            result += "]\n";
//            result += tableToString();
//
//
//            result += "\tREVERSE direction\n";
//            result += "\t\tKidCountRev :: [";
//
//            for (int k : KIDtoCountRev.keySet()) {
//                result += Integer.toString(k) + " : " + KIDtoCount.get(k) + ", ";
//            }
//            result += "]\n";
//            result += tableRevToString();
//
//            return result;
//        }
//    }


//    /**
//     * Creates string representing the table.  helper function
//     *
//     * @return
//     */
//    private String tableToString() {
//        String result = "";
//        result += "\t";
//        //result += String.format("%1$8s","")+"\t\t";
//        int z;
//        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
//            result += "|GAP|\t";
//            z = 0;    //start on first entry
//        } else {
//            result += Integer.toString(kmers.internalIndexToIndex[0]) + "\t";
//            z = 1;    //start on second entry
//            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
//        }
//        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
//            result += Integer.toString(kmers.internalIndexToIndex[k]) + "\t";
//            //check for gap; short circuit to not read off array
//            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
//                result += "|GAP|\t";
//        }
//        //addAndTrim last
//        result += "\n";
//
//
//        //KMER column headers
//        result += "\t";
//        //result += String.format("%1$8s","kid")+"\t\t";
//        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
//            result += "|GAP|\t";
//            z = 0;    //start on first entry
//        } else {
//            result += kmers.get(0) + "\t";
//            z = 1;    //start on second entry
//            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
//        }
//        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
//            result += kmers.get(kmers.internalIndexToIndex[k]) + "\t";
//            //check for gap; short circuit to not read off array
//            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
//                result += "|GAP|\t";
//        }
//        //addAndTrim last
//        result += "\n";
//
//
//        if (listKidRows.size() == 0) result += "\tNONE FOUND\n";
//
//        for (int r = 0; r < listKidRows.size(); r++) {
//            //result += String.format("%1$8s","kid")+"\t"+listKidRows.get(r) + "\t";
//            result += listKidRows.get(r) + "\t";
//            if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
//                result += "|GAP|\t";
//                z = 0;
//            } else {
//                //System.err.println("Accessing row "+r+" position 0");
//                Position t = alignmentTable[r][0];
//                if (t != null) {
//                    result += t.toString() + "\t";
//                } else {
//                    result += "null\t";
//                }
//                z = 1;
//                if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
//            }
//
//            for (int c = z; c < kmers.internalIndexToIndex.length; c++) {
//                if (alignmentTable[r][c] == null) result += "null\t";
//                else result += alignmentTable[r][c].toString() + "\t";
//                //check for gap; short circuit to not read off array
//                if ((c + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[c + 1] - kmers.internalIndexToIndex[c] > 1)
//                    result += "|GAP|\t";
//            }
//            result += "\n";
//        }
//
//        return result;
//    }


//    /**
//     * String version of reverse direction table of matching Kmers looked up in REVERSE direction
//     * helper function
//     *
//     * @return
//     */
//    public String tableRevToString() {
//        String result = "";
//
//        //table
//        result += "\t";
//
//        int z;
//        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
//            result += "|GAP|\t";
//            z = 0;    //start on first entry
//        } else {
//            result += Integer.toString(kmers.internalIndexToIndex[0]) + "\t";
//            z = 1;    //start on second entry
//            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
//        }
//        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
//            result += Integer.toString(kmers.internalIndexToIndex[k]) + "\t";
//            //check for gap; short circuit to not read off array
//            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
//                result += "|GAP|\t";
//        }
//        //addAndTrim last
//        result += "\n";
//
//        result += "\t";
//        //KMER column headers
//        if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
//            result += "|GAP|\t";
//            z = 0;    //start on first entry
//        } else {
//            result += kmers.get(0).reverseStrand() + "\t";
//            z = 1;    //start on second entry
//            if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
//        }
//        for (int k = z; k < kmers.internalIndexToIndex.length; k++) {
//            result += kmers.get(kmers.internalIndexToIndex[k]).reverseStrand() + "\t";
//            //check for gap; short circuit to not read off array
//            if ((k + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[k + 1] - kmers.internalIndexToIndex[k] > 1)
//                result += "|GAP|\t";
//        }
//        //addAndTrim last
//        result += "\n";
//
//        if (listKidRowsRev.size() == 0) result += "\tNONE FOUND\n";
//
//        for (int r = 0; r < listKidRowsRev.size(); r++) {
//            result += listKidRowsRev.get(r) + "\t";
//            if (kmers.internalIndexToIndex[0] != 0) {  //start with gap
//                result += "|GAP|\t";
//                z = 0;
//            } else {
//                if (alignmentTableRev[r][0] == null) result += "null\t";
//                else result += alignmentTableRev[r][0].toString() + "\t";
//                z = 1;
//                if (kmers.indexToInternalIndex[1] == -1) result += "|GAP|\t";
//            }
//
//            for (int c = z; c < kmers.internalIndexToIndex.length; c++) {
//                if (alignmentTableRev[r][c] == null) result += "null\t";
//                else result += alignmentTableRev[r][c].toString() + "\t";
//                //check for gap; short circuit to not read off array
//                if ((c + 1) != kmers.internalIndexToIndex.length && kmers.internalIndexToIndex[c + 1] - kmers.internalIndexToIndex[c] > 1)
//                    result += "|GAP|\t";
//            }
//            result += "\n";
//        }
//        return result;
//    }



//    protected void addSeedIfMinimumsMet(ArrayList<Seed> tempSeeds, Seed streakerStart, Seed prev, int myKid) {
//        int l = (prev.queryStart - streakerStart.queryStart) + 1;
////        if (l >= KLATSettingsOLD.MIN_SEED_ADJACENCY && l >= KLATSettingsOLD.MIN_SEED_HITS) {
//        if (l >= KLATsettings.MIN_SEED_ADJACENCY && l >= KLATsettings.MIN_SEED_HITS) {
//            boolean snp = streakerStart.snp || prev.snp;
//            boolean indel = streakerStart.indel || prev.indel;
//            boolean reverse = streakerStart.isReverse;
//            if (streakerStart.isReverse != prev.isReverse) {
//                System.err.println("\tWARNING\t two adjacent seeds; one reverse one forward.  SHOULD NOT HAPPEN");
//            }
//            tempSeeds.add(new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
//                    prev.end, reverse, l, l, myKid, snp, indel));
//            if (DEBUG)
//                System.err.println("New Seed \t" + new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
//                        prev.end, reverse, l, l, myKid, snp, indel));
//        }
//        return;
//    }

//    protected ArrayList<Seed> addSeed(ArrayList<Seed> tempSeeds, Seed streakerStart, Seed prev, int myKid) {
//
//        if (prev == null){
//            tempSeeds.add(streakerStart);
//        } else {
//            int l = (prev.queryStart - streakerStart.queryStart) + 1;
//
//            boolean snp = streakerStart.snp || prev.snp;
//            boolean indel = streakerStart.indel || prev.indel;
//            boolean reverse = streakerStart.isReverse;
//            if (streakerStart.isReverse != prev.isReverse) {
//                System.err.println("\tWARNING\t two adjacent seeds; one reverse one forward.  SHOULD NOT HAPPEN");
//            }
//            tempSeeds.add(new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
//                    prev.end, reverse, l, l, myKid, snp, indel));
//            if (DEBUG)
//                System.err.println("New Seed \t" + new Seed(streakerStart.queryStart, prev.queryEnd, streakerStart.start,
//                        prev.end, reverse, l, l, myKid, snp, indel));
//        }
//
//        return tempSeeds;
//    }

    @Override
    public SuperString alignmentsToBlast6(){
        SuperString result = new SuperString();
        for (PartialAlignment pa : bestAlignments) {
            result.add(toBlast6(pa) + "\n");
        }
        return result;
    }

    private String toBlast6(PartialAlignment pa) {
        int sstart,     //reference sequence START  (1-index, inclusive)
                send;   //reference sequence END    (1-index, inclusive)
        SuperString result = new SuperString();

        //TODO : write function without refSequence t
        //WTF is this for?  why does pa not hold correct value?

        sstart = pa.sstart;
        send = pa.send;
//        if (t.reverse) {
//            //RECALL STOP is EXCLUSIVE, here we report INCLUSIVE
//            sstart = t.stop - 1 - pa.sstart;
//            send = t.stop - 1 - pa.send;
//        } else {
//            sstart = t.start + pa.sstart;
//            send = t.start + pa.send;
//        }
//
//        //0
//        //result+=queryName+"\t";
//
//        result.addAndTrim(queryName);
//        result.add("\t");
//        //result+=t.myKID+"\t";
//
//        //1
//        if (KidDatabaseThreadSafe.ON == true){
//            result.addAndTrim(t.myKID + "|" + KidDatabaseThreadSafe.getName(t.myKID));
//        } else {
//            result.addAndTrim(t.myKID + "|" + myKidDB.getSequenceName(t.myKID));
//        }
//        result.add("\t");

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


        return result.toString();
    }


    protected void calculateBestAlignments() {

        SmithWatermanTruncated3 swt3;

        SuperString result = new SuperString();

        for (SuperSeed s : fwdSeeds) {
            if (AlignmentKLAT2.DEBUG == true) {
                System.err.println("\tSEED\t\t" + s);
                System.err.println("\tFASTKLAT\t" + AlignmentKLAT1.calculateFastKlatScore(s));
            }

            String ref = (s.toRefSeqRequest(kd, query.length())).getReferenceSequence();
            if (!ref.equals(DnaBitString.SENTINEL)) {
                swt3 = new SmithWatermanTruncated3(query, ref,s,true);
                ArrayList<PartialAlignment> pas = swt3.bestAlignments2();
                bestAlignments.addAll(pas);
            }
        }

        for (SuperSeed s : revSeeds) {
            if (AlignmentKLAT2.DEBUG == true) {
                System.err.println("\tSEED\t\t" + s);
                System.err.println("\tFASTKLAT\t" + AlignmentKLAT1.calculateFastKlatScore(s));
            }

            String ref = (s.toRefSeqRequest(kd, query.length())).getReferenceSequence();
            if (!ref.equals(DnaBitString.SENTINEL)) {
                swt3 = new SmithWatermanTruncated3(query, ref,s,true);
                ArrayList<PartialAlignment> pas = swt3.bestAlignments2();
                bestAlignments.addAll(pas);
            }

        }
        calculated = true;
    }




//    public void testPrintAlignments(KidDatabase myKidDB){
//        SmithWatermanAdvanced swa;
//
////        pollKmersForPositions();
////        calculateAlignmentTables();
////        calculateSeeds();
//
//        System.out.println("\nAlignments");
//        System.out.println("\tFORWARD");
//        for (Seed s : fwdSeeds) {
//            System.out.println("SEED "+s);
//            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
//            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
//            swa.printPrettyBestResults(System.out);
//
//        }
//        System.out.println("\tREVERSE");
//        for (Seed s : revSeeds) {
//            System.out.println("SEED "+s);
//            ReferenceSequenceRequest t = s.toRefSeqRequest(myKidDB, query.length());
//            swa = new SmithWatermanAdvanced(query, t.getReferenceSequence());
//            swa.printPrettyBestResults(System.out);
//        }
//    }


    /**
     * column labels for outformat7
     */
    public static String Blast6Header = "qseqid\tKLUE_ID\tpident\tlength\tmismatch\tgapopen\tqstart\tqend\tsstart\tsend\tevalue\tbitscore";





//    /**
//     * Note the assumption that seed is full adjacent may not be valid
//     * @param s
//     * @return
//     */
//    public static Double calculateFastKlatScore(Seed s){
//        return Seed.calculateFastKlatScoreAdjacentSeed(s);
//    }

    public ArrayList<PartialAlignment> getBestAlignments() {
        System.err.println("Warning\tAlignmentKLAT2 :: getBestAlignments() called before calculateBestAlignments() ");
        return bestAlignments;
    }

    public static void main(String[] args) {
    }
}

