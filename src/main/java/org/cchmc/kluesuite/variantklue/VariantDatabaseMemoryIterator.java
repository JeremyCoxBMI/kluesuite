package org.cchmc.kluesuite.variantklue;

/**
 * Created by osboxes on 13/04/17.
 *
 * Iterates by Variant over HashMap<Integer,TreeMap<Integer,Variant[]>> by KID, so it is really just
 * keys (KID) mapping 2 @ TreeMap<Integer,Variant[]> together
 */
public class VariantDatabaseMemoryIterator implements VariantDatabaseIterator<Variant> {


    int kid;
//    VariantDatabaseOLDMemory vdm;

//    TreeMap<Integer,Variant[]> treeInd;
//    TreeMap<Integer,Variant[]> treeSnp;

    VariantDatabaseTreeMapIterator ITind;
//    VariantDatabaseTreeMapIterator ITindNext;
//    Integer currInd, prevInd;

    VariantDatabaseTreeMapIterator ITsnp;
//    VariantDatabaseTreeMapIterator ITsnpNext;
//    Integer currSNP, prevSNP;

//    VariantDatabaseMemoryIterator(int KID, VariantDatabaseOLDMemory vdm) {
//        kid = KID;
//
//        //treeInd = vdm.indelMap.get(kid);
//        //treeSnp = vdm.snpMap.get(kid);
//
////        currInd = vdm.indelMap.get(kid).firstKey();
//        ITind = new VariantDatabaseTreeMapIterator(vdm.indelMap.get(kid));
//
////        currSNP = vdm.snpMap.get(kid).firstKey();
//        ITsnp = new VariantDatabaseTreeMapIterator(vdm.snpMap.get(kid));
//
//        return;
//    }


    public VariantDatabaseMemoryIterator(int KID, VariantDatabaseMemory vdm) {
        kid = KID;

        Object obj = vdm.indelMap.get(kid);
        ITind = new VariantDatabaseTreeMapIterator(vdm.indelMap.get(kid));

        ITsnp = new VariantDatabaseTreeMapIterator(vdm.snpMap.get(kid));

        int debug = 1;
    }


    @Override
    /**
     * The iterators never have to advance; one KID at a time
     */
    public boolean hasNext() {
//        boolean result;
//        if (ITind == null && ITsnp == null)
//            result = false;
//        else
//            result = true;
//
//        return result;
        if (ITind == null) {
            if (ITsnp == null) {
                return false;
            } else {
                return ITsnp.hasNext();
            }
        } else if (ITsnp == null)
            return ITind.hasNext();

        return ITind.hasNext() || ITsnp.hasNext();
    }

//    private TreeMap<Integer,Variant[]> nextTree(mutation m){
//        switch (m){
//            case SNP:
//
//
//                currSNP = treeSnp.firstKey();
//                ITsnp = new VariantDatabaseTreeMapIterator(treeSnp);
//            case INSERTION:
//            case DELETION:
//    }

    @Override
    public Variant next() {
        Variant result;
        Variant peekInd, peekSnp;

        if (ITind == null) {
            if (ITsnp == null) {
                result = null;
            } else {
                if (ITsnp.hasNext()) {
                    result = ITsnp.next();
                    //For convenience, convert to null if empty
                    if (!ITsnp.hasNext())
                        ITsnp = null;
                } else {
                    result = null;
                }
            }

        } else if (ITsnp == null) {
            if (ITind.hasNext()) {
                result = ITind.next();
                //For convenience, convert to null if empty
                if (!ITind.hasNext())
                    ITind = null;
            } else {
                result = null;
            }

        } else { //both are not null

            if (ITsnp.hasNext()) {
                peekSnp = ITsnp.peekPosition();
            } else {
                peekSnp = null;
            }

            if (ITind.hasNext()) {
                peekInd = ITind.peekPosition();
            } else {
                peekInd = null;
            }

            if (peekInd == null) {
                if (peekSnp == null) {
                    result = null;
                } else {
                    result = ITsnp.next();
                    //For convenience, convert to null if empty
                    if (!ITsnp.hasNext())
                        ITsnp = null;
                }
            } else if (peekSnp == null) {
                result = ITind.next();
                //For convenience, convert to null if empty
                if (!ITind.hasNext())
                    ITind = null;
            } else {
                //Normal code without boundary conditions
                if (peekInd.start < peekSnp.start) {
                    result = ITind.next();
                    //For convenience, convert to null if empty
                    if (!ITind.hasNext())
                        ITind = null;
                } else {
                    result = ITsnp.next();
                    //For convenience, convert to null if empty
                    if (!ITsnp.hasNext())
                        ITsnp = null;
                }
            }
        }


        //  VariantDatabaseMemoryIterator::next()
        return result;
    }

    @Override
    public void remove() {

    }

    // VariantDatabaseTreeMapIterator
    @Override
    public Variant peekPosition() {

        Variant peekInd, peekSnp;

        if (ITind == null)
            peekInd = null;
        else
            peekInd = ITind.peekPosition();

        if (ITsnp == null)
            peekSnp = null;
        else
            peekSnp = ITsnp.peekPosition();

        Variant result;

        if (peekInd == null) {
            if (peekSnp == null) {
                result = null;
            } else {
                result = ITsnp.peekPosition();
            }

        } else if (peekSnp == null) {
            result = ITind.peekPosition();

        } else {
            //Normal code without boundary conditions
            if (peekInd.start < peekSnp.start) {
                result = peekInd;
            } else {
                result = peekSnp;
            }
        }
        return result;
    }
}




