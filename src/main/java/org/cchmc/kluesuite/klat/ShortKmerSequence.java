package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.ShortKmer31;

import java.util.ArrayList;

/**
 * The purpose of this class is to allow variable length k  for k < Kmer31.KMER_SIZE.
 * Then, alignments using a short k are possible, if not slower.
 */

public class ShortKmerSequence extends KmerSequence{

    int kmerSize;

    public ShortKmerSequence(String query, int kmerSize){
        super();
        this.kmerSize = kmerSize;

        lastpos = -1; //the last position written to is PRIOR to array that has not been written yet.  Needed for Gap finding logic.

//        length = query.length() - Kmer31.KMER_SIZE + 1;
        length = query.length() - kmerSize + 1;
        if (length < 0) length = 0;

        Kmer31 tempy = new Kmer31(0L);
        long nextVal;
        kmers = new ArrayList<Kmer31>();
        gaps = new ArrayList<Gap>();

        if (query.length() < kmerSize){
            System.err.println("Cannot query a sequence as short as "+Integer.toString(query.length())+" to store.");
        } else {

//            Kmer31Iterator it = new Kmer31Iterator(query);

            DnaBitString dbs = new DnaBitString(query);

            for (int k=0; k < length; /*k++*/){
                ShortKmer31 temp = dbs.getShortKmer31(k, kmerSize);
                k++;

                if (k==(length-1) && temp == null){
                    //close final gap
                    gaps.add(new Gap(lastpos+1, k-lastpos ));
                }
            }

        } //end if (errors) else (DO)

        if (kmers.size() == 0 && gaps.size() == 0)  gaps.add( new Gap(0, length));
        createIndexMap();
    }

    public ShortKmer31[] getForwardShortKmer31() {
        ShortKmer31[] result = new ShortKmer31[length];
        for (int k=0; k< kmers.size(); k++){
            //I stuffed ShortKmer31 into ArrayList<Kmer31> --> not sure if I am gonna crash and burn here
            result[k] = (ShortKmer31) kmers.get(k);
        }

        return result;
    }


}
