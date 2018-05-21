package org.cchmc.kluesuite.klue.kiddatabase;

/**
 * Created by jwc on 4/12/18.
 */
public class GetLengthKey extends WholeNumberPairDatabaseKey {

    static final int LENGTH_KEY = -2000;

    private GetLengthKey(int a, int b) {
        super(a, b);
    }

    public GetLengthKey(int kid) {
        super(kid, LENGTH_KEY);
    }

    public int getKid() {
        return getA();
    }
}
