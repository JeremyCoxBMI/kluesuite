package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.zip.DataFormatException;

import static java.lang.System.exit;

/**
 * Created by osboxes on 22/09/16.
 */
public class BuildRocksDbKidDbPart {

    static protected int importCount = 0;
    static protected int totalLength = 0;
    static protected int period = 100;

    static int lowKid = Integer.MAX_VALUE;
    static int highKid = 0;


    static KidDatabaseMemory original;
    static RocksDB db = null;

    public static void importFNA(String filename) throws FileNotFoundException {
        int currentKID = -1;
        String currentSeq = "";
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        //skipping is holdover from copying code.  Here, it does nothing.
        boolean skipping = false;
        boolean debug = false;

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
                        System.err.println("           :: new entry detected  "+line);
                    }
                    // save previous iteration to database

                    if (!skipping) {
                        if (!ignore) {
                            storeSequence(currentKID, currentSeq);
                        }

                        // initialize next iteration

                        currentKID = original.indexOf(line.trim());
                        if (currentKID == -1) {
                            System.err.println("This sequence not found in database : " + line);
                            exit(0);
                        }
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

    public static void importFNASuperStringFast(String filename) throws FileNotFoundException {
        int currentKID = -1;
        SuperString currentSeq = new SuperString();
        //String currentName = "";
        boolean ignore = true; //do not write empty sequence to database

        //skipping is holdover from copying code.  Here, it does nothing.
        boolean skipping = false;
        boolean debug = false;

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
                        System.err.println("           :: new entry detected  "+line);
                    }
                    // save previous iteration to database

                    if (!skipping) {
                        if (!ignore) {
                            storeSequence(currentKID, currentSeq);
                        }

                        // initialize next iteration

                        currentKID = original.indexOf(line.trim());
                        if (currentKID == -1) {
                            System.err.println("This sequence not found in database : " + line);
                            exit(0);
                        }
                        //currentSeq = "";

//                        if (lowKid == 0){
//                            lowKid = currentKID;
//                        } else {
//                            lowKid = Math.min(currentKID, lowKid);
//                        }
//                        if (highKid == Integer.MAX_VALUE){
//                            highKid = currentKID;
//                        } else {
//                            highKid = Math.max(currentKID, highKid);
//                        }
                        lowKid = Math.min(currentKID, lowKid);
                        highKid = Math.max(currentKID, highKid);


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
                storeSequence(currentKID, currentSeq);
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void storeSequence(int index, String seq) {
        java.util.Date timer = new java.util.Date();

        DnaBitString buddy = new DnaBitString(seq);

        totalLength += buddy.getLength();
        importCount++;
        if (importCount % period == 0)  System.err.println("Seq imported "+importCount+" totalLength "+totalLength+" time "+ new Timestamp(timer.getTime()));

        byte[] key = RocksDbKlue.longToBytes((long) index);
        byte[] value = buddy.toByteArray();
        try {
            db.put(key, value);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }


    public static void storeSequence(int index, SuperString seq) {
        java.util.Date timer = new java.util.Date();

        DnaBitString buddy = null;
        try {
            buddy = new DnaBitString(seq);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

        totalLength += buddy.getLength();
        importCount++;
        if (importCount % period == 0)  System.err.println("Seq imported "+importCount+" totalLength "+totalLength+" time "+ new Timestamp(timer.getTime()));

        byte[] key = RocksDbKlue.longToBytes((long) index);
        byte[] value = buddy.toByteArray();
        try {
            db.put(key, value);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Arguments : <KidDatabaseMemory location>  <FNA file> <DB name>");
            exit(0);
        }

        String KidDBloc = args[0];
        String FNAfile = args[1];
        String DBname = args[2];

        original = KidDatabaseMemory.loadFromFileUnsafe(KidDBloc);

        Options options = new Options().setCreateIfMissing(true).setMaxOpenFiles(Settings_OLD.MAX_FILES);

        TimeTotals tt = new TimeTotals();


        try {
            db = RocksDB.open(options, DBname);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }

        tt.start();
        System.out.println("\n\nBegin File Import\t"+tt.toHMS()+"\n");
        try {
            //importFNAold(FNAfile);
            importFNASuperStringFast(FNAfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Import Complete\t"+tt.toHMS()+"\n");


        System.out.println("Verifying Contents Written");
        for (int k=lowKid; k<= highKid; k++){
            byte[] key = RocksDbKlue.longToBytes( (long) k );
            byte[] value;
            try {
                value = db.get(key);
            } catch (RocksDBException e) {
                value = null;
            }
            DnaBitString dns = new DnaBitString(value);

            System.out.println(k+"\t"+original.getName(k)+"\t"+dns.getLength()+"\t"+dns.getSequence(0,40));
        }

        System.out.println("\nTotal Time\t"+tt.toHMS()+"\n");

    }
}
