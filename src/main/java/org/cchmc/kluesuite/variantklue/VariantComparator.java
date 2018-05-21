package org.cchmc.kluesuite.variantklue;

import java.util.Comparator;

public class VariantComparator implements Comparator<Variant> {

    @Override
    public int compare(Variant o1, Variant o2) {
        return o1.compareTo(o2);
    }

}

