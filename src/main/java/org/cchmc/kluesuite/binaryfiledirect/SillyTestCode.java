package org.cchmc.kluesuite.binaryfiledirect;

import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.System.exit;

/**
 * Created by osboxes on 5/30/17.
 */
public class SillyTestCode {

    public static byte[] testBytes;

    public static void saveToFile(String filename, DnaBitString dns) throws IOException {
        UnsafeFileWriter ufw = UnsafeFileWriter.unsafeFileWriterBuilder(filename);

        byte[] bytes = new byte[dns.getWriteUnsafeSize()];


        UnsafeMemory um = new UnsafeMemory(bytes);
        dns.writeUnsafe(um);

        ufw.writeObject(um.toBytes());

        int length = um.toBytes().length - 4;
//        testBytes = new byte[length];
//        System.arraycopy(um.toBytes(),4,testBytes,0,length);

        ufw.close();
    }


    public static DnaBitString loadFromFile(String filename) throws IOException{
        DnaBitString dns = null;
        UnsafeFileReader ufr = UnsafeFileReader.unsafeFileReaderBuilder(filename);

        byte[] bytes = ufr.readNextObject();
        //debug
//        System.err.println("bytes vs testBytes");
//        System.err.println(Arrays.toString(bytes));
//        System.err.println(Arrays.toString(testBytes));

        dns = DnaBitString.unsafeMemoryBuilder(new UnsafeMemory(bytes));



        ufr.close();
        return dns;
    }

    public static void main(String[] args){

        byte[] test = new byte[8];

        UnsafeMemory um = new UnsafeMemory(test);
        long in = 9212767992367705987L;
        um.putLong(in);
        um.reset();

        long out = um.getLong();
        System.err.println(in);
        System.err.println(out);
        System.err.println(Arrays.toString(RocksDbKlue.longToBytes(in)));
        System.err.println(Arrays.toString(RocksDbKlue.longToBytes(out)));

        um.reset();
        um.putChar('W');
        um.putChar('X');
        um.putChar('Y');
        um.putChar('Z');
        um.reset();
        for(byte b : "WXYZ".getBytes()){
            System.err.println(Integer.toBinaryString((int)b));
        }
        for(int i = 0; i < 4; i++) {
            char c=um.getChar();
           System.err.println(Integer.toBinaryString((int)c)+"\t"+(int)c);
        }
        /*System.err.println(um.getChar());
        System.err.println(um.getChar());
        System.err.println(um.getChar());
        System.err.println(um.getChar());
*/

        String CODE;
        DnaBitString foo;
        DnaBitString beau;

//        CODE = "XWGTCGACCTGANGAGTC";
//        foo = new DnaBitString(CODE);
//        beau = null;

//        try {
//            saveToFile("testerfoo.bin",foo);
//            beau = loadFromFile("testerfoo.bin");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("CODE is (original, dns before, dns read");
//        System.out.println(CODE);
//        System.out.println(foo);
//        System.out.println(beau);
//



        CODE = "XWGT";
        foo = new DnaBitString(CODE);
        beau = null;

        byte[] bytes = new byte[foo.getWriteUnsafeSize()];
        um = new UnsafeMemory(bytes);
        System.err.println("Testing DnaBitString.writeUnsafe()");
        foo.writeUnsafe(um);
        System.err.println("Testing DnaBitString.readUnsafe()");
        um.reset();
        um.getInt();
        beau = DnaBitString.unsafeMemoryBuilder(um);
//        beau.readUnsafe(um);

//        try {
//            saveToFile("testerfoo.bin",foo);
//            beau = loadFromFile("testerfoo.bin");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("CODE is (original, dns before, dns read");
        System.out.println(CODE);
        System.out.println(foo);
        System.out.println(beau);
    }

}
