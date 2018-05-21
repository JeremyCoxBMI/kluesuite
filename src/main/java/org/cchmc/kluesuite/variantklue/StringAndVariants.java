package org.cchmc.kluesuite.variantklue;

import java.util.ArrayList;

/**
 * Created by osboxes on 18/11/16.
 *  A Simple (tuple) class / struct :: helper class Permutator
 *
 *  stores the String (of a sequence permutation) and the variants included (represented as List<Integer>)
 *
 *  These integers are the indexes of a companion List<Variant> that accompanies the StringAndVariants
 */


public class StringAndVariants {

    public String s;

    //contains list of indexes corresponding to Variant[] variants; those listed are indels included (i.e. modified)
    public ArrayList<Integer> var;

    public StringAndVariants(){
        s="";
        var = new ArrayList<Integer>();
    }

    StringAndVariants(String st, int bo){
        s=st;
        var = new ArrayList<Integer>();
        var.add(bo);
    }

    public StringAndVariants(String st, ArrayList<Integer> bo, int next){
        s=st;
        var = new ArrayList<Integer>(bo);
        var.add(next);
    }

    public StringAndVariants(String st, ArrayList<Integer> bo){
        s=st;
        var = new ArrayList<Integer>(bo);
    }

    public String toString(){return s;}

    public static String arrayToString(StringAndVariants[] sav){

        String result = "";
        //        result = "[ ";
        for (int k=0; k < sav.length; k++) {
            result += sav[k].s;
            if (k < sav.length-1)
                result += ",";
        }
//        result.subSequence(0,result.length()-2);
//        result += " ]";
        return result;
    }

}
