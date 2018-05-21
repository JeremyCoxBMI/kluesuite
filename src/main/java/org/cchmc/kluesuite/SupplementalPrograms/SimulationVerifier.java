package org.cchmc.kluesuite.SupplementalPrograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.datastreams.FastaFile;
import org.cchmc.kluesuite.datastreams.FastaSequence;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.variantklue.Variant;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

import static java.lang.System.exit;

/**
 * Created by jwc on 8/3/17.
 */
public class SimulationVerifier {

    static int[] counts = new int[4];

    static {
        for (int z=0; z < 4; z++){
            counts[z] = 0;
        }
    }



    public static boolean verify(int start, int stop, String text, String sequence, KidDatabaseMemory kd, int kid){

        boolean verify = true;
        int end;
        String antwort = null;

        start -= 1;  // start coordinate is 1-index
        //end = end;   //end coordinate is 1-index  -1 to 0-index  +1 for inclusive to exclusive
        int offset;

        if (text.length() > 5) {
            ArrayList<Variant> vars = null;
            try {
                vars = Variant.buildFromText(text); //splits2[3]);
            } catch (NumberFormatException e){
                return false;
            }

            counts[vars.size()-1] += 1;

            try {
                antwort = kd.getSequence(kid, start, stop, false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            offset = 0;

            end = (vars.get(0).start - 1) - start;


//            System.err.println(antwort.substring(0, end));
//            System.err.println(sequence.substring(0 + offset, end + offset));

            //what is this for?
            if (!antwort.substring(0, end).equals(sequence.substring(0 + offset, end + offset))) {
                verify = false;
            }

            for (int k = 0; k < vars.size(); k++) {
                Variant v = vars.get(k);
                int pos = (v.start-1) - start;  //why minus 1?  because 1 indexed but both are 1-indexed
//                int pos = v.start - start;  //why minus 1?  because 1 indexed but both are 1-indexed

                switch (v.type) {
                    case SNP:
                        if (!sequence.substring(offset + pos, offset + pos + 1).equals(v.insertSequence)) {
                            verify = false;
                        }
                        offset += 0;
                        pos += 1;
                        break;
                    case INSERTION:
                        if (!sequence.substring(offset + pos, offset + pos + v.length).equals(v.insertSequence)) {
                            verify = false;
                        }
                        offset += v.length;
                        break;
                    case DELETION:
                        offset -= v.length;
                }

                //next unadulterated sequence

                if (k + 1 < vars.size()) {
                    end = (vars.get(k + 1).start - 1) - start;
                } else {
                    end = stop - start -1;
                }

                System.err.println(sequence.substring(offset + pos, end + offset));
                System.err.println(antwort.substring(pos, end));
                if (end+offset >= sequence.length() || end >= antwort.length()){
                    System.err.println("Sequence out of String bounds::\t"+(end+offset)+" >= "+sequence.length()+"\t"+end+" >= "+antwort.length());
                    verify = false;
                } else if (!sequence.substring(offset + pos, end + offset).equals(antwort.substring(pos, end))) {
                    verify = false;
                }
            }

        } else {
            try {
                antwort = kd.getSequence(kid, start, stop, false);
//                antwort = kd.getSequence(kid, start-1, end, false); // bug fix -- bad solution
            } catch (Exception e) {
                e.printStackTrace();
            }
//            System.err.println(sequence+"\n"+antwort);
            verify = sequence.equals(antwort);
        }
        return verify;
    }



    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 3) {
            System.out.println("Evaluates mutations or plan sequences as matches to database, verify they are correct.");
            System.out.println("Variants are generated using length (not maximum) determined by observed distribution ");
            System.out.println("ARG 0 : location Kid Database Memory to read");
            System.out.println("ARG 1 : FastA to test");
            System.out.println("ARG 2 : FastA of sequences not matching (output)");
            System.out.println("STDOUT: List of sequences / verified or not");
            exit(0);
        }

        int countVerified = 0;
        int countNotVerified = 0;

        Date timer = new Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.err.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        KidDatabaseMemory kd = new KidDatabaseMemory();

        FastaFile fi = new FastaFile(args[1]);
        Iterator<FastaSequence> it = fi.sequenceIterator();


        FastaSequence fs;
        String text;

//        while (it.hasNext()) {
//            fs = it.next();
//        }



        System.err.println("Loading Kid Database Memory \t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        kd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);
//        System.err.println("Loading VariantDatabase (unsafe) \t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


//        HashSet<String> hs = new HashSet<String>();
//        try(BufferedReader br = new BufferedReader(new FileReader("names.txt"))) {
//
//            int k = 0;
//            for (String line; (line = br.readLine()) != null; ) {
//                hs.addAndTrim(line.trim());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        System.err.println("Loading Kid Database Memory COMPLETE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        try {

            FileWriter fw = new FileWriter(args[2]);
            BufferedWriter writer = new BufferedWriter(fw);

            while (it.hasNext()) {
                fs = it.next();
                fs.query = fs.query.substring(1);
                if (true) {                  //if (hs.contains(fs.query)) {
                    String[] splits = fs.query.split("\\s");
                    //See _TestExperiments class
                    String[] splits2 = splits[0].split("\\|\\|");
                    int start = Integer.parseInt(splits2[1]);
                    int stop = Integer.parseInt(splits2[2]);
                    if (splits2.length < 4) {
                        text = "";
                    } else {
                        //mutation codes
                        text = splits2[3];
                    }

                    int kid = kd.getKid(splits2[0]);
                    if (verify(start, stop, text, fs.sequence.toString(), kd, kid)) {
                        System.out.println("    VERIFIED\t" + fs.query);
                        countVerified++;
                    } else {
                        System.out.println("NOT VERIFIED\t" + fs.query);
                        countNotVerified++;
                        writer.write(">"+fs.query+"\n");
                        writer.write(fs.sequence.toString()+"\n");
                    }
                }

            }
            writer.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Simulation Verified Found");
        System.out.println(countVerified+"\tVERIFIED\tout of\t"+(countVerified+countNotVerified));
        System.out.println("Mutation counts from 1 to 4 are\t"+Arrays.toString(counts));

//        while (it.hasNext()){
//            fs = it.next();
//            fs.query = fs.query.substring(1);
//            String[] splits = fs.query.split("\\s");
//            //See _TestExperiments class
//            String[] splits2 = splits[0].split("\\|\\|");
//            int start = Integer.parseInt(splits2[1]);
//            int end = Integer.parseInt(splits2[2]);
//            if (splits2.length < 4) {
//                text = "";
//            }else{
//                text = splits2[3];
//            }
//
//            int kid = kd.getKid(splits2[0]);
//            if (verify(start, end, text, fs.sequence.toString(), kd, kid)) {
//                System.err.println("    VERIFIED\t" + fs.query);
//            } else {
//                System.err.println("NOT VERIFIED\t" + fs.query);
//            }
//        }
    }

}
