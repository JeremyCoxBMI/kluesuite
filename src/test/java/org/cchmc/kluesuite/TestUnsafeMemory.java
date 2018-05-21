package org.cchmc.kluesuite;

import io.netty.channel.Channel;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileReader;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileWriter;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.MyFixedBitSet;
import org.cchmc.kluesuite.variantklue.Variant;
import org.junit.Test;
import org.junit.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;


/**
 * Created by osboxes on 6/1/17.
 *
 * Official Test Cases using Assert
 * Do Not Remove
 */


public class TestUnsafeMemory {

    @Test
    public void testGetPutChar(){
        UnsafeMemory um = new UnsafeMemory(1);
        char c1 = '}';
        um.putChar(c1);
        int pos1 = um.getPos();
        um.reset();
        char c2 = um.getChar();
        Assert.assertEquals(c1,c2);
        Assert.assertEquals(pos1,um.getPos());
    }

    @Test
    public void testGetPutString(){
        String s1, s2;
        s1 = "I am a mummy!";

        UnsafeMemory um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(s1, UnsafeMemory.STRING_TYPE));

        um.putString(s1);
        int pos1 = um.getPos();
        um.reset();
        s2 = um.getString();
        Assert.assertEquals(s1,s2);
        Assert.assertEquals(pos1,um.getPos());
    }

    @Test
    public void testGetPutDnaString(){
        DnaBitString a,b;
        int pos1, byteCount;
        UnsafeMemory um;

        a = new DnaBitString("XWGT");

        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(a, UnsafeMemory.DNABITSTRING_TYPE));
        um.put(a, UnsafeMemory.DNABITSTRING_TYPE);
        pos1 = um.getPos();

        um.reset();
        byteCount = um.getInt();
        b = (DnaBitString) um.get(UnsafeMemory.DNABITSTRING_TYPE);
        Assert.assertEquals(a.toString(),b.toString());
        Assert.assertEquals(pos1,um.getPos());


        a = new DnaBitString("XATCGNWTTAC");

        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(a, UnsafeMemory.DNABITSTRING_TYPE));
        um.put(a, UnsafeMemory.DNABITSTRING_TYPE);
        pos1 = um.getPos();

        um.reset();
        byteCount = um.getInt();
        b = (DnaBitString) um.get(UnsafeMemory.DNABITSTRING_TYPE);
        Assert.assertEquals(a.toString(),b.toString());
        Assert.assertEquals(pos1,um.getPos());
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(a,UnsafeMemory.DNABITSTRING_TYPE));
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(b,UnsafeMemory.DNABITSTRING_TYPE));
    }


    @Test
    public void testHashMapIntegerCharacter() {
        HashMap<Integer,Character> a,b;
        int pos1, byteCount;
        UnsafeMemory um;

        a = new HashMap<Integer,Character>();
        a.put(19,'R');
        a.put(46, '}');
        a.put(15, 'z');
        a.put(0, '!');

        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(a, UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE));

        um.put(a, UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);
        pos1 = um.getPos();

        um.reset();
        byteCount = um.getInt();
        b = (HashMap<Integer,Character>) um.get(UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE);

        for(Integer k:a.keySet()){
            Assert.assertTrue(b.containsKey(k));
            Assert.assertEquals(a.get(k),b.get(k));
        }
        Assert.assertEquals(pos1,um.getPos());
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(a,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE));
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(b,UnsafeMemory.HASHMAP_INTEGER_CHARACTER_TYPE));

    }

    @Test
    public void testPutGetKid(){
        Kid a,b;
        int pos1, byteCount;
        UnsafeMemory um;



        a = new Kid("Sparky");
        a.setAccessionVersion("BID.19L");
        a.setGenusID(400);
        a.setKingdomID(2900);
        System.out.println(a);

        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(a, UnsafeMemory.KID_TYPE));
        um.put(a,UnsafeMemory.KID_TYPE);
        pos1 = um.getPos();
        um.reset();
        byteCount = um.getInt();
        b = (Kid) um.get(UnsafeMemory.KID_TYPE);
        System.out.println(b);
        Assert.assertEquals(a.toString(),b.toString());

        Assert.assertEquals(pos1,um.getPos());
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(a,UnsafeMemory.KID_TYPE));
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(b,UnsafeMemory.KID_TYPE));
    }


////    public static final long[] ARRAYLIST_DNABITSTRING_TYPE = new long[]{ARRAYLIST_UID,DnaBitString.serialVersionUID};
////    public static final long[] ARRAYLIST_STRING_TYPE = new long[]{ARRAYLIST_UID, STRING_UID};
////    public static final long[] ARRAYLIST_KID_TYPE = new long[]{ARRAYLIST_UID,KID_UID};
////    public static final long[] MYFIXEDBITSET_TYPE = new long[]{MyFixedBitSet.serialVersionUID};

    @Test
    public void testPutGetArrayListDBS(){
        DnaBitString a,b,c,d;
        int pos1, byteCount;
        UnsafeMemory um, um2;

        a = new DnaBitString("ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGZ");
        b = new DnaBitString("ATCGATCNGATCGAWT");

        ArrayList al = new ArrayList<DnaBitString>();
        al.add(a);
        al.add(b);
        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(al, UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE));
        um.put(al,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);
        pos1=um.getPos();
        um.reset();

        byteCount = um.getInt();

        ArrayList bl = (ArrayList<DnaBitString>) um.get(UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);

        Assert.assertEquals(al.get(0).toString(),bl.get(0).toString());
        Assert.assertEquals(al.get(1).toString(),bl.get(1).toString());

        Assert.assertEquals(pos1,um.getPos());
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(al,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE));
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(bl,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE));
    }

    public void testPutGetArrayListString(){
        String a,b,c,d;
        int pos1, byteCount;
        UnsafeMemory um, um2;

        a = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGZ";
        b = "ATCGATCNGATCGAWT";

        ArrayList al = new ArrayList<String>();
        al.add(a);
        al.add(b);
        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(al, UnsafeMemory.STRING_TYPE));
        um.put(al,UnsafeMemory.ARRAYLIST_STRING_TYPE);
        pos1=um.getPos();
        um.reset();

        byteCount = um.getInt();

        ArrayList bl = (ArrayList<String>) um.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);

        Assert.assertEquals(al.get(0),bl.get(0));
        Assert.assertEquals(al.get(1),bl.get(1));


        Assert.assertEquals(pos1,um.getPos());
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(al,UnsafeMemory.ARRAYLIST_STRING_TYPE));
        Assert.assertEquals(pos1,UnsafeMemory.getWriteUnsafeSize(bl,UnsafeMemory.ARRAYLIST_STRING_TYPE));
    }

    public void testPutGetArrayListKid() {
        Kid a, b, c, d;
        int pos1, byteCount;
        UnsafeMemory um, um2;


        a = new Kid("Sparky");
        a.setAccessionVersion("BID.19L");
        a.setGenusID(400);
        a.setKingdomID(2900);
        b = new Kid("Hadoopy");
        b.setAccessionVersion("Wanamaker");
        b.setGenusID(600);
        b.setKingdomID(2970);

        ArrayList al = new ArrayList<Kid>();
        al.add(a);
        al.add(b);
        um = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(al, UnsafeMemory.ARRAYLIST_KID_TYPE));
        um.put(al, UnsafeMemory.ARRAYLIST_KID_TYPE);
        pos1 = um.getPos();
        um.reset();

        String a2, b2, c2, d2;
        a2 = "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGZ";
        b2 = "ATCGATCNGATCGAWT";

        ArrayList al2 = new ArrayList<String>();
        al.add(a2);
        al.add(b2);
        um2 = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(al2, UnsafeMemory.STRING_TYPE));
        um2.put(al2, UnsafeMemory.ARRAYLIST_STRING_TYPE);
        int pos2 = um.getPos();
        um2.reset();

        byteCount = um.getInt();

        ArrayList bl = (ArrayList<DnaBitString>) um.get(UnsafeMemory.ARRAYLIST_KID_TYPE);

        Assert.assertEquals(al.get(0).toString(), bl.get(0).toString());
        Assert.assertEquals(al.get(1).toString(), bl.get(1).toString());

        Assert.assertEquals(pos1, um.getPos());
        Assert.assertEquals(pos1, UnsafeMemory.getWriteUnsafeSize(al, UnsafeMemory.ARRAYLIST_KID_TYPE));
        Assert.assertEquals(pos1, UnsafeMemory.getWriteUnsafeSize(bl, UnsafeMemory.ARRAYLIST_KID_TYPE));


        byteCount = um2.getInt();

        ArrayList bl2 = (ArrayList<String>) um2.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);

        Assert.assertEquals(al2.get(0), bl2.get(0));
        Assert.assertEquals(al2.get(1), bl2.get(1));
    }

    @Test
    public void testFileWriterReader(){

        final String file = "/data/1/nvme/test.delete.bin";


        UnsafeFileWriter ufw = null;
        try {
            ufw = UnsafeFileWriter.unsafeFileWriterBuilder(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DnaBitString a,b,c,d;
        int pos1, byteCount;
        UnsafeMemory um, um2;

        a = new DnaBitString("ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGZ");
        b = new DnaBitString("ATCGATCNGATCGAWT");

        ArrayList al = new ArrayList<DnaBitString>();
        ArrayList bl = new ArrayList<DnaBitString>();
        al.add(a);
        al.add(b);
        um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(al, UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE));
        um.put(al,UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);

        try {
            ufw.writeObject(um.toBytes());
            ufw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        um.reset();

        try {
            UnsafeFileReader ufr = UnsafeFileReader.unsafeFileReaderBuilder(file);
            UnsafeMemory read = new UnsafeMemory(ufr.readNextObject());
            bl = (ArrayList<DnaBitString>) read.get(UnsafeMemory.ARRAYLIST_DNABITSTRING_TYPE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(al.get(0).toString(),bl.get(0).toString());
        Assert.assertEquals(al.get(1).toString(),bl.get(1).toString());


    }

    public void testGetPutStringArrayList(){
        String s1, s2, s3, s4;
        s1 = "I am a mummy!";
        s2 = "No, you are.  Mom!";

        ArrayList<String> test = new ArrayList<String>();

        test.add(s1);
        test.add(s2);

        UnsafeMemory um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(test, UnsafeMemory.ARRAYLIST_STRING_TYPE));

        um.put(test,UnsafeMemory.ARRAYLIST_STRING_TYPE);
        int pos1 = um.getPos();
        um.reset();

        ArrayList<String> test2 = (ArrayList<String>) um.get(UnsafeMemory.ARRAYLIST_STRING_TYPE);


        Assert.assertEquals(test.get(1),s2);
        Assert.assertEquals(test.get(0),s1);
        Assert.assertEquals(pos1,um.getPos());
    }

    @Test
    public void testPutGetVariant(){
        Variant v1, v2;

        v1 = Variant.buildDELETION(5, 146, 1, "rsJWC123", "<6|G/->");
        UnsafeMemory um = new UnsafeMemory( UnsafeMemory.getWriteUnsafeSize(v1, UnsafeMemory.VARIANT_TYPE));
        um.put(v1,UnsafeMemory.VARIANT_TYPE);


        int pos1 = um.getPos();
        um.reset();

        int x = um.getInt();  //burn size indicator

        v2 = (Variant) um.get(UnsafeMemory.VARIANT_TYPE);

        Assert.assertEquals(v1.insertSequence,v2.insertSequence);
        Assert.assertEquals(v1.detailedName,v2.detailedName);
        Assert.assertEquals(v1.name,v2.name);
        Assert.assertEquals(v1.KID,v2.KID);
        Assert.assertEquals(v1.length,v2.length);
        Assert.assertEquals(v1.start,v2.start);
        Assert.assertEquals(v1.type.getValue(),v2.type.getValue());
        Assert.assertEquals(pos1,um.getPos());
    }


    @Test
    public void testPutGetTreeMap(){
        //todo
        TreeMap<Integer, Variant[]> tree1 = new TreeMap<Integer, Variant[]>();
        int KID = 4;
        Variant[] vd = new Variant[4];

        vd[0]=    Variant.buildDELETION(5, KID, 1, "rsJWC123", "<6|G/->");
        vd[1]=    Variant.buildINSERTION(15, KID, "aaa", "rsJWC456", "<16|-/AAA>");
        vd[2]=    Variant.buildSNP(75, KID, "W", "rsJWC1988", "<76|A/T>");
        vd[3]=    Variant.buildSNP(417, KID, "B", "rsJWCclarity", "<418|C/T,G>");




        tree1.put(19,vd);

        UnsafeMemory um = new UnsafeMemory(UnsafeMemory.getWriteUnsafeSize(tree1, UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE));
        um.put(tree1, UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);

        um.reset();
        int temp = um.getInt();  //burn block size

        TreeMap<Integer, Variant[]> tree2 = (TreeMap<Integer, Variant[]>) um.get(UnsafeMemory.TREEMAP_INTEGER_ARRAY_VARIANT_TYPE);

        Assert.assertEquals(1,1);
    }

}

//public static final long[] STRING_TYPE = new long[]{STRING_UID};  X
//public static final long[] KID_TYPE = new long[]{KID_UID};    X
//public static final long[] CHAR_TYPE = new long[]{CHAR_UID};  X
//public static final long[] HASHMAP_INTEGER_CHARACTER_TYPE = new long[]{HASHMAP_UID, INT_UID, CHAR_UID};  X
//public static final long[] ARRAYLIST_DNABITSTRING_TYPE = new long[]{ARRAYLIST_UID,DnaBitString.serialVersionUID}; X
//public static final long[] ARRAYLIST_STRING_TYPE = new long[]{ARRAYLIST_UID, STRING_UID}; X
//public static final long[] ARRAYLIST_KID_TYPE = new long[]{ARRAYLIST_UID,KID_UID}; X
    //Tested via ARRAYLIST_DNABITSTRING_TYPE case
//public static final long[] MYFIXEDBITSET_TYPE = new long[]{MyFixedBitSet.serialVersionUID}; X
//public static final long[] DNABITSTRING_TYPE = new long[]{DnaBitString.serialVersionUID}; X