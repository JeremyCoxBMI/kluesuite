package org.cchmc.kluesuite.variantklue;

/**
 * Created by osboxes on 18/11/16.
 *
 * This class contains all relevant data for a genetic variant: allows both SNPs and indels.
 */

public enum mutation {
    NONE(0), SNP(1), INSERTION(2), DELETION(3);

    private final int value;
    private mutation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static mutation getEnum(int x){
        switch(x){
//            case 0:   //included in default
//                return NONE;
//                break;
            case 1:
                return SNP;

            case 2:
                return INSERTION;

            case 3:
                return DELETION;

            default:
                return NONE;

        }
    }
}
