package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;
import org.cchmc.kluesuite.klue.Kmer31;

import java.util.ArrayList;

/**
 * Created by jwc on 10/5/17.
 */
public class ExactMatches extends Box2 {

    /**
     * end column and end row are assumed to be the seed end; i.e. not including the entire k-mer length
     * @param srow
     * @param scol
     * @param erow
     * @param ecol
     * @param previousMinFastKlatScore
     */
    public ExactMatches(int srow, int scol, int erow, int ecol,
                        int previousMinFastKlatScore, int previousMaxFastKlatScore,
                        int prevMinSW, int prevMaxSW) {
        super(srow, scol, erow, ecol);
        //SENTINEL value
//        cumulativeActualFastKlatScore = -1;
//        cumulativeSmithWatermanScore = -1;
        ecol += Kmer31.KMER_SIZE_MINUS_TWO;   //EXCLUSIVE to INCLUSIVE, EXTEND ONLY 30
        erow += Kmer31.KMER_SIZE_MINUS_TWO;   //EXCLUSIVE to INCLUSIVE, EXTEND ONLY 30
        type = BoxType.EXACT;
        cumulativeMinimumSWscore = prevMinSW + SmithWatermanTruncated2.MATCH * (ecol-scol+1);
        cumulativeMaximumSWscore = prevMaxSW + SmithWatermanTruncated2.MATCH * (ecol-scol+1);
        cumulativeMinimumFastKlatScore = previousMinFastKlatScore + (ecol-scol+1);
        cumulativeMaximumFastKlatScore = previousMaxFastKlatScore + (ecol-scol+1);
//        type=BoxType.EXACT;
    }

    public ExactMatches (Seed s,
                         int previousMinFastKlatScore, int previousMaxFastKlatScore,
                         int prevMinSW, int prevMaxSW){
        super(s);
//        cumulativeActualFastKlatScore = -1;
//        cumulativeSmithWatermanScore = -1;
        ecol += Kmer31.KMER_SIZE_MINUS_ONE;   //EXCLUSIVE to INCLUSIVE, EXTEND ONLY 30
        erow += Kmer31.KMER_SIZE_MINUS_ONE;   //EXCLUSIVE to INCLUSIVE, EXTEND ONLY 30
        type = BoxType.EXACT;
        //INCLUSIVE to INCLUSIVE (+1)
        cumulativeMinimumSWscore = prevMinSW + SmithWatermanTruncated2.MATCH * (ecol-scol+1);
        cumulativeMaximumSWscore = prevMaxSW + SmithWatermanTruncated2.MATCH * (ecol-scol+1);
        cumulativeMinimumFastKlatScore = previousMinFastKlatScore + (ecol-scol+1);
        cumulativeMaximumFastKlatScore = previousMaxFastKlatScore + (ecol-scol+1);

    }
    @Override
    public void calculateScores(int prevSWscore, int prevFastKlatScore) {
        cumulativeActualFastKlatScore = prevFastKlatScore + (ecol-scol +1);
        cumulativeSmithWatermanScore = prevSWscore + SmithWatermanTruncated2.MATCH * (ecol-scol +1 );
    }

}
