package org.cchmc.kluesuite.klat2;

import org.cchmc.kluesuite.klat.PartialAlignment;
import org.cchmc.kluesuite.klue.SuperString;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabase;
import org.cchmc.kluesuite.klue.kiddatabase.KidDatabaseAllDisk;

import java.util.ArrayList;

/**
 * Created by COX1KB on 4/26/2018.
 */
public interface KLAT {

    //take input of KLUE database, KidDatabase and query

    //calculations separate from constructor for parallelism, especially so far as pollKmers is concerned
    public ArrayList<PartialAlignment> calculateFullAlignments();

    //results
    public ArrayList<PartialAlignment> getBestAlignments();

    //results as text
    public SuperString alignmentsToBlast6();



}
