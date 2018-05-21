package org.cchmc.kluesuite.klat2;

import java.util.concurrent.*;




/**
 * Created by jwc on 5/21/18.
 *
 *
 * SEED package org.cchmc.kluesuite.multithread.KlatMultithread;
 *
 */
//public class MultiThreadKLAT {
//
//    ExecutorService lookupTasks;
//    ExecutorService alignmentTasks;
//    CompletionService<LookupResult> compService;
//
//    protected int CORE_POOL_SIZE = 15;
//    protected int MAX_POOL_SIZE = 30;
//    protected int KEEP_ALIVE_TIME = 100;
//    protected int TASK_BACKLOG_LIMIT = 100;
//
//
//    public MultiThreadKLAT(){
//        lookupTasks = new ThreadPoolExecutor(
//                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
//        new ArrayBlockingQueue<>(TASK_BACKLOG_LIMIT),
//                new ThreadPoolExecutor.CallerRunsPolicy()
//        );
//
//        compService = new ExecutorCompletionService<>(lookupTasks);
//    }
//
//
//    private final class Task implements Callable<LookupResult>{
//
//        @Override
//        public LookupResult call() throws Exception {
//            return null;
//        }
//    }
//
//
//    private static final class LookupResult(){
//
//    }
//
//}
