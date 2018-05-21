package org.cchmc.kluesuite.klat;

import org.cchmc.kluesuite.klat2.PartialAlignment2;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabase;
import org.cchmc.kluesuite.multithread.KidDatabaseThreadSafe;
import org.cchmc.kluesuite.variantklue.Variant;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by jwc on 2018/04/26.
 *
 * PartialAlignment1 does not fully conform
 */
public abstract class PartialAlignment implements Comparable<PartialAlignment>, Comparator<PartialAlignment>{

    //Constructor: calculates all values, except VariantCalls
    public int numMatches;

    /**
     *
     * Sam File CIGAR string
     */
    public String CIGARString;

    /**
     * possibly a duplication of the length parameter.
     * This counts the total length of matches, mismatches, and gaps in the alignment.
     * or, then length of the laignment.  Note this can be more or less than the query length, because there may be unaligned edges.
     */
    public int numAligned;


    /**
     * BlastN format6 reporting term
     * percent of identical matches
     *
     * Percent Identity = (Matches nextOffset 100)/Length of aligned region (with gaps)
     */
    public float pident;


    /**
     * BlastN format6 reporting term
     * this is alignment length
     *
     * IN SAM FILE, tlen is reference alignment length
     * IN BLAST, length is reference sequence used to align length
     */
    public int length;

    /**
     * BlastN format6 reporting term
     *
     */
    public int mismatch;

    /**
     * BlastN format6 reporting term
     * Position in query where alignment starts, INCLUSIVE 0-indexed
     */
    public int qstart;

    /**
     * BlastN format6 reporting term
     * Position in query where alignment ends, INCLUSIVE 0-indexed
     */
    public int qend;

    /**
     * BlastN format6 reporting term
     * Position in reference sequence where alignment begins, INCLUSIVE 0-indexed
     * NOTE this is 0-indexed, as we do not know the offset of the sequence submitted
     * Furthermore, if it is reversed, that complicates matters
     */
    public int sstart;

    /**
     * BlastN format6 reporting term
     * Position in reference sequence where alignment ends, INCLUSIVE 0-indexed
     * NOTE this is 0-indexed, as we do not know the offset of the sequence submitted
     * Furthermore, if it is reversed, that complicates matters
     */
    public int send;

    /**
     * BlastN format6 reporting term
     */
    public double evalue;

    /**
     * BlastN format6 reporting term
     */
    public int bitscore;

    /**
     * Fast Klat Score for the AlignmentKLAT1
     */
    public int fastKLATscore = 0;

    /**
     * is the alignment on reverse strand
     */
    boolean reverse;

    /**
     * Optional data element, describes SNPs that could be cause of mismatches
     */
    public ArrayList<Variant> mismatchesCalled;

    /**
     * Smith Waterman Score
     */
    public int score;

    /**
     * Total number of gaps in alignment
     */
    public int gapopen;

    //Add all getters

    public int getNumMatches() {
        return numMatches;
    }

    public String getCIGARString() {
        return CIGARString;
    }

    public boolean isReverse(){
        return reverse;
    }

    public int getNumAligned() {
        return numAligned;
    }

    public int getLength() {
        return length;
    }

    public int getMismatch() {
        return mismatch;
    }

    public double getEvalue() {
        return evalue;
    }

    public int getBitscore() {
        return bitscore;
    }

    public ArrayList<Variant> getMismatchesCalled() {
        return mismatchesCalled;
    }

    public int getFastKLATscore() {
        return fastKLATscore;
    }

    public float getPercentIdentity() {
        return pident;
    }

    public int getScore(){ return score;}

    public ArrayList<PartialAlignment> bestAlignments(){
        return bestAlignments();
    }

    @Override
    public int compareTo(PartialAlignment o) {
        return getScore() - o.getScore();
    }

    @Override
    public int compare(PartialAlignment o1, PartialAlignment o2) {
        return o1.compareTo(o2);
    }

    public abstract String printAlignment();

//    /**
//     * creates output for this alignment only
//     *
//     * Note that KLUE is 0-index system, but outformat 6 uses 1-indexing
//     * @param myKidDB
//     * @return
//     */
//    public String toBlast6(KidDatabase myKidDB) {
//        int sstart, send;
//        SuperString result = new SuperString();
//
//        //String result = "";
//        if (reverse){
//
//        }
//        if (t.reverse) {
//            //RECALL STOP is EXCLUSIVE, here we report INCLUSIVE
//            sstart = t.stop - 1 - pa.sstart;
//            send = t.stop - 1 - pa.send;
//        } else {
//            sstart = t.start + pa.sstart;
//            send = t.start + pa.send;
//        }
//
//        //0
//        //result+=queryName+"\t";
//
//        result.addAndTrim(queryName);
//        result.add("\t");
//        //result+=t.myKID+"\t";
//
//        //1
//        if (KidDatabaseThreadSafe.ON == true){
//            result.addAndTrim(t.myKID + "|" + KidDatabaseThreadSafe.getName(t.myKID));
//        } else {
//            result.addAndTrim(t.myKID + "|" + myKidDB.getName(t.myKID));
//        }
//        result.add("\t");
//
//        //2
////        result+=pa.pident*100+"\t";
//        result.addAndTrim(Double.toString(pa.pident*100));
//        result.add("\t");
//
//        //3
////        result+=pa.length+"\t";
//        result.addAndTrim(Integer.toString(pa.length));
//        result.add("\t");
//
//        //4
////        result+=pa.mismatch+"\t";
//        result.addAndTrim(Integer.toString(pa.mismatch));
//        result.add("\t");
//
//        //5
////        result+=pa.gapopen+"\t";
//        result.addAndTrim(Integer.toString(pa.gapopen));
//        result.add("\t");
//
//        //6
////        result+=(pa.qstart+1)+"\t";
//        result.addAndTrim(Integer.toString(pa.qstart+1));
//        result.add("\t");
//
//        //7
////        result+=(pa.qend+1)+"\t";
//        result.addAndTrim(Integer.toString(pa.qend+1));
//        result.add("\t");
//
//        //8
////        result+=(sstart+1)+"\t";
//        result.addAndTrim(Integer.toString(sstart+1));
//        result.add("\t");
//
//        //9
////        result+=(send+1)+"\t";
//        result.addAndTrim(Integer.toString(send+1));
//        result.add("\t");
//
//        //10
////        result+=pa.evalue+"\t";
//        result.addAndTrim(Double.toString(pa.evalue));
//        result.add("\t");
//
//        //11
////        result+=pa.bitscore+"\t";
//        result.addAndTrim(Integer.toString(pa.bitscore));
//        result.add("\t");
//
//        //12
////        result+=pa.fastKLATscore+"";
//        result.addAndTrim(Integer.toString(pa.fastKLATscore));
//        //no delimiter, last
//
////        //13
////        //Called variants (optional)
////        if (CALL_VARIANTS){
////            result.add("\t");
////            result.addAndTrim(Variant.variantNameList(pa.callVariants()));
////        }
//
//        return result.toString();
//    }


}
