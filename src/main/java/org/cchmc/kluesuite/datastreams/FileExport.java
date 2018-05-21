package org.cchmc.kluesuite.datastreams;

import htsjdk.samtools.*;
import org.cchmc.kluesuite.klat.AlignmentKLAT1;

import java.io.*;

/**
 * Created by osboxes on 22/03/17.
 * Class to write KLAT results as an output file.
 * Needs fancy builder, including options of which columns to include.
 */
public class FileExport {

    /**
     * Different types of export formats
     */
    public enum ExportType {SAM, BAM, BLAST6, BLAST7};
    /**
     * Export type for this instance
     */
    private ExportType exportType;
    /**
     * Saved file writer for this instance
     */
    private SAMFileWriter samWriter;
    /**
     * Saved filewriter for later reference
     */
    private FileWriter fw;
    /**
     * Filewtier for blast6 and 7 format
     */
    private BufferedWriter brWriter;

    /**
     * Creates a file exporter. Must call close() on exiting program to properly save file.
     * @param name Path to the file, include extension
     * @param exportType Type of file to export
     * @param textHeader String for the text header of the file.
     */
    public FileExport(String name, ExportType exportType, String textHeader) {
        this.exportType = exportType;
        File file = new File(name);
        SAMFileHeader header = new SAMFileHeader();
        header.setTextHeader(textHeader);


        if(exportType == ExportType.SAM) {
            SAMFileWriterFactory factory = new SAMFileWriterFactory();
            samWriter = factory.makeSAMWriter(header, false, file);
        }
        else if (exportType == ExportType.BAM) {
            SAMFileWriterFactory factory = new SAMFileWriterFactory();
            samWriter = factory.makeBAMWriter(header, false, file);
        }
        else if (exportType == ExportType.BLAST6 || exportType ==ExportType.BLAST7) {
            try {
                fw = new FileWriter(file);
                brWriter = new BufferedWriter(fw);
                if(exportType == ExportType.BLAST7) {
                    brWriter.write(AlignmentKLAT1.Blast6Header + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void close() {
        if(exportType == ExportType.BAM || exportType == ExportType.SAM) {
            samWriter.close();
        }
        else if(exportType == ExportType.BLAST6 || exportType == ExportType.BLAST7) {
            try {
                brWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSAMRecord(SAMRecord record) {
        samWriter.addAlignment(record);
    }

    public void writeRecord(Record record) {
        //Blast6, append string to file as plaintext
        //SAM and BAM need to be written as special format
        //
        if(exportType == ExportType.SAM || exportType == ExportType.BAM) {
            writeSAMRecord(record.getSAMRepresentation());
        }
        else if(exportType == ExportType.BLAST6 || exportType == ExportType.BLAST7){
            try {
                brWriter.write(record.getBLASTRepresentation() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        FileExport exporter = new FileExport("Test.sam", ExportType.SAM, "");
        Record rec = new Record();
        rec.queryName = "AAAAAAAAAAAAAAAAAAAAAAAAADDDDDDDDDDDDDDDDDDDD";
        rec.referenceName = "PQWOIEJDPWIJEPOFJISEFJPEWIFPIEWJF";
        exporter.writeRecord(rec);
        exporter.close();

        exporter = new FileExport("Test.bam", ExportType.BAM, "");
        exporter.writeRecord(rec);
        exporter.close();

        exporter = new FileExport("Test.blast6", ExportType.BLAST6, "");
        exporter.writeRecord(rec);
        exporter.close();

        exporter = new FileExport("Test.blast7", ExportType.BLAST7, "");
        exporter.writeRecord(rec);
        exporter.close();

    }
}
