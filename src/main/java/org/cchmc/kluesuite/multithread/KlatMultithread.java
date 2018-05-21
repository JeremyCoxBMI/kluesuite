package org.cchmc.kluesuite.multithread;

import org.cchmc.kluesuite.datastreams.FastaSequence;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.KidDatabaseMemory;

import java.util.Iterator;
import java.util.concurrent.*;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by jwc on 8/24/17.
 *
 * This will not work unless KidDatabaseThreadSafe.ON =true, KidDatabaseThreadSafe.kd is set to a database.
 */
public class KlatMultithread {

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
    public static int TASK_BACKLOG_LIMIT = 10;

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

    Iterator<FastaSequence> it;
    KLUE klue;
    //KidDatabaseMemory kd;

    /**
     * This will not work unless KidDatabaseThreadSafe.ON =true, and
     *                 KidDatabaseThreadSafe.kd is set to a database.
     *
     * @param it    Queries to process
     * @param klue  K-mer database
     */
    public KlatMultithread(Iterator<FastaSequence> it, KLUE klue){//}, KidDatabaseMemory kd){
        this.it = it;
        this.klue = klue;
        //this.kd = kd;
    }

    public void calculateAlignments() {

        ExecutorService executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, ALIVE_TIME_UNIT,
                new ArrayBlockingQueue<>(TASK_BACKLOG_LIMIT),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        CompletionService<Integer> compService = new ExecutorCompletionService<>(executorService);

        long count = 1;
        FastaSequence fs;
        while (it.hasNext()){
            fs = it.next();

            if (fs.sequence.length() > 30) {
                AlignmentTask task = new AlignmentTask(fs.query, fs.sequence.toString(), klue);
                compService.submit(task);
                count++;
            }
        }


//        /**
//         * Nothing to do in future; integer is just a success code.
//         */
//        for (int k=0; k<count;k++) {
//            try {
//                //this is supposed to wait
//                Future<Integer> future = compService.take();
//                Integer result = future.get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                exit(900);
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//                exit(910);
//            }
//        }


        /**
         * Nothing to do in future; integer is just a success code.
         */
        int waitCount = 0;
        int reported = 0;
        for (int k=0; k<count;k++) {
            try {
                Future<Integer> future = compService.poll();
                if (future == null){
                    waitCount++;
                    if (waitCount == MAX_WAIT_TO_QUIT){
                        break;
                    }
                    sleep(100);

                } else {
                    Integer result = future.get();
                    reported++;
                    waitCount=0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                exit(900);
            } catch (ExecutionException e) {
                e.printStackTrace();
                exit(910);
            }
        }

        //executorService.invokeAll();
        executorService.shutdown(); //always reclaim resources
        try {
            executorService.awaitTermination(100L,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.err.println("Found\t"+(count-1)+"\tSequences");
        System.err.println("Reported\t"+reported+"\tSequences");
        if ((count-1) != reported) {
            System.err.println("All queries were not aligned.  The program may have timed out waiting for threads to report.");
        }

    }
}
