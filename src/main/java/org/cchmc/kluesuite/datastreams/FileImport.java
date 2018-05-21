package org.cchmc.kluesuite.datastreams;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.cchmc.kluesuite.klue.SuperString;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by osboxes on 22/03/17.
 * Reads input files.  Requires fancy builder:  FastA or FastQ
 */
public class FileImport {
    //How to parse file from disk
    public enum CompressionFormat {UNCOMPRESSED, BZIP, GZIP}
    //How to parse text from file
    public enum FileFormat {FASTA, FASTQ}

    //File saved
    private File file;
    //Compression format
    private CompressionFormat readFormat;
    //File format
    private FileFormat fileFormat;
    //Buffered Reader for the file
    private BufferedReader br;
    //previous line
    private String temporaryLine;

    /**
     * Creates a file importer
     * @param name Path of file.
     * @param readFormat Read Format.
     * @param fileFormat File Format.
     */
    public FileImport(String name, CompressionFormat readFormat, FileFormat fileFormat) throws
            FileNotFoundException, IOException, CompressorException
    {
        file = new File(name);
        this.readFormat = readFormat;
        this.fileFormat =  fileFormat;

        if(readFormat == CompressionFormat.UNCOMPRESSED) {
            br = new BufferedReader(new FileReader(file));
        }
        //Use java default GZIP Input Stream for GZip
        else if (readFormat == CompressionFormat.GZIP) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            br = new BufferedReader(new InputStreamReader(gzip));
        }
        //Use commons-compress library for BZip http://commons.apache.org/proper/commons-compress/examples.html
        else if (readFormat == CompressionFormat.BZIP) {
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fin);
            CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
            br = new BufferedReader(new InputStreamReader(input));
        }
    }

    /**
     * Gets next line in file based on compression.
     * @return String of next line or null if the file has ended.
     */
    private String getLine() throws IOException
    {
        if (temporaryLine == null)
            return br.readLine();
        String tmp = temporaryLine;
        temporaryLine = null;
        return tmp;
    }

    /**
     * Reads the next file
     * @return
     */
    private SuperString getNextSuperString() throws IOException
    {
        char delimiter = getDelimiter(fileFormat);
        //Delimiter is always at the start of a line.
        //Delimiter marks the start of a new superstring.
        String line = getLine();
        if(line == null)
            return null;

        SuperString ss = new SuperString(line);
        if (fileFormat == FileFormat.FASTA) {
            while ((line = getLine()) != null) {
                if (line.charAt(0) == delimiter) {
                    temporaryLine = line;
                    return ss;
                } else {
                    ss.addAndTrim(line);
                }
            }
        }
        else if(fileFormat == FileFormat.FASTQ) {
            for(int i = 0; i < 3; i++) {
                line = getLine();
                ss.addAndTrim(line);
            }
        }

        return ss;
    }

    /**
     * Get next query from file based on format.
     * @return Query instance describing next query, or null if there are no more entries.
     */
    public Query getNextEntry() throws IOException {
        SuperString ss = getNextSuperString();
        if (ss != null)
            return parseQuery(fileFormat, ss);
        return null;
    }

    /**
     * Get the delimiter for a file format
     * @param fileFormat File format
     * @return delimiter char
     */
    private static char getDelimiter(FileFormat fileFormat) {
        char delimiter = '>';
        if (fileFormat == FileFormat.FASTQ) {
            delimiter = '@';
        }
        return delimiter;
    }

    /**
     * Parses a query from formatted text.
     * @param format Format of the file.
     * @param text Group of lines in the given format.
     * @return A formatted query.
     */
    public static Query parseQuery(FileFormat format, SuperString text) {
        String queryName = text.strings.get(0).substring(1);
        String querySequence = "";
        if(format == FileFormat.FASTQ) {
            querySequence = text.strings.get(1);
        }
        else if(format == FileFormat.FASTA) {
            StringBuilder builder = new StringBuilder( );
            for (int line = 1; line < text.strings.size(); line++) {
                builder.append(text.strings.get(line));
            }
            querySequence = builder.toString();
        }
        return new Query(queryName, querySequence);
    }

    /**
     * Test for running the file import script
     */
    public static void main(String[] args){
        String testPath = "C:\\Users\\mals7h\\Documents\\test.fa";
        FileFormat testFormat = FileFormat.FASTA;
        CompressionFormat testComp = CompressionFormat.UNCOMPRESSED;

        try {
            FileImport testFile = new FileImport(testPath, testComp, testFormat);
            Query next = null;
            while((next = testFile.getNextEntry()) != null) {
                System.out.println(next.queryName);
                System.out.println("\t" + next.querySequence);
            }
            testPath = "C:\\Users\\mals7h\\Documents\\test.fa.gz";
            testComp = CompressionFormat.GZIP;
            testFile = new FileImport(testPath, testComp, testFormat);
            next = null;
            while((next = testFile.getNextEntry()) != null) {
                System.out.println(next.queryName);
                System.out.println("\t" + next.querySequence);
            }
            testPath = "C:\\Users\\mals7h\\Documents\\test.fa.bz2";
            testComp = CompressionFormat.BZIP;
            testFile = new FileImport(testPath, testComp, testFormat);
            next = null;
            while((next = testFile.getNextEntry()) != null) {
                System.out.println(next.queryName);
                System.out.println("\t" + next.querySequence);
            }
            testPath = "C:\\Users\\mals7h\\Documents\\run1.short.fq";
            testFormat = FileFormat.FASTQ;
            testComp = CompressionFormat.UNCOMPRESSED;
            testFile = new FileImport(testPath, testComp, testFormat);
            next = null;
            while((next = testFile.getNextEntry()) != null) {
                System.out.println(next.queryName);
                System.out.println("\t" + next.querySequence);
            }
            testPath = "C:\\Users\\mals7h\\Documents\\run1.short.fq.gz";
            testComp = CompressionFormat.GZIP;
            testFile = new FileImport(testPath, testComp, testFormat);
            next = null;
            while((next = testFile.getNextEntry()) != null) {
                System.out.println(next.queryName);
                System.out.println("\t" + next.querySequence);
            }
            testPath = "C:\\Users\\mals7h\\Documents\\run1.short.fq.bz2";
            testFormat = FileFormat.FASTQ;
            testComp = CompressionFormat.BZIP;
            testFile = new FileImport(testPath, testComp, testFormat);
            next = null;
            while((next = testFile.getNextEntry()) != null) {
                System.out.println(next.queryName);
                System.out.println("\t" + next.querySequence);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException e) {
            e.printStackTrace();
        }
    }
}
