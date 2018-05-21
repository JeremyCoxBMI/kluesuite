package org.cchmc.kluesuite.masterklue;

import org.cchmc.kluesuite.klue.Kmer31;

/**
 * Created by osboxes on 5/25/17.
 *
 * Eventually to replace klat::KLATsettings
 */
public class KLATsettings extends Settings {

    // * Parameters for first level KLAT alignment calculations.
    // * (    1st level: identify possible candidates SEEDS
    // *      2nd level: calculate alignment

    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
    // 1st level parameters
    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#

    /**
     * The minimum number of kmers within MAX_SEED_QUERY_GAP distance
     * and MAX_SEED_ALIGN_GAP of each other in the
     * query alignment and reference alignment locations.  Must have minimum number of kmers to be a seed.
     *
     * NOTE that a meaningful choice here is for MIN_SEED_HITS > MIN_SEED_ADJACENCY, because it is guaranteed that
     * MIN_SEED_HITS >= MIN_SEED_ADJACENCY
     */

    /**
     * All constants/settings must be classes (Integer, Double, etc..) because of settings storage
     */
    /**
     * Minimum number of hits within a seed made from adjacent k-mer look-ups.  Not the same as MIN_FAST_KLAT_SCORE,
     * which is enforced at the agglomeration level
     */
    public static final String MinSeedHits = "MIN_SEED_HITS";
    public static Integer MIN_SEED_HITS = 2;


    public static final String EvalueK = "EVALUE_K";
    public static final Double EVALUE_K = 0.621;

    public static final String EvalueLambda = "EVALUE_LAMBDA";
    public static final Double EVALUE_LAMBDA = 1.33;

    //NOT CURRENTLY USED
    public static final String EvalueH = "EVALUE_H";
    public static final Double EVALUE_H = 1.12;




    /**
     * To be considered a seed, this is the number of kmers that must be adjacent.
     * Value of 2 cuts out singletons as a measure against false positives.
     */
    public static final String MinSeedAdjaceny = "MIN_SEED_ADJACENCY";
    public static Integer MIN_SEED_ADJACENCY = 2;


    /**
     * If distance between two kmer positions in a query sequence is < MAX_SEED_QUERY_GAP and
     * distance between same two kmer positions in reference sequence is < MAX_SEED_ALIGN_GAP,
     * they merge Into the same seed.
     */
    public static final String MaxSeedQueryGap = "MAX_SEED_QUERY_GAP";
    public static Integer MAX_SEED_QUERY_GAP = 10;  //10 consecutive errors allowed



    /**
     * If distance between two kmer positions in a query sequence is < MAX_SEED_QUERY_GAP and
     * distance between same two kmer positions in reference sequence is < MAX_SEED_ALIGN_GAP,
     * they merge into the same seed.
     */
    public static final String MaxSeedReferenceGap = "MAX_SEED_REFERENCE_GAP";
    public static Integer MAX_SEED_REFERENCE_GAP = 15; //15 consecutive errors allowed


    /**
     *   Seed combination proceeds by three steps
     *   1) Adjacent seeds combined
     *   2) Seeds within THIS DISTANCE are combined in greedy manner and not recombined with others
     *   3) Seeds within Whisker difference are combined combinatorial (not greedy)
     *   //TODO include in settings list
     */
    public static Integer EXCLUSIVE_SEED_AGGLOMERATION_DISTANCE = 10;

    /**
     * Used as a threshold to throw out minimalist seeds.
     * FAST_KLAT_SCORE defined as number of matches (hits) in the combined Seeds (or SuperSeed)
     */
    public static final String MinFastKlatScore = "MIN_FLAST_KLAT_SCORE";
    public static Integer MIN_FAST_KLAT_SCORE = 35;  //min hits of


    public static final String MinPercentIdentity = "MIN_PERCENT_INDENTITY";
    public static Integer MIN_PERCENT_IDENTITY = 80;


    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
    // 2nd level parameters
    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#

    /**
     * How far out from SEEDS should I attempt to make local alignment to the reference sequence.
     * When the reference sequence is recalled for alignment, we extend bounds in each direction by WHISKERS.
     * Note, in KLAT v2, this is calculated algorithmically as well
     */
    public static final String Whiskers = "WHISKER_LENGTH_ALIGNMENT";
    public static Integer WHISKERS_LENGTH_ALIGNMENT = 50;

//    /***
//     * Distance willing to cross inside the reference sequence as a gap for an alignment.
//     * This determines whether nearby seeds will be considered as two ends of a gap.
//     * default should be big enough to allow exon alternate selection.  Note database will contain transcriptome as well as genome
//     */
//    public static final String ReferenceSequenceMaxGap = "REFERENCE_SEQ_MAX_GAP";
//    public static Integer REFERENCE_SEQ_MAX_GAP = 50;


    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
    // Reporting
    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#

    public static final String maxEvalue = "MAX_EVALUE";
    public static Double MAX_EVALUE = new Double(1)/(10^15);


    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
    // not implemented yet settings
    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#

    /**
     * Forces KLAT to use maximum Kmer size.
     * Note that the application of this option is probably unwise.  This will slow the engine down considerably.
     * Other settings determine KLAT's behavior for when to look at smaller k-mer sizes on demand as necessary.
     */
    public static final String ForceMaxKmer = "FORCE_KMER_SIZE";
    public static Integer FORCE_KMER_SIZE = Kmer31.KMER_SIZE;

    /**
     * for FAST_KLAT, look up only first and last, plus skip STRIDE-1 bases between lookups
     * NOT IMPLEMENTED
     * maybe -- do FAST_KLAT
     */
    public static final String Stride = "LOOKUP_STRIDE";
    public static Integer LOOKUP_STRIDE = 1;

    /**
     * Errors are more likely to occur at the ends of a read, so why start the stride at the end (offset 0)
     * Changing this allows the stride to skip offset bases before landing and reading.
     */
    public static final String StrideOffset = "STRIDE_OFFSET";
    public static Integer STRIDE_OFFSET = 0;

    /**
     *  If Stride is sufficiently large, very few kmers per sequence will be looked up.  This forces a minimum size.
     */
    public static final String StrideMinLookups = "STRIDE_MIN_LOOKUPS";
    public static Integer STRIDE_MIN_LOOKUPS = 4;



    public static final String RedoAligmentOnLowScore = "REDO_ALIGNMENT_ON_LOW_SCORE";
    public static String REDO_ALIGNMENT_ON_LOW_SCORE = "no";

    public static final String KmerSizeOnRedo = "KMER_SIZE_ON_REDO";
    public static Integer KMER_SIZE_ON_REDO = 21;



    //TODO: include here, implement later  Issue #75
    public static Boolean USE_NEEDLEMAN_WUNSCH = false;

    /**
     * This values should not be altered.
     *
     * REQUIREMENT:
     * MATCH >= abs(MISMATCH), MISMATCH == GAP
     *
     * Otherwise, SmithWatermanTruncated is not guaranteed to work
     */
    public static final String SmithWatermanMatch = "SMITH_WATERMAN_MATCH";
    public static Integer SMITH_WATERMAN_MATCH = 2;


    /**
     * This values should not be altered.
     * If     MISMATCH !=  GAP  or  MATCH < abs(MISMATCH)
     *      SmithWatermanTruncated is not guaranteed to work
     */
    public static final String SmithWatermanMismatch = "SMITH_WATERMAN_MISMATCH";
    public static Integer SMITH_WATERMAN_MISMATCH = -1;


    /**
     * This values should not be altered.
     * If     MISMATCH !=  GAP  or  MATCH < abs(MISMATCH)
     *      SmithWatermanTruncated is not guaranteed to work
     */
    public static final String SmithWatermanGap = "SMITH_WATERMAN_GAP";
    public static Integer SMITH_WATERMAN_GAP = -1;


    /**
     * Not sure how this works yet -- do we want to check for overlaps really quick?
     */
    //public static Integer MAX_SUPERSEEDS_AS_INDIVIDUALS = 6;

    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#
    //WildKLAT settings
    // *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *# *#

    /*
     *  If a wild card query exceeds this number of possible look-ups, then throw an error and only do this many
     */
    public static final String MaxPermutations = "MAX_PERMUTATIONS";
    public static Integer MAX_PERMUTATIONS = 64;  //(only 4^3)



    static{
        set = new SettingsFile("KLATsettings.txt", "KLATsettings");

        // Iterating by 10, so other lines can be easily inserted (BASIC FTW)

        set.add(new SettingsFileEntry(30, "# ***Level 1*** Parameters : Lookup of K-mers for identifying seeds ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(35, "#    Increase speed at cost specificity ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(38, "#    All parameters filter lookup seeds to pare down possibilities (i.e. throw them out).  ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(39, "     #    These parameters affect seed agglomeration, i.e. combining of seeds.  ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(40, MinSeedHits, MIN_SEED_HITS, VarType.INTEGER ));
        set.add(new SettingsFileEntry(50, MinSeedAdjaceny, MIN_SEED_ADJACENCY, VarType.INTEGER ));
        set.add(new SettingsFileEntry(55, "      #    Fast Klat Score is the number of reference sequence bases matched", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(60, MinFastKlatScore, MIN_FAST_KLAT_SCORE, VarType.INTEGER ));
        set.add(new SettingsFileEntry(100, "      #    Max Gap allowed to combine speeds to reduce possibilities ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(110, MaxSeedQueryGap, MAX_SEED_QUERY_GAP, VarType.INTEGER ));
        set.add(new SettingsFileEntry(120, MaxSeedReferenceGap, MAX_SEED_REFERENCE_GAP, VarType.INTEGER ));






        set.add(new SettingsFileEntry(250, "#***Level 2*** Parameters to impact alignment calculations", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(260, "#   When aligning, maximum extension past edge of repudiated alignment in Reference sequence, could catch gaps, indels, etc (increases run time)", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(261, "#   KLAT will minimum of WHISKERS_LENGTH_ALIGNMENT or extensions indicated by seeds", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(270,  Whiskers, WHISKERS_LENGTH_ALIGNMENT, VarType.INTEGER ));
//        set.addAndTrim(new SettingsFileEntry(270, "#   ", null, VarType.COMMENT ));
//        set.addAndTrim(new SettingsFileEntry(280, ReferenceSequenceMaxGap, REFERENCE_SEQ_MAX_GAP, VarType.INTEGER ));

        //  REQUIRED:  GAP >= abs(MISMATCH), MISMATCH == GAP

        set.add(new SettingsFileEntry(310, "#AlignmentKLAT Hit Filter Parameters", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(320, MinPercentIdentity, MIN_PERCENT_IDENTITY, VarType.INTEGER ));
        set.add(new SettingsFileEntry(330, maxEvalue, MAX_EVALUE, VarType.DOUBLE ));


        set.add(new SettingsFileEntry(380, "# ***Speed*** Parameters to simply increase performance speed at trade-off specificity", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(390, "#    Skip #(stride - 1) out of every #(stride) adjacent k-mers: increases K-mer blindness ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(400, Stride, LOOKUP_STRIDE, VarType.INTEGER ));
        set.add(new SettingsFileEntry(410, "#    Skip indicated number of first K-mers ", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(420, StrideOffset, STRIDE_OFFSET, VarType.INTEGER ));
        set.add(new SettingsFileEntry(430, StrideMinLookups, STRIDE_MIN_LOOKUPS, VarType.INTEGER ));
        set.add(new SettingsFileEntry(440, "# ***Specificity*** Increase specificty at cost of speed", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(450, "#    Smaller K-mer size allows for more error/variants", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(460, ForceMaxKmer, FORCE_KMER_SIZE, VarType.INTEGER ));
        set.add(new SettingsFileEntry(470, "#    Redo alignment with smaller K-mer if fail to achieve minimum score", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(480, RedoAligmentOnLowScore, REDO_ALIGNMENT_ON_LOW_SCORE, VarType.YESNO ));
        set.add(new SettingsFileEntry(490, KmerSizeOnRedo, KMER_SIZE_ON_REDO, VarType.INTEGER ));

        set.add(new SettingsFileEntry(550, "# E-value calculation parameters (E-value generally regarded as archaic)", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(560, EvalueK, EVALUE_K, VarType.DOUBLE ));
        set.add(new SettingsFileEntry(570, EvalueLambda, EVALUE_LAMBDA, VarType.DOUBLE ));
        set.add(new SettingsFileEntry(580, "#    E-value H currenlty not used", null, VarType.COMMENT ));
        set.add(new SettingsFileEntry(590, EvalueH, EVALUE_H, VarType.DOUBLE ));

        set.processVariableList();
    }


    public static void initialize(){
        new KLATsettings();
    }
}
