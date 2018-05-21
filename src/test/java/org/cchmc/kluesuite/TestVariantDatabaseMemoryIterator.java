package org.cchmc.kluesuite;


import org.cchmc.kluesuite.variantklue.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.TreeMap;



/**
 * Created by osboxes on 01/05/17.
 */
public class TestVariantDatabaseMemoryIterator {




    @Test
    public void testIterating(){
        VariantDatabaseMemory vd = new VariantDatabaseMemory("junk");


        Variant[] v = new Variant[6];



               v[0] =  Variant.buildINSERTION(0,1,"one", "one", "one");
        vd.addIndel(1, v[0]);
                v[1] = Variant.buildINSERTION(30,1,"two", "two", "two");
        vd.addIndel(1, v[1]);
                v[2] = Variant.buildINSERTION(60,1,"three", "three", "three");
        vd.addIndel(1,v[2]);
                v[3] = Variant.buildINSERTION(90,1,"four", "four", "four");
        vd.addIndel(1, v[3]);
        v[4] = Variant.buildSNP(201,1, "C", "xray", "xray");
        vd.addSNP(1,v[4]);
                v[5] = Variant.buildDELETION(340,1,14,"last14", "last14");
        vd.addIndel(1, v[5]);



        VariantDatabaseMemoryIterator it = vd.iterator(1);

        System.out.println("RESULT\t\t\t\t\tEXPECTED");
        for (int k=0; it.hasNext(); k++){
            Variant var = it.next();
            System.out.println(var+"\t"+v[k]);
            Assert.assertTrue(var.equals(v[k]));
        }

    }



//    public static void main(String[] args) {
//
//        Variant[] arr1 = new Variant[2];
//        Variant[] arr2 = new Variant[1];
//
//        arr1[0] = Variant.buildSNP(4, 1, "C", "multithread", "baconator");
//        arr1[1] = Variant.buildSNP(64, 1, "C", "bacon2", "baconator2");
//        arr2[0] = Variant.buildDELETION(105, 1, 2, "byebye", "bye-bye-bye");
//
//        TreeMap<Integer, Variant[]> treeInd = new TreeMap<Integer, Variant[]>();
//        TreeMap<Integer, Variant[]> treeSnp = new TreeMap<Integer, Variant[]>();
//
//        treeInd.put(4, arr1);
//        treeInd.put(64, arr1);
//        treeSnp.put(105, arr2);
//
//
//        MyVariantIterator it =
//                new MyVariantIterator(arr1);
//
//
//        Variant v = null;
//        while (it.hasNext()) {
//            v = it.next();
//            System.out.println(v);
//
//        }
//
//        System.out.println();
//        it =
//                new MyVariantIterator(arr2);
//
//        while (it.hasNext()) {
//            v = it.next();
//            System.out.println(v);
//        }
//
//        System.out.println();
//        VariantDatabaseTreeMapIterator it2 = new VariantDatabaseTreeMapIterator(treeInd);
//
//        while (it2.hasNext()) {
//            v = it2.next();
//            System.out.println(v);
//        }
//
//        System.out.println();
//
//        VariantDatabaseMemory vd = new VariantDatabaseMemory();
//        try {
//            vd.loadFromFile("babytest.VariantDatabaseOLD");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        Set<Integer> s = vd.getKeys();
//
//        int count = 0;
//        VariantDatabaseIterator<Variant> it4 = new VariantDatabaseMemoryIterator(1,vd);
//        for (Integer kid : s) {
//            while (it4.hasNext()){
//                System.out.println(it4.next());
//            }
//        }
//    }
}
