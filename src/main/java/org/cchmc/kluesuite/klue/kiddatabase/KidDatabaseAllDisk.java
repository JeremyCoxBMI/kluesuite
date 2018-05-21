package org.cchmc.kluesuite.klue.kiddatabase;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.helperclasses.LogStream;
import org.cchmc.kluesuite.klue.*;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import static htsjdk.samtools.util.StringUtil.stringToBytes;
import static java.lang.System.exit;

/**
 * Created by jwc on 4/11/18.
 *
 * Everything stored in key-value store
 *
 * Note associated keys in lexicographic order
 * 8 byte long integer (WholeNumberPairedDatabaseKey) Kid and -2000    ==> to length
 * 8 byte long integer (WholeNumberPairedDatabaseKey) kid and -145     ==> to exceptions HashMap
 * 8 byte long integer (WholeNumberPairedDatabaseKey) kid and -1576    ==> to String
 * 8 byte long integer (WholeNumberPairedDatabaseKey) kid and position ==> DnaBitString fragment
 * String of longer than 8 bytes ==> kid & length
 *
 *
 * aString.getBytes("UTF-8");
 * https://stackoverflow.com/questions/5513144/converting-char-to-byte
 *
 */
public class KidDatabaseAllDisk implements KidDatabase {

    public static final String KMER_SUFFIX = ".kmer";
    public static final String DISKALL_SUFFIX = ".kidDB.diskall";
    public static final String TEMPORARY_KIDDB_SUFFIX = ".deleteme.kid.temp";
    public static final String TEMPORARY_KLUE_SUFFIX = ".deleteme.kmer.temp";
    public static final String STARTEND_SUFFIX = ".kmer.startEnd";
    public static final String TEMPORARY_STARTEND_SUFFIX = ".kmer.deleteme.startEnd";

    static {
        RocksDB.loadLibrary();
    }

    String databasePrefix;
    String rocksdbfilename;

    private static int NUM_LONG_IN_CHUNK = 4; //128 letters
    private static int NUM_BASE_IN_CHUNK = 128;
    private static int NUM_BITS_LONG = 64;
    public static final int maxFiles = 5;

    RocksDB rocks;

    public KidDatabaseAllDisk(String prefix, boolean readonly){

        databasePrefix = prefix;
        rocksdbfilename = prefix+".kidDB.alldisk";

        Options options = new Options().setCreateIfMissing(true).setMaxOpenFiles(maxFiles);

        try {
            if (readonly) rocks = RocksDB.openReadOnly(options, rocksdbfilename);
            else rocks = RocksDB.open(options, rocksdbfilename);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public KidDatabaseAllDisk(String prefix, String databaseName, boolean readonly) {

        int maxFiles = 5;
        databasePrefix = prefix;
        rocksdbfilename = databaseName;

        Options options = new Options().setCreateIfMissing(true).setMaxOpenFiles(maxFiles);

        try {
            if (readonly) rocks = RocksDB.openReadOnly(options, rocksdbfilename);
            else rocks = RocksDB.open(options, rocksdbfilename);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public void putData(int kid, String name, SuperString ssdns) {

        LogStream.stdout.printlnTimeStamped("putting onto database\t"+kid+"\t"+name+"\t"+ssdns.get(0,20));

        try {
            DnaBitString dns = new DnaBitString(ssdns);

            //lexicographic order write
            int l = dns.getLength();
            WholeNumberPairDatabaseKey w = new GetLengthKey(kid);
            rocks.put(w.toBytes(), convertIntToBytes(l));

            dns.putExceptions(rocks, new GetExceptionsArrKey(kid));

            w = new GetSequenceNameKey(kid);
            GetKidFromSequenceNameKey s = GetKidFromSequenceNameKey.Builder(name);
            rocks.put(w.toBytes(), s.toBytes());

            //not written in lexicographic order here
            rocks.put(s.toBytes(), convertIntToBytes(kid));

            //DO LAST, so increasing order
            long[] longs = dns.toLongArray();
            storeSequence128(longs, kid);

        } catch (RocksDBException e) {
            e.printStackTrace();  //rocks.put()...
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();    //byte[] s = ...
        } catch (DataFormatException e) {
            e.printStackTrace();    //DNaBitString
        }
    }

    public void putData(int kid, String name, SuperString ssdns, KLUE kmers, KLUE startEnd) {

        LogStream.stdout.printlnTimeStamped("putting onto database\t"+kid+"\t"+name+"\t"+ssdns.get(0,20));

        try {
            DnaBitStringToDb dns = new DnaBitStringToDb(new DnaBitString(ssdns), kmers, kid);

            //lexicographic order write
            int l = dns.getLength();
            WholeNumberPairDatabaseKey w = new GetLengthKey(kid);
            rocks.put(w.toBytes(), convertIntToBytes(l));

            dns.putExceptions(rocks, new GetExceptionsArrKey(kid));

            w = new GetSequenceNameKey(kid);
            GetKidFromSequenceNameKey s = GetKidFromSequenceNameKey.Builder(name);
            rocks.put(w.toBytes(), s.toBytes());

            //not written in lexicographic order here
            rocks.put(s.toBytes(), convertIntToBytes(kid));

            //DO LAST, so increasing order
            LogStream.stdout.printlnTimeStamped("\tputting onto database piecemail DnaBitString\t"+kid+"\t"+name+"\t"+ssdns.get(0,20));
            long[] longs = dns.toLongArray();
            storeSequence128(longs, kid);

            LogStream.stdout.printlnTimeStamped("\tputting onto database kmers\t"+kid+"\t"+name+"\t"+ssdns.get(0,20));
            dns.writeAllPositions(startEnd);

        } catch (RocksDBException e) {
            e.printStackTrace();  //rocks.put()...
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();    //byte[] s = ...
        } catch (DataFormatException e) {
            e.printStackTrace();    //DNaBitString
        }
    }


//    public static byte[] stringToBytes(String s) throws UnsupportedEncodingException {
//        if (s.length() < 9) s = s+"        "; //8 spaces
//        return s.getBytes("UTF-8");
//    }
//
//    public static byte[] lengthNamePair(int length, String name) throws UnsupportedEncodingException {
//        byte[] s = name.getBytes("UTF-8");
//        ByteBuffer r = ByteBuffer.allocate(8+s.length);
//        r.putInt(length);
//        r.putInt(name.length());
//        r.put(s);
//        return r.array();
//    }
//
//    public static byte[] kidTo4Bytes(int kid){
//        ByteBuffer r = ByteBuffer.allocate(4);
//        r.putInt(kid);
//        return r.array();
//    }
//
//    public static byte[] kidLengthPairToBytes(int kid, int length){
//        ByteBuffer r = ByteBuffer.allocate(8);
//        r.putInt(kid);
//        r.putInt(length);
//        return r.array();
//    }
//
//    /**
//     * Kid is first in Kid, Length pair
//     * Length is first in Length, Name pair
//     * @param b
//     * @return
//     */
//    public static int first4BytesToInt(byte[] b){
//        ByteBuffer r = ByteBuffer.allocate(b.length);
//        r.put(b);
//        r.position(0);
//        return r.getInt();
//    }

//    /**
//     * Length is second in Kid, Length pair
//     * @param b
//     * @return
//     */
//    public static int second4BytesToInt(byte[] b){
//        ByteBuffer r = ByteBuffer.allocate(b.length);
//        r.put(b);
//        r.position(4);
//        return r.getInt();
//    }
//
//    private static GetSequenceNameKey eightBytesToPair(byte[] value) {
//        ByteBuffer r = ByteBuffer.allocate(value.length);
//        r.put(value);
//        r.position(0);
//        int kid = r.getInt();
//        int length = r.getInt();
//        return new GetSequenceNameKey(kid, length);
//    }


//    public static String extractString(byte[] b) throws UnsupportedEncodingException {
//        ByteBuffer r = ByteBuffer.allocate(b.length);
//        r.put(b);
//        r.position(4);
//        int length = r.getInt();
//        byte[] result = new byte[length];
//        r.get(result);
//        return new String(result, "UTF-8");
//    }


    public Integer getLength(int kid){
        WholeNumberPairDatabaseKey w = new GetLengthKey(kid);
        Integer result = null;
        byte[] value = null;
        try {
            value = rocks.get(w.toBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        if (value != null)
            result = convertBytesToInt(value);

        return result;
    }

    public Integer getKid(String name) {
        GetKidFromSequenceNameKey k = null;
        try {
            k = GetKidFromSequenceNameKey.Builder(name);
            if (k == null)
                return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        Integer result = null;
        byte[] value = null;
        try {
            value = rocks.get(k.toBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        if (value != null)
            result = convertBytesToInt(value);

        return result;

    }

    public Integer getLength(String name){
        Integer kid = getKid(name);
        if (kid == null) return null;
        return getLength(kid);

    }

    public String getSequenceName(int kid) {
        WholeNumberPairDatabaseKey w = new GetSequenceNameKey(kid);
        String result = null;
        byte[] value = null;
        try {
            value = rocks.get(w.toBytes());
            if (value != null) {
                GetKidFromSequenceNameKey x = new GetKidFromSequenceNameKey(value);
                result = x.getString();
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

    public HashMap<Integer,Character> getExceptions(int kid){
        WholeNumberPairDatabaseKey w = new GetExceptionsArrKey(kid);

        HashMap<Integer,Character> result = null;
        byte[] value = null;
        try {
            value = rocks.get(w.toBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        if (value != null) {
            UnsafeMemory um = new UnsafeMemory(value);
            um.reset();
            int memorySize = um.getInt();
            result = (HashMap<Integer,Character>) um.get(UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        }
        return result;
    }

    private static Integer convertBytesToInt(byte[] value) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put(value);
        bb.position(0);
        return bb.getInt();
    }

    private static byte[] convertIntToBytes(int value) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(value);
        return bb.array();
    }

//
//
//    public String getName(int kid){
//        WholeNumberPairDatabaseKey w = new GetKidFromSequenceNameKey(kid);
//
//        byte[] key = kidTo4Bytes(kid);
//        byte[] value = null;
//        String result = null;
//        try {
//            value = rocks.get(key);
//            if (value != null)
//                result = extractString(value);
//        } catch (RocksDBException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//
//    public Integer getKid(String name){
//        Integer result = null;
//        byte[] value = null;
//        try {
//            byte[] key = stringToBytes(name);
//            value = rocks.get(key);
//            if (value != null)
//                result = second4BytesToInt(value);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (RocksDBException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//
//    public GetSequenceNameKey getKidLengthPair(String name){
//        Integer kid = null;
//        Integer length = null;
//        byte[] value = null;
//        try {
//            byte[] key = stringToBytes(name);
//            value = rocks.get(key);
//
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (RocksDBException e) {
//            e.printStackTrace();
//        }
//
//        if (value != null) {
//            return eightBytesToPair(value);
//        }
//
//        return null;
//    }


    /**
     * This only stores the sequence to RocksDB, not anything else
     *
     * @param longs
     * @param index  kid to write
     */
    private void storeSequence128(long[] longs, int index ) throws RocksDBException {
        if (longs != null) {
            int len = longs.length;

            int chunks = (len - 1) / NUM_LONG_IN_CHUNK + 1;
            int position;
            GetDnaBitStringFragmentKey key;
            long[] writeMe = new long[4];

            int chunk;
            for (chunk = 0; chunk < chunks-1; chunk++) {
                position = chunk * 128;
                key = new GetDnaBitStringFragmentKey(index, position);
                for (int k = 0; k < 4; k++) {
                    writeMe[k] = longs[k + position / 32];
                }
                rocks.put(key.toBytes(), RocksDbKlue.longArrayToBytes(writeMe));
            }

            //last chunk may be short
            position = chunk * 128;
            key = new GetDnaBitStringFragmentKey(index, position);
            for (int k = 0; k < 4; k++) {
                if (k + position / 32 < longs.length){
                    writeMe[k] = longs[k + position / 32];
                } else {
                    writeMe[k] = 0L;
                }
            }

            rocks.put(key.toBytes(), RocksDbKlue.longArrayToBytes(writeMe));
        }
    }


//    public void storeSequence(int index, String seq) {
//        java.util.Date timer = new java.util.Date();
//
//        //Assert that index  = sequenceLength.size()
//        while(sequenceLength.size() < index ){
//            sequenceLength.add(0);
//        }
//        if (sequenceLength.size() == index) {
//            sequenceLength.add(seq.length());
//        } else {
//            sequenceLength.set(index,seq.length());
//        }
//
//        int kid = index;
//        long[] longs;
//
//
//        DnaBitString buddy = null;
//
//        buddy = new DnaBitString(seq);
//        //longs = buddy.compressed.toLongArray();
//
//        //storeSequence128(longs, index);
//        storeSequence(index, buddy);
//
//    }
//
//
//    /**
//     * All store sequence methods call this method
//     *
//     * @param index
//     * @param seq
//     */
//    public void storeSequence(int index, DnaBitString seq) {
//
////        System.err.println("WARNING :: KidDatabaseDisk::storeSequence(int, DnaBitString) not available");
//
//        long[] longs = seq.compressed.toLongArray();
//        storeSequence128(longs, index);
//
//        exceptionsArr.add(seq.exceptions);
//        sequenceLength.add(seq.getLength());
//
//        //erase bits in memory
//        seq.compressed = new MyFixedBitSet();
//
//    }
//
//    @Override
//    protected void addSequenceLength(int length) {
//        sequenceLength.add(length);
//    }
//
//
//    public int getSequenceLength(int index){
//        return sequenceLength.get(index);
//    }
//
//    @Override
    public DnaBitString getSequence(int myKID) {

        ArrayList<ArrayList<Long>> longs = new ArrayList<>();

        Integer len = getLength(myKID);
        if (len == null){
            //undefined length means undefined kid
            return null;
        }


        int chunks = (len - 1) / NUM_BASE_IN_CHUNK + 1;
        int position;
        GetDnaBitStringFragmentKey key;
        long[] writeMe = new long[4];

        int chunk;
        //Lookup all chunks, write to 2D array
        for (chunk = 0; chunk < chunks; chunk++) {
            position = chunk * 128;
            key = new GetDnaBitStringFragmentKey(myKID, position);
            try {
                byte[] b = rocks.get(key.toBytes());
                longs.add(RocksDbKlue.bytesToArrayListLong(b));
            } catch (RocksDBException e) {
                e.printStackTrace();
                return null;
            }
        }

        int num_longs = NUM_LONG_IN_CHUNK*longs.size();
        long[] result = new long[num_longs];
        int z=0;
//        System.err.println("\tLongs\t"+longs);

        //Write all results to output array (i.e. flatten)
        for (int k=0; k<longs.size();k++){
//            System.err.println("\tLongs[k]\t"+longs.get(k));
            if (longs.get(k) != null) {
                for (int j = 0; j < longs.get(k).size(); j++) {
                    result[z] = longs.get(k).get(j);
                    z++;
                }
            }
        }

        return new DnaBitString(result,len, getExceptions(myKID));
    }

    public String getSequence(int myKID, int from, int to, boolean reverse) throws Exception {

        int sequenceLength = getLength(myKID);
        if (from >= sequenceLength){
            //do nothing
            return null;
        }

        //sequence is read right to left!!!!!!
        int fromBit = bitA(from,sequenceLength);
        int toBit = bitB((to-1),sequenceLength); //EXCLUSIVE to INCLUSIVE



        int fromChunk = fromBit / (NUM_BASE_IN_CHUNK*2);  //INCLUSIVE
        int toChunk = toBit / (NUM_BASE_IN_CHUNK*2);   //INCLUSIVE

//        int sz = toChunk - fromChunk + 1;
        int sz = fromChunk - toChunk +1;

        long[] stretch = new long[sz*4];
        int stretchStart = (toChunk) * NUM_BASE_IN_CHUNK;

        for (int k = 0; k < sz; k++ ){

            GetDnaBitStringFragmentKey key = new GetDnaBitStringFragmentKey(myKID, (toChunk+k)*128);
            ArrayList<Long> chunk = RocksDbKlue.bytesToArrayListLong(rocks.get(key.toBytes()));
            for( int z=0; z<4; z++){
                stretch[4*k+z] = chunk.get(z);
            }
        }


        to = Math.min(to, sequenceLength);
        StringBuilder result = new StringBuilder( (to-from) );

        Map<Integer,Character> exceptions = getExceptions(myKID);

        //check stretch is correct
        for (int k = from; k < to; k++) {

            if (exceptions.containsKey(k)) {
                result.append(exceptions.get(k));
            } else {

                if (getBit(stretch,bitA(k,sequenceLength),toChunk)) {
                    if (getBit(stretch,bitB(k,sequenceLength),toChunk)) {
                        result.append('A');
                    } else {
                        result.append('G');
                    }
                } else {
                    if (getBit(stretch, bitB(k,sequenceLength),toChunk)) {
                        result.append('C');
                    } else {
                        result.append('T');
                    }
                }
            }
        }

        return result.toString();
    }

    private boolean getBit(long[] longs, int bit, int toChunk) {
        int index = bit - (toChunk * NUM_BASE_IN_CHUNK)*2;
        int row = index / (NUM_BITS_LONG);
        int col = index % (NUM_BITS_LONG);
        return !( (longs[row] & (1L << col)) == 0);
    }

    /**
     * copied from DnaBitString
     * @param index
     * @return
     */
    protected int bitA(int index, int sequenceLength){
        return (2*sequenceLength -1)-(2*index);
    }

    /**
     * copied from DnaBitString
     * @param index
     * @return
     */
    protected int bitB(int index, int sequenceLength){
        return (2*sequenceLength -1)-(2*index+1);
    }

    /**
     * Reinitialize database.  Useful for switching to read Only mode from write mode when debugging.
     * @param readOnly
     */
    public void restartKlue(boolean readOnly) throws RocksDBException {
        rocks.close();

        Options options = new Options().setCreateIfMissing(true).setMaxOpenFiles(5);

        if (readOnly) rocks = RocksDB.openReadOnly(options, rocksdbfilename);
        else rocks = RocksDB.open(options, rocksdbfilename);

    }

    /**
     * RocksDb prefers to have shutDown() called at end of program, although will automatically do this on shutDown calls
     */
    public void shutDown(){
        rocks.close();
    }


    /**
     *
     * imports Kid, Sequencename, Length, DnaBitString all stored to Disk
     * @param filename
     * @param start
     * @param end
     */
    public void importFnaEverythingButKmers(String filename, int start, int end) {
        //unlimited import
        if (start == 1 && end == 1) end = Integer.MAX_VALUE;
        if (start < 1) start = 1;

        int currentKID = 0;

        SuperString currentSeq = new SuperString();
        String currentName ="";
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        LogStream.stdout.println("");
        LogStream.stdout.printlnTimeStamped("FNA import begins ");

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for (String line; (line = br.readLine()) != null; ) {

                // if blank line, it does not count as new sequence
                if (line.trim().length() == 0) {
                    if (    !ignore && currentKID >= start ) {
                        putData(currentKID, currentName, currentSeq);
                    }
                    ignore = true;
                }
                    // if line starts with ">", then it is start of a new reference sequence
                else if (line.charAt(0) == '>') {
                    if (    !ignore && currentKID >= start ) {
                        putData(currentKID, currentName, currentSeq);
                    }
                    //reset
                    currentKID++;
                    currentName = line.substring(1);
                    currentSeq = new SuperString();

                    ignore = false;
                } else {
                    currentSeq.addAndTrim(line.trim());
                }

                if (currentKID >= end){
                    break;
                }
            } //end for

            //write last
            putData(currentKID, currentName, currentSeq);

            br.close();

        }catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }


    /**
     *
     * imports Kid, Sequencename, Length, DnaBitString all stored to Disk
     * @param filename
     * @param start which kid to start on 1 == first (0 is reserved)
     * @param end
     */
    public void importFnaEverythingButKmers(String filename, int start, int end, KLUE kmers, KLUE startEnd) {
        //unlimited import
        if (start == 1 && end == 1) end = Integer.MAX_VALUE;
        if (start < 1) start = 1;

        int currentKID = 0;

        SuperString currentSeq = new SuperString();
        String currentName ="";
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        LogStream.stdout.println("");
        LogStream.stdout.printlnTimeStamped("FNA import begins ");

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for (String line; (line = br.readLine()) != null; ) {

                // if blank line, it does not count as new sequence
                if (line.trim().length() == 0) {
                    if (    !ignore && currentKID >= start ) {
                        putData(currentKID, currentName, currentSeq, kmers, startEnd);
                    }
                    ignore = true;
                }
                // if line starts with ">", then it is start of a new reference sequence
                else if (line.charAt(0) == '>') {
                    if (    !ignore && currentKID >= start ) {
                        putData(currentKID, currentName, currentSeq, kmers, startEnd);
                    }
                    //reset
                    currentKID++;
                    currentName = line.substring(1);
                    currentSeq = new SuperString();

                    ignore = false;
                } else {
                    currentSeq.addAndTrim(line.trim());
                }

                if (currentKID >= end){
                    break;
                }
            } //end for

            //write last
            putData(currentKID, currentName, currentSeq, kmers, startEnd);

            br.close();

        }catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }


    public void importFnaEverythingButKmers(String fastA) {
        importFnaEverythingButKmers(fastA,1,1);
    }

    public void importFnaEverything(String fastA, KLUE kmers, KLUE startEnd) {
        importFnaEverythingButKmers(fastA,1,1, kmers, startEnd);
    }

}
