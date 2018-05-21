package org.cchmc.kluesuite.klue;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by joe on 4/18/17.
 */
public class PooledAsyncKLUE implements AsyncKLUE {

    private final KLUE klue;
    private final ExecutorService es;
    private final BlockingQueue<Runnable> tasks;

    public PooledAsyncKLUE(KLUE klue, int coreThreads, int maxThreads, long keepAliveTime, TimeUnit keepAliveTimeUnit) {
        this.klue = klue;

        tasks = new LinkedBlockingQueue<>();
        es = new ThreadPoolExecutor(coreThreads, maxThreads, keepAliveTime, keepAliveTimeUnit, tasks);
    }

    @Override
    public void put(final long key, final ArrayList<Long> positions, final KlueCallback<Void> callback) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        klue.put(key, positions);
                        callback.callback(null);
                    } catch (Exception e) {
                        callback.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            callback.exception(e);
        }
    }

    @Override
    public void append(final long key, final long pos, final KlueCallback<Void> cb) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        klue.append(key, pos);
                        cb.callback(null);
                    } catch (Exception e) {
                        cb.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            cb.exception(e);
        }
    }

    @Override
    public void get(final long key, final KlueCallback<ArrayList<Long>> cb) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.callback(klue.get(key));
                    } catch (Exception e) {
                        cb.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            cb.exception(e);
        }
    }

    @Override
    public void getAll(final long[] keys, final KlueCallback<ArrayList<ArrayList<Long>>> cb) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.callback(klue.getAll(keys));
                    } catch (Exception e) {
                        cb.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            cb.exception(e);
        }

    }

    @Override
    public void getAllPL(final long[] keys, final KlueCallback<ArrayList<PositionList>> cb) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.callback(klue.getAllPL(keys));
                    } catch (Exception e) {
                        cb.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            cb.exception(e);
        }
    }

    @Override
    public void getShortKmerMatches(final long shorty, final int prefixLength, final KlueCallback<PositionList> cb) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.callback(klue.getShortKmerMatches(shorty, prefixLength));
                    } catch (Exception e) {
                        cb.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            cb.exception(e);
        }
    }

    @Override
    public void shutdown(final KlueCallback<Void> cb) {
        try {
            tasks.put(new Runnable() {
                @Override
                public void run() {
                    try {
                        es.shutdown();
                        klue.shutDown();
                        cb.callback(null);
                    } catch (Exception e) {
                        cb.exception(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            cb.exception(e);
        }
    }
}
