package org.cchmc.kluesuite.binaryfiledirect;

import java.io.*;

/**
 * Created by osboxes on 5/30/17.
 *
 * Customarily, the programmer uses UnsafeMemory class to make bite size byte arrays to write to the file.
 */
public class UnsafeFileWriter {

    private String filename;

    private FileOutputStream Fwriter;
    private BufferedOutputStream writer;

    private UnsafeFileWriter(){
        Fwriter = null;
        writer = null;
        filename = null;
    }

    public static UnsafeFileWriter unsafeFileWriterBuilder(String filename) throws IOException{
        UnsafeFileWriter ufw = new UnsafeFileWriter();

        ufw.filename = filename;
        ufw.Fwriter = new FileOutputStream(filename);
        ufw.writer = new BufferedOutputStream(ufw.Fwriter);

        return ufw;
    }

    public void writeObject(byte[] bytes) throws IOException{
        writer.write(bytes,0,bytes.length);
        courtesyFlush();
    }

    public void writeObject(UnsafeMemory um) throws IOException{
        writer.write(um.toBytes(),0,um.toBytes().length);
        courtesyFlush();
    }

    public void close() throws IOException {
        writer.flush();
        Fwriter.flush();
        writer.close();
        Fwriter.close();
    }

    public void flush() {
        try {
            writer.flush();
            Fwriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flushes all buffers to complete file writing.
     * flush twice = courtesy flush
     */
    public void courtesyFlush() {

        try {
            writer.flush();
            Fwriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeObject(String fileName) throws IOException {
//        byte[] b = new byte[  UnsafeMemory.getWriteUnsafeSize(fileName, UnsafeMemory.STRING_TYPE)  ];
        UnsafeMemory um = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(fileName, UnsafeMemory.STRING_TYPE));
        um.putString(fileName);
        writeObject(um.toBytes());
        courtesyFlush();
    }
}
