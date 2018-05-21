package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileReader;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileWriter;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import static java.lang.Thread.sleep;

/**
 * Created by jwc on 7/13/17.
 *
 *
 * This class mimicks KidDatabaseMemory, but however uses a KidDatabaseMemory to store sequences in 128 letter chunks in RocksDB
 * 128 letters in 256 bits, or 4 longs
 *
 * Note that DnaBitStrings are saved, just with an empty MyFixedBitSet
 *
 * Needs unsafe methods and
 */
public class KidDatabaseDisk extends KidDatabaseMemory {

    /**
     * If KidDatabaseDisk primary file is locked, wait this many times to open
     */
    private static final int MAX_SECOND_TO_WAIT = 1000;

    public static boolean DEBUG = false;

//    public static boolean forceLoadRocksAsReadOnly = true;

    private static final long serialVersionUID_ArrayListHashMap_INT_CHAR = 99012601006L;

    private static final long serialVersionUID = 12601006L;

    private static int NUM_LONG_IN_CHUNK = 4; //128 letters
    private static int NUM_BASE_IN_CHUNK = 128;
    private static int NUM_BITS_LONG = 64;

    /**
     * stores
     */
    String rocksDbKlueFileName;
    RocksDbKlue sequence128db;
    boolean readOnly = false;

    ArrayList<Integer> sequenceLength = new ArrayList<Integer>();


    /**
     * Because DnaBitString not stored in memory, we need to store exceptions in memory here
     * each index is a Kid
     */
    protected ArrayList<HashMap<Integer,Character>> exceptionsArr;


    /**
     * Constructor to open an empty instance
     */
    private KidDatabaseDisk(){
        //for use with loadFromFileUnsafe() builder
    }

    public KidDatabaseDisk(String KidFile, String RocksFile, boolean readonly){
        super();
        this.fileName = KidFile;
        this.readOnly = readonly;
        sequence128db = new RocksDbKlue(RocksFile, readonly);
        sequenceLength.add(0);
        exceptionsArr = new ArrayList<HashMap<Integer,Character>>();
        exceptionsArr.add(new HashMap<Integer,Character>());
        this.rocksDbKlueFileName = RocksFile;
    }

    public static KidDatabaseDisk builderKidDatabase128DBS_ShallowCopy(KidDatabaseMemory kd, String newFileName, String newRocksFileName){

        KidDatabaseDisk result = new KidDatabaseDisk(newFileName, newRocksFileName, false);

        result.nameIndex = kd.nameIndex;
        result.entries = kd.entries;
        result.kingdoms = kd.kingdoms;

        //result.kingdoms = null;  //already set null
        for (int z=1; z<kd.sequences.size();z++){
            result.storeSequence(z,kd.sequences.get(z));
        }
        return result;
    }

//    public void setReadOnly(Boolean b){
//        this.readOnly = b;
//    }

    public void storeSequence(int index, SuperString seq, TimeTotals tt) {
        java.util.Date timer = new java.util.Date();

        //Assert that index  = sequenceLength.size()
        while(sequenceLength.size() < index ){
            sequenceLength.add(0);
        }
        if (sequenceLength.size() == index) {
            sequenceLength.add(seq.length());
        } else {
            sequenceLength.set(index,seq.length());
        }

        int kid = index;
        long[] longs = null;

        if (DEBUG) System.out.println("Constructing DnaBitString for kid\t" + index + "\t" + tt.toHMS());
        DnaBitString buddy = null;
        try {

            buddy = new DnaBitString(seq);
            //longs = buddy.compressed.toLongArray();

        } catch (DataFormatException e) {
            e.printStackTrace();
        }

//        storeSequence128(longs, index);
        storeSequence(index,buddy);
        System.out.println("Import for\t" + index + "\tfinished\t" + tt.toHMS());
    }

    /**
     * This only stores the sequence to RocksDB, not anything else
     *
     * @param longs
     * @param index  kid to write
     */
    private void storeSequence128(long[] longs, int index ){
        if (longs != null) {
            int len = longs.length;

            int chunks = (len - 1) / NUM_LONG_IN_CHUNK + 1;
            int position;
            locationKey key;
            long[] writeMe = new long[4];

            int chunk;
            for (chunk = 0; chunk < chunks-1; chunk++) {
                position = chunk * 128;
                key = new locationKey(index, position);
                for (int k = 0; k < 4; k++) {
                    writeMe[k] = longs[k + position / 32];
                }

                System.err.println(key+"\t"+key.key+"\t"+ Arrays.toString(writeMe));
                sequence128db.put(key.key, RocksDbKlue.longArrayToBytes(writeMe));
            }

            //last chunk may be short
            position = chunk * 128;
            key = new locationKey(index, position);
            for (int k = 0; k < 4; k++) {
                if (k + position / 32 < longs.length){
                    writeMe[k] = longs[k + position / 32];}
                else {
                    writeMe[k] = 0L;
                }
            }

            sequence128db.put(key.key, RocksDbKlue.longArrayToBytes(writeMe));
        }
    }


    public void storeSequence(int index, String seq) {
        java.util.Date timer = new java.util.Date();

        //Assert that index  = sequenceLength.size()
        while(sequenceLength.size() < index ){
            sequenceLength.add(0);
        }
        if (sequenceLength.size() == index) {
            sequenceLength.add(seq.length());
        } else {
            sequenceLength.set(index,seq.length());
        }

        int kid = index;
        long[] longs;


        DnaBitString buddy = null;

        buddy = new DnaBitString(seq);
        //longs = buddy.compressed.toLongArray();

        //storeSequence128(longs, index);
        storeSequence(index, buddy);

    }


    /**
     * All store sequence methods call this method
     *
     * @param index
     * @param seq
     */
    public void storeSequence(int index, DnaBitString seq) {

//        System.err.println("WARNING :: KidDatabaseDisk::storeSequence(int, DnaBitString) not available");

        long[] longs = seq.compressed.toLongArray();
        storeSequence128(longs, index);

        exceptionsArr.add(seq.exceptions);
        sequenceLength.add(seq.getLength());

        //erase bits in memory
        seq.compressed = new MyFixedBitSet();

    }

    public int getSequenceLength(int index){
        return sequenceLength.get(index);
    }

    @Override
    public DnaBitString getSequence(int myKID) {
        if(myKID > last)  return null;
        ArrayList<ArrayList<Long>> longs = new ArrayList<>();

        int len = getSequenceLength(myKID); //number of letters

        int chunks = (len - 1) / NUM_BASE_IN_CHUNK + 1;
        int position;
        locationKey key;
        long[] writeMe = new long[4];

        int chunk;
        for (chunk = 0; chunk < chunks; chunk++) {
            position = chunk * 128;
            key = new locationKey(myKID, position);
            longs.add(sequence128db.get(key.key));
        }

        int num_longs = NUM_LONG_IN_CHUNK*longs.size();
        long[] result = new long[num_longs];
        int z=0;
        for (int k=0; k<longs.size();k++){
            for (int j=0; j < longs.get(k).size(); j++){
                result[z] = longs.get(k).get(j);
                z++;
            }
        }

        DnaBitString result2 = new DnaBitString(result,len);
        result2.exceptions = exceptionsArr.get(myKID);
        return result2;
    }



    @Override
    public String getSequence(int myKID, int from, int to, boolean reverse) throws Exception {

        int sequenceLength = getSequenceLength(myKID);
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

        //TODO
        long[] stretch = new long[sz*4];
        int stretchStart = (toChunk) * NUM_BASE_IN_CHUNK;

        for (int k = 0; k < sz; k++ ){

            locationKey key = new locationKey(myKID, (toChunk+k)*128);
            ArrayList<Long> chunk = sequence128db.get(key.toLong());
            for( int z=0; z<4; z++){
                stretch[4*k+z] = chunk.get(z);
            }
        }


        to = Math.min(to, sequenceLength);
        StringBuilder result = new StringBuilder( (to-from) );

        Map<Integer,Character> exceptions = exceptionsArr.get(myKID);

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







    @Override
    public int getWriteUnsafeSize() {

        //header
        //size bytes and serial
        int total = UnsafeMemory.SIZE_OF_INT +UnsafeMemory.SIZE_OF_LONG;

        //fileName and Last
        total+=UnsafeMemory.getWriteUnsafeSize(fileName, UnsafeMemory.STRING_TYPE);
        total+=UnsafeMemory.SIZE_OF_INT;

        //arrays
        total+=UnsafeMemory.getWriteUnsafeSize(nameIndex,UnsafeMemory.ARRAYLIST_STRING_TYPE);
        total+=UnsafeMemory.getWriteUnsafeSize(entries,UnsafeMemory.ARRAYLIST_KID_TYPE);
        total+=UnsafeMemory.getWriteUnsafeSize(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);

        //No longer stored in memory
        //total+=UnsafeMemory.getWriteUnsafeSize(sequences,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);


        //rocksDbKlueFileName
        total += UnsafeMemory.getWriteUnsafeSize(rocksDbKlueFileName, UnsafeMemory.STRING_TYPE);

        //boolean readOnly = false;
        total += UnsafeMemory.SIZE_OF_BOOLEAN;

        //ArrayList<Integer> sequenceLength = new ArrayList<Integer>();
        total += UnsafeMemory.getWriteUnsafeSize(sequenceLength, UnsafeMemory.ARRAYLIST_INT_TYPE);

        //ArrayList<HashMap<Integer,Character>> exceptionsArr;
        total += UnsafeMemory.SIZE_OF_INT           // byte header
                + UnsafeMemory.SIZE_OF_LONG         // SerialUID
                + UnsafeMemory.SIZE_OF_INT;          // number of entries

        for (int z=0; z < exceptionsArr.size(); z++){
            total += UnsafeMemory.getWriteUnsafeSize(exceptionsArr.get(z), UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        }


        return total;
    }


    public void writeUnsafe(UnsafeMemory um) {

        //size of read block
        int mySize = getWriteUnsafeSize();
        um.putInt(mySize);
        //serial
        um.putLong(serialVersionUID);

        //class member
        um.putString(fileName);


        //rocksDbKlueFileName
        um.putString(rocksDbKlueFileName);
        //boolean readOnly
        um.putBoolean(readOnly);

        um.putInt(last);

        //arrays
        um.put(nameIndex, UnsafeMemory.ARRAYLIST_STRING_TYPE);
        um.put(entries, UnsafeMemory.ARRAYLIST_KID_TYPE);
        um.put(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);


        //No longer stored in memory
        //um.put(sequences,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);



        //ArrayList<Integer> sequenceLength = new ArrayList<Integer>();
        um.put(sequenceLength, UnsafeMemory.ARRAYLIST_INT_TYPE);
        //ArrayList<HashMap<Integer,Character>> exceptionsArr;

        int total = 0;
        total += UnsafeMemory.SIZE_OF_INT           // byte header
                + UnsafeMemory.SIZE_OF_LONG         // SerialUID
                + UnsafeMemory.SIZE_OF_INT;          // number of entries

        for (int z=0; z < exceptionsArr.size(); z++){
            total += UnsafeMemory.getWriteUnsafeSize(exceptionsArr.get(z), UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        }

        um.putInt(total);
        um.putLong(serialVersionUID_ArrayListHashMap_INT_CHAR);
        um.putInt(exceptionsArr.size());
        for (int z=0; z < exceptionsArr.size(); z++){
            um.put(exceptionsArr.get(z), UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        }

    }



    public void readUnsafe(UnsafeMemory um) throws ClassCastException {
        long serial = um.getLong();
        if (serial != serialVersionUID){
            System.err.println("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
            throw new ClassCastException("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
        }
        fileName = um.getString();

        //rocksDbKlueFileName
        rocksDbKlueFileName = um.getString();
        //boolean readOnly = false;
        readOnly = um.getBoolean();


        readUnsafeAfterHeader(um);

    }


    public void readUnsafe2(UnsafeMemory um, String RocksDB) throws ClassCastException {
        long serial = um.getLong();
        if (serial != serialVersionUID){
            System.err.println("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
            throw new ClassCastException("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
        }
        fileName = um.getString();

        //rocksDbKlueFileName
        rocksDbKlueFileName = um.getString();
        rocksDbKlueFileName = RocksDB;
        //boolean readOnly = false;
        readOnly = um.getBoolean();
        readOnly = true;


        readUnsafeAfterHeader(um);

    }

    /**
     *
     * @param um
     * @throws ClassCastException
     */
    private void readUnsafeAfterHeader(UnsafeMemory um) throws ClassCastException {
        last = um.getInt();

        um.getInt(); //peel byte header
        nameIndex = (ArrayList<String>) um.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);
        um.getInt(); //peel byte header
        entries= (ArrayList<Kid>) um.get( UnsafeMemory.ARRAYLIST_KID_TYPE);
        um.getInt(); //peel byte header
        kingdoms= (HashMap<Integer,String>) um.get(UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);

        //sequences= (ArrayList<DnaBitString>) um.get(UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);


        //if (forceLoadRocksAsReadOnly) readOnly = true;
        sequence128db = new RocksDbKlue(rocksDbKlueFileName, readOnly);

        um.getInt(); //peel byte header
        sequenceLength = (ArrayList<Integer>) um.get(UnsafeMemory.ARRAYLIST_INT_TYPE);

        //ArrayList<HashMap<Integer,Character>> exceptionsArr;

        int byteSize = um.getInt();
        long serial = um.getLong();
        if (serial != serialVersionUID_ArrayListHashMap_INT_CHAR){
            System.err.println("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
            throw new ClassCastException("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
        }

        int arrSize = um.getInt();
        exceptionsArr = new ArrayList<>();
        for (int z=0; z < arrSize; z++){
            um.getInt(); //peel byte header
            exceptionsArr.add(   (HashMap<Integer,Character>) um.get(UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE)  );
        }

    }



    /**
     * Reinitialize database.  Useful for switching to read Only mode from write mode when debugging.
     * @param readOnly
     */
    public void restartKlue(boolean readOnly){
        sequence128db.shutDown();
        this.readOnly = readOnly;
        sequence128db = new RocksDbKlue(this.rocksDbKlueFileName, this.readOnly);
    }

    /**
     * RocksDb prefers to have shutDown() called at end of program, although will automatically do this on shutDown calls
     */
    public void shutDown(){
        sequence128db.shutDown();
    }


    public static KidDatabaseDisk loadFromFileUnsafe(String filename, String RocksDB) {
        byte[] buffer = null;
        try {
            UnsafeFileReader ufr = UnsafeFileReader.unsafeFileReaderBuilder(filename);
            buffer = ufr.readNextObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UnsafeMemory um = new UnsafeMemory(buffer);

        KidDatabaseDisk result;

        result = new KidDatabaseDisk();

        result.readUnsafe2(um, RocksDB);

        return result;
    }


    public static KidDatabaseDisk loadFromFileUnsafe(String filename) {

        byte[] buffer = null;
        try {
            UnsafeFileReader ufr = UnsafeFileReader.unsafeFileReaderBuilder(filename);
            buffer = ufr.readNextObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UnsafeMemory um = new UnsafeMemory(buffer);

        KidDatabaseDisk result;

        result = new KidDatabaseDisk();

        result.readUnsafe(um);

        return result;
    }


    public static KidDatabaseDisk loadFromFileUnsafeWaitOnFile(String filename) {

        byte[] buffer = null;
        UnsafeFileReader ufr = null;
        int k = 0;
        //try to open file if file locked
        while(ufr != null && k < MAX_SECOND_TO_WAIT) {
            try {
                ufr = UnsafeFileReader.unsafeFileReaderBuilder(filename);
                buffer = ufr.readNextObject();
            } catch (FileNotFoundException e) {
                try {
                    sleep(1000);
                    k++;
                } catch (InterruptedException e1) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        UnsafeMemory um = new UnsafeMemory(buffer);

        KidDatabaseDisk result;

        result = new KidDatabaseDisk();

        result.readUnsafe(um);

        return result;
    }



    public void setReadOnly(boolean ro){
        readOnly = ro;
    }

    @Override
    public void saveToFileUnsafe() {
        UnsafeFileWriter ufw = null;
        try {
            ufw = UnsafeFileWriter.unsafeFileWriterBuilder(fileName);
            UnsafeMemory um = new UnsafeMemory(getWriteUnsafeSize());
            this.writeUnsafe(um);
            ufw.writeObject(um.toBytes());
            ufw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public class locationKey{
        int kid;

        /**
         * location is position in the reference sequence
         * or coordinate 0 to X, where X is length of reference sequence
         */
        int location;

        long key;

//        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
//            bb.put(bytes);
//            bb.position(0);
//            for (int k = 0; k < (bytes.length/8); k++) {
//            result.addAndTrim(bb.getLong());
//        }


        public locationKey(int kid, int position){
            this.kid = kid;
            location = position;
            key = toLong();
        }

        private long toLong() {
            long x = (long) kid;
            x = x << 32;
            return (x += location);
        }

        public locationKey(long number){
            key = number;
            long shifter = number >> 32;
            kid = (int) shifter;
            shifter = shifter << 32;
            number -= shifter;
            location = (int) number;
        }

        public int getKid(){
            return kid;
        }

        public int getLocation(){
            return location;
        }
        public String toString(){
            return "kid\t"+kid+"\tposition\t"+location;
        }
    }


    public static void main(String[] args){
        KidDatabaseDisk kd = new KidDatabaseDisk("KidFile.deleteme", "RocksDb.deleteme",false);

        DnaBitString.SUPPRESS_WARNINGS = true;

        kd.add(new Kid("testDBS"));
        kd.add(new Kid("testString"));
//        kd.addAndTrim(new Kid("testLongs"));

        SuperString sequence = new SuperString();

        for (int k=0; k < 2*128/8+1; k++) {
            sequence.addAndTrim("ATCGATCX");
        }

        kd.storeSequence(1, new DnaBitString(sequence.toString()));
        kd.storeSequence(2,sequence.toString());
//        kd.storeSequence128(new DnaBitString(sequence.toString()).toLongArray(),3);

//        System.out.println("Expect False on testLongs exceptions not stored");
        try {
            for (int k=1; k <= 2; k++){
                System.out.println(kd.getName(k)+"\t"+sequence.toString().equals(kd.getSequence(k).toString()));
                System.out.println(kd.getName(k)+":16:95\t"+sequence.get(19,65).equals(kd.getSequence(k,19,65,false)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //restart as read only, so second Kid Database can be started for comparison
        kd.restartKlue(true);
        kd.saveToFileUnsafe();


        UnsafeMemory um = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(kd.nameIndex,UnsafeMemory.ARRAYLIST_STRING_TYPE));
        um.put(kd.nameIndex, UnsafeMemory.ARRAYLIST_STRING_TYPE);
        um.reset(4);
        ArrayList<String> nameIndex = (ArrayList<String>) um.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);
        for (int z=0; z<nameIndex.size();z++){
            System.out.println( "nameIndex\t"+z+"\t"+kd.nameIndex.get(z).equals(nameIndex.get(z)));
        }


        System.out.println("Testing from loaded database");
        KidDatabaseDisk kd2 = KidDatabaseDisk.loadFromFileUnsafe(kd.fileName);
        try {
            for (int k=1; k <= 2; k++){
                System.out.println(kd2.getName(k)+"\t"+sequence.toString().equals(kd2.getSequence(k).toString()));
                System.out.println(kd2.getName(k)+":16:95\t"+sequence.get(19,65).equals(kd2.getSequence(k,19,65,false)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int z=0; z<nameIndex.size();z++){
            System.out.println( "nameIndex\t"+z+"\t"+kd.nameIndex.get(z).equals(kd2.nameIndex.get(z)));
            System.out.println( "exceptionsArr\t"+z+"\t"+kd.exceptionsArr.get(z).equals(kd.exceptionsArr.get(z)));
        }

    }
}
