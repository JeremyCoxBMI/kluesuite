package org.cchmc.kluesuite.variantklue;

import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeSerializable;
import org.cchmc.kluesuite.klue.Position;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.cchmc.kluesuite.variantklue.mutation.*;

;

public class Variant implements Comparable<Variant>, Comparator<Variant>, UnsafeSerializable {

    /**
     * Determines if detailed name is included in the name output
     */
    public static boolean DETAILED_NAMES = true;

    /**
     * the type of mutation (insertion or deletion)
     */
    public mutation type;

    /**
     * length of mutation
     * deletion: number bases deleted
     * insertion: number bases inserted
     */
    public int length;

    /**
     * the position where the mutation begins.  Note that this interpretation depends on mutation.
     *  SNP start is the position of the base
     *  DELETION start is the ref sequence position where the deletion begins (i.e. first base deleted)
     *  INSERTION start is the ref sequence position where the insert is placed; the normal base in this position is
     *  pushed back
     */
    public int start;

    /**
     * corresponding KID; each indel belongs to a reference sequence
     */
    public int KID;

    /**
     * FASTA formatted sequence values for SNP (length 1) or INSERT (variable length)
     * Note that NO VALUE is indicated by null, not empty String.
     */
    public String insertSequence;

    /**
     * UCSC database names all variants with "rs" number
     */
    public String name;

    /**
     * Contains the details (position, mutation substitution) encoded as a string
     */
    public String detailedName;

    public boolean isReverse = false;

    /**
     *
     * @param position
     * @param KID
     * @param value     only first letter in the string will be used as a SNP; see DNAcodes for SNP values
     * @return
     */
    public static Variant buildSNP(int position, int KID, String value, String name, String detailedName){
        return new Variant(SNP, 1, position, KID, value, name, detailedName);
    }

    public static Variant buildDELETION(int position, int KID, int length, String name, String detailedName){
        return new Variant(DELETION, length, position, KID, null, name, detailedName);
    }

    public static Variant buildINSERTION(int position, int KID, String insert, String name, String detailedName){
        return new Variant(INSERTION, insert.length(), position, KID, insert, name, detailedName);
    }

    public static ArrayList<Variant> buildFromText(String text) throws NumberFormatException {
        ArrayList<Variant> result = new ArrayList<Variant>();


        String[] variants;
        variants = text.split(",");
//        System.err.println(Arrays.toString(variants));

        for (int k=0; k < variants.length; k++) {

            Variant v = new Variant();
            v.detailedName = variants[k];

            //Parse mutations

            //split at mutation type
            String[] splitBY2 = variants[k].split("\\[");

            v.name = splitBY2[0];
            //mutation code
            Character type = splitBY2[1].charAt(0);

            //get <> mutation code
            String[] splitBingo = splitBY2[1].substring(3, splitBY2[1].length() - 1).split("\\|");
            v.start = Integer.parseInt(splitBingo[0]);

            //split on / ==> multiple slashes will be detected
            String[] countSlasher = splitBingo[1].split("\\/");

            //TODO  sloppy bug work around  ISSUE #58
            if (countSlasher.length == 3) {
                //TODO testing what to do
                v.insertSequence = countSlasher[1];
                if (false)  throw new NumberFormatException("Goofy ass ambiguous bug:  "+splitBingo[1]);
            } else {
                v.insertSequence = countSlasher[1];
            }
            v.length = v.insertSequence.length();

            switch(type) {
                case 'S':
                    v.type = SNP;
                    break;
                case 'D':
                    v.type = DELETION;
                    //v.length = countSlasher[0].length();
                    break;
                case 'I':
                    v.type = INSERTION;
                    break;
            }
            result.add(v);
        }

        return result;
    }


    Variant(){
        this.type = NONE;
        this.length = 0;
        this.start = 0;
        this.KID = 0;
        this.insertSequence = null;
        this.name = null;
        this.detailedName = null;
    }

    Variant(mutation t, int length, int start, int KID, String insert, String name){
        this.type = t;
        this.length = length;
        this.start = start;
        this.KID = KID;
        this.insertSequence = insert;
        this.name = name;
        this.detailedName = "";
    }

//    public static Variant buildSNP(int position, int KID, String value, String name, String detailedName){
//        return new Variant(SNP, 1, position, KID, value, name, detailedName);
//    }

    public Variant(mutation t, int length, int start, int KID, String insert, String name, String detailedName){
        this.type = t;
        this.length = length;
        this.start = start;
        this.KID = KID;
        this.insertSequence = insert;
        this.name = name;
        this.detailedName = detailedName;
    }

    @Override
    public String toString() {
        return type+"  \tk "+KID+"  \ts "+start+"  \tl "+length+"  \t "+insertSequence;
    }

    @Override
    public int compare(Variant o1, Variant o2) {
        return o1.compareTo(o2);
    }

    @Override
    public int compareTo(Variant o) {
        return (this.start - o.start);
    }

    private static final long serialVersionUID = 1015001L;

//    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
//        stream.writeObject(type);
//        stream.writeObject(start);
//        stream.writeObject(length);
//        stream.writeObject(KID);
//        stream.writeObject(insertSequence);
//    }
//
//    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        type = (mutation) stream.readObject();
//        start = (int) stream.readObject();
//        length = (int) stream.readObject();
//        KID = (int) stream.readObject();
//        insertSequence = (String) stream.readObject();
//    }


    public static String variantNameList( List<Variant> vars){
        String result = "";
        boolean first = true;
        for (int k=0; k< vars.size(); k++){
            result += vars.get(k).name;
            switch (vars.get(k).type) {
                case SNP:
                    result+="|S|";
                    break;
                case INSERTION:
                    result+="|I|";
                    break;
                case DELETION:
                    result+="|D|";
                    break;
            }
            if(DETAILED_NAMES){
                result += vars.get(k).detailedName;
            }
            if (k < vars.size()-1){
                result+= ",";
            }
        }
        return result;
    }

    /**
     *
     * Only list variants contained in List<Integer>
     * @param vars
     * @return
     */
    public static String variantNameList( List<Variant> vars, List<Integer> approved){
        String result = "";
        boolean first = true;
        int count=0;
        for (int k=0; k< vars.size(); k++){

            if (approved.indexOf(k) > -1) {
                result += vars.get(k).name;
                count++;
                switch (vars.get(k).type) {
                    case SNP:
                        result += "[S]";
                        break;
                    case INSERTION:
                        result += "[I]";
                        break;
                    case DELETION:
                        result += "[D]";
                        break;
                }
                if (DETAILED_NAMES) {
                    result += vars.get(k).detailedName;
                }
                if (count < approved.size()) {
                    result += ",";
                }

            } //if not null
        }
        //I have no idea where extra comma comes from...Variant
        while(result.length()!=0 && result.charAt(result.length()-1) == ','){
            result = result.substring(0,result.length()-2);
        }
        return result;
    }



    public static String detailedVariantName(String start, String observed){
        return '<' +start+'|'+observed+'>';
    }

    public boolean equals(Variant v){
        boolean result = true;
        if (v.type != type)
            result = false;
        else if (v.start != start)
            result = false;
        else if (v.length != length)
            result = false;
        else if (v.KID != KID)
            result = false;
        return result;
    }

    @Override
    public int getWriteUnsafeSize() {
        //5 int, 1 long, 3 strings
        int total = 0;

        total = 5 * UnsafeMemory.SIZE_OF_INT + 1 * UnsafeMemory.SIZE_OF_LONG;

        total += UnsafeMemory.getWriteUnsafeSize(insertSequence, UnsafeMemory.STRING_TYPE);
        total += UnsafeMemory.getWriteUnsafeSize(name,           UnsafeMemory.STRING_TYPE);
        total += UnsafeMemory.getWriteUnsafeSize(detailedName,   UnsafeMemory.STRING_TYPE);

        return total;
    }

    @Override
    public void writeUnsafe(UnsafeMemory um) {
        um.putInt(getWriteUnsafeSize());
        um.putLong(serialVersionUID);
        um.putMutation(type);
        um.putInt(length);
        um.putInt(start);
        um.putInt(KID);
        um.putString(insertSequence);
        um.putString(name);
        um.putString(detailedName);
    }

    @Override
    public void readUnsafe(UnsafeMemory um) throws ClassCastException {
        int numBytes = um.getInt();
        long serial = um.getLong();
        type = um.getMutation();
        length = um.getInt();
        start = um.getInt();
        KID = um.getInt();
        insertSequence = um.getString();
        name = um.getString();
        detailedName = um.getString();
    }


    public static Variant checkVariantInVariantDatabaseAndModify(Variant v, VariantDatabaseDisk vd) {
        Position p = new Position(v.KID,v.start);
        ArrayList<Variant> lookup = vd.get(p);
        if (lookup != null) {
            for (Variant x : lookup) {
                if (v.equals(x)) {
                    v = x;
                    break;
                }
            }
        }
        return v;
    }


}

