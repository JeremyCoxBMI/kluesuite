package org.cchmc.kluesuite.helperclasses;

import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.memoryklue.KVpair;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Random;

/**
 * To avoid monstrous java overheads, large amount of data is imported as one-dimensional array
 * To sort it, we heapify it.
 * Then, we read it one by one to write to a file --> when we need to make a sorted copy (lexicographic database write).
 *
 * This is a min heap.
 *
 * LIMITED KLUE :: append() operation only
 *                  Special operations to sort (i.e. heapify() ) and read out ( remove() )
 *
 *
 */


public class MinHeapLongsByArray {

    /**
     * Use a simple array to avoid Java garbage collector.
     * A min heap as an array
     */
    public long keys[];


    /**
     * current size by values added
     */
    public int size;

    /**
     * current size by memory allocation of STATIC array
     */
    public int MAX_SIZE;


    /**
     * last index written, e.g. starts at -1, so next index to write is 0
     */
    public int lastIdx;

    public MinHeapLongsByArray(int size) {
        //initialize
        MAX_SIZE = size;
        keys = new long[MAX_SIZE];
        this.size = 0;
        lastIdx = -1;
    }


    public void heapify() {
        //Need heapsort!
        //https://en.wikipedia.org/wiki/Heapsort
        if (size == 0) {
            System.err.println("\tWARNING\tHeap has size 0, nothing to heapify");
        } else {
            //System.err.println("\t\tHeapify DEBUG start.  Size = " + size);
            int start = parent(size - 1);
            //int start = size - 1; //BUG 2017-05-19
            while (start >= 0) {
                siftDown(start, size - 1);
                start--;
            }
            //System.err.println("\t\tHeapify DEBUG end");
            lastIdx = size - 1; //signal to remove that you are now allowed to remove and where to start
        }
    }

    public void append(long x){
        keys[size] = x;
        size++;
    }

    /**
     * Coordin
     *
     * @param start coordinate (array index)
     * @param end   coordinate (array index)
     */
    private void siftDown(int start, int end) {
        int root = start;
        int child, swap, rchild;
        while (leftChild(root) <= end) {
            child = leftChild(root);
            swap = root;
            //promote the biggest child to the root
//            //This is max heap, want min heap
//            if ( pairs[swap*2] < pairs[child*2] )   swap = child;
//            rchild = rightChild(root);
//            if ( rchild <= end && pairs[swap*2] < pairs[rchild*2])  swap = rchild;
            if (keys[swap] > keys[child]) swap = child;
            rchild = rightChild(root);
            if (rchild <= end && keys[swap] > keys[rchild]) swap = rchild;

            if (swap != root) {
                swap(swap, root);
                root = swap;
            } else {
                break;
            }
        }
    }

    private int leftChild(int i) {
        return 2 * i + 1;
    }

    private int rightChild(int i) {
        return 2 * i + 2;
    }

    private int parent(int i) {
        return (i - 1) / 2;
    }

    public void reset() {
        size = 0;
    }

    public void swap(int index0, int index1) {
        long swap0 = keys[index0];
        keys[index0] = keys[index1];
        keys[index1] = swap0;
    }

    public void print() {
        for (int k = 0; k < size; k++) System.out.println(keys[k]);
    }


    /**
     * Removes in order, assuming heapify() is satisfied
     *
     * @return
     */
    public long remove() {
        if (lastIdx == -1) {
            System.err.println("  WARNING :: \tMinHeapLongssByArray :: call to remove()  WITHOUT calling heapify first");
            System.exit(1);
        }

        long result = -1L;
        if (hasNext()) {
            result = keys[0];

            //Reverse heapify read out
            swap(0, lastIdx);
            lastIdx--;
            siftDown(0, lastIdx);
//            while (hasNext() && keys[0] == result) {    //keys[0] is key at top of heap
//                swap(0, lastIdx);
//                lastIdx--;
//                siftDown(0, lastIdx);
//            }

        }
        return result;
    }

    public long peek() {
        if (lastIdx == -1) {
            System.err.println("  WARNING :: Call to HeapKueFastImport.peek()  WITHOUT calling heapify first");
        }

        long result = -1L;
        if (hasNext()) {
            result = keys[0];
        }

        return result;
    }


    public boolean hasNext() {
        return lastIdx >= 0;
    }

    public int percentageRemaining() {
        return lastIdx / (MAX_SIZE / 100);
    }

    public String toString() {
        String result = "";
        for (int k = 0; k < 6; k++) {
            result += keys[k]+"\n";//" || ";
        }
        return result;
    }

    public void resetLastToMax() {
        lastIdx = size - 1;
    }


}