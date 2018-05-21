package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.masterklue.KLATsettings;
import org.junit.Assert;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;

/**
 * Created by jwc on 7/11/17.
 *
 *
 *
 *  klat2 basically uses a completely different alignment algorithm, using a truncated version of Smith-Waterman for speed
 *  This necessitates when agglomerating seeds to track alignment runs within the agglomerated seeds, so that these
 *  perfect match regions can reduce computation time
 *
 *
 * DESIGN: SuperSeed will be used after Seeds are create with adjacency streaks.  This wrapper contains them.
 * In most cases, this means there will be few SuperSeeds for memory overhead
 *
 *
 */
public class SuperSeed extends Seed {

//    //addAndTrim a FastKlatScore field
//    public int fastKlatScore;

    /**
     * How far the seed extends to the right
     * EXCLUSIVE BOUND
     *
     */
    public int rightEdge;

    /**
     * Size of kmer used to make the seed.  (Expect value 15 through 31).
     *
     * Value 0 indicates a mixture
     */
    public final int myKmerSize;

    /**
     * Sentinel for myKmerSize
     */
    public static int KMER_SENTINEL_MIXTURE = 0;

    /**
     * contains all seeds used to agglomerate this seed
     * note that adjacent seeds are already combined into one
     * order by variable start, increasing
     */
    public ArrayList<Seed> children;

    /**
     * construction starts with a single Seed with AdjacencyStreak, which is then agglomerated into SuperSeed
     * @param kmerSize
     * @param seed
     */
    public SuperSeed(int kmerSize, Seed seed ){
        //ASSERT   seed.isAdjacency() == true

        myKmerSize = kmerSize;
        //fastKLATscore = seed.fastKLATscore;
        fastKLATscore = seed.fastKLATscore;
        queryStart = seed.queryStart;
        queryEnd = seed.queryEnd;
        start = seed.start;
        end = seed.end;
        myKid = seed.myKid;
        hits = seed.hits;
        adjacency = seed.adjacency;
        isReverse=seed.isReverse;
        snp=seed.snp;
        indel = seed.indel;

        children = new ArrayList<Seed>();
        children.add(seed);

        // if kmersize is five, no overlap; distance is four
        // pos 10 (exclusive)
        //        9* 10* 11*  12*  13* 14   (kmer does not extend to 14)
        //  but end is exclusive  10 -1 + 5 <= 14
        rightEdge = end -1 + myKmerSize; // EXCLUSIVE
    }

    /**
     * Copy constructor.  Notably, makes a deep copy of children.
     * @param ss
     */
    public SuperSeed(SuperSeed ss) {

        //Seed data members
        fastKLATscore = ss.fastKLATscore;
        queryStart = ss.queryStart;
        queryEnd = ss.queryEnd;
        start = ss.start;
        end = ss.end;
        myKid = ss.myKid;
        hits = ss.hits;
        adjacency = ss.adjacency;
        isReverse = ss.isReverse;
        snp = ss.snp;
        indel = ss.indel;


        //SuperSeed data members
        myKmerSize = ss.myKmerSize;
        children = new ArrayList<Seed>();
        for (Seed seed : ss.children) {
            children.add(new Seed(seed)); // no shallow copying!
        }
        rightEdge = ss.rightEdge;
    }

    /**
     * Should not be used on SuperSeeds.  Data loss will result.
     * @param kmerSize
     * @param seed1
     * @param seed2
     */
    public static SuperSeed buildSuperSeed(int kmerSize, Seed seed1, Seed seed2 ) throws DataFormatException {

        Class class1 = seed1.getClass();
        Class class2 = seed2.getClass();
        SuperSeed result = null;
        if (class1.equals(Seed.class) && class2.equals(Seed.class)) {
            return mergeIfAble(seed1, seed2, kmerSize);
        } else {
            throw new DataFormatException("SuperSeed:buildSuperSeed(int,Seed,Seed) was not passed two seeds.");
            //return null;
        }
    }

    /**
     * This adds another seed to the SuperSeed, also updates fields
     *
     * Exceptions are thrown when nonsenslical data is encountered, which is meaningless
     *
     * Returns true on success
     * @param seed
     * @return  true if successful
     */
    public boolean add(Seed seed) throws DataFormatException {


        if (myKid != seed.myKid) {
            throw new DataFormatException("WARNING:: seed for kid " + myKid + " being combined with seed for " + seed.myKid
                    + "\nSuperSeed::add(Seed) does nothing");
        }


        if (isReverse != seed.isReverse) {
            if (isReverse) {
                throw new DataFormatException("WARNING:: SuperSeed in reverse having forward Seed being added");
            } else {
                throw new DataFormatException("WARNING:: SuperSeed in forward having reverse Seed being added");
            }
        }

        return this.mergeToMeIfAble(seed);
    }

    /**
     * Updates SuperSeed values based on a new seed
     * DOES NOT add seed, trying adding first
     * @param seed
     */
    public void updateValues(Seed seed) {
        //        //Guaranteed to be no overlap by boundary checking
//        int overlap = 0;
//        //distance must be at least KmerSize apart, otherwise
//        if (start < seed.start){
//            overlap = Math.max(0, myKmerSize - (seed.start - start));
//        } else {
//            overlap = Math.max(0, myKmerSize - (start - seed.start));
//        }
//        fastKLATscore += seed.fastKLATscore - overlap;

        if (isAdjacencyStreak() &&
                seed.isAdjacencyStreak() &&
                //contiguous on query and reference ==> so the diagonal is true
                (       (end == seed.start && queryEnd == seed.queryStart)    ||
                        (start == seed.end && queryStart == seed.queryEnd)
                )
                ) {
            adjacency += seed.adjacency;
        } else {
            adjacency = Math.max(adjacency, seed.adjacency);
        }

        fastKLATscore += seed.fastKLATscore;
        queryStart = Math.min(queryStart, seed.queryStart);
        queryEnd = Math.max(queryEnd, seed.queryEnd);
        start = Math.min(start, seed.start);
        end = Math.max(end, seed.end);
        rightEdge = end -1 + myKmerSize; // EXCLUSIVE


        hits += seed.hits;
        snp = snp || seed.snp;
        indel = indel || seed.indel;
        //children.add(seed);
    }

    /**
     *  Add a seed to the SuperSeed if it is allowed.
     * @param seed1
     * @return      True if successfully merged
     */
    private boolean mergeToMeIfAble(Seed seed1) throws DataFormatException {
        boolean result = false;
        //keep seeds sorted
        int k;
        for (k = 0; k < children.size(); k++){
            if (children.get(k).queryStart > seed1.queryStart) {
                //break, end search
                break;
            }
        }

        //TODO duplication with updateValues?

        //boundary check on seeds to left AND right, indicate seeds otherwise
        Seed left = null;
        Seed right = null;
        if (k > 0){
            left = children.get(k-1);
        }
        if (k < children.size()){
            right = children.get(k);
        }

        if ( left != null ) {
            if (  //short circuits!
             right != null &&
             mayMerge(seed1,left,myKmerSize) &&
                mayMerge(seed1,right,myKmerSize) ){
                result = true;
                children.add(k,seed1);
                updateValues(seed1);
            } else if ( right == null && mayMerge(seed1,left,myKmerSize)){
                result = true;
                children.add(k,seed1);
                updateValues(seed1);
            }
        } else if (right != null && mayMerge(seed1,right, myKmerSize)){

            result = true;
            children.add(k,seed1);
            updateValues(seed1);
        }

        return result;
    }


    public static boolean mayMerge(Seed a, Seed b, int kmerSize) throws DataFormatException {
        try {
            Assert.assertTrue(a.adjacency >= KLATsettings.MIN_SEED_ADJACENCY);
            Assert.assertTrue(b.adjacency >= KLATsettings.MIN_SEED_ADJACENCY);
        } catch (AssertionError e) {
            throw new DataFormatException("SuperSeed::mayMerge called when a seed does not meet min_seed_adjacency");
        }

        return Seed.mayMergeAgglomeratedSeeds(a,b,kmerSize);

//        boolean result = false;
//
//        if (b == null || a == null) {
//            return result;
//        } else {
//
//            Seed temp;
//            //force order
//            //ASSERTION : a.start <= b.start
//            if (b.start < a.start) {
//                //result used as temporary value
//                temp = a;
//                a = b;
//                b = temp;
//            }
//
//            //WE ASSERT: a.start <= b.start
//
//            int aRefRightEdge = a.end + kmerSize - 1;  //because EXCLUSIVE ==> INCLUSIVE
//            int aQueRightEdge = a.queryEnd + kmerSize - 1;
//
//            //Checking for reference or query overlap
//            boolean noOverlap =  aRefRightEdge <= b.start  && aQueRightEdge <= b.queryStart;
//
//            //Checking seeds are close enough
//            boolean withinWhisker = aRefRightEdge + KLATsettings.MAX_SEED_REFERENCE_GAP >= b.start
//                    ||
//                    aQueRightEdge + KLATsettings.MAX_SEED_QUERY_GAP >= b.start;
//
//            boolean kidMatch = (a.myKid == b.myKid);
//
//
//
//
//            boolean IN_ORDER,
//                    REF_WHISKER_NO_OVERLAP,
//                    QUERY_OVERLAP = false,
//                    KID_MATCH;
//
//
//            // if kmersize is five, no overlap; distance is four
//            // pos 10 (exclusive)
//            //        9* 10* 11*  12*  13* 14   (kmer does not extend to 14)
//            //  but end is exclusive  10 -1 + 5 <= 14
//
//            // determine if there is overlap between them by reference sequence
//            // Issue #65 addressed
//            if (a.end == b.start){
//                REF_WHISKER_NO_OVERLAP = true;
//            } else {
//                REF_WHISKER_NO_OVERLAP = (a.end + kmerSize) - 1 <= b.start;  //EXCLUSIVE vs INCLUSIVE: add 1
//            }
//
//            // range b:[r,s] cannot overlap a:[w,nextOffset]
//            // Stop is exclusive, so different comparators needed
//            // query are allowed to be close (adjacent): could be a deletion
//            QUERY_OVERLAP = (
//                    (a.queryEnd <= b.queryEnd && a.queryEnd > b.queryStart)
//                            ||
//                            (a.queryStart < b.queryEnd && a.queryStart >= b.queryStart)
//            );
//
//
//            //Need to prevent combining when they are not in increasing order for both query and reference
//            //asserts that one is completely left, one completely right
//            IN_ORDER = (a.queryEnd <= b.queryStart && a.end <= b.start) ||
//                    (b.queryEnd <= a.queryStart && b.end <= a.start);
//
//
//            KID_MATCH = (a.myKid == b.myKid);
//
//            if (KID_MATCH &&
//                    REF_WHISKER_NO_OVERLAP &&
//                    IN_ORDER &&
//                    !QUERY_OVERLAP
//                    ) {
//                result = true;
//            }
//
//        }
//        return result;
    }


    /**
     * @param a     Seed is treated as Seed class, even if SuperSeed
     * @param b     Seed or SuperSeed allowed, thanks to merge() method
     * @param kmerSize
     * @return          combined SuperSeed, otherwise null
     */
    public static SuperSeed mergeIfAble(Seed a, Seed b, int kmerSize) throws DataFormatException {

        if (b == null || a == null){
            return null;
        }

        if (    mayMerge(a,b,kmerSize) ) {
            return merge(a, b, kmerSize);
        }else {
            return null;
        }

    }


    private SuperSeed mergeIfAble(SuperSeed superSeed) throws DataFormatException {
        if (myKid != superSeed.myKid) {
            throw new DataFormatException("WARNING:: SuperSeed for kid " + myKid + " being combined with superSeed for " + superSeed.myKid
                    + "\nSuperSeed::merge(SuperSeed) does nothing");
        }

        if (isReverse != superSeed.isReverse) {
            if (isReverse) {
                throw new DataFormatException("WARNING:: SuperSeed in reverse having forward Seed being added");
            } else {
                throw new DataFormatException("WARNING:: SuperSeed in forward having reverse Seed being added");
            }
        }

        SuperSeed result = new SuperSeed(superSeed);

        boolean success = true;
        for (Seed s: children) {
            if (!result.mergeToMeIfAble(s)) {
                success = false;
                break;
            }
        }

        if (success) { return result; }
        else { return null; }
    }

    /**
     * merges two superseeds, no boundary checking
     * @param superSeed
     * @throws DataFormatException
     */
        private void mergeWith(SuperSeed superSeed) throws DataFormatException {


            if (myKid != superSeed.myKid) {
                throw new DataFormatException("WARNING:: SuperSeed for kid " + myKid + " being combined with superSeed for " + superSeed.myKid
                        + "\nSuperSeed::merge(SuperSeed) does nothing");
            }


            if (isReverse != superSeed.isReverse) {
                if (isReverse) {
                    throw new DataFormatException("WARNING:: SuperSeed in reverse having forward Seed being added");
                } else {
                    throw new DataFormatException("WARNING:: SuperSeed in forward having reverse Seed being added");
                }
            }


            if (superSeed.end > end){
                end = superSeed.end;
                rightEdge = end + myKmerSize -1;
            }

            //myKmerSize cannot be updated here

            fastKLATscore += superSeed.fastKLATscore;
                //maximum fastKLAT must be completely recalculated every time; intense operation
            queryStart = Math.min(queryStart,superSeed.queryStart);
            queryEnd = Math.max(queryEnd, superSeed.queryEnd);
            start = Math.min(start, superSeed.start);
            end = Math.max(end,superSeed.end);

            hits += superSeed.hits;
            if (isAdjacencyStreak() &&
                    superSeed.isAdjacencyStreak() &&
                    //contiguous on query and reference ==> so the diagonal is true
                    (       (end == superSeed.start && queryEnd == superSeed.queryStart)    ||
                            (start == superSeed.end && queryStart == superSeed.queryEnd)
                    )
                    ) {
                adjacency += superSeed.adjacency;
                fastKLATscore -= myKmerSize;  //if adjacent, then there is no left seed whisker to count towards fastKlat
            } else {
                adjacency = Math.max(adjacency, superSeed.adjacency);
            }
            snp= snp || superSeed.snp;
            indel = indel || superSeed.indel;

            children = combineChildren(children, superSeed.children);
        }






    /**
     * combines children lists, preserving ordering
     * @param children2
     * @param children1
     * @return
     */
    private ArrayList<Seed> combineChildren(ArrayList<Seed> children2, ArrayList<Seed> children1) {
        ArrayList<Seed> result = new ArrayList<>(children1.size()+children2.size());
        int k=0;
        int j=0;

        //children1 and children2 already sorted by start, vombines in n1+n2 time
        while(true) {
            if (j >= children2.size()) {
                result.add(children1.get(k));
                break;
            } else if (k >= children1.size()) {
                result.add(children2.get(j));
                break;
            } else if (children1.get(k).start > children2.get(j).start) {
                result.add(children1.get(k));
                k++;
            } else {
                result.add(children1.get(j));
                j++;
            }
        }
        return result;
    }

    /**
         * Accepts Seeds or SuperSeeds
         *
         * @param a
         * @param b
         * @return
         */
        private static SuperSeed merge(Seed a, Seed b, int kmerSize){
            Class class1 = a.getClass();
            Class class2 = b.getClass();
            SuperSeed result = null;
            if (class1.equals(SuperSeed.class) && class2.equals(SuperSeed.class)){
                try {
                    ((SuperSeed) a).mergeWith((SuperSeed) b);
                    result = (SuperSeed) a;
                } catch (DataFormatException e) {
                    e.printStackTrace();
                    exit(0);
                }
            } else if(class1.equals(SuperSeed.class) && class2.equals(Seed.class)){
                result = (SuperSeed) a;
                try {
                    result.add(b);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            } else if (class2.equals(SuperSeed.class) && class1.equals(Seed.class)){
                result = (SuperSeed) b;
                try {
                    result.add(a);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    result = new SuperSeed(kmerSize, a);
                    result.add(b);

//                    return result;
//                    result = SuperSeed.buildSuperSeed(kmerSize, a,b);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                    exit(0);
                }
            }
            return result;
        }

    public Iterator<Seed> iterator() {
            return children.iterator();
    }

    public int numSeeds() {
            return children.size();
    }
}
