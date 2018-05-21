package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.Seed;

import java.util.Comparator;

/**
 * Created by jwc on 8/30/17.
 *
 * Deprecated
 */
public class Diagonal implements Comparable<Diagonal>, Comparator<Diagonal> {

    /**
     * starting row of diagonal, INCLUSIVE
     */
    public int srow;

    /**
     * starting column of diagonal, INCLUSIVE
     */
    public int scol;

    /**
     * ending row of diagonal, *INCLUSIVE*
     */
    public int erow;

    /**
     * ending row of diagonal, *INCLUSIVE*
     */
    public int ecol;

    /**
     * Note erow and ecol are INCLUSIVE
     * @param srow
     * @param scol
     * @param erow
     * @param ecol
     */
    public Diagonal(int srow, int scol, int erow, int ecol ){
        this.scol = scol;
        this.srow = srow;
        this.erow = erow;
        this.ecol = ecol;
    }

    @Override
    public int compareTo(Diagonal o) {
        //result = queryEnd - seed.queryEnd;
        //Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
        if (srow != o.srow){
            return srow - o.srow;
        } else if (scol != o.scol) {
            return scol - o.scol;
        }

            //equal
            return 0;
    }

    @Override
    public int compare(Diagonal o1, Diagonal o2) {
        return o1.compareTo(o2);
    }
}
