package org.cchmc.kluesuite.variantklue;

import java.util.Arrays;

/**
 * Created by osboxes on 11/05/17.
 */
public class VariantDatabaseArrayIterator implements VariantDatabaseIterator<Variant> {

    Variant[] arr;
    int next;

    public VariantDatabaseArrayIterator(Variant[] array){
        arr = array;
        Arrays.sort(arr);
        next = 0;
    }

    @Override
    public Variant peekPosition() {
        if (hasNext())
            return arr[next];
        else
            return null;
    }

    @Override
    public boolean hasNext() {
        return (next <  arr.length);
    }

    @Override
    public Variant next() {
        if (hasNext())
            return arr[next++];  //post operator, returns [next] from array
        else
            return null;
    }

    @Override
    public void remove() {

    }
}
