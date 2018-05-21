package org.cchmc.kluesuite.variantklue;

/**
 * Created by osboxes on 18/11/16.
 *
 * Design change realization for variants is that SNPs are easily handled by simply writing all permutations of k-mers
 * matching to database.
 *
 * Indels require more consideration.  While we can simply go 30 before and after mutations and write those sequences using
 * normal coordinates based off of the substring's first position.  So that coordinates still increase by 1, but for this
 * short segment, being recorded, the final index of last letter will not necessarily match due to insert or deletions.
 *
 * DESIGN:
 * We want to access a KID's indel list in positional order, but also we want to be able to look up a position in the list.
 * For simplification, we simply use a List<> sorted by position.
 */

public class pair{
    public int l;  //lower bound INCLUSIVE
    public int u; // upper bound EXCLUSIVE

    pair(){
        l =0;
        u=0;
    }

    public pair(int min, int max){
        l=min;
        u=max;
    }

    public boolean overlaps( pair p){
        return (l <= p.l && p.l < u)   ||  (l <= p.u && p.u < u);
    }

    public boolean overlaps( int pl, int pu){
        return (l <= pl && pl < u)   ||  (l <= pu && pu < u);
    }
}
