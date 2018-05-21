package org.cchmc.kluesuite.klue.kiddatabase;

/**
 * Created by jwc on 4/12/18.
 */
public class GetExceptionsArrKey extends WholeNumberPairDatabaseKey {

    static final int EXCEPTIONS_HASHMAP_POSITION = -145;

    //overridden so you can't use it
    //does that work?  IDK
    private GetExceptionsArrKey(int kid, int position) {
        super(kid, position);
    }

    public GetExceptionsArrKey(int kid){
        super(kid, EXCEPTIONS_HASHMAP_POSITION);
    }

    public int getKid() { return getA(); }

}
