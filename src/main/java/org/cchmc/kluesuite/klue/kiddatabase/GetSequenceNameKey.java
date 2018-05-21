package org.cchmc.kluesuite.klue.kiddatabase;

/**
 * Created by jwc on 4/11/18.
 */
public class GetSequenceNameKey extends WholeNumberPairDatabaseKey {

    static final int STRING_KEY = -1576;

    private GetSequenceNameKey(int a, int b){
        super(a,b);
    }

    public GetSequenceNameKey(int kid){
        super(kid, STRING_KEY);
    }

    public int getKid() { return getA(); }
}
