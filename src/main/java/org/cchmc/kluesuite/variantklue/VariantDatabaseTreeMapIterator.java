package org.cchmc.kluesuite.variantklue;



import java.util.Iterator;
import java.util.TreeMap;

/**
 *  Iterate over a TreeMap, returning Variant, not Variant[]
 */
public class VariantDatabaseTreeMapIterator implements Iterator<Variant> {

    TreeMap<Integer, Variant[]> tree;
    Integer currPosition;
    MyVariantIterator currentIT;

    Variant nextValue;

    public VariantDatabaseTreeMapIterator(TreeMap<Integer, Variant[]> tree) {
        this.tree = tree;
        currPosition = tree.firstKey();
        currentIT = new MyVariantIterator(tree.get(currPosition));
        int debug = 1;

        advanceToNext();
    }

    // VariantDatabaseTreeMapIterator
    @Override
    public boolean hasNext() {
        return (nextValue != null);
    }


    // VariantDatabaseTreeMapIterator
    @Override
    public Variant next() {

        Variant result = nextValue;
        advanceToNext();

        return result;


    }

    // VariantDatabaseTreeMapIterator
    @Override
    public void remove() {

    }


    // VariantDatabaseTreeMapIterator
    public Variant peekPosition() {
        return nextValue;
//            Variant result;
//            MyVariantIterator it;
//            if (currentIT.hasNext()){
//                result = currentIT.peekPosition();
//            } else {
//                currPosition = tree.higherKey(currPosition);
//                it = new MyVariantIterator(tree.get(currPosition));
//                if (it.hasNext())
//                    result = it.peekPosition();
//                else
//                    result = null;
//            }
//            return result;
    }

    private void advanceToNext() {
        if (currentIT.hasNext()) {
            nextValue = currentIT.next();

        } else {
            if (currentIT == null)
                nextValue = null;

            //why is this a while loop?
            while ( ((currentIT == null) || !currentIT.hasNext()) ) {
                currPosition = tree.higherKey(currPosition);
                if (currPosition == null) {  //STOP CONDITION
                    currentIT = null;
                    nextValue = null;
                    break;
                } else { //Iterate
                    currentIT = new MyVariantIterator(tree.get(currPosition));
                    nextValue = currentIT.next();
                    break;  //bug no break  2017-06-12
                }
            }
            int debug =1;
        }
    }
}
