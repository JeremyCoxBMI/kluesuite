//package org.cchmc.kluesuite.builddb;
//
//import org.cchmc.kluesuite.TimeTotals;
//import org.cchmc.kluesuite.memoryklue.KVpair;
//import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
//
//import org.cchmc.kluesuite.klue.*;
//import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.zip.DataFormatException;
//
//import static java.lang.System.exit;
//
///**
// * Created by jwc on 5/29/17.
// */
//public class FastaToRocksKluePieces {
//
//
//    RocksDbKlue rocksklue;
//    KidDatabase kdndbs;
//    public static boolean debug = false;
//    int arraySize;
//    MemoryKlueHeapFastImportArray hklue;
//    String outFileName;
//
//    public FastaToRocksKluePieces(KidDatabaseDisk kdndbs, int arrSize){
//        //this.rocksklue = klue;
//        this.kdndbs = kdndbs;
//        this.arraySize = arrSize;
//        this.hklue = new MemoryKlueHeapFastImportArray(arraySize); //allocate once, reuse
//    }
//
//    public FastaToRocksKluePieces(KidDatabaseMemory kdndbs, int arrSize){
//        //this.rocksklue = klue;
//        this.kdndbs = kdndbs;
//        this.arraySize = arrSize;
//        this.hklue = new MemoryKlueHeapFastImportArray(arraySize); //allocate once, reuse
//    }
//
//    public void importFNA(String filename, String output) throws FileNotFoundException {
//        outFileName = output;
//        int currentKID = -1;
//        SuperString currentSeq = new SuperString();
//        //String currentName = "";
//        boolean ignore = true; //do not write empty sequence to database
//
//        //skipping is holdover from copying code.  Here, it does nothing.
//        boolean skipping = false;
//
//        TimeTotals tt = new TimeTotals();
//        tt.start();
//
//        System.out.println("\nFNA import begins " + tt.toHMS() + "\n");
//        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
//
//            for (String line; (line = br.readLine()) != null; ) {
//
//                if (debug) {
//                    System.err.println("Single line:: " + line);
//                }
//
//                // if blank line, it does not count as new sequence
//                if (line.trim().length() == 0) {
//                    if (debug) {
//                        System.err.println("           :: blank line detected  ");
//                    }
//                    if (!skipping) {
//                        if (!ignore) {
//                            storeSequence(currentKID, currentSeq, tt);
//                        }
//                    }
//                    ignore = true;
//
//                    // if line starts with ">", then it is start of a new reference sequence
//                } else if (line.charAt(0) == '>') {
//                    if (debug) {
//                        System.err.println("           :: new entry detected  " + line);
//                    }
//                    // save previous iteration to database
//
//                    if (!skipping) {
//                        if (!ignore) {
//                            storeSequence(currentKID, currentSeq, tt);
//                        }
//
//                        // initialize next iteration
//
////                        if (indexOf(line.trim()) == -1) {
////                            //original.addWithTrim(new Kid(line.trim()));
////                            //addNewKidEntry(line);
////                            addWithTrim(new Kid(line.trim()));
////                        }
//
//                        currentKID = kdndbs.getKid(line.trim()); // original.indexOf(line.trim());
//                        if (currentKID == -1) {
//                            System.err.println("This sequence not found in database : " + line);
////                            listEntries(0);
//                            exit(0);
//                        }
//
//
//                        currentSeq = new SuperString();
//
//                        ignore = false;
//                    }
//                } else {
//                    if (!skipping) {
//                        //currentSeq += line.trim();
//                        currentSeq.addWithTrim(line.trim());
//                    }
//                }
//
//            } //end for
//
//            br.close();
//
//            if (!ignore) {
//                storeSequence(currentKID, currentSeq, tt);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void storeSequence(int currentKID, SuperString currentSeq, TimeTotals tt) {
//        System.out.println("Constructing DnaBitString for kid\t" + currentKID + "\t" + tt.toHMS());
//        DnaBitStringToDb buddy = null;
//
//        System.err.println("KID\t"+currentKID+"\tIntializing HeapKlue : arraySize ::\t" + arraySize);
//        hklue.reset();
//
//        rocksklue = new RocksDbKlue(outFileName+"."+kdndbs.getName(currentKID)+"piece", false);
//
//        try {
//            System.err.println("DnaBitString construction");
//            DnaBitString tinyTim = new DnaBitString(currentSeq);
//
//            System.err.println("DnaBitStringToDb construction");
//            buddy = new DnaBitStringToDb(tinyTim, hklue, currentKID);
//            System.err.println("WriteAllPositions");
//            buddy.writeAllPositions();
//            tinyTim = null;
//            buddy = null;
//        } catch (DataFormatException e) {
//            e.printStackTrace();
//        }
//
//        System.err.println("WriteToRocks");
//        writeOutToRocks(hklue, tt);
//
//
//        rocksklue.shutDown();
////        hklue = null;
//
//    }
//
//    private void writeOutToRocks(MemoryKlueHeapFastImportArray hklue, TimeTotals tt) {
//
//                //hklue.heapify();
//          System.err.println("***DEBUG***\tlastIdx\t"+hklue.lastIdx);
////                System.err.println("\t\tHeapify completed	" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
////                System.err.println("***DEBUG***\tlastIdx\t"+hklue.lastIdx);
//
//        PositionList pl = new PositionList();
//        KVpair t, prev;
//        prev = new KVpair(-1, -1);
//
//        int k = -1;
//
//        //#hklue.resetLastToMax();
//        hklue.heapify();
//
//        while (hklue.hasNext()) {
//
//            t = hklue.remove();
//            if (t.key != prev.key) {
//                //write out last, start new position list
//
//                //short circuits on null / may not exist
//                if (pl ==null)
//                    pl = new PositionList();
//
//                //Using append in case something already written (should not be, however)
//                if (pl.length() == 100){
//                    System.err.println("PositionList >= 100 members for\t"+k+"\t"+new Kmer31(prev.key));
//                    System.err.println(Arrays.toString(pl.toLongArray()));
//                }
//
//                rocksklue.append(prev.key, pl);
//                k++;
//                if (k % (50 * 1000 * 1000) == 0) {
//                    System.out.println("\t\tWriting k-mer " + k / 1000 / 1000 + " million\t" + "\t" + tt.toHMS());
//                }
//                pl = new PositionList(t.value);
//            } else {
//                //else, continuing to write values for same key
//                pl.addWithTrim(t.value);
//            }
//
//            prev = t;
//        }
//
//
//        System.out.println("\t\tTotal k-mer written to RocksDb" + k  + "\t" + "\t" + tt.toHMS());
//
//    }
//
//
//}
