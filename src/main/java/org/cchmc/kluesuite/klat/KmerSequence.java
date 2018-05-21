package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kmer31;

import java.util.ArrayList;

/**
 * An abbreviated data storage for Kmer31 sequence.  This is complex enough to encapsulate from AlignmentKLAT1.java
 * @author osboxes
 *
 * 2016-08-15   v2.0    Imported without modification from v1.6.  Minor (1 line) : Switched to using Iterator from DnaBitStringSlow.
 */

public class KmerSequence {
    ArrayList<Kmer31> kmers;
    public ArrayList<Gap> gaps;
//    static Kmer31 SENTINEL = new Kmer31(Long.MIN_VALUE);	//PERPETUALLY ILLEGAL VALUE, will not be found in STORE

    /**
     * due to gaps, there may be fewer actual kmers stored than there are indexes
     * this maps sequence index to index of ArrayList<Kmer31> kmers
     */
    int[] indexToInternalIndex;

    /**
     * maps index of kmer to sequence index
     */
    int[] internalIndexToIndex;

    /**
     * Number of kmers in the sequence
     */
    int length;

    /**
     * last position recorded by constructor
     */
    int lastpos;			//last position written during constructor

    /**
     * 2^60, used for rotating Kmer31 by 1 position and adding a new
     */
    public static Long twoE60 = 1L << 60;

    /**
     * Helper function to constructor.  Assume Kmer31 are being written in order.
     * @param pos
     * @param addit
     */
    protected void addKmer( int pos, Kmer31 addit){
        // If kmers has fewer positions than it should, create gap
        if (pos > lastpos + 1 ){
            gaps.add(new Gap(lastpos+1, pos-lastpos-1 ));   //bug was here on first argument to Gap()
        }
        lastpos = pos;
        kmers.add(addit);
    }

    /**
     * Takes gaps and creates appropriate index mappings
     */
    protected void createIndexMap() {
        indexToInternalIndex = new int[length];

        int g;	//gap number we are on
        int nextGap;
        Gap currGap;
        int gapTotal=0;
        for (Gap me : gaps){
            gapTotal += me.length;
        }

        internalIndexToIndex = new int[ length-gapTotal ];

        if (gaps.size() == 0) {
            currGap = null;
            g = -1;
            nextGap = -1;
        } else {
            g=0;
            currGap = gaps.get(g);
            nextGap = currGap.pos;
        }

        int runningTotal = 0;
        for (int k=0; k < length-gapTotal; k++){
            if ((k+runningTotal)==nextGap){
                runningTotal += currGap.length;
                g++;
                if (g<gaps.size()){
                    currGap = gaps.get(g);
                    nextGap = currGap.pos;
                } else {
                    currGap = null;
                    g = -1;
                    nextGap = -1;
                }
            }

            internalIndexToIndex[k] = k + runningTotal;
        }

        // -1 is SENTINEL, initialize to all -1, then overwrite
        for (int k=0; k < length; k++){
            indexToInternalIndex[k] = -1;
        }

        for (int k=0; k < length-gapTotal; k++){
            indexToInternalIndex[ internalIndexToIndex[k]  ] = k;
        }
        // now values are -1 if nothing corresponds or the correct index
    }

    /***
     * UNCERTAIN what constitutes failure here
     * @return
     */
    public boolean isValid(){
        return !(internalIndexToIndex.length == 0);
    }

    /**
     * Constructor
     * @param query String representing DNA (or RNA) sequence
     */
    public KmerSequence(String query){
        lastpos = -1; //the last position written to is PRIOR to array that has not been written yet.  Needed for Gap finding logic.

        length = query.length() - Kmer31.KMER_SIZE + 1;
        if (length < 0) length = 0;

        Kmer31 tempy = new Kmer31(0L);
        long nextVal;
        kmers = new ArrayList<Kmer31>();
        gaps = new ArrayList<Gap>();

        if (query.length() < Kmer31.KMER_SIZE){
            System.err.println("Cannot query a sequence as short as "+Integer.toString(query.length())+" to store.");
        } else {

//            Kmer31Iterator it = new Kmer31Iterator(query);
//            DnaBitStringSlow.myIterator it = (DnaBitStringSlow.myIterator) new DnaBitStringSlow(query).iterator();
            DnaBitString dns = new DnaBitString(query);
            DnaBitString.myIterator it = dns.iterator();

            int k=-1;
            while (it.hasNext()){
                //iterate
                Kmer31 temp = it.next();
                k++;

                if (temp != null) addKmer(k,temp);
                if (k==(it.length()-1) && temp == null){
                    //close final gap
                    gaps.add(new Gap(lastpos+1, k-lastpos ));
                }
            }
        } //end if (errors) else (DO)

        if (kmers.size() == 0 && gaps.size() == 0)  gaps.add( new Gap(0, length));
        createIndexMap();
    }


    /**
     * empty constructor for inheritance
     */
    protected KmerSequence(){}

    /***
     * Returns the Kmer31 associated with the index given
     * May be NULL
     *
     * @param seqIndex		this corresponds to the position of the first letter in the reference sequence
     * @return
     */
    public Kmer31 get(int seqIndex ){
        Kmer31 result = Kmer31.SENTINEL;
        if (seqIndex > length ){
            System.err.println("Trying to access index of "+Integer.toString(seqIndex)+" out of bounds.  Max allowed is "+Integer.toString(length-1));
        }
        int k;
        if ( (k = indexToInternalIndex[seqIndex]) != -1 ){
            result = kmers.get(k);
        }
        return result;
    }

    /**
     * returns all positions in kmer sequence
     * @return
     */
    public long[] getAllForward() {
        long[] result = new long[kmers.size()];
        for (int k=0; k<result.length; k++)     result[k] = kmers.get(k).toLong();
        return result;
    }

    /**
     * returns all positions in kmer sequence in reverse
     * Typically, this function is not needed for alignments.  Included as complement of the other.
     * @return
     */
    public long[] getAllReverse() {
        long[] result = new long[kmers.size()];
        for (int k=0; k<result.length; k++)     result[k] = kmers.get(k).reverseStrand().toLong();
        return result;
    }

    /**
     * returns all positions in kmer sequence in forward and in reverse
     * @return
     */
    public long[] getAll() {
        long[] result = new long[2*kmers.size()];
        for (int k=0; k<kmers.size(); k++) {
            result[2*k] = kmers.get(k).toLong();
            result[2*k+1] = kmers.get(k).reverseStrand().toLong();
        }
        return result;
    }


    public String toString() {
        String result = "";
        int gapper = 0; //tracks number used for blanks
        for (int k=0; k< indexToInternalIndex.length; k++){
            Kmer31 temp = get(k);
            if (temp == Kmer31.SENTINEL){
                result += Integer.toString(gapper);
                gapper = (gapper+1)%10;
                if (k == indexToInternalIndex.length -1 ){
                    result += "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
                }

            } else if (k == indexToInternalIndex.length -1 ) {
                gapper = 0;
                result += temp.toString();
            } else {
                gapper = 0;
                result += temp.toString().charAt(0);
            }
        }
        return result;
    }

    /**
     * Converts Kmers list to long[]
     * @return
     */
    public long[] toKmerKeys() {
        long[] result = new long[kmers.size()];
        for(int k=0; k<kmers.size();k++)    result[k] = kmers.get(k).toLong();
        return result;
    }
}
