package org.cchmc.kluesuite.datastreams;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import org.cchmc.kluesuite.klat.PartialAlignment1;
import org.cchmc.kluesuite.klat.ReferenceSequenceRequest;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

/**
 * Created by MALS7H on 5/18/2017.
 */
public class Record {

    //Common between SAM and BLAST
    public String queryName = "";
    public String referenceName = "";
    private int referenceStart = -1;     //sequenceStart for BLAST, POS for SAM
    private int alignmentLength;    //tLength for SAM


    //BLAST
    private double percentOfIdentity;
    private int mismatch;
    private int gapopen;
    private int queryStart;
    private int queryEnd;
    private int sequenceEnd;
    private double expectedValue;
    private double bitScore;

    //SAM
    private boolean
        pairedFlag1,
        properlyAlignedFlag2,
        unmappedFlag4,
        nextUnmappedFlat8,
        reverseFlag16,
        nextReverseFlag32,
        firstSegment64,
        lastSegment128,
        secondayAlignment256,
        notPassingFiltersFlag512,
        opticalDuplicate1024,
        supplementaryAlignment2048;
    private int mapq;
    private String CIGARstr = "";
    private int refNext = -1;
    private int posNext;
    public String segSequence = "";
    public String quality = "";

    public static Record getSAMRecord(PartialAlignment1 pa, ReferenceSequenceRequest t, KidDatabaseMemory myKidDB, String queryName) {
        Record rec = new Record();

        if (t.reverse) {
            //RECALL STOP is EXCLUSIVE, here we report INCLUSIVE
            rec.referenceStart = t.stop - 1 - pa.sstart;
            rec.sequenceEnd = t.stop - 1 - pa.send;
        } else {
            rec.referenceStart = t.start + pa.sstart;
            rec.sequenceEnd = t.start + pa.send;
        }
        rec.queryName = queryName;
        rec.referenceName = t.myKID+"|"+myKidDB.getName(t.myKID);
        rec.alignmentLength = pa.length;
        rec.CIGARstr = pa.CIGARString;

        return rec;
    }

    public SAMRecord getSAMRepresentation() {
        //Maybe something different instead of an empty string?
        //https://samtools.github.io/hts-specs/SAMv1.pdf
        //http://static.javadoc.io/com.github.samtools/htsjdk/1.132/htsjdk/samtools/SAMRecord.html#setReadString(java.lang.String)
        SAMRecord record = new SAMRecord(new SAMFileHeader());

        record.setReadName(queryName);

        record.setProperPairFlag(pairedFlag1);
        record.setNotPrimaryAlignmentFlag(properlyAlignedFlag2);
        record.setReadUnmappedFlag(unmappedFlag4);
        record.setMateUnmappedFlag(nextUnmappedFlat8);
        record.setReadNegativeStrandFlag(reverseFlag16);
        record.setMateNegativeStrandFlag(nextReverseFlag32);
        record.setFirstOfPairFlag(firstSegment64);
        record.setSecondOfPairFlag(lastSegment128);
        record.setReadPairedFlag(secondayAlignment256);
        record.setReadFailsVendorQualityCheckFlag(notPassingFiltersFlag512);
        record.setDuplicateReadFlag(opticalDuplicate1024);
        record.setSupplementaryAlignmentFlag(supplementaryAlignment2048);

        if (referenceName == null || referenceName.isEmpty()) {
            record.setReferenceName(SAMRecord.NO_ALIGNMENT_REFERENCE_NAME);
        }
        else {
            record.setReferenceName(referenceName);
        }

        record.setMappingQuality(mapq);
        record.setCigarString(CIGARstr);
        record.setAlignmentStart(alignmentLength);
        record.setMateAlignmentStart(posNext);
        record.setBaseQualityString(quality);
        record.setReadString(segSequence);

        if (refNext != -1) {
            record.setMateReferenceIndex(refNext);
        }
        record.setReferenceIndex(referenceStart);
        return record;
    }

    public static Record getBLASTRecord(PartialAlignment1 pa, ReferenceSequenceRequest t, KidDatabaseMemory myKidDB, String queryName) {
        Record rec = new Record();

        if (t.reverse) {
            //RECALL STOP is EXCLUSIVE, here we report INCLUSIVE
            rec.referenceStart = t.stop - 1 - pa.sstart;
            rec.sequenceEnd = t.stop - 1 - pa.send;
        } else {
            rec.referenceStart = t.start + pa.sstart;
            rec.sequenceEnd = t.start + pa.send;
        }
        rec.queryName = queryName;
        rec.referenceName = t.myKID+"|"+myKidDB.getName(t.myKID);
        rec.percentOfIdentity = pa.pident*100;
        rec.alignmentLength = pa.length;
        rec.mismatch = pa.mismatch;
        rec.gapopen = pa.gapopen;
        rec.queryStart = pa.qstart + 1;
        rec.queryEnd = pa.qend + 1;
        rec.expectedValue = pa.evalue;
        rec.bitScore = pa.bitscore;

        return rec;
    }

    public String getBLASTRepresentation() {
        int sstart, send;
        String result = "";

        result+=queryName+"\t";
        result+=referenceName+"\t";
        result+=percentOfIdentity+"\t";
        result+=alignmentLength+"\t";
        result+=mismatch+"\t";

        result+=gapopen+"\t";
        result+=(queryStart+1)+"\t";
        result+=(queryEnd+1)+"\t";
        result+=(referenceStart+1)+"\t";
        result+=(sequenceEnd+1)+"\t";
        result+=expectedValue+"\t";
        result+=bitScore+"";

        return result;
    }
}
