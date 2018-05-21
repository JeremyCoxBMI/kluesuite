package org.cchmc.kluesuite.variantklue;

import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.util.*;

import static java.lang.System.exit;

/**
 * Created by jwc on 7/31/17.
 *
 *
 * This database stores positional information and maps it to list of Variants.
 * Use of iterator allows jumping to location in database to read entire length of reference sequence.
 *
 *
 * Positions had to be "re-cajiggered" to be in lexicographic order.
 * In next major upgrade, need to change this
 *
 * Stores all Variants in a Database based in Fisk (RocksDB)
 * Maps Kid, Poistion, to a Variant List
 * Accesses a range of positions with little time penalty, due to Lexicographic order
 *
 */


public class VariantDatabaseDisk {

    RocksDB rdb;
    String databasePath;
    Options options;

    VariantDatabaseDisk(String path, boolean readonly, int maxFiles) {

        databasePath = path;
        options = new Options().setCreateIfMissing(true).setMaxOpenFiles(maxFiles);

        try {
            if (readonly) {
                rdb = RocksDB.openReadOnly(options, databasePath);
            }else{
                rdb = RocksDB.open(options, databasePath);
            }
        } catch (RocksDBException e) {
            System.out.println("RocksDbKlue Constructor failed.");
            e.printStackTrace();
        }
    }

    public VariantDatabaseDisk(String path, boolean readonly) {
        this(path, readonly, 5);
    }


    public void put(Position p, ArrayList<Variant> variants) {
        int x = 0;
        x = 2 * UnsafeMemory.SIZE_OF_INT;  //byte size parameter, number of elements
        for (Variant v : variants) {
            x += v.getWriteUnsafeSize();
        }

        UnsafeMemory um = new UnsafeMemory(x);
        um.putInt(x);
        um.putInt(variants.size());

        for (Variant v : variants) {
            v.writeUnsafe(um);
        }

        byte[] key = RocksDbKlue.longToBytes(p.toEncodedLong());

        try {
            rdb.put(key, um.toBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Variant> get(Position p) {
        byte[] read;
        ArrayList<Variant> result = new ArrayList<Variant>();
        byte[] key = RocksDbKlue.longToBytes(p.toEncodedLong());
        try {
            read = rdb.get(key);
            UnsafeMemory um = new UnsafeMemory(read);
            int sizeHeader = um.getInt();
            int numVar = um.getInt();

            for (int k = 0; k < numVar; k++) {
                Variant v = new Variant();
                v.readUnsafe(um);
                result.add(v);
            }

            return result;
        } catch (RocksDBException e) {
            e.printStackTrace();
            exit(1);
        }
        return null;

    }

//    priv RocksIterator getIterator() {
//        return rdb.newIterator();
//    }

    public ArrayList<Variant> getVariants(int Kid, int from, int to) {
        //Position start = new Position(Kid,from);
        //Position end = new Position(Kid, to);

        byte[] start = RocksDbKlue.longToBytes(new Position(Kid,from).toEncodedLong());
        //byte[] end = RocksDbKlue.longToBytes(new Position(Kid,to).toEncodedLong());
        long stop = new Position(Kid,to).toEncodedLong();

        RocksIterator it = rdb.newIterator();
        it.seek(start);


        ArrayList<Variant> result = new ArrayList<>();

        //read key, then read value if needed
        long temp = RocksDbKlue.bytesToLong(it.key());

        // temp < end
        while(temp < stop) {
            ArrayList<Variant> var = new ArrayList<>();

            byte[] read = it.value();
            UnsafeMemory um = new UnsafeMemory(read);
            int sizeHeader = um.getInt();
            int numVar = um.getInt();

            for (int k = 0; k < numVar; k++) {
                Variant v = new Variant();
                v.readUnsafe(um);
                var.add(v);
            }

            result.addAll(var);

            //reset
            it.next();
            temp = RocksDbKlue.bytesToLong(it.key());
        }
        return result;
    }



    public static void main(String[] args) {
//        if (args.length != 4) {
//
//            System.err.println("ARG 0 : KidDatabaseMemory numbers load file");
//            System.err.println("ARG 1 : input FA file");
//            System.err.println("ARG 2 : Variant Database (Memory version)");
//            System.err.println("ARG 3,4,+ : kmer databases to open");
//            System.err.println("Program writes to STDOUT");
//            exit(0);
//        }

        VariantDatabaseDisk vdr = new VariantDatabaseDisk("deleteme.temp", false);

        Position p = new Position(8, 144);
        p.setFlag(62,true);

        ArrayList<Variant> av = new ArrayList<Variant>();

        av.add(Variant.buildDELETION(144, 8, 3, "delete3", "delete3"));
        av.add(Variant.buildINSERTION(144,8,"AAA","insertAAA","insertAAA"));
        av.add(Variant.buildSNP(144,8,"C","snpC","snpC"));


        System.out.println("Testing Variant encoding/decoding");
        UnsafeMemory um = new UnsafeMemory(av.get(0).getWriteUnsafeSize());
        //um.put(av.get(0), VARIANT_TYPE);
        av.get(0).writeUnsafe(um);

        Variant n1 = new Variant();
        um.reset();
        n1.readUnsafe(um);
        System.out.println("BEFORE\t"+av.get(0));
        System.out.println("AFTER\t"+n1);


        System.out.println("Position "+p);
        System.out.println("Variants before ");
        System.out.println(av);


        System.out.println("Testing Position encoding");
        System.out.println("Before\t"+p);
        System.out.println("encoded\t"+p.toEncodedLong());
        System.out.println("decoded\t"+Position.postionBuilder(p.toEncodedLong()));


        System.out.println("Testing Lexicographic Order");
        Position p2 = new Position(4, 144);
        Position p3 = new Position(8, 154);
        System.out.println("Expect p < p3");
        System.out.println("p : "+p.toEncodedLong());
        System.out.println("p3 : "+p3.toEncodedLong());
        System.out.println("p < p3 : "+ (p.toEncodedLong() < p3.toEncodedLong()));

        System.out.println("Expect p2 << p3");
        System.out.println("p2 : "+p2.toEncodedLong());
        System.out.println("p3 : "+p3.toEncodedLong());
        System.out.println("p2 < p3 : "+ (p2.toEncodedLong() < p3.toEncodedLong()));



        vdr.put(p,av);

        ArrayList<Variant> av2 = vdr.get(p);


        System.out.println("Variants after ");
        System.out.println(av2);


        Position p1 = new Position(4, 144);
        Variant c1 = Variant.buildDELETION(144,4,4,"d4","d4");

        av2 = new ArrayList<Variant>();
        av2.add(c1);
        vdr.put(p1,av2);


        p1 = new Position(8, 154);
        c1 = Variant.buildDELETION(154,8,2,"d2_8","d2_8");

        av2 = new ArrayList<Variant>();
        av2.add(c1);
        vdr.put(p1,av2);


        av2 = vdr.getVariants(8, 120, 140);
        System.out.println("Expect no Variants");
        System.out.println(av2);


        av2 = vdr.getVariants(8, 120, 145);
        System.out.println("Expect three Variants, KID 8 only");
        System.out.println(av2);


        av2 = vdr.getVariants(8, 120, 165);
        System.out.println("Expect four Variants, KID 8 only, 3@144, 1@ 154");
        System.out.println(av2);


    }


}

