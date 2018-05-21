package org.cchmc.kluesuite.klue;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by joe on 4/17/17.
 */
@AllArgsConstructor
public class SyncKLUE implements KLUE {

    private final AsyncKLUE klue;

    @Override
    public void put(long key, ArrayList<Long> positions) {
        LockingAsyncCallback<Void> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.put(key, positions, cb);
        cb.await();
    }

    @Override
    public void append(long key, long pos) {
        LockingAsyncCallback<Void> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.append(key, pos, cb);
        cb.await();
    }

    @Override
    public ArrayList<Long> get(long key) {
        LockingAsyncCallback<ArrayList<Long>> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.get(key, cb);
        return cb.await();
    }

    @Override
    public ArrayList<ArrayList<Long>> getAll(long[] keys) {
        LockingAsyncCallback<ArrayList<ArrayList<Long>>> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.getAll(keys, cb);
        return cb.await();
    }

    @Override
    public ArrayList<PositionList> getAllPL(long[] keys) {
        LockingAsyncCallback<ArrayList<PositionList>> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.getAllPL(keys, cb);
        return cb.await();
    }

    @Override
    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        LockingAsyncCallback<PositionList> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.getShortKmerMatches(shorty, prefixLength, cb);
        return cb.await();
    }

    @Override
    public void shutDown() {
        LockingAsyncCallback<Void> cb = new LockingAsyncCallback<>();
        cb.lock();
        klue.shutdown(cb);
        cb.await();
    }

    private static class LockingAsyncCallback<T> implements KlueCallback<T> {
        private T val;
        private Exception e;
        private Lock l;
        private Condition c;

        private void lock() {
            l.lock();
        }

        private T await() {
            try {
                c.await();
                if (e != null)
                    throw new RuntimeException(e);

                return val;
            } catch (InterruptedException e1) {
                throw new RuntimeException(e1);
            } finally {
                l.unlock();
            }
        }

        @Override
        public void callback(T value) {
            l.lock();
            try {
                this.val = value;
                c.signalAll();
            } finally {
                l.unlock();
            }
        }

        @Override
        public void exception(Exception e) {
            l.lock();
            try {
                this.e = e;
                c.signalAll();
            } finally {
                l.unlock();
            }
        }
    }
}
