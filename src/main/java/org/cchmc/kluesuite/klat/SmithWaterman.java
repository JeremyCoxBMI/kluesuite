package org.cchmc.kluesuite.klat;

import java.util.ArrayList;

/**
 * Created by jwc on 7/10/17.
 *
 * Class to take two strings and create many PartialAlignment1 object representing multiple alignments
 */
public interface SmithWaterman {


    //Constants used for calculations.  Different values give different results.

//    /**
//     * Sets point value for a match in the alignment table
//     * @param k
//     */
//    public void setMatchValue(int k);
//
//    /**
//     * Sets point value for a mismatch in the alignment table
//     * @param k
//     */
//    public void setMisMatchValue(int k);
//
//    /**
//     * Sets point value for a gap in the alignment table  (i.e. a gap is a single letter in sequence)
//     * @param k
//     */
//    public void setGapValue(int k);

    //report highest scoring alignments.  multiple results because TIES are common.
    public ArrayList<PartialAlignment1> bestAlignments();


}
