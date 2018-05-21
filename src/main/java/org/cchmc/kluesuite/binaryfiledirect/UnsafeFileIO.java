package org.cchmc.kluesuite.binaryfiledirect;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by jwc on 6/7/17.
 *
 * Loading or saving one large data structure to a file allows the advantage of using speed of UnsafeMemory
 * BUT conserving memory by atomizing the object into bite size pieces
 * ################################################
 * This is for LARGE objects.  Can have multiple large object in a single file readUnsafe / writeUnsafe
 * ################################################
 * Note that a dataStructure should have either UnsafeFileIO (large memory size)
 *                                              XOR
 *                                              UnsafeSerializable Interface (small to medium size)
 */

public interface UnsafeFileIO {

    /**
     * The size in bytes of the object
     * Note this is ALWAYS written first when an object is written to UnsafeMemory or to UnsafeFileWriter.
     * @return
     */
    public int getWriteUnsafeSize();

    /**
     *  Loads this SINGLE object from a FILE without other Objects.
     *
     *  Pay special attention when using UnsafeSerializable operations.
     *  You can read objects serially just using UnsafeSerialiable.readUnsafe() member functions
     *  If you do so, you MUST manually read the 4 byte integer header FIRST
     *
     * @param filename
     * @throws IOException
     */
    public void loadFromFileUnsafe(String filename) throws IOException;

    /**
     * Saves this OBJECT to a FILE without other Objects.
     * @throws IOException
     */
    public void saveToFileUnsafe() throws IOException;

    /**
     * Assumes a fresh file is opened.
     * Specically, ufr.read() has not been called to peel off an int for byte size
     * This function loads the byte file using UnsafeFileReader.
     *
     * @param ufr UnsafeFileReader, with the next object being *this
     * @throws IOException
     */
    public void readUnsafe(UnsafeFileReader ufr) throws ClassCastException;


    /**
     * This function writes the byte file using UnsafeFileWriter
     * allows multiple large objects to write to a single file
     * @throws IOException
     */
    public void writeUnsafe(UnsafeFileWriter ufw) throws IOException;

}
