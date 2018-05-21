package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.variantklue.Variant;

import java.util.ArrayList;

/**
 * Created by jwc on 10/5/17.
 */
public interface PartialAlignment {

    //Constructor: calculates all values, except VariantCalls

    //Add all getters

    public float getPercentIdentity();

    public int getNumMatches();

    public String getCIGARString();

    public int getNumAligned();

    public int getLength();

    public int getMismatch();

    public double getEvalue();

    public int getBitscore();

    public ArrayList<Variant> getMismatchesCalled();

    public int getFastKLATscore();

    public String toFullReport();

    public ArrayList<Variant> callVariants();

}
