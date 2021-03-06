package org.cchmc.kluesuite;

import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.KlueDataImport;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by osboxes on 18/08/16.
 */
public class TestRocksDB {

    public static void main(String[] args) {
        int MAXFILES = 20;
        String DBlocation = "./test";
        String inFile = "./test.fna";


        RocksDbKlue klue = new RocksDbKlue(DBlocation, false, MAXFILES);

        KidDatabaseMemory myKidDB = new KidDatabaseMemory();


        try {
            myKidDB.importFNAold(inFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        KlueDataImport kdi = new KlueDataImport(klue, myKidDB);

        String skip = "";

        try {
            kdi.readFNAfileSKIPto(inFile, skip);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int kid = 1;
        int pos = 35;
        Kmer31 key;
        PositionList pl;

        try {
            kid = 2;
            pos = 35;
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, false));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" FWD : "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, true));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" REV "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);

            kid = 2;
            pos = 362;
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, false));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" FWD : "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, true));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" REV "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);


            kid = 4;
            pos = 70;
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, false));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" FWD : "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);
            key = new Kmer31(myKidDB.getSequence (kid, pos, Kmer31.KMER_SIZE+pos, true));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" REV "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);

            kid = 5;
            pos = 70;
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, false));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" FWD : "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);
            key = new Kmer31(myKidDB.getSequence(kid, pos, Kmer31.KMER_SIZE+pos, true));
            System.out.println("Kmer "+key+" looking up kid "+kid+" var "+pos+" REV "+key );
            pl = new PositionList(klue.get(key.toLong()));
            System.out.println(pl);


        } catch (Exception e) {
            e.printStackTrace();
        }



    }


}
