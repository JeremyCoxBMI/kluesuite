package org.cchmc.kluesuite.klue;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileReader;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileWriter;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.klat.KmerSequence;
import org.cchmc.kluesuite.klue.kiddatabase.GetDnaBitStringFragmentKey;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;
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
 *
 * TODO dumpt to text does not include exceptions array
 */
public class KidDatabaseDisk extends KidDatabaseMemory {

    /**
     * If KidDatabaseDisk primary file is locked, wait this many times to open
     */
    private static final int MAX_SECOND_TO_WAIT = 1000;

    //Variable to set true if using an abridged import method
    public static boolean importSpecial = false;
    public static int offset = 0;

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
        super();
        sequenceLength.add(0);
        exceptionsArr = new ArrayList<HashMap<Integer,Character>>();
        exceptionsArr.add(new HashMap<Integer,Character>());
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
            sequenceLength.add(0);
        }

        if ( sequenceLength.size() != exceptionsArr.size())
           System.err.println("DEBUG HERE");

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
            longs = buddy.compressed.toLongArray();

        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        if (buddy == null) {
            exceptionsArr.add(new HashMap<>());
        }else {
            exceptionsArr.add(buddy.exceptions);
        }
        storeSequence128(longs, index);
//        storeSequence(index,buddy);


        int PERIOD_ANNOUNCE = 100 * 1000;
        if (!KidDatabaseMemory.squelch) {
            System.out.println("Import for\t" + index + "\tfinished\t" + tt.toHMS());
        }
        if (index %  PERIOD_ANNOUNCE == 2){
            System.err.println("\tFinished processing kid\t"+this.last+"\t"+tt.toHMS());
        }
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
            GetDnaBitStringFragmentKey key;
            long[] writeMe = new long[4];

            int chunk;
            for (chunk = 0; chunk < chunks-1; chunk++) {
                position = chunk * 128;
                key = new GetDnaBitStringFragmentKey(index, position);
                for (int k = 0; k < 4; k++) {
                    writeMe[k] = longs[k + position / 32];
                }

//                System.err.println(key+"\t"+key.key+"\t"+ Arrays.toString(writeMe));
                sequence128db.put(key.toBytes(), RocksDbKlue.longArrayToBytes(writeMe));
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

            sequence128db.put(key.toBytes(), RocksDbKlue.longArrayToBytes(writeMe));
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

    @Override
    protected void addSequenceLength(int length) {
            sequenceLength.add(length);
    }


    public int getSequenceLength(int index){
        return sequenceLength.get(index);
    }

    @Override
    public DnaBitString getSequence(int myKID) {
        if(myKID > last && !importSpecial)  return null;
        ArrayList<ArrayList<Long>> longs = new ArrayList<>();

        int len;
        if (importSpecial)
            len = getSequenceLength(myKID - offset);
        else
            len = getSequenceLength(myKID); //number of letters
        //System.err.println("\tLength\t"+len);

        if (len == 0) return null;

//        if (myKID == 37 || myKID == 38) {
//            String debug = "HERE";
//        }

        int chunks = (len - 1) / NUM_BASE_IN_CHUNK + 1;
        int position;
        GetDnaBitStringFragmentKey key;
        long[] writeMe = new long[4];

        int chunk;
        //Lookup all chunks, write to 2D array
        for (chunk = 0; chunk < chunks; chunk++) {
            position = chunk * 128;
            key = new GetDnaBitStringFragmentKey(myKID, position);
            longs.add(sequence128db.get(key.toBytes()));
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

        DnaBitString result2 = new DnaBitString(result,len);
        if (importSpecial)
            result2.exceptions = exceptionsArr.get(myKID-offset);
        else
            result2.exceptions = exceptionsArr.get(myKID);
        return result2;
    }


    public KmerSequence getKmerSequence(int myKID, int from, int to, boolean reverse) throws Exception {
        //TODO OPTIMIZATION  ISSUE #90  implement to make efficiency; do not need to convert to String
        //TODO in release, add to template
        return null;
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

        long[] stretch = new long[sz*4];
        int stretchStart = (toChunk) * NUM_BASE_IN_CHUNK;

        for (int k = 0; k < sz; k++ ){

            GetDnaBitStringFragmentKey key = new GetDnaBitStringFragmentKey(myKID, (toChunk+k)*128);
            ArrayList<Long> chunk = sequence128db.get(key.toBytes());
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
        int debug =UnsafeMemory.getWriteUnsafeSize(nameIndex,UnsafeMemory.ARRAYLIST_STRING_TYPE);
        total+=debug;
        debug+=UnsafeMemory.getWriteUnsafeSize(entries,UnsafeMemory.ARRAYLIST_KID_TYPE);
        total+=debug;
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



    public static KidDatabaseDisk loadFromFnaFile(String filename, int numExpected, String RocksDB) {
        KidDatabaseDisk result;

        result = new KidDatabaseDisk();
        KidDatabaseDisk.PERIOD = 1000 * 1000;
        try {
            result.importFnaNoSequencesStored(filename, numExpected);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        result.exceptionsArr = new ArrayList<HashMap<Integer,Character>>();
        for (int k=0; k< result.nameIndex.size(); k++) result.exceptionsArr.add(new HashMap<Integer,Character>());
        result.sequence128db = new RocksDbKlue(RocksDB,true);

        return result;
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

    /**
     * only imports sequences in correct KID range based on FNA file
     * @param filename
     * @param start
     * @param end
     */
    public void importFnaBitSequences(String filename, int start, int end) {

            int currentKID = -1;
            SuperString currentSeq = new SuperString();
            //String currentName = "";
            boolean ignore = true; //do not write empty sequence to database

            TimeTotals tt = new TimeTotals();
            tt.start();

            System.out.println("\nFNA import begins " + tt.toHMS() + "\n");
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

                for (String line; (line = br.readLine()) != null; ) {

                    if (debug) {
                        System.err.println("Single line:: " + line);
                    }

                    // if blank line, it does not count as new sequence
                    if (line.trim().length() == 0) {
                        if (debug) System.err.println("           :: blank line detected  ");

                        if (!ignore)
                            if(currentKID >= start && currentSeq.length() > 0) {
                                storeSequence(currentKID, currentSeq, tt);
                            } else {
                                sequenceLength.add(currentSeq.length());
                                exceptionsArr.add(new HashMap<>());
                            }

                        else if (!ignore) {
                            sequenceLength.add(currentSeq.length());
                            exceptionsArr.add(new HashMap<>());
                        }
                        ignore = true;

                        // if line starts with ">", then it is start of a new reference sequence
                    } else if (line.charAt(0) == '>') {
                        if (debug) System.err.println("           :: new entry detected  " + line);

                        // save previous iteration to database
                        if (!ignore && currentSeq.length() > 0) {
                            storeSequence(currentKID, currentSeq, tt);
                        } else if (!ignore) {
                            sequenceLength.add(currentSeq.length());
                            exceptionsArr.add(new HashMap<>());
                        }
                        // initialize next iteration
                        if (indexOf(line.trim()) == -1) {
                            //original.addAndTrim(new Kid(line.trim()));
                            //addNewKidEntry(line);
                            add(new Kid(line.trim()));
                            if (getLast()==start || (getLast()== 1 && start == 1)){
                                System.err.println("Found KID\t" + currentKID + "\tbit string import started");
                            }
                        }

                        currentKID = getKid(line.trim()); // original.indexOf(line.trim());
                        if (currentKID == -1) {
                            System.err.println("This sequence not found in database : " + line);
                            listEntries(0);
                            exit(0);
                        }
                        //currentSeq = "";

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
                if (!ignore && currentKID >= start && currentSeq.length() > 0) {
                    storeSequence(currentKID, currentSeq, tt);
                }
//                else if (!ignore) {
//                    sequenceLength.add(currentSeq.length());
//                    exceptionsArr.add(new HashMap<>());
//                }
                br.close();

            }catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

    }

    public void importKidDBText(String s) {

        try(BufferedReader br = new BufferedReader(new FileReader(s))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] pieces = line.split("\t");
                add(new Kid(pieces[0]));
                sequenceLength.add(Integer.parseInt(pieces[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;



    }

    public HashMap<Integer, Character> getExceptions(int z) {
        if (z-offset <= last)
            return exceptionsArr.get(z-offset);
        else
            return null;
    }

    public void putExceptions(int z, HashMap<Integer, Character> tempy) {
        exceptionsArr.add(z, tempy);
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


    public void startNewPiecemail(int piece){
        sequence128db.shutDown();
        sequence128db = new RocksDbKlue(rocksDbKlueFileName+"."+piece,false);
    }


    public void processPiecesmailRange(int begin, int end, String filename) {


        int currentKID = -1;
        SuperString currentSeq = new SuperString();
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        //skipping is holdover from copying code.  Here, it does nothing.
        boolean skipping = false;
//        boolean debug = false;

        TimeTotals tt = new TimeTotals();
        tt.start();

        System.out.println("\nFNA import begins " + tt.toHMS() + "\n");
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for (String line; (line = br.readLine()) != null; ) {

                if (debug) {
                    System.err.println("Single line:: " + line);
                }

                // if blank line, it does not count as new sequence
                if (line.trim().length() == 0) {
                    if (debug) {
                        System.err.println("           :: blank line detected  ");
                    }
                    if (!skipping) {
                        if (!ignore) {
                            if (begin <= currentKID && currentKID < end) {
                                storeSequence(currentKID, currentSeq, tt);
                            }
                        }
                    }
                    ignore = true;

                    // if line starts with ">", then it is start of a new reference sequence
                } else if (line.charAt(0) == '>') {
                    if (debug) {
                        System.err.println("           :: new entry detected  " + line);
                    }
                    // save previous iteration to database

                    if (!skipping) {
                        if (!ignore) {
                            if (begin <= currentKID && currentKID < end) {
                                storeSequence(currentKID, currentSeq, tt);
                            }
                        }

                        // initialize next iteration

                        //ADD skipped since database exists
//                        if (indexOf(line.trim()) == -1) {
//                            //original.addAndTrim(new Kid(line.trim()));
//                            //addNewKidEntry(line);
//                            add(new Kid(line.trim()));
//                        }

                        currentKID = getKid(line.trim()); // original.indexOf(line.trim());
                        if (currentKID == -1) {
                            System.err.println("This sequence not found in database : " + line);
                            listEntries(0);
                            exit(0);
                        }
                        //currentSeq = "";

                        currentSeq = new SuperString();

                        ignore = false;
                    }
                } else {
                    if (!skipping) {
                        //currentSeq += line.trim();
                        currentSeq.addAndTrim(line.trim());
                    }
                }

            } //end for

            br.close();

            if (!ignore) {
                if (begin <= currentKID && currentKID < end) {
                    storeSequence(currentKID, currentSeq, tt);
                }
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static KidDatabaseDisk loadFileFromText(String filename) {
        KidDatabaseDisk result = new KidDatabaseDisk();
        result.fileName = filename;
        filename += filename+".txt";
        try(BufferedReader br = new BufferedReader(new FileReader(filename+".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] pieces = line.split("\t");
                result.add(new Kid(pieces[0]));
                result.sequenceLength.add(Integer.parseInt(pieces[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.sequence128db = new RocksDbKlue(filename+".disk", false);
        return result;
    }


    public static KidDatabaseDisk loadFileFromText(String filename, int chunk) {
//        String kidDbName = args[0]+".kidDB."+String.format("%02d", chunk);
//        String rocks128Name = args[0]+".kidDB.disk."+String.format("%02d", chunk);
//        String rocksName = args[0]+".kmer."+String.format("%02d", chunk);;
//        String startEndName = args[0]+".startEnd."+String.format("%02d", chunk);

        KidDatabaseDisk result = new KidDatabaseDisk();
        result.fileName = filename;
        try(BufferedReader br = new BufferedReader(new FileReader(filename+".txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] pieces = line.split("\t");
                result.add(new Kid(pieces[0]));
                result.sequenceLength.add(Integer.parseInt(pieces[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.sequence128db = new RocksDbKlue(filename+".disk."+String.format("%02d", chunk), false);
        return result;
    }



    /**
     * imports sequences in correct KID range based on FNA file; presumes KID database text import
     * ExceptionsArr and SequenceLength only store data for the target sequences.
     * Offset = (start -1), so when read, need to add +start-1 to KID
     *
     * Returns number of sequences imported (may be less than end)
     *
     * @param filename
     * @param start
     * @param end
     */
    public int importOnlyFnaBitSequences(String filename, int start, int end) {
        int currentKID = 0;
        int result = 0;
//        String currName = null;
        boolean store = false;
        SuperString seq = new SuperString();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for (String line; (line = br.readLine()) != null; ) {
                if (line.charAt(0) == '>'){
                    if (currentKID >= start) {
                        if (seq.length() > 0) {
//                            System.err.print("KID\t"+currentKID+"\thas length\t"+seq.length());
                            storeOnlyBitSequence(currentKID, seq);
                        } else {
                            sequenceLength.add(0);
                            exceptionsArr.add(new HashMap<Integer,Character>());
                        }
                        result++;
                        seq = new SuperString();
//                        currName = line.trim().substring(1);
                        store = true;
                    }
                    currentKID++;
                } else if (store) {
                    seq.addAndTrim(line);
                }
                if (currentKID >= end) break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void storeOnlyBitSequence(int index, SuperString seq) {
        int kid = index;
        long[] longs = null;

        DnaBitString buddy = null;
        try {
            buddy = new DnaBitString(seq);
            longs = buddy.compressed.toLongArray();

        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        if (buddy == null) {
            exceptionsArr.add(new HashMap<>());
            sequenceLength.add(0);
        }else {
            sequenceLength.add(buddy.getLength());
            exceptionsArr.add(buddy.exceptions);
        }
        storeSequence128(longs, index);

        int PERIOD_ANNOUNCE = 100 * 1000;
        if (!KidDatabaseMemory.squelch) {
            System.out.println("Import for\t" + index + "\tfinished\t");
        }
        if (index %  PERIOD_ANNOUNCE == 2){
            System.err.println("\tFinished processing kid\t"+index+"\t");
        }

    }


}
