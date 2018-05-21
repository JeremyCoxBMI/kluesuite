package org.cchmc.kluesuite.variantklue;

import org.cchmc.kluesuite.klat.ReferenceSequenceRequest;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;


public interface VariantDatabase<E,C> {    //E is element, C is collection

    //public String fileName;


    /**
     * Adds an indel to the databse.
     *
     * @param addme
     */
    public void addIndel(int KID, E addme);


    public void addSNP(int KID, E addme);


    public boolean containsKey(int KID);

    /**
     * Find index in the list of Indels that most closely matches the position requested.
     * Returns null if there is no list, but you can check with containsKey operation.
     *
     * @param key      KID for which we wish to retrieve the INDEL list
     * @param position position in the sequence corresponding to KID
     * @return
     */
    public C nearestIndel(int key, int position) ;

    /**
     * Find index in the list of Indels that most closely matches the position requested.
     * Index is more desirable for looking forward and backward.
     * Returns null if there is no list, but you can check with containsKey operation.
     *
     * @param key      KID for which we wish to retrieve the INDEL list
     * @param position position in the sequence corresponding to KID
     * @return
     */
    public Integer indexNearestIndel(int key, int position);


    /**
     * It will be very helpful to get positions in sorted order once written
     * <p>
     * OPTIONAL, if appropriate to do so
     */
    public boolean sortValueLists();


    public C getIndel(int key, int pos);

    public C getSNPs(int key, int pos);

    public Set<Integer> getIndelKeys();

    public Set<Integer> getSNPKeys();

    public void setFilename(String file);

    public String getFilename();

    public C getIndel(int key, pair limits);

    public C getSNPs(int key, pair limits);

    public void getSNPs(int key, pair limits, C result);

    public void getIndels(int key, pair limits, C result);

    public StringAndVariants[] getAllVariants(int kid, int from, int to, KidDatabaseMemory kdb, boolean reverse) ;

    public StringVariant[] getAllVariants2(int kid, int from, int to, KidDatabaseMemory kdb, boolean reverse);

    public StringAndVariants[] getAllVariants(ReferenceSequenceRequest t, KidDatabaseMemory rkd);

    public StringVariant[] getAllVariants2(ReferenceSequenceRequest t, KidDatabaseMemory rkd);


    /**
     * @param filename filename containing UCSC variant definitions
     * @param myKidDb  existing database
     * @param prefix   used in constructing the database
     * @throws IOException
     *
     * OPTIONAL
     */
    public void importValues(String filename, KidDatabaseMemory myKidDb, String prefix) throws IOException;

    public Set<Integer> getKeys();


//    public void saveToFile() throws FileNotFoundException ;
//
//    public void loadFromFile(String filename) throws FileNotFoundException;
//
//
//    public void writeObject() throws IOException;
//
//    public void writeObject(ObjectOutputStream stream) throws IOException;
//
//    public void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException;


    VariantDatabaseIterator iterator(int KID);

    int getNumVariants(Integer kid);
}
