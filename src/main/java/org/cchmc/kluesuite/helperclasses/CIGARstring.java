package org.cchmc.kluesuite.helperclasses;

import org.cchmc.kluesuite.klue.SuperString;

/**
 * Created by COX1KB on 4/27/2018.
 */
public class CIGARstring {

    public static final int NONE = 0;
    public static final int DELETION = 1;
    public static final int INSERTION = 2;
    public static final int MATCH = 3;
    public static final int MISMATCH = 4;

    int currentCode;
    int currentCount;
    SuperString CIGAR;
    boolean finalized;

    public CIGARstring(){
        CIGAR = new SuperString();
        finalized = false;
        currentCode = NONE;
        currentCount = 0;
    }

    public void addDeletion(){
        finalized = false;
        if (DELETION != currentCode){
            addCurrToString();
            currentCode = DELETION;
            currentCount = 1;
        } else {
            currentCount++;
        }
    }

    public void addInsertion(){
        finalized = false;
        if (INSERTION != currentCode){
            addCurrToString();
            currentCode = INSERTION;
            currentCount = 1;
        } else {
            currentCount++;
        }
    }

    public void addMatch(){
        finalized = false;
        if (MATCH != currentCode){
            addCurrToString();
            currentCode = MATCH;
            currentCount = 1;
        } else {
            currentCount++;
        }
    }

    public void addMatch(int multiple){
        finalized = false;
        if (MATCH != currentCode){
            addCurrToString();
            currentCode = MATCH;
            currentCount = multiple;
        } else {
            currentCount += multiple;
        }
    }

    public void addMisMatch(){
        finalized = false;
        if (MISMATCH != currentCode){
            addCurrToString();
            currentCode = MISMATCH;
            currentCount = 1;
        } else {
            currentCount++;
        }
    }

    public String toString(){
        if (!finalized){
            addCurrToString();
            finalized = true;
        }
        return CIGAR.toString();
    }


    private void addCurrToString() {
        if (currentCount > 0) { //skips when nothing yet added value
            switch (currentCode) {
                case DELETION:
                    CIGAR.add('D');
                    break;
                case INSERTION:
                    CIGAR.add('I');
                    break;
                case MATCH:
                    CIGAR.add('=');
                    break;
                case MISMATCH:
                    CIGAR.add('X');
                    break;
                default:
                    break;
            }
            CIGAR.add(Integer.toString(currentCount));
        }
    }

}
