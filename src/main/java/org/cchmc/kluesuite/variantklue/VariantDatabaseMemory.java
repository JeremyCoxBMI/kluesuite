package org.cchmc.kluesuite.variantklue;


import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.binaryfiledirect.*;
import org.cchmc.kluesuite.klat.ReferenceSequenceRequest;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.Kmer31;

import java.io.*;
import java.util.*;

import static org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory.*;

/**
 * Created by osboxes on 25/04/17.
 *
 * Stores all Variants in a Database based in Memory
 * Maps Kid, Poistion, to a Variant List
 *
 */
public class VariantDatabaseMemory implements VariantDatabase<Variant, Variant[]>, UnsafeFileIO {

    String fileName;

    private static final long serialVersionUID = 1124001L;

    //KID maps to <position, Variant list>
    public HashMap<Integer, TreeMap<Integer,Variant[]>> snpMap;
    public HashMap<Integer, TreeMap<Integer,Variant[]>> indelMap;

    public VariantDatabaseMemory(String filename) {
        fileName = filename;
        snpMap = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
        indelMap = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
    }

//    public VariantDatabaseMemory(VariantDatabase1 vd) {
//        fileName = vd.fileName);
//        snpMap = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
//        indelMap = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
//        vd.sortValueLists();
//
//        putValuesOntoMap(vd.getSNPKeys(), snpMap, vd, true);
//        putValuesOntoMap(vd.getIndelKeys(), indelMap, vd, false);
//    }

    /**
     * Only to be used for importing data
     */
    public VariantDatabaseMemory() {
        super();
        snpMap = null;
        indelMap = null;
        fileName = "";
    }



    @Override
    public boolean containsKey(int KID) {
        return snpMap.containsKey(KID) || indelMap.containsKey(KID);
    }

    /**
     * Find index in the list of Indels that most closely matches the position requested.
     * Returns null if there is no list, but you can check with containsKey operation.
     *
     * @param key      KID for which we wish to retrieve the INDEL list
     * @param position position in the sequence corresponding to KID
     * @return
     */
    @Override
    public Variant[] nearestIndel(int key, int position) {
        TreeMap<Integer, Variant[]> temp = indelMap.get(key);
        Integer ceiling =  temp.ceilingKey(position);
        Integer floor = temp.floorKey(position);

        if (ceiling == floor){
            return temp.get(ceiling);
        } else if (floor == null) {
            return temp.get(temp.firstKey());
        } else if (ceiling == null){
            return temp.get(temp.lastKey());
        } else {
            int d1 = Math.abs(ceiling - position);
            int d2 = Math.abs(position - floor);
            if (d1 <= d2)
                return temp.get(ceiling);
            else
                return temp.get(floor);
        }

    }

    /**
     * Find index in thVariant[]list of Indels that most closely matches the position requested.
     * Index is more desirable for looking forward and backward.
     * Returns null if there is no list, but you can check with containsKey operation.
     *
     * @param key      KID for which we wish to retrieve the INDEL list
     * @param position position in the sequence corresponding to KID
     * @return
     */
    @Override
    public Integer indexNearestIndel(int key, int position) {
        TreeMap<Integer, Variant[]> temp = indelMap.get(key);
        Integer ceiling =  temp.ceilingKey(position);
        Integer floor = temp.floorKey(position);

        if (ceiling == floor){
            return ceiling;
        } else if (floor == null) {
            return temp.firstKey();
        } else if (ceiling == null){
            return temp.lastKey();
        } else {
            int d1 = Math.abs(ceiling - position);
            int d2 = Math.abs(position - floor);
            if (d1 <= d2)
                return ceiling;
            else
                return floor;
        }
    }


    /**
     * It will be very helpful to get positions in sorted order once written
     * <p>
     * OPTIONAL, if appropriate to do so
     * NOT APPROPRIATE with this DATA structure
     */
    @Override
    public boolean sortValueLists() {
        return true;
    }

    @Override
    /**
     * Lists all indel variants falling at position [pos]
     * May return null
     *
     */
    public Variant[] getIndel(int kid, int pos) {
        return indelMap.get(kid).get(pos);
    }

    @Override
    public Variant[] getSNPs(int kid, int pos) {
        return snpMap.get(kid).get(pos);
    }

    @Override
    public Set<Integer> getIndelKeys() {
        return indelMap.keySet();
    }

    @Override
    public Set<Integer> getSNPKeys() {
        return snpMap.keySet();
    }

    @Override
    public void setFilename(String file) {
        fileName = file;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    /**
     * Lists all indel variants falling within [limits] of KID = key
     * May return null
     *
     */
    public Variant[] getIndel(int kid, pair limits) {
        //how to iterate over map?
        TreeMap<Integer, Variant[]> indelMapAtKid = indelMap.get(kid);
        if (indelMapAtKid == null){
            return null;
        }
        ArrayList<Integer> sortKids = new ArrayList<Integer>(indelMapAtKid.keySet());
        int k = 0;
        while(  indelMapAtKid.get(  sortKids.get(k)  )[0].start < limits.l ) k++;

        ArrayList<Variant[]> result = new ArrayList<Variant[]>();
        Collections.sort(result, new VariantArrayComparator());

        int arrLength = 0;
        while(  indelMapAtKid.get(  sortKids.get(k)  )[0].start < limits.u){
            result.add(indelMapAtKid.get(  sortKids.get(k)  ));
            arrLength += indelMapAtKid.get(  sortKids.get(k)  ).length;
        }

        int z = 0;
        Variant[] result2 = new Variant[arrLength];
        for (Variant[] v  : result){
            for (int m=0; m< v.length; m++)   {
                result2[z] = v[m];
                z++;
            }
        }

        return result2;
    }

    @Override
    public Variant[] getSNPs(int kid, pair limits) {
        TreeMap<Integer, Variant[]> snpMapAtKid = snpMap.get(kid);
        if (snpMapAtKid == null){
            return null;
        }
        ArrayList<Integer> sortKids = new ArrayList<Integer>(snpMapAtKid.keySet());
        int k = 0;
        while(  snpMapAtKid.get(  sortKids.get(k)  )[0].start < limits.l ) k++;

        ArrayList<Variant[]> result = new ArrayList<Variant[]>();
        Collections.sort(result, new VariantArrayComparator());

        int arrLength = 0;
        while(  snpMapAtKid.get(  sortKids.get(k)  )[0].start < limits.u){
            result.add(snpMapAtKid.get(  sortKids.get(k)  ));
            arrLength += snpMapAtKid.get(  sortKids.get(k)  ).length;
        }

        int z = 0;
        Variant[] result2 = new Variant[arrLength];
        for (Variant[] v  : result){
            for (int m=0; m< v.length; m++)   {
                result2[z] = v[m];
                z++;
            }
        }

        return result2;
    }

    @Override
    /**
     * Add snps to array result
     */
    public void getSNPs(int kid, pair limits, Variant[] result) {
        Variant[] firstResults = result;
        Variant[] moreResults = getSNPs(kid, limits);

        if (firstResults == null){
            result = moreResults;
        } else if (moreResults == null){
            result = firstResults;
        } else {

            result = new Variant[firstResults.length + moreResults.length];
            int z = 0;
            for (int k = 0; k < firstResults.length; k++) {
                result[z] = firstResults[k];
                z++;
            }
            for (int k = 0; k < moreResults.length; k++) {
                result[z] = moreResults[k];
                z++;
            }
        }
    }

    @Override
    public void getIndels(int key, pair limits, Variant[] result) {

    }


    /**
     * Add snps to array result
     */
    public void getIndel(int kid, pair limits, Variant[] result) {
        Variant[] firstResults = result;
        Variant[] moreResults = getIndel(kid, limits);

        //NOTE if both are null, correct result is null.  This code does that.
        if (firstResults == null){
            result = moreResults;
        } else if (moreResults == null){
            result = firstResults;
        } else {

            result = new Variant[firstResults.length + moreResults.length];
            int z = 0;
            for (int k = 0; k < firstResults.length; k++) {
                result[z] = firstResults[k];
                z++;
            }
            for (int k = 0; k < moreResults.length; k++) {
                result[z] = moreResults[k];
                z++;
            }
        }
    }



    public StringVariant[] getAllVariants2(int kid, int from, int to, KidDatabaseMemory kdb, boolean reverse){
        PermutatorLimitAdjacent chuckNorris = new PermutatorLimitAdjacent(kdb, this);
        pair limits = new pair(from, to);
        Variant[] variants = getIndel(kid, limits);
        getSNPs(kid, limits, variants);
        DnaBitString dns = new DnaBitString(kdb.getSequence(kid).toString());


        VariantDatabaseIterator<Variant> it = new VariantDatabaseArrayIterator(variants);

        ArrayList<Variant> var2 = new ArrayList<>();
        ArrayList<StringAndVariants[]> result1 = new ArrayList<>();
        Variant curr;

        while (it.hasNext()){
            curr = it.next();
            var2.add(curr);
            int refStart = curr.start - Kmer31.KMER_SIZE + 1;
            int refStop = curr.start + Kmer31.KMER_SIZE - 1;

            while (it.peekPosition().start == curr.start && var2.size() < 5){ //TODO 5 - or variable?
                var2.add(curr);
            }

            result1.add( chuckNorris.generateVariants(var2,refStart,refStop,dns) );
        }

        int count = 0;
        for (StringAndVariants[] sv : result1){
            count += sv.length;
        }

        StringVariant[] result = new StringVariant[count];
        int z=0;
        for (StringAndVariants[] sv : result1) {
            for (StringAndVariants v : sv){
                result[z++] = new StringVariant(v,var2);
            }
        }
        return result;


//        StringAndVariants[] result = chuckNorris.generateVariants(variants, from, to, dns);
//        if (reverse){
//            for (StringAndVariants sb : result){
//                sb.s = new StringBuilder(sb.s).reverse().toString();
//            }
//        }
//
//
//        StringVariant[] result2 = new StringVariant[result.length];
//        //for (StringAndVariants sb : result){
//        for (int k=0; k<result.length;k++){
//            ArrayList<Variant> v = new ArrayList<Variant>();
//            for (int m : result[k].var){
//                v.addAndTrim(variants[m]);
//            }
//            result2[k] = new StringVariant(result[k].s,v);
//        }
//
//        return result2;
    }

    public StringAndVariants[] getAllVariants(ReferenceSequenceRequest t, KidDatabaseMemory rkd) {
        return getAllVariants(t.myKID, t.start, t.stop, rkd, t.reverse); //t.myKidDB, t.reverse);

    }

    public StringVariant[] getAllVariants2(ReferenceSequenceRequest t, KidDatabaseMemory rkd) {
        return getAllVariants2(t.myKID, t.start, t.stop, rkd, t.reverse); //t.myKidDB, t.reverse);

    }

    /**
     * Deprecated
     *
     * @param kid
     * @param from
     * @param to
     * @param kdb
     * @param reverse
     * @return
     */
    public StringAndVariants[] getAllVariants(int kid, int from, int to, KidDatabaseMemory kdb, boolean reverse){
        PermutatorLimitAdjacent chuckNorris = new PermutatorLimitAdjacent(kdb, this);
        pair limits = new pair(from, to);
        Variant[] variants = getIndel(kid, limits);
        getSNPs(kid, limits, variants);
        DnaBitString dns = new DnaBitString(kdb.getSequence(kid).toString());
        StringAndVariants[] result = chuckNorris.generateVariants(variants, from, to, dns);
        if (reverse){
            //TODO test this
            for (StringAndVariants sb : result){
                sb.s = new StringBuilder(sb.s).reverse().toString();
            }
        }
        return result;
    }







    /**
     * Helper function to addIndel, addSNP
     * @param KID
     * @param addme
     */
    private static TreeMap<Integer, Variant[]> addVariant(int KID, Variant addme, TreeMap<Integer, Variant[]> tree){

        Variant[] result;

        if (tree == null){
            tree = new TreeMap<Integer,Variant[]>();
            result = new Variant[1];
            result[0] = addme;
        } else {
            result = tree.get(addme.start);

            if (result == null) {
                result = new Variant[1];
                result[0] = addme;
            } else {
                Variant[] resultOLD = result;
                result = new Variant[resultOLD.length + 1];
                for (int k = 0; k < resultOLD.length; k++) {
                    result[k] = resultOLD[k];                    result[resultOLD.length] = addme;

                    //last one  result2 (last) = result.length - 1 = resultOLD.length
                }

                result[resultOLD.length] = addme;
            }
        }

        tree.put(addme.start,result);
        return tree;
    }

    @Override
    public void addIndel(int KID, Variant addme) {
        if (addme.type != mutation.INSERTION && addme.type != mutation.DELETION) throw new ClassCastException("Expecting an INDEL but got "+addme);
        TreeMap<Integer,Variant[]> tree = indelMap.get(KID);
        //UPDATE tree
        tree = addVariant(KID,addme,tree);
        indelMap.put(KID,tree);
    }

    @Override
    public void addSNP(int KID, Variant addme) {
        if (addme.type != mutation.SNP) throw new ClassCastException("Expecting a SNP but got "+addme);
        TreeMap<Integer,Variant[]> tree = snpMap.get(KID);
        //UPDATE tree
        tree = addVariant(KID,addme,tree);
        snpMap.put(KID,tree);
    }


    /**
     * @param filename filename containing UCSC variant definitions
     * @param myKidDb  existing database
     * @param prefix   used in constructing the database
     * @throws IOException
     */
    @Override
    public void importValues(String filename, KidDatabaseMemory myKidDb, String prefix) throws IOException {

        // Construct BufferedReader from FileReader
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line = null;
        String seqName, myClass, observed, newSeq;
        int start, stop, kid; //length;
        String[] splits;
        boolean first = true;
        String[] multiObserved = new String[1];

        //UCSC format this is false
        first = false;
        int indelCNT = 0;
        int mnpCNT = 0;
        int microCNT =0;
        int entries=0;
        while ((line = br.readLine()) != null) {
//            if (line.equals("585\tchr1\t10107\t10108\trs62651026\t0\t+\tC\tC\tC/T\tgenomic\tsingle\tunknown\t0\t0\tnear-gene-5\texact\t1\t\t1\tBCMHGSC_JDW,\t0\t\t\t\t")){
//                int DEBUG = 1;
//            }

            splits = line.split("\t", -1);
            if (first){
                first = false;  //skip header
            } else if (line.length() > 0 && line.charAt(0)!='#' && splits.length > 10){   //most error lines have 1 column, but whatever using cut-off 10
                //System.out.println(line);
//                if(splits.length < 10){
//                    first=first;
//                }
                seqName = prefix+splits[1];
                myClass = splits[11];
                //myClass could be "deletion"m "insertion", "single"
                //other values ignored?

                //DEBUG 2016-12-22 :: source file is 1-indexed; KLUE is 0-indexed
                start = Integer.parseInt(splits[2]) - 1;
                stop = Integer.parseInt(splits[3]) - 1;

                //length = end - start;
                observed = splits[9];
                if(observed.equals("lengthTooLong")){
                    newSeq = splits[8];
                } else {

                    multiObserved = observed.split("/");
                    if (multiObserved.length >= 2) {
                        newSeq = multiObserved[1];
                    } else {
                        newSeq = "";
                    }
                }
                //System.out.println(observed+"\t\t\t"+observed.split("/"));

                String debugStr = seqName;
                kid = myKidDb.indexOf(seqName);
//                if (kid == -1){
//                    kid=kid;
//                    System.out.println("Unknown sequence Name\t"+seqName);
//                }

                if (kid > 0) {
                    Variant temp;
                    if (entries % 100000 == 0)
                        System.out.println("\t\tImported record " + (new Double(entries / 1000)) / 1000 + " M");
                    entries++;
                    switch (myClass) {
                        case "single":
                            if (multiObserved.length > 2){
                                for (int x=1; x<multiObserved.length; x++){
                                    newSeq = multiObserved[x];
                                    temp = Variant.buildSNP(start, kid, newSeq, splits[4]+"_"+x, Variant.detailedVariantName(splits[2], x+":"+observed));
                                    addSNP(kid,temp);
                                }
                            } else {
                                temp = Variant.buildSNP(start, kid, newSeq, splits[4], Variant.detailedVariantName(splits[2], observed));
                                //System.out.println(temp);
                                addSNP(kid, temp);
                            }
                            break;

                        case "insertion":
                            if (multiObserved.length > 2){
                                for (int x=1; x<multiObserved.length; x++){
                                    newSeq = multiObserved[x];
                                    temp = Variant.buildINSERTION(start, kid, newSeq, splits[4]+"_"+x, Variant.detailedVariantName(splits[2], x+":"+observed));
                                    addIndel(kid,temp);
                                }
                            } else {
                                temp = Variant.buildINSERTION(start, kid, newSeq, splits[4], Variant.detailedVariantName(splits[2], observed));
                                addIndel(kid, temp);
                            }
                            break;

                        case "deletion":
                            if (multiObserved.length > 2){
                                for (int x=1; x<multiObserved.length; x++){
                                    newSeq = multiObserved[x];
                                    temp = Variant.buildDELETION(start, kid, (stop - start), splits[4]+"_"+x, Variant.detailedVariantName(splits[2], x+":"+observed));
                                    addIndel(kid,temp);
                                }
                            } else {
                                temp = Variant.buildDELETION(start, kid, (stop - start), splits[4], Variant.detailedVariantName(splits[2], observed));
                                addIndel(kid, temp);
                            }
                            break;

                        case "in-del":
                            indelCNT++;
                            break;
                        case "mnp":
                            mnpCNT++;
                            break;
                        case "microsatellite":
                            microCNT++;
                            break;
                        default:
                            //other cases are undefined
                            System.err.println("ERROR: cannot process entry type : " + myClass + "\tname\t" + splits[4]);
                            break;
                    } // end switch
                } //end if name exists
            } else {
                System.out.println("\tThis input line not processed :\t\t"+line);
                //System.out.println(line);
            }
        } //end while readline()

        System.err.println("\tWARNING: ignored several entries as planned:");
        System.err.println("\t\t"+indelCNT+" 'in-del' entries");
        System.err.println("\t\t"+mnpCNT+" 'mnp' entries");
        System.err.println("\t\t"+microCNT+" 'microsatellite' entries");
        br.close();
        sortValueLists();
    }

    /**
     *
     * @return   Set of KID numbers
     */
    @Override
    public Set<Integer> getKeys() {
        Set<Integer> t = getSNPKeys();
        HashSet<Integer> result = new HashSet<Integer>(t);
        result.addAll(getIndelKeys());
        return result;
    }


//    public Set<Integer> getKeysKid(int kid) {
//        Set<Integer> t = snpMap.get(kid).getKeys();
//        HashSet<Integer> result = new HashSet<Integer>(t);
//        result.addAll(indelMap.get(kid).getKeys());
//        return result;
//    }


//    @Override
//    public void saveToFile() throws FileNotFoundException {
//
//        Integer nextOffset;
//        Set<Integer> keys;
//
//        Kryo kryo = new Kryo();
//        Output output = new Output(new FileOutputStream(fileName));
//
//        kryo.writeObject(output, fileName);
//
//        //Programmer's note: Kryo is known to have trouble with large data objects.  Here we break it up to not crash.
//        // (Search Kryo NegatizeZero array)
//
//        //indelMap
//        keys = indelMap.keySet();
//        nextOffset = keys.size();
//        kryo.writeObject(output, nextOffset);
//        for (Integer k : keys){
//            kryo.writeObject(output, k);
//            kryo.writeObject(output, indelMap.get(k));
//        }
//
//        //snpMap
//        keys = snpMap.keySet();
//        nextOffset = keys.size();
//        kryo.writeObject(output, nextOffset);
//        for (Integer k : keys){
//            kryo.writeObject(output, k);
//            kryo.writeObject(output, snpMap.get(k));
//        }
//
//        output.close();
//
//    }
//
//
//    public void loadFromFile(String filename) throws FileNotFoundException {
//
//        Integer nextOffset;
//        Integer pos;
//        TreeMap<Integer,Variant[]> t;
//        Kryo kryo = new Kryo();
//        Input input = new Input(new FileInputStream(filename));
//
//
//        //VariantDatabaseMemory vd2 = new VariantDatabaseMemory();
//        this.indelMap = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
//        this.snpMap = new HashMap<Integer, TreeMap<Integer,Variant[]>>();
//
//
//        this.fileName = kryo.readObject(input, String.class);
//
//        //indelMap
//        nextOffset = kryo.readObject(input, Integer.class);
//        for (int k=0; k<nextOffset; k++){
//            pos = kryo.readObject(input, Integer.class);
//            t = kryo.readObject(input, TreeMap.class);
//            this.indelMap.put(pos, t);
//        }
//
//        //snpMap
//        nextOffset = kryo.readObject(input, Integer.class);
//        for (int k=0; k<nextOffset; k++){
//            pos = kryo.readObject(input, Integer.class);
//            t = kryo.readObject(input, TreeMap.class);
//            this.snpMap.put(pos, t);
//        }
//
//        input.close();
//    }

    @Override
    public int getWriteUnsafeSize(){
        int total=0;
        //TODO
        //Irrelevant?
        return total;
    }

    @Override
    /**
     * Create using default constructor, and then load.
     */
    public void loadFromFileUnsafe(String filename) throws IOException {
        UnsafeFileReader ufr = UnsafeFileReader.unsafeFileReaderBuilder(filename);
        this.readUnsafe(ufr);
    }


    @Override
    public void saveToFileUnsafe() throws IOException{
        UnsafeFileWriter ufw = null;
        ufw = UnsafeFileWriter.unsafeFileWriterBuilder(fileName);
        writeUnsafe(ufw);
        ufw.close();
    }

    @Override
    public void readUnsafe(UnsafeFileReader ufr) throws ClassCastException {
//        String fileName;
//
//        private static final long serialVersionUID = 1124001L;
//
//        //KID maps to <position, Variant list>
//        public Map<Integer, TreeMap<Integer,Variant[]>> snpMap;
//        public Map<Integer, TreeMap<Integer,Variant[]>> indelMap;

        int topSize;
        long serial;
        try {
            topSize = ufr.getInt();
            serial = ufr.getLong();
            if (serial != serialVersionUID){
                System.out.println("VariantDatabaseMemory.readUnsafe(ufr) :: Expecting HashMap class, but SerialUID do not match :: unknown :: expecting\t"+serial);
                throw new ClassCastException("VariantDatabaseMemory.readUnsafe(ufr) :: Expecting HashMap class, but SerialUID do not match :: unknown :: expecting\t"+serial);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileName = ufr.getString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //read in HashMaps
        byte[] read;
        try {

            HashMap<Integer, TreeMap<Integer, Variant[]>> hm;
            int size, key;
            TreeMap<Integer,Variant[]> tree;
            UnsafeMemory um;

            size = ufr.getInt();    //ignore byte size
            serial = ufr.getLong();

            if (serial != UnsafeMemory.HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID){
                System.out.println("VariantDatabaseMemory.readUnsafe(ufr) :: Expecting HashMap<int,Tree<int,Var[]>> class, but SerialUID do not match :: unknown :: expecting\t"
                        +UnsafeMemory.HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID+"\tactual is \t"+serial);
                throw new ClassCastException("VariantDatabaseMemory.readUnsafe(ufr) :: Expecting HashMap<int,Tree<int,Var[]>> class, but SerialUID do not match :: unknown :: expecting\t"
                        +UnsafeMemory.HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID+"\tactual is \t"+serial);
            }

            hm = new HashMap<Integer, TreeMap<Integer, Variant[]>>();
            size = ufr.getInt();
            for (int z=0; z<size;z++){
                key = ufr.getInt();
                read = ufr.readNextObject();
                um = new UnsafeMemory(read);
                tree = (TreeMap<Integer, Variant[]>) um.get(UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
                hm.put(key,tree);
            }
            snpMap = hm;

            hm = new HashMap<Integer, TreeMap<Integer, Variant[]>>();
            size = ufr.getInt();
            for (int z=0; z<size;z++){
                key = ufr.getInt();
                read = ufr.readNextObject();
                um = new UnsafeMemory(read);
                tree = (TreeMap<Integer, Variant[]>) um.get(TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
                hm.put(key,tree);
            }
            indelMap = hm;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void writeUnsafe(UnsafeFileWriter ufw) throws IOException {

        //header VariantDatabase
        UnsafeMemory um = new UnsafeMemory(SIZE_OF_INT+UnsafeMemory.SIZE_OF_LONG);
        um.putInt(getWriteUnsafeSize());
        um.putLong(serialVersionUID);
        ufw.writeObject(um.toBytes());

        ufw.writeObject(fileName);

        //snpMap
        //header
        um.reset();
        um.putInt(UnsafeMemory.getWriteUnsafeSize(snpMap,HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_TYPE));
        um.putLong(UnsafeMemory.HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID);
        ufw.writeObject(um.toBytes());

        HashMap<Integer, TreeMap<Integer, Variant[]>> hm;
        int size;
        TreeMap<Integer,Variant[]> tree;
        Set<Integer> keys;

        um=new UnsafeMemory(SIZE_OF_INT);
        hm = (HashMap<Integer, TreeMap<Integer, Variant[]>>) snpMap;
        keys = hm.keySet();
        um.putInt(keys.size());
        ufw.writeObject(um.toBytes());


        for (int key : keys){
            tree = (TreeMap<Integer, Variant[]>) hm.get(key);
            int sizer=SIZE_OF_INT+UnsafeMemory.getWriteUnsafeSize(tree,UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
            um=new UnsafeMemory(sizer);
            um.putInt(key);
            um.put(tree,UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
            ufw.writeObject(um.toBytes());
        }

        um=new UnsafeMemory(SIZE_OF_INT);
        hm = (HashMap<Integer, TreeMap<Integer, Variant[]>>) indelMap;
        keys = hm.keySet();
        um.putInt(keys.size());
        ufw.writeObject(um.toBytes());

        for (int key : keys){
            tree = (TreeMap<Integer, Variant[]>) hm.get(key);
            um=new UnsafeMemory(SIZE_OF_INT+UnsafeMemory.getWriteUnsafeSize(tree,UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE));
            um.putInt(key);
            um.put(tree,UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
            ufw.writeObject(um.toBytes());
        }
    }




    public void writeObject() throws IOException {
        writeObject();
    }


    public void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeObject(fileName);
        stream.writeObject(snpMap);
        stream.writeObject(indelMap);
    }

    public void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        fileName = (String) stream.readObject();
        snpMap = (HashMap<Integer, TreeMap<Integer, Variant[]>>) stream.readObject();
        indelMap = (HashMap<Integer, TreeMap<Integer, Variant[]>>) stream.readObject();
        //Map<Integer, TreeMap<Integer,Variant[]>> snpMap;
    }



    //public Iterator<Variant> iterator(int kid){
    public VariantDatabaseMemoryIterator iterator(int kid){
        //return new myIterator(kid);
        return new VariantDatabaseMemoryIterator(kid,this);
    }

    /**
     * Number of Variants at
     * @param kid
     * @return
     */
    @Override
    public int getNumVariants(Integer kid) {
        int result = 0;

        Set<Integer> keys = indelMap.get(kid).keySet();
        for (int key:keys)
            result += indelMap.get(kid).get(key).length;

        keys = snpMap.get(kid).keySet();
        for (int key:keys)
            result += snpMap.get(kid).get(key).length;

        return result;
    }

    public void removeDuplicates(){

        for (int kid : snpMap.keySet()){
            for (int treeIdx : snpMap.get(kid).keySet()){
                Variant[] v = snpMap.get(kid).get(treeIdx);
                int duplicate = 0;
                for (int k=0; k<v.length-1; k++){
                    if (v[k].equals(v[k+1])){
                        duplicate++;
                    }
                }

                Variant[] result = new Variant[v.length-duplicate];

                int write = 0;
                result[write] = v[0];
                write++;
                for (int k=1; k< v.length; k++){
                    if (!v[k].equals(v[k-1])){
                        result[write] = v[k];
                        write++;
                    }
                }
                snpMap.get(kid).put(treeIdx, result);
            }
        }

        for (int kid : indelMap.keySet()){
            for (int treeIdx : indelMap.get(kid).keySet()){
                Variant[] v = indelMap.get(kid).get(treeIdx);
                int duplicate = 0;
                for (int k=0; k<v.length-1; k++){
                    if (v[k].equals(v[k+1])){
                        duplicate++;
                    }
                }

                Variant[] result = new Variant[v.length-duplicate];

                int write = 0;
                result[write] = v[0];
                write++;
                for (int k=1; k< v.length; k++){
                    if (!v[k].equals(v[k-1])){
                        result[write] = v[k];
                        write++;
                    }
                }
                indelMap.get(kid).put(treeIdx, result);
            }
        }

    }

    public void removeAllButChr1(){
        for (Integer k : indelMap.keySet()){
            if (k!= 1){
                indelMap.put(k,new TreeMap<Integer, Variant[]>());
            }
        }
        for (Integer k : snpMap.keySet()){
            if (k!= 1){
                snpMap.put(k,new TreeMap<Integer, Variant[]>());
            }
        }
    }

//    @Override
//    public int getWriteUnsafeSize() {
//        return 0;
//    }
//
//    @Override
//    public void writeUnsafe(UnsafeMemory um) {
//
//    }
//
//    @Override
//    public void readUnsafe(UnsafeMemory um) throws ClassCastException {
//
//    }


    public static void saveToFileUnsafe(String fileName, VariantDatabaseMemory[] vdm) throws IOException{
        UnsafeFileWriter ufw = null;
        ufw = UnsafeFileWriter.unsafeFileWriterBuilder(fileName);

        UnsafeMemory um = new UnsafeMemory(SIZE_OF_INT+UnsafeMemory.SIZE_OF_LONG);

        int total = SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG;
        for (int k=0; k< vdm.length;k++) {
            total += vdm[k].getWriteUnsafeSize() - (SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG);
        }

        um.putInt(total);
        um.putLong(serialVersionUID);
        ufw.writeObject(um.toBytes());

        ufw.writeObject(fileName);

        //snpMap
        //header
        um.reset();

        total = SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG;
        for (int k=0; k< vdm.length;k++) {
            total += UnsafeMemory.getWriteUnsafeSize(vdm[k].snpMap,HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_TYPE) - (SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG);
        }

        um.putInt(total);
        um.putLong(UnsafeMemory.HASHMAP_INTEGER_TREEMAP_INTEGER_ARRAY_VARIANT_UID);
        ufw.writeObject(um.toBytes());


        HashMap<Integer, TreeMap<Integer, Variant[]>> hm;// = new HashMap<Integer, TreeMap<Integer, Variant[]>>[vdm.length];
        int size;
        TreeMap<Integer,Variant[]> tree;

        //SnpMAP
        um=new UnsafeMemory(SIZE_OF_INT);
        Set<Integer> s = vdm[0].snpMap.keySet();
        HashSet<Integer> s2 = new HashSet<Integer>(s);

        for (int k=1; k< vdm.length;k++) {
            s2.addAll( vdm[k].snpMap.keySet() );
        }

        um.putInt(s2.size());
        ufw.writeObject(um.toBytes());

        for (int k=0; k< vdm.length;k++) {
            hm = vdm[k].snpMap;
            Set<Integer> keys = hm.keySet();
            for (int key : keys) {
                tree = (TreeMap<Integer, Variant[]>) hm.get(key);
                int sizer = SIZE_OF_INT + UnsafeMemory.getWriteUnsafeSize(tree, UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
                um = new UnsafeMemory(sizer);
                um.putInt(key);
                um.put(tree, UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
                ufw.writeObject(um.toBytes());
            }
        }


        //indelMap
        um=new UnsafeMemory(SIZE_OF_INT);
        s = vdm[0].indelMap.keySet();
        s2 = new HashSet<Integer>(s);

        for (int k=1; k< vdm.length;k++) {
            s2.addAll( vdm[k].indelMap.keySet() );
        }

        um.putInt(s2.size());
        ufw.writeObject(um.toBytes());

        for (int k=0; k< vdm.length;k++) {
            hm = vdm[k].indelMap;
            Set<Integer> keys = hm.keySet();
            for (int key : keys) {
                tree = (TreeMap<Integer, Variant[]>) hm.get(key);
                int sizer = SIZE_OF_INT + UnsafeMemory.getWriteUnsafeSize(tree, UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
                um = new UnsafeMemory(sizer);
                um.putInt(key);
                um.put(tree, UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);
                ufw.writeObject(um.toBytes());
            }
        }

        ufw.close();
    }


    public int size() {
        int result = 0;
        Set<Integer> kids = indelMap.keySet();
        for (int kid : kids )   result += indelMap.get(kid).keySet().size();
        kids = snpMap.keySet();
        for (int kid : kids )   result += snpMap.get(kid).keySet().size();

        return result;
    }
}
