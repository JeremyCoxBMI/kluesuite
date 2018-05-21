package org.cchmc.kluesuite.multithread;

import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.util.concurrent.Callable;

/**
 * Created by jwc on 8/24/17.
 */
public class AlignmentTask implements Callable<Integer> {

    String qname, seq;
    KLUE klue;
    KidDatabaseMemory kddb;

    /**
     * This will not work unless KidDatabaseThreadSafe.ON =true, KidDatabaseThreadSafe.kd is set to a database.
     * @param queryName
     * @param sequence
     * @param k
     */
    AlignmentTask(String queryName, String sequence, KLUE k){
        qname = queryName;
        seq = sequence;
        klue = k;
    }

    /**
     * Returns 0 at completion, because something must be passed.
     * @return
     * @throws Exception
     */
    @Override
    public Integer call() throws Exception {
        System.err.println("Processing query \t" + qname);
        AlignmentKLAT1 alig = new AlignmentKLAT1(seq, qname, klue);
        System.out.println(alig.results(kddb));

        return 0;
    }
}
