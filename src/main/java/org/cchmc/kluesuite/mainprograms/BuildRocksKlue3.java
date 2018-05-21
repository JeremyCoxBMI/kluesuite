package org.cchmc.kluesuite.mainprograms;

import org.apache.commons.compress.compressors.CompressorException;
import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeFileWriter;
import org.cchmc.kluesuite.binaryfiledirect.UnsafeMemory;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.builddb.DnaBitStringToDbForwardOnly;
import org.cchmc.kluesuite.builddb.KidDatabaseMemorySneaky;
import org.cchmc.kluesuite.datastreams.FileImport;
import org.cchmc.kluesuite.datastreams.Query;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.Kid;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klueforward.KlueForwardDataImport;
import org.cchmc.kluesuite.klueforward.RocksDbKLUEforward;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;

import static java.lang.System.exit;

/**
 * Created by jwc on 8/1/17.
 */
public class BuildRocksKlue3 {


    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Takes a FASTA dna fle and builds a corresponding Klue v3 K-mer database");
            System.out.println("The essential idea is to allow massive parallelization of many small databases.");
            System.out.println("\t--> Uses a master Kid Database Disk, but closes promptly.");
            System.out.println("\t--> DnaBitString written to separate file.");
            System.out.println("\t\t--> Later, we can pick and choose which sequences to use for master DB.");
            System.out.println("\t\t\t--> Then we update Kid Database Disk and K-mer database");
            System.out.println("\t--> How to avoid file collisions? Watch for updates and then start next?");
            System.out.println("ARG 0 : Fasta file");
            System.out.println("ARG 1 : Kid Database Disk file");
            System.out.println("ARG 2 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            exit(0);
        }

        long MK_SIZE = Integer.parseInt(args[2])*1000L*1000L;
        if (MK_SIZE > Integer.MAX_VALUE) {
            MK_SIZE = (long) (Integer.MAX_VALUE);
        }

        String fasta = args[0];
        String dbsFile = fasta + ".dnabitstring";
        String kmers = fasta + ".klueforward";


        TimeTotals tt = new TimeTotals();
        tt.start();
        java.util.Date timer = new java.util.Date();

        System.out.print("Synchronize time systems\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS()+"\n");

        FileImport fa = null;
        try {
            fa = new FileImport(fasta, FileImport.CompressionFormat.UNCOMPRESSED, FileImport.FileFormat.FASTA);
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        } catch (CompressorException e) {
            e.printStackTrace();
            exit(1);
        }

        System.err.println("Loading Kid Database Disk\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        KidDatabaseDisk kd  = KidDatabaseDisk.loadFromFileUnsafeWaitOnFile(args[1]);
        System.err.println("\tKid Database import complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tInitializing KLUE Disk K-mer database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(kmers,false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        HashMap<String,Integer> nameToKid = new HashMap<String, Integer>();


//        System.err.println("Starting import or KID\t" + kid + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
//
//        DnaBitStringToDb dbstd = new DnaBitStringToDb(kd.getSequence(kid),hpkfia,kid);
//        dbstd.writeAllPositions();
//
//        //dump remaining kmers in memory klue
//        //will use new file if necessary
//        hpkfia.dumpToDatabase();
//        System.err.println("KID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());





//        TODO KidDatabase waits for file availability
//        File file = new File(filename);
//        while (!file.exists()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) { /* safe to ignore */ }
//        }


        Query q = null;
        try {
            q = fa.getNextEntry();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }

        while (q != null){
            String query = q.queryName.substring(1); // cut off ">"
            kd.add(new Kid(query));
              int kid = kd.getKid(query);
            nameToKid.put(query, kid);

            try {
                q = fa.getNextEntry();
            } catch (IOException e) {
                e.printStackTrace();
                exit(1);
            }
        }

        System.err.println("\tSaving Updates Kid Database\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        kd.saveToFileUnsafe();
        System.err.println("\tKid Database closed\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE,rocksklue );

        q = null;
        try {
            q = fa.getNextEntry();
        } catch (IOException e) {
            e.printStackTrace();
            exit(1);
        }

        while (q != null){
            String query = q.queryName.substring(1); // cut off ">"

            //recall from assigned Kid
            int kid = nameToKid.get(query);

            DnaBitString dns = new DnaBitString(q.querySequence);

            try {
                UnsafeFileWriter uw = UnsafeFileWriter.unsafeFileWriterBuilder(dbsFile+"."+kid);
                UnsafeMemory um = new UnsafeMemory(dns.getWriteUnsafeSize());
                dns.writeUnsafe(um);
                uw.writeObject(um.toBytes());
                uw.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Cannot open output file "+dbsFile+"."+kid);
            }

            DnaBitStringToDbForwardOnly dbstd = new DnaBitStringToDbForwardOnly(kd.getSequence(kid),hpkfia,kid);

            //MemoryKlueHeapFastImportArray dumps to file as it proceeds as needed
            //creates multiple files
            dbstd.writeAllPositions();


            System.err.println("KID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

            try {
                q = fa.getNextEntry();
            } catch (IOException e) {
                e.printStackTrace();
                exit(1);
            }
        }
        //dump remaining kmers in memory klue
        //will use new file if necessary
        hpkfia.dumpToDatabase();

    }
}