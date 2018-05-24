package org.cchmc.kluesuite.multithread;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.PositionList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by COX1KB on 3/24/2018.
 *
 * WHY NOT USING ASYNC KLUE
 */
public class MultiThreadKlue {
    /**
     * Starting pool size
     */
    public static int CORE_POOL_SIZE = 10;

    /**
     * Maximum size of pool
     */
    public static int MAX_POOL_SIZE = 20;

    /**
     * Number threads in blocking queue.
     */
    public int TASK_BACKLOG_LIMIT = 10;

    /**
     * Unused threads die after this amount of time.
     */
    public static long KEEP_ALIVE_TIME = 50;
    public static TimeUnit ALIVE_TIME_UNIT = MILLISECONDS;


    /**
     * Number of times the thread block can poll() null
     */
    public static int MAX_WAIT_TO_QUIT = 10;

    /**
     * Time to wait when poll() == null
     */

    public static int WAIT_TIME_MS = 100;


    ExecutorService executorService;
    CompletionService<Integer> compService;


    int numThreads;
    int numDatabases;

    private ArrayList<Integer> klueIndexesUnused;
    KLUE[] klueArr;

    public MultiThreadKlue(int numThreads, int numDatabases, KLUE[] klueArr) {
        this.numDatabases = numDatabases;
        this.numThreads = numThreads;
        this.klueArr = klueArr;

        klueIndexesUnused = new ArrayList<Integer>(numDatabases);
        for (int k=0; k<numDatabases; k++)  klueIndexesUnused.add(k);

        TASK_BACKLOG_LIMIT = 4 * numDatabases;

        executorService = new ThreadPoolExecutor(
                numThreads, 2*numThreads, KEEP_ALIVE_TIME, ALIVE_TIME_UNIT,
                new ArrayBlockingQueue<>(TASK_BACKLOG_LIMIT),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        compService = new ExecutorCompletionService<>(executorService);

    }

   private synchronized KLUE getKlueReservationIfAvailable(){
        KLUE result = null;
        int s = klueIndexesUnused.size();
        if (s > 0){
            result = klueArr[klueIndexesUnused.get(s-1)];
            klueIndexesUnused.remove(s-1);
        }
        return result;
    }

    private synchronized void setKlueReservationFree(int x){
        klueIndexesUnused.add(x);
    }

    public void shutDown(){
        for (int k=0; k<numDatabases; k++)  klueArr[k].shutDown();
        executorService.shutdown(); //always reclaim resources
    }

    public ArrayList<PositionList> getAllPL( long[] keys){
        //new LookupRequest( keys );
        //put on exectutor block...

        return null;
    }
}
