package org.cchmc.kluesuite.zDevelopmentPrograms;

import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.System.exit;

/**
 * Created by osboxes on 15/09/16.
 */
public class ReadRocksDbQueries {


    public static ArrayList<DnaBitString> readSequencesToMemory(String filename){
        ArrayList<DnaBitString> result = new ArrayList<DnaBitString>();
        boolean ignore = true; //do not write empty sequence to database
        boolean skipping = false;
        String currentSeq = "";

        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {

            for(String line; (line = br.readLine()) != null; ) {

                // if blank line, it does not count as new sequence
                if (line.trim().length() == 0){
                    if (!skipping) {
                        if (!ignore) {
                            result.add( new DnaBitString(currentSeq));
                        }
                    }
                    ignore = true;

                    // if line starts with ">", then it is start of a new reference sequence
                } else if( line.charAt(0) == '>'){

                    if (!ignore){
                        result.add( new DnaBitString(currentSeq));
                    }

                    // initialize next iteration
                    currentSeq ="";
                    ignore = false;

                } else {
                    if(!ignore) currentSeq += line.trim();
                }
            } //end for

            br.close();

            if (!ignore){
                result.add( new DnaBitString(currentSeq));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void makeQueries(DnaBitString s, KLUE klue){
        Iterator<Kmer31> it = s.iterator();
        Kmer31 curr;
        while (it.hasNext()){
            curr = it.next();
            if (curr != null){
                PositionList pl = new PositionList(klue.get(curr.toLong()));
                System.out.println("Kmer "+curr);
                System.out.println(pl);
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Proper syntax is ' java -cp classpath/kluesuite.jar -Xmx[arg3]m [program] [arg1 : input queries filename] [arg2 : DB location]'");
            exit(0);
        }
        String file = args[0];
        String database = args[1];

        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("\n\nOpening Database :: may take minutes");
        RocksDbKlue klue = new RocksDbKlue(database, true, Settings_OLD.MAX_FILES);
        System.out.println("\tOpening Main database files took");
        System.out.println("\t"+tt.toHMS());
        System.out.println("\t"+tt.toHMSuS());

        System.out.println("\n\nBenchmark #2 :: reading query file and making random access queries");
        System.out.println("\n\tReading in sequences, converting to DnaBitStringGeneric.");
        tt.start();
        ArrayList<DnaBitString> seqs = readSequencesToMemory(file);
        System.out.println("\tReading sequences from file took");
        System.out.println("\t"+tt.toHMS());
        System.out.println("\t"+tt.toHMSuS());

        System.out.println("\n\tQuerying database for sequence kmers.");
        tt.start();
        for(DnaBitString s : seqs)    {
            //System.err.println("\t"+s);
            makeQueries(s,klue);
        }
        System.out.println("\tMaking queries took");
        System.out.println("\t"+tt.toHMS());
        System.out.println("\t"+tt.toHMSuS());




//        System.out.println("\n\nBenchmark #1 :: reading the whole file by Iterator");
//
//        byte[] key, value;
//
//        long end = 1L << 58;
//        System.out.println("\tOnly reading one sixteenth of database, to " + new Kmer31(end));
//        tt.start();
//        RocksIterator it = klue.newIterator();
//        it.seekToFirst();
//        long k=0;
//        while (it.isValid()){
//            key = it.key();
//            value = it.value();
//            it.next();
//            k++;
//            if( k % (1000*1000) == 0  ){
//                long curr = RocksDbKlue.bytesToLong(key);
//                System.out.println("\t\titerated "+k/1000000+ " million "+new Kmer31(curr));
//                if (curr >= end){
//                    System.out.println("\t\tStopping Now, having passed "+new Kmer31(end));
//                    break;
//                }
//
//            }
//        }
//        System.out.println("\tIterating over 1/16 database took");
//        System.out.println("\t"+tt.toHMS());
//        System.out.println("\t"+tt.toHMSuS());


    }
}
