package org.cchmc.kluesuite._oldprograms;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;
import org.cchmc.kluesuite.variantklue.Variant;
import org.cchmc.kluesuite.variantklue.VariantDatabaseMemory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.TreeMap;

import static java.lang.System.exit;

/**
 * Created by jwc on 6/2/17.
 */
public class vKLUEdatabaseBuildStep03pieces {

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Takes a UCSC variants file and builds entries in kmer database, as well as Variant database.");
            System.out.println("ARG 0 : location to KidDatabaseMemory");
            System.out.println("ARG 1 : location variant database to build (recommend 1 at a time)");
            System.out.println("ARG 2 : UCSC variant definitions file");
            System.out.println("ARG 3 : prefix for chromosome names (if used in FastA file)");
            exit(0);
        }

        java.util.Date timer = new java.util.Date();
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("Synchronize time systems \t" + new Timestamp(timer.getTime()) + "\t" + tt.toHMS());

        VariantDatabaseMemory vd = new VariantDatabaseMemory(args[1]); //  + ".vd3");
        System.err.println("Loading KidDb\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        KidDatabaseMemory rkd = KidDatabaseMemory.loadFromFileUnsafe(args[0]);

        String prefix = args[3];

//        KidDatabaseMemory rkd = new KidDatabaseMemory();
//        for (int i=1; i < 26; i++)
//            rkd.addWithTrim(new Kid("38chr"+i));


        System.err.println("Accepts only UCSC Variant database format.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tImporting VariantDatabaseOLDMemory.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());

        try {
            vd.importValues(args[2], rkd, prefix);
            System.err.println("");
            System.err.println("\t\tsnpMap has "+vd.snpMap.keySet().size()+" entries.");
            System.err.println("\t\t\tsnpMap KID list");
            for (int k : vd.snpMap.keySet()){
                System.err.println("\t\t\t"+k+"\t\tPosition entries : "+vd.snpMap.get(k).keySet().size());
            }
            System.err.println("\t\tindelMap has "+vd.indelMap.keySet().size()+" entries.");
            System.err.println("\t\t\tindelMap KID list");
            for (int k : vd.indelMap.keySet()){
                System.err.println("\t\t\t"+k+"\t\tPosition entries : "+vd.indelMap.get(k).keySet().size());
            }
//            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has "+vd.snpMap.get(1));
//            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has keys :"+vd.snpMap.get(1).keySet());
            System.err.println("\t\tDEBUG\tsnpMap::KID=1 has "+vd.snpMap.get(1).keySet().size()+" keys.");

            TreeMap<Integer, Variant[]> temp = vd.snpMap.get(1);
            Iterator<Integer> it = temp.keySet().iterator();

            Integer x = it.next();
            Integer y = temp.get(x).length;
            System.out.println("\t\t\tAt position " + x + ", there are # Variants = " + temp.get(x).length);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("\tImporting VariantDatabaseMemory complete.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tSaving VariantDatabaseMemory file.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());
        System.err.println("\tSaving VariantDatabaseMemory file : \t"+vd.getFilename());
        try {
            vd.saveToFileUnsafe();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("\tSave Variant Database3 file COMPLETE.\t"+new Timestamp(timer.getTime())+"\t"+tt.toHMS());



    } //end main


}
