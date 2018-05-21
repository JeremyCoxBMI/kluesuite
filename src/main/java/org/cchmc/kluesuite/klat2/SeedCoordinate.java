package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klue.SuperString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

//TODO requires inclusion of reference coordinate, need to check they match

/**
 * Created by COX1KB on 4/25/2018.
 */
public class SeedCoordinate implements Comparator<SeedCoordinate>, Comparable<SeedCoordinate>{
    public int kid;
    //public int nextOffset;
    public int queryPos;
    public int refPos;

    public SeedCoordinate(int kid, int qPos, int rPos){
        this.kid=kid;
        queryPos = qPos;
        refPos = rPos;
    }

    public SeedCoordinate(SeedCoordinate notAcopy, int next, boolean reverse){
        kid=notAcopy.kid;
        queryPos = notAcopy.queryPos + next;
        refPos = notAcopy.refPos + next;
        if (reverse)
            queryPos=notAcopy.queryPos-next;
        else
            queryPos=notAcopy.queryPos+next;
    }

    public String toString(){
        return  queryPos +" -> ("+kid+", "+refPos+") ";
    }

    public static String hashStrings(HashMap<SeedCoordinate, Boolean> h){
        SuperString ss = new SuperString();
        ArrayList<SeedCoordinate> al = new ArrayList<>(h.keySet());
        Collections.sort(al);
        for (SeedCoordinate t : al){
            ss.add(t+"\n");
        }
        return ss.toString();
    }

    @Override
    public int compareTo(SeedCoordinate o) {
        if (kid != o.kid){
            return kid - o.kid;
        } else
        if (queryPos != o.queryPos){
            return queryPos - o.queryPos;
        } else
        if (refPos != o.refPos){
            return refPos - o.refPos;
        } else {
            return 0;
        }
    }

    @Override
    public int compare(SeedCoordinate o1, SeedCoordinate o2) {
        return o1.compareTo(o2);
    }



    //to minimize collisions, need to use bits at the front of the int, so kid * -1
    @Override
    public int hashCode(){
        return (-1*kid) & (refPos << 10) & queryPos;
    }

    @Override
    public boolean equals(Object o){
        SeedCoordinate t = (SeedCoordinate) o;
        boolean result = true;
        if (kid != t.kid || refPos != t.refPos || queryPos != t.queryPos)   result = false;
        return result;
    }
}
