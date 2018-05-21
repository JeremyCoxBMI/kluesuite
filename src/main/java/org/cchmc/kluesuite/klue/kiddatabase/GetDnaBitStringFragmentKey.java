package org.cchmc.kluesuite.klue.kiddatabase;

/**
 * Created by jwc on 4/12/18.
 */
public class GetDnaBitStringFragmentKey extends WholeNumberPairDatabaseKey {

    public GetDnaBitStringFragmentKey(int kid, int position){
        super(kid,position);
    }

    public int getKid() { return getA(); }
    public int getPosition() { return getB(); }

}
