package org.cchmc.kluesuite.variantklue;

import org.cchmc.kluesuite.variantklue.Variant;

import java.util.Iterator;

/**
 * Iterates over Variant[]  Simple
 */
public class MyVariantIterator implements Iterator<Variant> {
    int indexOfNext;
    Variant[] v;

    //MyVariantIterator
    public MyVariantIterator(Variant[] arr) {
        v = arr;
        indexOfNext = 0;
    }

    //MyVariantIterator
    @Override
    public boolean hasNext() {
        if (v == null)
            return false;
        else
            //Curr is the next to be returned, so if it exists, you hasNext()
            return (indexOfNext < (v.length));
    }

    //MyVariantIterator
    @Override
    public Variant next() {
        if (v == null)
            return null;

        if (!hasNext()) {
            return null;
        }

        //indexOfNext++;
        return v[indexOfNext++]; // return v[indexOfNext] then addWithTrim 1
    }

    //MyVariantIterator
    @Override
    public void remove() {

    }

    //MyVariantIterator
    public Variant peekPosition() {
        if (indexOfNext == (v.length)) {
            return null;
        } else {
            return v[indexOfNext];
        }
    }
}