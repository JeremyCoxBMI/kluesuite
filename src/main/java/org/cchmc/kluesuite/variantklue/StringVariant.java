package org.cchmc.kluesuite.variantklue;

import java.util.ArrayList;

/**
 * Created by osboxes on 21/12/16.
 */
public class StringVariant {

    /**
     * The DNA sequence incorporating
     */
    public String s;

    //contains list of indexes corresponding to Variant[] variants; those listed are indels included (i.e. modified)
    public ArrayList<Variant> var;

    public StringVariant(String t, ArrayList<Variant> v){
        s =t;
        var= new ArrayList<Variant>(v);
    }

    public StringVariant(StringAndVariants t, ArrayList<Variant> v){
        s =t.s;
        var= new ArrayList<Variant>();
        for (Integer k : t.var) {
            if (k != null) {
                var.add(v.get(k));
            }
        }
    }

    public String nameListToString(){
        return Variant.variantNameList(var);
    }
}
