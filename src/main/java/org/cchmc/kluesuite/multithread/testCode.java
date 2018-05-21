package org.cchmc.kluesuite.multithread;

import org.cchmc.kluesuite.datastreams.FastaFile;
import org.cchmc.kluesuite.datastreams.FastaSequence;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by jwc on 8/24/17.
 */
public class testCode {

    public static int CORE_POOL_SIZE = 10;
    public static int MAX_POOL_SIZE = 20;
    public static int TASK_BACKLOG_LIMIT = 10;
    public static long KEEP_ALIVE_TIME = 50;
    public static TimeUnit ALIVE_TIME_UNIT = MILLISECONDS;


    public static void main(String[] args) {

        Random rand = new Random(135L);

        ExecutorService executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, ALIVE_TIME_UNIT,
                new ArrayBlockingQueue<>(TASK_BACKLOG_LIMIT),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        CompletionService<Integer> compService = new ExecutorCompletionService<>(executorService);

        Iterator<FastaSequence> it = new FastaFile(args[0]).sequenceIterator();

        long count = 1;
        while (it.hasNext()){
            FastaSequence f = it.next();
            TestTask task = new TestTask(count);
            count++;
            compService.submit(task);

        }

        for (int k=0; k<count;k++) {
            try {
                Future<Integer> future = compService.take();
                Integer result = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown(); //always reclaim resources
    }
}
