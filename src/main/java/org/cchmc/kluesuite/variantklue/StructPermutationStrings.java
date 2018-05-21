package org.cchmc.kluesuite.variantklue;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by jwc on 6/12/17.
 *
 * Two dimensional data object storing [Position] X [Strings] created by variants at this position
 */
public class StructPermutationStrings {

    /**
     * A permutation returns multiple Strings; this holds the STRING and the variants used to create it
     */
    public ArrayList<StringAndVariants[]> sav;


    /**
     * Each list at a coordinate is INDEXED the same position the Variants in String and Variants
     * This is a decoder ring of sorts for integers found there.  This indicates which ones are used in this string.
     * vs as in "VariantS"
     */
    public ArrayList<ArrayList<Variant>> vs;

    public StructPermutationStrings(){
       sav = new ArrayList<StringAndVariants[]>();
        vs = new  ArrayList<ArrayList<Variant>>();
    }

    public void add(StringAndVariants[] va, ArrayList<Variant> av){
        sav.add(va);
        vs.add((ArrayList<Variant>)av.clone());
    }

    public StringAndVariants[] getStringAndVariantsArray(int index){
        return sav.get(index);
    }

    public ArrayList<Variant> getVariantsArrayList(int index){
        return vs.get(index);
    }

    public Variant[] getVariantsArray(int index){
        Variant[] r = new Variant[vs.get(index).size()];
        r = (vs.get(index)).toArray(r);
        return r;
    }


    public int size() {
        return vs.size();
    }
}
