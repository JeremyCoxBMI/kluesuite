package org.cchmc.kluesuite.mainprograms.old;

import org.cchmc.kluesuite.SupplementalPrograms.SimulateVariableNumVariants;
import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite._oldclasses.Permutator;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.klue.Kmer31;
import org.cchmc.kluesuite.memoryklue.MemoryKlueHeapFastImportArray;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.variantklue.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.System.exit;
import static org.cchmc.kluesuite.variantklue.mutation.DELETION;

/**
 * Created by osboxes on 24/04/17.
 *
 * REMAKE of HumanVariantDatabaseBuildStep0X  series
 * Using new classes, new strategies.  Starting over from scratch.
 *  -- using Kryo
 *  -- using memory only objects except for kmer
 *  -- not including many human sequences with "_" in name
 *
 * 1)  KidDatabaseMemory / DnaBitString database
 * 2)  Build normal K-mer database
 * 3)  Build VariantDatabaseOLD
 * 4)  Write Variants to K-mer database
 * 5)  Recompile K-mer in-order database and in-order 16 part databases
 * 6)  Per your option, convert
 *              KidDatabaseMemory and VariantDatabaseOLD to disk-based options
 * 7) Update KidDatabaseMemory with detailed entries (optional)
 *
 * Step 04B :: writes to memory klue before dumping to disk
 *
 *
 * This program writes variant combos of only length args[3]
 * if this number is 2, all singleton variants are overlooked.
 */



public class Step04BuildVariantKmerDatabasePiecemail {


    public static SimulateVariableNumVariants.VariantsStruct processVariants(ArrayList<Variant> variants, DnaBitString dns){

        int refStart = variants.get(0).start - WHISKER_SIZE;   //INCLUSIVE start
        if (refStart < 0) refStart = 0;
        int refStop = 0; //variants.get(0).start + WHISKER_SIZE;

        //We need to keep track of how multiple variants affect the end by position,  and also by offset
        int endPos = variants.get(0).start;
        int offset = 0;


        if (variants.get(0).type == DELETION) {
            //refStop is the look up length, impacts length of reference sequences, others do no
            // refStop += variants.get(0).length;
            if (variants.size() >= 2  &&
                    variants.get(1).start <= variants.get(0).start+variants.get(0).length) {
                variants.get(0).length = variants.get(1).start - variants.get(0).start;
            }
            offset += variants.get(0).length;

        }

        for (int k = 1; k < variants.size(); k++) {
            if (variants.get(k).type == DELETION) {
                //refStop is the look up length, impacts length of reference sequences, others do not
                if (variants.size() > k+1  &&
                        variants.get(k+1).start <= variants.get(k).start+variants.get(k).length) {
                    variants.get(k+1).length = variants.get(k+1).start - variants.get(k).start;
                }
                offset  += variants.get(k).length;
            }
            endPos = variants.get(k).start;
        }

        refStop = endPos + offset + WHISKER_SIZE +1; //exclusive
        StringAndVariants[] writeUs = SimulateVariableNumVariants.generateSequenceAllVariants(variants, refStart, refStop, dns, variants.size());
        SimulateVariableNumVariants.VariantsStruct result = new SimulateVariableNumVariants.VariantsStruct();
        result.writeUs = writeUs;
        result.refStart = refStart;
        result.refStop = refStop;

        return result;
    }


    //guarantees that before and after  first and last variant  there are this many bases.`
    public static int WHISKER_SIZE = 50;







    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Takes an Variant Database and Kid Database and builds entries in (new) kmer database.");
            System.out.println("ARG 0 : location Kid Database Memory to read");
            System.out.println("ARG 1 : location Variant Database Memory to read");
            System.out.println("ARG 2 : variant only kmer database to be written (combine later with human genome non-varaint DB)");
            System.out.println("ARG 3 : multiple of variants written into database (only tuples of this size)");
            System.out.println("ARG 4 : range of kid to process (e.g.  1  or  8-10");
            System.out.println("ARG 5 : size of MemoryKlue (in millions) (max 2147 due to MAX_INTEGER");
            exit(0);
        }

        long MK_SIZE = Integer.parseInt(args[5])*1000L*1000L;
        if (MK_SIZE > Integer.MAX_VALUE) {
            MK_SIZE = (long) (Integer.MAX_VALUE);
        }




        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        Set<Integer> s = new HashSet<Integer>();//vd.getKeys();
        int tupleNumber=Integer.parseInt(args[3]);
        String[] range = args[4].split("-");
        if (range.length == 1){
            s.add(Integer.parseInt(range[0]));
        } else {
            for (int k = Integer.parseInt(range[0]); k <= Integer.parseInt(range[1]); k++){  //range is inclusive both ends
                s.add(k);
            }
        }




        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        KidDatabaseMemory kd  = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

//        //DEBUG BLOCK ALTERNATIVE to save MEMORY
//        KidDatabaseMemory kd = KidDatabaseMemoryNoDnaBitString.loadFromFileUnsafe(args[0]);
//
//        SuperString ss = new SuperString();
//        for (int k=0;k<1000; k++)   ss.addWithTrim("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//        kd.addWithTrim(new Kid("bogus"));
//        kd.storeSequence(1,ss,tt);
//
//        // END :: DEBUG BLOCK ALTERNATIVE FOR MEMORY

        System.err.println("\tLoading Variant Database.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        VariantDatabaseMemory vd = new VariantDatabaseMemory();
        try {
            vd.loadFromFileUnsafe(args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.err.println("\tLoad Variant Database file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());


        System.err.println("\tInitializing KLUE\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        RocksDbKlue rocksklue = new RocksDbKlue(args[2],false); // false = read and write
        System.err.println("\tKLUE Initialization complete\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray((int)MK_SIZE,rocksklue );



        Permutator.DEBUG = false;

        System.err.println("set of kid to process is : "+s);
        //int count = 0;
        for (Integer kid : s) {
            System.err.println("Starting permutations for KID\t" + kid + "\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
            System.err.println("Number Variants for KID\t" + kid + ":\t" + vd.getNumVariants(kid));
            //initialize
//            PermutatorLimitAdjacent perm = new PermutatorLimitAdjacent(hpkfia, kd, vd);
//            perm.ONLY_MAX_ADJACENT = true;
//            perm.MAX_ADJACENT = tupleNumber;

            DnaBitString dns = kd.getSequence(kid);
            VariantDatabaseMemoryIterator it = vd.iterator(kid);
//            StructPermutationStrings sps = perm.processVariants(it,kid,0);

            ArrayList<Variant> variants = new ArrayList<>();

            Variant curr;
            while ((it.hasNext())){
                curr = it.next();
                int size = variants.size();

                if (size==0){
                    variants.add(curr);
                }
                else if (size >0){  //will never have more than tupleNumber - 1, because of decrement below
                    if (variants.get(size-1).start + Kmer31.KMER_SIZE >= curr.start){
                        variants.add(curr);
                    } else {
                        variants = new ArrayList<Variant>();
                        variants.add(curr);
                    }
                }

                if (variants.size() == tupleNumber){
                    SimulateVariableNumVariants.VariantsStruct r = processVariants(variants,dns);

                    for (StringAndVariants sav : r.writeUs){
                        DnaBitStringToDb dbstd = new DnaBitStringToDb(new DnaBitString(sav.s),hpkfia,kid);
                        dbstd.writeAllPositions();
                    }
                    //iterate
                    variants.remove(0);
                }

            }



            System.err.println("KID\t" + kid + " processed\t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());
        }

        System.out.println("Start FINAL export to RocksDbKlue \t"+tt.toHMS());
        int kmersDumped = hpkfia.dumpToDatabase();


        System.err.println("Import to kmer database complete: "+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\t\ttotal variant processed  :\t"+Permutator.tempCount2);
        System.err.println("\t\tKmers dumped by HeapKlue :\t" + kmersDumped);
        System.err.println("\t\tkmer per variant dumped  :\t"+new Double(kmersDumped)/Permutator.tempCount2/2);  //forward and reverse, divide by 2

        System.err.println("\t\tkmers written to HeapKlue:\t"+Permutator.tempCount+"\t(not including reverse, otherwise double)");
        System.err.println("\t\tkmer per variant written :\t"+new Double(Permutator.tempCount)/Permutator.tempCount2);

        rocksklue.shutDown();
        System.out.println("Complete export to RocksDbKlue \t"+tt.toHMS());

    } //end main



}
