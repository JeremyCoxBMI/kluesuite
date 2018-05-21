package org.cchmc.kluesuite.klue;


import org.cchmc.kluesuite.klue.kiddatabase.KidDatabase;
import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileReader;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileWriter;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeSerializable;


import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;
import static org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory.*;

/**
 * This class tracks all Kid and their related information.
 * KID is a Klue ID index, for tracking sequence identification.
 *
 * KID <= 0 are illegal
 * 1 <= KID <= 3 are reserved for testing purposes
 *
 * 2016-08-15   v2.0    Imported from v1.6.  No major changes, but class names have changed.
 *                      Added .tsv file import for information regarding sequences.
 *
 */


public class KidDatabaseMemory implements KidDatabase, UnsafeSerializable {

    private static final long serialVersionUID = 2601006L;



    /**
     * How often status messages are displayed (per database update on some functions)
     */
    public static int PERIOD = 10000;

    /**
     * This tracks the KID generation.  This will roll over into negative numbers as it increments, allowing full 32-bit addressing.
     * Note that KID == 0  is SENTINEL value.
     * However, ArrayList cannot have a size greater than Integer.MAX_VALUE.
     * http://programmers.stackexchange.com/questions/190954/what-is-the-maximum-value-of-index-of-an-arraylist
     */

    public String fileName;
    public static int MAX_KID = Position.MAX_KID;

    /**
     * INDEX of the last entry in the database  (note database is 0-indexed, by 0 is illegal)
     */
    protected int last;

    //Deprecated
    //protected int count;



    protected ArrayList<Kid> entries;
    protected ArrayList<String> nameIndex;
    protected ArrayList<DnaBitString> sequences;

    static public boolean debug = false;

    /**
     * Currently not used/implemented
     */
    protected HashMap<Integer, String> kingdoms;
    public static boolean squelch = false;

    public KidDatabaseMemory() {
        last = 0;    //KID = 0 is forbidden // counting starts at 1
        entries = new ArrayList<Kid>();
        nameIndex = new ArrayList<String>();
        sequences = new ArrayList<DnaBitString>();

        /**
         * all Kingdoms used in database as a set, converts to String names
         * frequently to use:  kingdoms.keySet().contains()
         */
        kingdoms = new HashMap<Integer, String>();

        entries.add(new Kid());        //entry zero will be reserved with SENTINEL entry.  It is invalid.
        nameIndex.add(entries.get(0).sequenceName);
        sequences.add(new DnaBitString(""));

        fileName = Settings_OLD.KidDbLocation;
    }


    /**
     * default constructor, prebuild memory containers to fit a size for efficiency
     */
    public KidDatabaseMemory(int size) {
        entries = new ArrayList<Kid>();
        nameIndex = new ArrayList<String>();
        sequences = new ArrayList<DnaBitString>();
    }

    /**
     * New constructor -- 2017-04-14 Import from Disk based store
     * <p>
     * Destroys rkd, then closes it
     */
//    public KidDatabaseMemory(RocksKidDatabase rkd) {
//        this.fileName = rkd.fileName + ".KidDatabaseMemory";
//        this.last = rkd.last;
//        //this.count = rkd.count;
//
//        //not making copies -- destructive constructor
//        this.entries = rkd.entries;
//        this.nameIndex = rkd.nameIndex;
//        this.kingdoms = rkd.kingdoms;
//
//
//        this.sequences = new ArrayList<DnaBitString>();
//        for (int k = 0; k <= last; k++) {
//            DnaBitString dns = rkd.getSequence(k);
//            if (dns != null) {
//                this.sequences.addAndTrim(dns);
//            } else {
//                System.err.println("WARNING: \tKidDatabaseMemory(RocksKidDatabase) constructor reached unexpected KID with null DnaBitString : KID + " + k);
//            }
//        }
//
//        rkd.shutdown();
//    }


    /**
     * Increase size of store by adding Kid mentioned.
     * note that addAndTrim( new Kid(String sequenceName) ) only initializes the sequence name.
     * Other values in Kid must still be imported / parsed.
     * Does not check if the entry already exists
     *
     * @param value
     */
    public void add(Kid value) {
        if (!squelch) System.err.println("Adding sequenceName : " + value.sequenceName);
        entries.add(value);
        nameIndex.add(value.sequenceName);
        sequences.add(null);
        last++;
    }


    /**
     * Increase size of store by adding Kid mentioned.
     * note that addAndTrim( new Kid(String sequenceName) ) only initializes the sequence name.
     * Other values in Kid must still be imported / parsed.
     * Does not check if the entry already exists
     *
     * @param value
     */
    public void addUsingArray(Kid value, Kid[] kids, String[] names) {

        if (!squelch) System.err.println("Adding sequenceName : " + value.sequenceName);
        last++;
        kids[last] = value;
        names[last] = value.sequenceName;
    }


    /**
     * next KID that will be added when addAndTrim() is called.
     *
     * @return
     */
    public int getNextKid() {
        return last + 1;
    }

    /**
     * The highest KID in the system
     *
     * @return
     */
    public int getMaxKid() {
        return last;
    }


    @Override
    public Integer getLength(int kid) {
        return sequences.get(kid).getLength();
    }

    public Integer getKid(String name) {
        if (name.charAt(0) == '>') {
            return nameIndex.indexOf(name.substring(1));
        } else {
            return nameIndex.indexOf(name);
        }
    }

    @Override
    public Integer getLength(String name) {
        Integer x = getKid(name);
        if (x != null)
            return sequences.get(x).getLength();
        else
            return null;
    }

    @Override
    public String getSequenceName(int kid) {
        return nameIndex.get(kid);
    }

    @Override
    public HashMap<Integer, Character> getExceptions(int kid) {
        return sequences.get(kid).exceptions;
    }

    /**
     * Returns number of entries.  Note that this includes 1,2, and 3, which are testing numbers.
     *
     * @return
     */
    public int size() {
        return entries.size() - 1;    //entry at zero does not count
    }

    /**
     * Returns true if the sequence has been stored in memory.
     *
     * @param seqName
     * @return
     */
    public boolean contains(String seqName) {
        return nameIndex.contains(seqName);
    }

    /**
     * find the index corresponding to the sequence name
     *
     * @param seqName
     * @return
     */
    public int indexOf(String seqName) {
        if (seqName.charAt(0) == '>'){
            return nameIndex.indexOf(seqName.substring(1));
        }
        return nameIndex.indexOf(seqName);
    }

    /**
     * get name of sequence stored at index
     *
     * @param index
     * @return
     */
    public String getName(int index) {
        if (index < 1 || index >= nameIndex.size())
            return "";
        else
            return nameIndex.get(index);
    }

    /**
     * Store DNA sequence to memory construct.
     * Allows overloading, if will be backed by Store of some kind.
     *
     * @param index
     * @param seq
     */
    public void storeSequence(int index, String seq) {
        sequences.set(index, new DnaBitString(seq));
    }

    public void storeSequence(int index, DnaBitString seq) {

        for (int k=sequences.size(); k <= index; k++)   sequences.add(new DnaBitString(""));

        sequences.set(index, seq);
    }

    public int getSequenceLength(int index) {
        if (0 <= index && index <= last && sequences.get(index) != null)
            return sequences.get(index).getLength();
        else
            return -1;  //1 << 30;
    }

//    public void saveToFile() {
//        try {
//
//            FileOutputStream fout = new FileOutputStream(fileName);
//            ObjectOutputStream oos = new ObjectOutputStream(fout);
//            oos.writeObject(this);
//            oos.close();
//            fout.close();
//            System.out.println("\n\t Save to File Complete");
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public static KidDatabaseMemory loadFromFile(String filename) {
//        KidDatabaseMemory result;
//        int DEBUG16 = 12;
//        try {
//            FileInputStream fin = new FileInputStream(filename);
//            ObjectInputStream ois = new ObjectInputStream(fin);
//            result = (KidDatabaseMemory) ois.readObject();
//            ois.close();
//            System.out.println("\n\t KidDatabaseMemory :: Load from file Complete");
//            return result;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }


    /**
     * Saves database to Kryo file.
     * Saves database to Kryo file.
     *
     * @param filename The filename to use.  If null, uses existing.
     * @throws FileNotFoundException
     */
//    public void saveToFileKryo(String filename) throws FileNotFoundException {
//
////        Integer nextOffset;
////        Set<Integer> keys;
////
////        Kryo kryo = new Kryo();
////        Output output = new Output(new FileOutputStream(fileName));
////
////        kryo.writeObject(output, fileName);
////
////        //Programmer's note: Kryo is known to have trouble with large data objects.  Here we break it up to not crash.
////        // (Search Kryo NegatizeZero array)
////
////        //indelMap
////        keys = indelMap.keySet();
////        nextOffset = keys.size();
////        kryo.writeObject(output, nextOffset);
////        for (Integer k : keys){
////            kryo.writeObject(output, k);
////            kryo.writeObject(output, indelMap.get(k));
////        }
////
////        //snpMap
////        keys = snpMap.keySet();
////        nextOffset = keys.size();
////        kryo.writeObject(output, nextOffset);
////        for (Integer k : keys){
////            kryo.writeObject(output, k);
////            kryo.writeObject(output, snpMap.get(k));
////        }
////
////        output.close();
//
//
//
//
//        if (filename != null)
//            this.fileName = filename;
//
//        Kryo kryo = new Kryo();
//        Output output = new Output(new FileOutputStream(fileName));
//
//
////        kryo.writeClassAndObject(output, fileName);
////        Integer nextOffset = last;
////        kryo.writeClassAndObject(output, nextOffset);
////        //kryo.writeObject(output, count);
////        Integer size = sequences.size();
////        kryo.writeClassAndObject(output, size);
////        kryo.writeClassAndObject(output, kingdoms);
////
////        kryo.writeClassAndObject(output, sequences);
//
//
//        kryo.writeObject(output, fileName);
//        Integer nextOffset = last;
//        kryo.writeObject(output, nextOffset);
//
//        kryo.writeObject(output, entries);
//        kryo.writeObject(output, nameIndex);
//
////        kryo.register(DnaBitString.class, new DnaBitStringKryoSerializer());
//        kryo.writeObject(output, kingdoms);
//
//        Integer size = sequences.size();
//        kryo.writeObject(output, size);
////        kryo.writeObject(output, sequences);
//        kryo.register(DnaBitString.class);
//        for (int k=0; k<size; k++) {
//            DnaBitString dns = sequences.get(k);
//            kryo.writeObject(output, dns);
//        }
//
//        output.close();
//
//    }


//    /**
//     * THIS FUNCTION CURRENTLY DOES NOT WORK
//     * <p>
//     * Load from File using better Kryo library
//     *
//     * @param filename
//     * @return
//     * @throws FileNotFoundException
//     */
//    public static KidDatabaseMemory loadFromFileKryo(String filename) throws FileNotFoundException {
//
//
//        KidDatabaseMemory result = new KidDatabaseMemory();
//
//        Kryo kryo = new Kryo();
//        Input input = new Input(new FileInputStream(filename));
//
////        kryo.register(DnaBitString.class, new DnaBitStringKryoSerializer());
//
//        result.fileName = kryo.readObject(input, String.class);
//        Integer nextOffset = kryo.readObject(input, Integer.class);
//        result.last = (int) nextOffset;
//        if (debug) {
//            System.err.println("DEBUG39 :: Pulled out result.last : " + result.last);
//            if (result.last < -1) {
//
//            }
//        }
//        //result.count = (int) kryo.readObject(input, Integer.class);
//        result.entries = kryo.readObject(input, ArrayList.class);
//        result.nameIndex = kryo.readObject(input, ArrayList.class);
//
////        kryo.register(DnaBitString.class, new DnaBitStringKryoSerializer());
//        kryo.register(DnaBitString.class);
//
//
//        result.kingdoms = (HashMap<Integer, String>) kryo.readObject(input, HashMap.class);
//        Integer size = kryo.readObject(input, Integer.class);
//        if (debug) {
//            System.err.println("\tDEBUG49 :: Pulled out sequences.size() (manual) : " + size);
//        }
//
//
//        for (int k=0; k<size; k++)
//            result.sequences.addAndTrim( kryo.readObject(input,DnaBitString.class));
////        result.sequences = (ArrayList<DnaBitString>) kryo.readObject(input, ArrayList.class);
//
//
//        return result;
//    }
//
//
//    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
//        stream.writeObject(fileName);
//        stream.writeObject(last);
//        stream.writeObject(entries);
//        stream.writeObject(nameIndex);
//        stream.writeObject((Integer)sequences.size());
//
//        for (int k=0; k<sequences.size();k++)
//            stream.writeObject(sequences.get(k));
////            sequences.get(k).writeObject(stream);
////        stream.writeObject(sequences);
//        stream.writeObject(kingdoms);
//    }
//
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        fileName = (String) stream.readObject();
//        last = (Integer) stream.readObject();
//        entries = (ArrayList<Kid>) stream.readObject();
//        nameIndex = (ArrayList<String>) stream.readObject();
//        int size = (Integer) stream.readObject();
//        kingdoms = (HashMap<Integer, String>) stream.readObject();
//
//        sequences = new ArrayList<>();
//        for (int k=0; k<size;k++)
//            System.err.println("Importing DnaBitString for "+nameIndex.get(k));
//            sequences.addAndTrim( (DnaBitString) stream.readObject());
////            DnaBitString dns = new DnaBitString();
////            sequences.addAndTrim( dns.readObject(stream) );
////        sequences = (ArrayList<DnaBitString>) stream.readObject();
//        kingdoms = (HashMap<Integer, String>) stream.readObject();
//    }

    /**
     * Creates Kid entries if not already existing.
     * Imports the FNA sequences into the database.
     * <p>
     * Deprecated due to slow speed
     *
     * @param filename
     * @throws FileNotFoundException
     */
    public void importFNAold(String filename) throws FileNotFoundException {
        int currentKID = this.getMaxKid();
        String currentSeq = "";
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        //skipping is holdover from copying code.  Here, it does nothing.
        boolean skipping = false;
//        boolean debug = false;

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
                            storeSequence(currentKID, currentSeq);
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
                            storeSequence(currentKID, currentSeq);
                        }

                        // initialize next iteration

                        if (!this.contains(line.trim())) {
                            this.add(new Kid(line.trim()));
                        }
                        currentKID = this.indexOf(line.trim());
                        currentSeq = "";

                        ignore = false;
                    }
                } else {
                    if (!skipping) {
                        currentSeq += line.trim();
                    }
                }

            } //end for

            br.close();

            if (!ignore) {
                storeSequence(currentKID, currentSeq);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void importFNA(String filename) throws FileNotFoundException {


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
                            storeSequence(currentKID, currentSeq, tt);
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
                            storeSequence(currentKID, currentSeq, tt);
                        }

                        // initialize next iteration

                        if (indexOf(line.trim()) == -1) {
                            //original.addAndTrim(new Kid(line.trim()));
                            //addNewKidEntry(line);
                            add(new Kid(line.trim()));
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
                storeSequence(currentKID, currentSeq, tt);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void importFnaNoSequencesStored(String filename, int numberKmersExpected) throws FileNotFoundException {
        Kid[] entriesArr = new Kid[numberKmersExpected+5];
        String[] nameArr = new String[numberKmersExpected+5];
        entriesArr[0] = null;
        nameArr[0] = "";

        TimeTotals tt = new TimeTotals();
        tt.start();

        int k=0;

        int currentKID = -1;
        SuperString currentSeq = new SuperString();
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        //skipping is holdover from copying code.  Here, it does nothing.
        boolean skipping = false;
//        boolean debug = false;

//        TimeTotals tt = new TimeTotals();
//        tt.start();

        System.out.println("\nKiDB loading from FNA begins " + tt.toHMS() + "\n");
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
//                            storeSequence(currentKID, currentSeq, tt);
                            addSequenceLength(currentSeq.length());
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
//                            storeSequence(currentKID, currentSeq, tt);
                            addSequenceLength(currentSeq.length());
                        }

                        // initialize next iteration

                        if (indexOf(line.trim()) == -1) {
                            //original.addAndTrim(new Kid(line.trim()));
                            //addNewKidEntry(line);
                            addUsingArray(new Kid(line.trim()), entriesArr, nameArr);
                            k++;
                            if (k % PERIOD == 0){
                                System.err.println("\t\tFinished KID\t"+getLast()+"\t"+tt.toHMS());
                            }
                        }

//                        currentKID = getKid(line.trim()); // original.indexOf(line.trim());
//                        if (currentKID == -1) {
//                            System.err.println("This sequence not found in database : " + line);
//                            listEntries(0);
//                            exit(0);
//                        }
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
//                storeSequence(currentKID, currentSeq, tt);
                addSequenceLength(currentSeq.length());
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Copying entries from array to arrayList\t"+getLast()+"\t"+tt.toHMS());

        this.entries.addAll( new ArrayList<Kid>(Arrays.asList(entriesArr)) );
        this.nameIndex = new ArrayList<String>(Arrays.asList(nameArr));

//        int s = entries.size();
//        for (int z=1; z <= (s-last); z++){  //skip adding 1 to z everytime  (would be index s-z-1)
//            entries.remove(s-k);
//            nameIndex.remove(s-k);
//        }
        while( last+1 < entries.size() ){
            entries.remove(entries.size()-1);
            nameIndex.remove(nameIndex.size()-1);
        }

        System.err.println("Finished\t"+getLast()+"\t"+tt.toHMS());

    }

    protected void addSequenceLength(int length) {
        //DO NOTHING
        //PLace Holder for child class
    }


    public void storeSequence(int index, SuperString seq, TimeTotals tt) {
        java.util.Date timer = new java.util.Date();

        System.out.println("Constructing DnaBitString for kid\t" + index + "\t" + tt.toHMS());
        DnaBitString buddy = null;
        try {
            buddy = new DnaBitString(seq);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        storeSequence(index, buddy);
        System.out.println("Import for\t" + index + "\tfinished\t" + tt.toHMS());
    }


//    /**
//     * Builds empty KID from scratch, including DnaBitString records.
//     *
//     * @param files
//     * @param path
//     * @return
//     */
//    public static KidDatabaseMemory buildKidDb(List<String> files, String path) {
//        RocksKidDatabase myKidDB = new RocksKidDatabase(path, false);
//
//        myKidDB.addAndTrim(new Kid("Reserved for testing KID 1"), "RESERVED (NO DATA)");
//        myKidDB.addAndTrim(new Kid("Reserved for testing KID 2"), "RESERVED (NO DATA)");
//        myKidDB.addAndTrim(new Kid("Reserved for testing KID 3"), "RESERVED (NO DATA)");
//
//        for (String file : files) {
//            String currSeq = "";
//            String name = "";
//
//            try {
//                BufferedReader br = new BufferedReader(new FileReader(file));
//                for (String line; (line = br.readLine()) != null; ) {
//                    if (line.length() > 0 && line.charAt(0) == '>') {
//
//                        if (name.length() > 0) {
//                            myKidDB.addAndTrim(new Kid(name), currSeq);
////                            int k = myKidDB.indexOf(name);
////                            myKidDB.storeSequence(k,currSeq);
//                            name = line.trim();  //DOES NOT include "\n"
//                            currSeq = "";
//                        }
//                    } else {
//                        currSeq += line.trim();
//                    }
//                } //end for
//            } catch (IOException e) {
//                e.printStackTrace();
//                exit(-1);
//            }
//
//            //write last one read
//            myKidDB.addAndTrim(new Kid(name), currSeq);
//        }
//
//        return myKidDB;
//    }


    /**
     * Import all the salient details for (Kid class) entries from tab delimited file.
     *
     * @param filename
     * @throws IOException
     */
    public void importTsvKidInformation(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String[] info;
        int kid;
        String line = null;
        while ((line = br.readLine()) != null) {

            info = line.split("\t");
            if (!this.contains(info[0])) {
                this.add(new Kid(info[0]));
            }
            //FORMAT
            // "sequence name\tAccession\tAccessionVersion\ttaxonID\tspeciesID\tgenusID\tkingdomID\tisTranscriptome:{0,1}"

            kid = indexOf(info[0]);
            Kid point = entries.get(kid);
            point.Accession = info[1];
            point.AccessionVersion = info[2];
            point.taxonID = Integer.parseInt(info[3]);
            point.speciesID = Integer.parseInt(info[4]);
            point.genusID = Integer.parseInt(info[5]);
            point.kingdomID = Integer.parseInt(info[6]);
            if (Integer.parseInt(info[7]) > 0) point.transcriptome = true;
            else point.transcriptome = false;
        }

        br.close();

    }


//    public static void main(String[] args) {
//
//        System.out.println("\nTesting that roll over works properly.");
//        KidDatabaseMemory test = new KidDatabaseMemory();
//        test.last = Integer.MAX_VALUE;
//        System.out.println(test.getMaxKid());
//        System.out.println(test.getNextKid());
//        System.out.println(Integer.MIN_VALUE);
//
//
//        System.out.println("\n\nLoading file");
//        test = KidDatabaseMemory.loadFromFile(test.fileName);
//
//        System.out.println(test.indexOf(">gi|9634679|ref|NC_002188.1| Fowlpox virus, complete genome"));
//
//
//        System.out.println("\nForward and Reverse get Sequence");
//        try {
//            System.out.println(test.getSequence(4, 10, 45, false));
//            System.out.println(test.getSequence(4, 10, 45, true));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public String toString() {
        String result = "";
        for (int k = 0; k < entries.size(); k++) {
            result += Integer.toString(k) + " :: " + entries.get(k).toString() + "\n";
        }
        return result;
    }

    public String getSequence(int myKID, int from, int to, boolean reverse) throws Exception {

//        DnaBitString result = null;
        String result = null;
        DnaBitString temp = sequences.get(myKID);


        //WTF was this here for?
        //if (myKID < sequences.size())  sequences.get(myKID);
        //if (result != null) {
//        if (myKID < sequences.size()){


        if (getSequenceLength(myKID) >= from){
            if (to > getSequenceLength(myKID))
                to = getSequenceLength(myKID);
            if (reverse) {
                return temp.getSequenceReverseStrand(from, to);
            } else {
                return temp.getSequence(from, to);
            }
        }
        return DnaBitString.SENTINEL;  //result is "EMPTY"
    }

    @Override
    public void shutDown() {

    }

    public DnaBitString getSequence(int myKID) {
        if(myKID > last)  return null;
        DnaBitString result = sequences.get(myKID);
        return result;
    }

    public void listEntries(int from, int to) {
        for (int k = from; k < to; k++) {
            System.err.println(k + "\t" + entries.get(k).sequenceName);
        }
    }

    public void listEntries(int from) {
        listEntries(from, entries.size());
    }

    public Iterator<String> nameIterator() {
        return nameIndex.iterator();
    }


    /**
     * Used to truncate a database to make a fast loading database for testing.
     *
     * @param kill reverse order sorted array, so removed without changing indexes
     */
    public void removeSequences(Integer[] kill) {
        for (Integer k : kill) {
            sequences.set(k, null); //force delete
            sequences.remove(k);
            System.err.println("\tSequences now has length " + sequences.size());
        }
    }

    public void importFNAoneline(String s) {

        try (BufferedReader br = new BufferedReader(new FileReader(s))) {

            String line = br.readLine();
            line = line.replace("\n", "");
            add(new Kid("hg38chr1"));
            sequences.set(1, new DnaBitString(line));

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveNumbers(String file)  {
        FileWriter fw = null;
        BufferedWriter writer = null;



        try {

            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            DnaBitString dns = getSequence(1);

            long[] arr = dns.toLongArray();

            int bytes = dns.getNumBytes();
            int length = dns.getLength();

            if ( (bytes+7)/8 != arr.length) {
                System.err.println("MISMATCH ON BYTES");
                System.err.println("bytes\t"+bytes);
                System.err.println("length\t"+arr.length);
            }

            writer.write(Integer.toString(arr.length)+"\n");
            System.err.println("arr.length\t"+arr.length);


            writer.write(Integer.toString(length)+"\n");
            System.err.println("length\t"+length);

            for(int k=0; k< arr.length; k++){
                writer.write(Long.toString(arr[k])+"\n");
            }
            writer.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadNumbers(String file) {
        long[] arr;
        int num_long, num_bases;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            num_long = Integer.parseInt(line);
            line = br.readLine();
            num_bases = Integer.parseInt(line);


//            num_long =	7779889;
//            num_bases = 248956422;


            arr = new long[num_long];
            for (int k=0;
                 k< num_long;
                 k++){
                line = br.readLine();
                if (k == 2500)
                    k=k;
                if (line.length() > 0)
                    arr[k] = Long.parseLong(line);
                else
                    break;
            }
            DnaBitString dns = new DnaBitString(arr,num_bases);

            add(new Kid("hg38chr1"));
            sequences.set(1,dns);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


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
        total+=UnsafeMemory.getWriteUnsafeSize(sequences,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);


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
        um.putInt(last);

        //arrays
        um.put(nameIndex, UnsafeMemory.ARRAYLIST_STRING_TYPE);
        um.put(entries, UnsafeMemory.ARRAYLIST_KID_TYPE);
        um.put(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);


        um.put(sequences,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);

        um.putString(fileName);
        um.putInt(last);

        //arrays
        um.put(nameIndex, UnsafeMemory.ARRAYLIST_STRING_TYPE);
        um.put(entries, UnsafeMemory.ARRAYLIST_KID_TYPE);
        um.put(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);


        um.put(sequences,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);

    }



    public void readUnsafe(UnsafeMemory um) throws ClassCastException {
        long serial = um.getLong();
        if (serial != serialVersionUID){
            System.err.println("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
            throw new ClassCastException("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
        }
        fileName = um.getString();
        last = um.getInt();
        nameIndex = (ArrayList<String>) um.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);
        entries= (ArrayList<Kid>) um.get( UnsafeMemory.ARRAYLIST_KID_TYPE);
        kingdoms= (HashMap<Integer,String>) um.get(UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);

        sequences= (ArrayList<DnaBitString>) um.get(UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);

    }

    /**
     * Helper function written to minimize having ALL DnaBitStrings in memory as byte[] and as DnaBitString at same time.
     * @param ufr
     * @throws ClassCastException
     */
    private void readUnsafe(UnsafeFileReader ufr) throws ClassCastException {
        try {

            //header
            int topSize = ufr.getInt();
            long serial = ufr.getLong();
            if (serial != serialVersionUID){
                System.err.println("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
                throw new ClassCastException("KidDatabaseMemory.readUnsafe :: wrong SerialUID :: expected\t"+serialVersionUID+"\tfound\t"+serial);
            }

            //fileName and last
            fileName = ufr.getString();
            last = ufr.getInt();

            //Arrays
            UnsafeMemory read;
            read = new UnsafeMemory(ufr.readNextObject());
            nameIndex = (ArrayList<String>) read.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);
            read = new UnsafeMemory(ufr.readNextObject());
            entries= (ArrayList<Kid>) read.get( UnsafeMemory.ARRAYLIST_KID_TYPE);
            read = new UnsafeMemory(ufr.readNextObject());
            kingdoms= (HashMap<Integer, String>) read.get( UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);

            //sequences
            int size = ufr.getInt();  //burn data
            boolean skip = false;
            long serial0 = ufr.getLong();
            long serial1 = ufr.getLong();
            int arrSize = ufr.getInt();

            if (serial0 != ARRAYLIST_UID){
                System.err.println("UnsafeMemory.getArrayList expected Arraylist class, instead\t"+serial);
                throw new ClassCastException("UnsafeMemory.getArrayList expected Arraylist class, instead\t"+serial);
            }

            //Loads DnaBitString corresponding byte[] one at a time.
            if (serial1 == DNABITSTRING_UID) {
                //want to skip first entry; already built in constructor.  But needs to be read
                if (arrSize > 0)
                    read = new UnsafeMemory(ufr.readNextObject());

                //skip first
                for (int k=1; k<arrSize; k++){
                    //want to skip first entry; already built in constructor.  But needs to be read
                    read = new UnsafeMemory(ufr.readNextObject());
                    sequences.add((DnaBitString) DnaBitString.unsafeMemoryBuilder(read));
                }
            }
//            read = new UnsafeMemory(ufr.readNextObject());
//            kingdoms= (HashMap<Integer,String>) read.get(UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Helper function written to minimize having ALL DnaBitStrings in memory as byte[] and as DnaBitString at same time.
     * @param ufw
     * @throws ClassCastException
     */
    private void writeUnsafe(UnsafeFileWriter ufw) throws IOException {

        int total = UnsafeMemory.SIZE_OF_INT +UnsafeMemory.SIZE_OF_LONG;

        total+=UnsafeMemory.getWriteUnsafeSize(fileName, UnsafeMemory.STRING_TYPE);
        total+=UnsafeMemory.SIZE_OF_INT;
        total+=UnsafeMemory.getWriteUnsafeSize(nameIndex,UnsafeMemory.ARRAYLIST_STRING_TYPE);
        total+=UnsafeMemory.getWriteUnsafeSize(entries,UnsafeMemory.ARRAYLIST_KID_TYPE);

        total+=UnsafeMemory.getWriteUnsafeSize(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);

        //ArrayList headers
        total+= UnsafeMemory.SIZE_OF_INT +  //size
                UnsafeMemory.SIZE_OF_LONG + UnsafeMemory.SIZE_OF_LONG +   //Serials
                UnsafeMemory.SIZE_OF_INT ; //array size


        //BUG was range check was wrong
        //total+=4;



        UnsafeMemory um = new UnsafeMemory(total);
        int mySize = getWriteUnsafeSize();
        um.putInt(mySize); // bug was here?
        um.putLong(serialVersionUID);


        um.putString(fileName);
        um.putInt(last);

        um.put(nameIndex, UnsafeMemory.ARRAYLIST_STRING_TYPE);
        um.put(entries, UnsafeMemory.ARRAYLIST_KID_TYPE);
        um.put(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        //um.putInt(total + UnsafeMemory.getWriteUnsafeSize(sequences, ARRAYLIST_DNABITSTRING_TYPE));
        mySize =UnsafeMemory.getWriteUnsafeSize(sequences, ARRAYLIST_DNABITSTRING_TYPE);
        um.putInt(mySize);



        um.putLong(ARRAYLIST_UID);
        um.putLong(DNABITSTRING_UID);
        um.putInt(sequences.size());
        ufw.writeObject(um.toBytes());

        for (int k=0; k<sequences.size(); k++){
            um = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(sequences.get(k),UnsafeMemory.DNABITSTRING_TYPE));
            um.put(sequences.get(k), UnsafeMemory.DNABITSTRING_TYPE);
            ufw.writeObject(um.toBytes());  //where bug was - missing
            ufw.flush();
        }

        //um=new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(kingdoms,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE));
        //um.put(kingdoms, UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        //ufw.writeObject(um.toBytes());
    }

    public static KidDatabaseMemory loadFromFileUnsafe(String filename) {

        KidDatabaseMemory result = new KidDatabaseMemory();

        try {
            UnsafeFileReader ufr = UnsafeFileReader.unsafeFileReaderBuilder(filename);
            result.readUnsafe(ufr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }



    public void saveToFileUnsafe() {
        UnsafeFileWriter ufw = null;
        try {
            ufw = UnsafeFileWriter.unsafeFileWriterBuilder(fileName);
            writeUnsafe(ufw);
            ufw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        UnsafeMemory um;
//        um = new UnsafeMemory( getWriteUnsafeSize());
////        int size = UnsafeMemory.SIZE_OF_LONG+UnsafeMemory.SIZE_OF_INT;
////        size += UnsafeMemory.getWriteUnsafeSize(fileName, UnsafeMemory.STRING_TYPE) + UnsafeMemory.SIZE_OF_INT;
////
////        um = new UnsafeMemory(new byte[12]);
////        um.putInt(getWriteUnsafeSize());
////        um.putLong(serialVersionUID);
////        um.putString(fileName);
////        um.putInt(last);
////
////        um = new UnsafeMemory();
////        um.put(nameIndex, UnsafeMemory.ARRAYLIST_STRING_TYPE);
////        um.put(entries, UnsafeMemory.ARRAYLIST_KID_TYPE);
////        um.put(sequences,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);
////
//        writeUnsafe(um);
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    public ArrayList<Kid> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<Kid> entries) {
        this.entries = entries;
    }

    public ArrayList<String> getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(ArrayList<String> nameIndex) {
        this.nameIndex = nameIndex;
    }

    public ArrayList<DnaBitString> getSequences() {
        return sequences;
    }

    public void setSequences(ArrayList<DnaBitString> sequences) {
        this.sequences = sequences;
    }

    public HashMap<Integer, String> getKingdoms() {
        return kingdoms;
    }

    public void setKingdoms(HashMap<Integer, String> kingdoms) {
        this.kingdoms = kingdoms;
    }


    public void add(Kid temp, String s) {
        add(temp);

        storeSequence(getLast(),s);
    }

    public static int countSequencesFNA(String filename) {
        int result = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.charAt(0) == '>')result++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveToFileText() {
        try {
            FileWriter fw = new FileWriter(fileName + ".txt");
            BufferedWriter writer = new BufferedWriter(fw);
            for (int k = 1; k < getLast(); k++) {
                writer.write(getName(k) + "\t" + getSequenceLength(k)+"\n");
            }
        } catch (IOException e) {
            System.err.println("WARNING :: KidDatabaseMemory::saveToFileText failed to open output file.");
            e.printStackTrace();
        }

    }




    public static KidDatabaseMemory loadFileFromText(String filename) {
        KidDatabaseMemory result = new KidDatabaseMemory();
        result.fileName = filename;
        filename += filename+".txt";
        try(BufferedReader br = new BufferedReader(new FileReader("names.txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] pieces = line.split("\t");
                result.add(new Kid(pieces[0]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}