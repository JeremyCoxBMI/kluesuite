package org.cchmc.kluesuite.binaryfiledirect;

import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by osboxes on 5/30/17.
 *
 * Reads an Unsafe DataFile into object chunks based on size headers written as first entry.
 * Note this removes the byte size information for readUnsafe() family functions.
 *
 */
public class UnsafeFileReader {

    private String filename;
    private FileInputStream reader;
    private BufferedInputStream bis;

    private UnsafeFileReader(){
        filename = null;
        reader =null;
        bis = null;
    }

    public static UnsafeFileReader unsafeFileReaderBuilder(String filename) throws FileNotFoundException {
        UnsafeFileReader ufr = new UnsafeFileReader();
        ufr.filename = filename;
        ufr.reader = new FileInputStream(filename);
        ufr.bis = new BufferedInputStream(ufr.reader);

        return ufr;
    }

    /**
     * Reads the next object from the stream, which means specifically to read a byte count header.
     * NOTE  _PRIMITIVES_ int, char, long DO NOT have byte count header and have separate functions
     * @return
     * @throws IOException
     */
    public byte[] readNextObject() throws IOException {
        int size = getInt();
        //reads byte size header
        byte[] result = new byte[  (size- UnsafeMemory.SIZE_OF_INT)  ];
        bis.read(result,0,result.length);
        return result;
    }

    /**
     * INT, LONG, CHAR, STRING datatypes put no size or Serial UID information, in order to save space /time
     * Hence, they need specific readers
     * @return
     * @throws IOException
     */
    public int getInt() throws IOException {
        byte[] result = new byte[UnsafeMemory.SIZE_OF_INT];
        bis.read(result,0,UnsafeMemory.SIZE_OF_INT);
        UnsafeMemory um = new UnsafeMemory(result);
        return um.getInt();
    }

    /**
     * INT, LONG, CHAR, STRING datatypes put no size or Serial UID information, in order to save space /time
     * Hence, they need specific readers
     * @return
     * @throws IOException
     */
    public String getString() throws IOException {
        int size = getInt();
        byte[] result = new byte[size];
        bis.read(result,0,size);
        char[] result2 = new char[size];
        for (int k=0; k<size;k++){
            result2[k] = (char) result[k];        }

        return new String(result2);  //String((char[]) result);
    }

    /**
     * INT, LONG, CHAR datatypes put no size or Serial UID information, in order to save space /time
     * Hence, they need specific readers
     * @return
     * @throws IOException
     */
    public long getChar() throws IOException {
        byte[] result = new byte[UnsafeMemory.SIZE_OF_CHAR];
        bis.read(result,0,UnsafeMemory.SIZE_OF_CHAR);
        UnsafeMemory um = new UnsafeMemory(result);
        return um.getChar();
    }

    /**
     * INT, LONG, CHAR datatypes put no size or Serial UID information, in order to save space /time
     * Hence, they need specific readers
     * @return
     * @throws IOException
     */
    public long getLong() throws IOException {
        byte[] result = new byte[UnsafeMemory.SIZE_OF_LONG];
        bis.read(result,0,UnsafeMemory.SIZE_OF_LONG);
        UnsafeMemory um = new UnsafeMemory(result);
        return um.getLong();
    }

    public void close() throws IOException {
//        bis.close();
        bis.close();
        reader.close();
    }

//    private long getLong(){
//
//    }
//
//    private char getChar(){
//
//    }
}
