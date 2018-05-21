package org.cchmc.kluesuite.klue;

import java.io.IOException;

import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeSerializable;
import sun.misc.Unsafe;

/**
 * This class will track all the information known about a KID, which ultimately will be quite advanced.
 * Ironically, KID is not stored here, it is stored in the parent class mapping.
 * This is more or less a placeholder.
 *
 * @author osboxes
 *
 *	2016-08-15	v2.0	Imported from v1.6.  Added AccessionVersion and transcriptome
 *  2016-09-13          Updated to include kingdomID; note that serialization ID changed
 */




public class Kid implements UnsafeSerializable {
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }



    public String getAccession() {
        return Accession;
    }

    public void setAccession(String accession) {
        Accession = accession;
    }

    public String getAccessionVersion() {
        return AccessionVersion;
    }

    public void setAccessionVersion(String accessionVersion) {
        AccessionVersion = accessionVersion;
    }

    public int getTaxonID() {
        return taxonID;
    }

    public void setTaxonID(int taxonID) {
        this.taxonID = taxonID;
    }

    public int getSpeciesID() {
        return speciesID;
    }

    public void setSpeciesID(int speciesID) {
        this.speciesID = speciesID;
    }

    public int getGenusID() {
        return genusID;
    }

    public void setGenusID(int genusID) {
        this.genusID = genusID;
    }

    public int getKingdomID() {
        return kingdomID;
    }

    public void setKingdomID(int kingdomID) {
        this.kingdomID = kingdomID;
    }

    public boolean isTranscriptome() {
        return transcriptome;
    }

    public void setTranscriptome(boolean transcriptome) {
        this.transcriptome = transcriptome;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * Given name in database Fasta sequence
     */
    public String sequenceName;

    /**
     * The Accession alphanumeric key without version number ( no '.' and number at end)
     */
    public String Accession;

    /**
     * The Accession alphanumeric key with version number ( includes '.' and number at end)
     */
    public String AccessionVersion;

    /**
     * NCBI taxon number; may be at strain or species level  RARELY you could submit another clade
     */
    public int taxonID;

    /**
     * NCBI taxon number for species
     */
    public int speciesID;

    /**
     * NCBI taxon number for species
     */
    public int genusID;

    /**
     * NCBI Taxon, representing "Kingdom" grouping
     * Note this is somewhat arbitrary.  It is more apt to say this is the "large grouping"
     */
    public int kingdomID;

    /**
     * Marks an RNA version of a gene
     */
    boolean transcriptome;

    public static String SENTINEL = "FAKE";

    Kid() {
        sequenceName = SENTINEL;
        taxonID = speciesID = genusID = -1;
        Accession = "";
        AccessionVersion = "";
        transcriptome = false;
    }

    public Kid( String value ){
        if (value.length() > 0 && value.charAt(0) == '>'){
            value = value.substring(1); //cut off first character
        }

        sequenceName = value;
        taxonID = speciesID = genusID = -1;
        Accession = "";
        AccessionVersion = "";
        transcriptome = false;
    }

    private static final long serialVersionUID = 1003003L;

//    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
//        stream.writeObject(sequenceName);
//        stream.writeObject(Accession);
//        stream.writeObject(AccessionVersion);
//        stream.writeObject(taxonID);
//        stream.writeObject(speciesID);
//        stream.writeObject(genusID);
//        stream.writeObject(kingdomID);
//        stream.writeObject(transcriptome);
//    }
//
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        sequenceName = (String) stream.readObject();
//        Accession = (String) stream.readObject();
//        AccessionVersion = (String) stream.readObject();
//        taxonID = (int) stream.readObject();
//        speciesID = (int) stream.readObject();
//        genusID = (int) stream.readObject();
//        kingdomID = (int) stream.readObject();
//        transcriptome = (boolean) stream.readObject();
//    }

//    public String toString(){
//        return "\t"+sequenceName;
//    }




    @Override
    public int getWriteUnsafeSize() {
        //header
        int total = UnsafeMemory.SIZE_OF_INT + UnsafeMemory.SIZE_OF_LONG;

        //strings
        total += UnsafeMemory.getWriteUnsafeSize(sequenceName, UnsafeMemory.STRING_TYPE);
        total += UnsafeMemory.getWriteUnsafeSize(Accession, UnsafeMemory.STRING_TYPE);
        total += UnsafeMemory.getWriteUnsafeSize(AccessionVersion, UnsafeMemory.STRING_TYPE);

        //taxon ID's and boolean
        total += 4* UnsafeMemory.SIZE_OF_INT + UnsafeMemory.SIZE_OF_BOOLEAN;
        return total;
    }

    @Override
    public void writeUnsafe(UnsafeMemory um) {
//        public String sequenceName;
//        public String Accession;
//        public String AccessionVersion;
//        public int taxonID;
//        public int speciesID;
//        public int genusID;
//        public int kingdomID;
//        boolean transcriptome;

        um.putInt(getWriteUnsafeSize());
        um.putLong(UnsafeMemory.KID_UID);

        um.putString(sequenceName);
        um.putString(Accession);
        um.putString(AccessionVersion);
        um.putInt(taxonID);
        um.putInt(speciesID);
        um.putInt(genusID);
        um.putInt(kingdomID);
        um.putBoolean(transcriptome);
    }

    @Override
    public void readUnsafe(UnsafeMemory um) throws ClassCastException {
        long serial = um.getLong();
        if (serial != UnsafeMemory.KID_UID){
            throw new ClassCastException(
                    "Reading UNSAFE Kid, but found wrong serialVersionUID =\t"+serial+"\texpecting\t"+serialVersionUID
            );
        }
        sequenceName = (String) um.get(UnsafeMemory.STRING_TYPE);
        Accession = (String) um.get(UnsafeMemory.STRING_TYPE);
        AccessionVersion = (String) um.get(UnsafeMemory.STRING_TYPE);
        taxonID = (int) um.getInt();
        speciesID = (int) um.getInt();
        genusID = (int) um.getInt();
        kingdomID = (int) um.getInt();
        transcriptome = (boolean) um.getBoolean();
    }

    public static Kid unsafeMemoryBuilder(UnsafeMemory unsafeMemory) {
        Kid result = new Kid();
        result.readUnsafe(unsafeMemory);
        return result;
    }

    @Override
    public String toString(){
        String result = getSequenceName();
        result += "\t"+getAccession();
        result += "\t"+getAccessionVersion();
        result += "\t"+getTaxonID();
        result += "\t"+getSpeciesID();
        result += "\t"+getGenusID();
        result += "\t"+getKingdomID();
        return result;
    }
}
