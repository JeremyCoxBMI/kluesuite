package org.cchmc.kluesuite.memoryklue;

import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.Position;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite._oldclasses.PermutatorLimitAdjacent;

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


public class MemoryKlueHeapFastImportArray implements KLUE {

    /**
     * Threshold used to write database out (suau
     */
    double percent99size;


    /**
     * old method to estimate RAM usage
     */
    static double INFLATION = 1.4; //Rate at which Java over allocates array

    //Sort and Remove duplicates before writing to permanent database
    //Key feature when creating variant sequences
    //Other usages, best to set to off
    static boolean REMOVE_DUPLICATES = true;


    /**
     * the heap increases size as it is built.  This is the maximum size.
     */
    int MAX_SIZE;


    /**
     * Use a simple array to avoid Java garbage collector.
     * A min heap as an array
     */
    public long keys[];

    /**
     * Use a simple array to avoid Java garbage collector.
     * A min heap as an array ; minimum is determined by the _key_
     * This is thus in same ordering as keys
     * Note an array of some struct is less effecient than primitives, so we use primitives
     */
    public long values[];


    /**
     * current size
     */
    public int size;

    /**
     * if the heap is full
     */
    public boolean isFull;

    /**
     * last index written, e.g. starts at -1, so next index to write is 0
     */
    public int lastIdx;


    /**
     * OPTIONAL: if heap is full, gets dumped to a file
     * then a fresh database, new name is made.  Straight writing to a file without appending is much faster.
     * This can be combined later.
     */
    private RocksDbKlue dump;

    /**
     * used to name subsequent instance of dump
     */
    private String dumpName;


    /**
     * used to name subsequent instance of dump (normal, then normal.p1, then normal.p2...)
     */
    private int dumpCount = 1;

    /**
     * Used to track how many kmers are written to new RocksDbKlue
     */
    private int numKmersDumped;

    /**
     * constructor.  Minimal need for construction is a size.  Not that this constructor maximum size is
     * ~2147 million, or 2^31, which requires about 256 GB to allocate
     * @param size
     */
    public MemoryKlueHeapFastImportArray(int size){

        //System.err.println("\n\t\tMemoryKlueHeapFastImportArray constructor :: approx memory used (OLD est)"+(INFLATION*32*size)/(1024*1024*1024)+" GB");
        System.err.println("\n\t\tMemoryKlueHeapFastImportArray constructor :: approx memory used "+new Double(size)/1000/1000/50*6+" GB");

        //initialize
        MAX_SIZE = size;
        keys = new long[MAX_SIZE];
        values = new long[MAX_SIZE];
        System.err.println("\t\tMemoryKlueHeapFastImportArray constructor :: memory allocated successfully");

        size=0;
        lastIdx = -1;
        isFull = false;
        percent99size = 0.99*MAX_SIZE;

        //optional values for database dumping, trigger when 99% full
        dump = null;
        dumpName = null;
        numKmersDumped = 0;
    }

    /**
     * Optional constructor for database dumping.
     * @param size
     * @param dumper
     */
    public MemoryKlueHeapFastImportArray(int size, RocksDbKlue dumper){
        this(size);
        dump = dumper;
        dumpName = dump.getDatabasePath();
    }


//    public MemoryKlueHeapFastImportArray(MemoryKlueHeapFastImportArray ){


    public void heapify(){
        //Need heapsort!
        //https://en.wikipedia.org/wiki/Heapsort
        if (size == 0){
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

    /**
     * Coordin
     * @param start  coordinate (array index)
     * @param end   coordinate (array index)
     */
    private void siftDown(int start, int end){
        int root = start;
        int child, swap, rchild;
        while ( leftChild(root) <= end ){
            child = leftChild(root);
            swap = root;
            //promote the biggest child to the root
//            //This is max heap, want min heap
//            if ( pairs[swap*2] < pairs[child*2] )   swap = child;
//            rchild = rightChild(root);
//            if ( rchild <= end && pairs[swap*2] < pairs[rchild*2])  swap = rchild;
            if ( keys[swap] > keys[child] )   swap = child;
            rchild = rightChild(root);
            if ( rchild <= end && keys[swap] > keys[rchild])  swap = rchild;

            if (swap != root) {
                swap(swap, root);
                root = swap;
            } else {
                break;
            }
        }
    }

    private int leftChild(int i) { return 2*i+1; }
    private int rightChild(int i) { return 2*i+2; }
    private int parent(int i) {return (i-1)/2;}

    public void reset(){
        size=0;
    }

    public void swap(int index0, int index1){
        long swap0 = keys[index0];
        long swap1 = values[index0];
        keys[index0] = keys[index1];
        values[index0]= values[index1];
        keys[index1] = swap0;
        values[index1] = swap1;
    }

    public void print(){
        for(int k=0; k<size; k++)  System.out.println(keys[k]+"\t"+values[k]);
    }

    @Override
    public void put(long key, ArrayList<Long> positions) {
        for (long p : positions)    append(key, p);
    }

    @Override
    public void append(long key, long pos) {

//        System.err.println("\thklue appending:\t"+new Kmer31(key)+"\tat\t"+new Position(pos));
        if (!isFull) {
            keys[size] = key;
            values[size] = pos;
            size++;

            if (size == MAX_SIZE) isFull = true;


//            if (MAX_SIZE > 99 && size % (MAX_SIZE / 100) == 0) {
//                java.util.Date timer = new java.util.Date();
//                System.out.println("\t\tMemoryKlueHeapFastImportArray = import progress: \t" + size / 1000.0 / 1000.0 + " million    " + new Timestamp(timer.getTime()) + "\tkid\t" + new Position(pos).getMyKID());
//            }
        } // end if (!isFull)
        if (size > percent99size) {
            java.util.Date timer = new java.util.Date();
            System.out.println("\t\tWARNING\tMemoryKlueHeapFastImportArray is past 99% full\t" + size / 1000.0 / 1000.0 + " million    " + new Timestamp(timer.getTime()) + "\tkid\t" + new Position(pos).getMyKID());
            System.out.println("\t\tTriggering database dump\t" + size / 1000.0 / 1000.0 + " million    " + new Timestamp(timer.getTime()) + "\tkid\t" + new Position(pos).getMyKID());

            System.out.println("\t\t\tLast Variant was\t"+ PermutatorLimitAdjacent.lastVariant+"\n");  //debug only

            if (dump == null){
                System.err.println("\t\tWARNING : will lead to database truncation : No database to dump to this dump == null.");

            } else {
                System.out.println("\t\tThis is file dump # "+dumpCount);
                dumpToDatabase(dump);
                dump.shutDown();
                String newPath = dumpName + ".p" + dumpCount;
                System.err.println("\tInitializing new RocksDbKlue\t" + newPath);
                dump = null;  //garbage collection
                dump = new RocksDbKlue(newPath, false);

                dumpCount++;
                //reset THIS database
                lastIdx = -1;
                size = 0;
                isFull = false;
            }
        }
    }


    @Override
    public ArrayList<Long> get(long key) {
        //YOU MUST HEAPIFY FIRST


        return null;
    }

    @Override
    public ArrayList<ArrayList<Long>> getAll(long[] keys) {
        return null;
    }

    @Override
    public ArrayList<PositionList> getAllPL(long[] keys) {
        return null;
    }

    @Override
    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        return null;
    }

    @Override
    public void shutDown() {

    }



    /**
     * Removes in order, assuming heapify() is satisfied
     * @return
     */
     public KVpair remove() {
         if (lastIdx == -1){
             System.err.println("  WARNING :: Call to HeapKueFastImport.remove()  WITHOUT calling heapify first");
         }

         KVpair result = null;
         if (hasNext()){
             result = new KVpair(keys[0], values[0]);

             //Reverse heapify read out
             swap(0, lastIdx);
             lastIdx--;
             siftDown(0, lastIdx);
             while(hasNext() && keys[0] == result.key){    //keys[0] is key at top of heap
                 result.add(values[0]);
                 swap(0, lastIdx);
                 lastIdx--;
                 siftDown(0, lastIdx);
             }

         }
//         PositionList reduce = new PositionList(result.value);
//         reduce.sortAndRemoveDuplicates();
//         result.value = reduce.toArrayListLong();

         return result;
     }

    public KVpair peek() {
        if (lastIdx == -1){
            System.err.println("  WARNING :: Call to HeapKueFastImport.peek()  WITHOUT calling heapify first");
        }

        KVpair result = null;
        if (hasNext()){
            result = new KVpair(keys[0], values[0]);
        }
//        PositionList reduce = new PositionList(result.value);
//        reduce.sortAndRemoveDuplicates();
//        result.value = reduce.toArrayListLong();

        return result;
    }





    public boolean hasNext(){
        return lastIdx >= 0;
    }

    public int percentageRemaining() {
        return lastIdx / (MAX_SIZE/100);
    }

    public String toString(){
        String result="";
        for (int k=0; k < 6; k++){
            result+=keys[k]+"\t"+values[k]+"\n";//" || ";
        }
        return result;
    }

    public void resetLastToMax(){
        lastIdx = size - 1;
    }

    public static void main(String[] args) {
        MemoryKlueHeapFastImportArray hpkfia = new MemoryKlueHeapFastImportArray(215);


        Random rand = new Random(6003);

        for (long k=190; k >= 0; k--) {
            hpkfia.append(0L+k, (long) rand.nextInt(2000) );
        }

        System.out.println("Keys/values array before heapify:");
        for (int k=0; k< hpkfia.size; k++) {
            System.out.print(k+":\t"+hpkfia.keys[k]);
            System.out.println(":\t"+hpkfia.values[k]);
        }

        hpkfia.heapify();

        System.out.println("Keys/values array after heapify:");
        for (int k=0; k< hpkfia.size; k++) {
            System.out.print(k+":\t"+hpkfia.keys[k]);
            System.out.println("\t"+hpkfia.values[k]);

        }

        for (int k=0; k< hpkfia.size; k++) {
            KVpair kv = hpkfia.remove();
            System.out.print(k+":\t"+kv.key);
            System.out.println("\t"+kv.value);
        }


    }


    /**
     * If database is defined in the constructor, the database is known
     * Moreover, it may have dumpoed several times already
     * @return
     */
    public int dumpToDatabase() {
        if (dump == null){
            System.err.println("WARNING:\tMemoryKlueHeapFastImportArray :: dumpToDatabase called without database defined.  No action.");
            return 0;
        } else {
            return dumpToDatabase(dump);
        }
    }


    /**
     * Uses put() to rapidly write database in order to disk.  Note that you must close existing database and start anew
     * after the dump, because put() overwrites.
     * @param rocksklue
     * @return  number of k-mer pairs processed
     */
    private int dumpToDatabase(KLUE rocksklue){

        this.heapify();

        PositionList pl= new PositionList();
        KVpair t;

        //prev will not match first read, using Sentinel=-5
        KVpair prev = new KVpair(-5, null);

        //int lastSize = 101;

        int k=0;
        while (this.hasNext()){
            t = this.remove();

            //System.err.println("Next key pair\t"+t);//debug

            //first time through, will not match, auto-written; should never match
            //every entry is read out in order by key as arrayof values
            if (t.key != prev.key) {
                if (REMOVE_DUPLICATES) {
                    pl = new PositionList(t.value);
                    pl.sortAndRemoveDuplicates();
                    rocksklue.put(t.key, pl.toArrayListLong());
                } else {
                   rocksklue.put(t.key, t.value);
                }
                k++;
                //if (k % (maxSize/10) == 0) System.out.println("\t\texport progress: "+k/1000.0/1000.0+" million\t"+writeTime);
            } else {
                System.err.println("WARNING  Found identical key in new memoryklue.remove().");
                //pl.addAndTrim( t.value );
            }

            prev = t;
        }
        return (numKmersDumped+=k);
    }


    /**
     * pulls consecutive reads that have the same key
     * @return
     */
//    public DatabaseEntry removeAllSameKey() {
//
//
//
//
//        if (lastIdx == -1){
//            System.err.println("  WARNING :: Call to HeapKueFastImport.removeAllSameKey()  WITHOUT calling heapify first");
//        }
//
//        KVpair peek = null;
//
//        peek = peek();
//        int keyToUse = (int) peek.key;
//        DatabaseEntry result = new DatabaseEntry(keyToUse);
//
//
//        //does not read past the entry with correct key
//        while (hasNext() && peek.key == keyToUse) {
//            peek = remove(); //advance through array
//            result.addAndTrim(peek.value);
//            peek = peek();  //reset
//        }
//
//        return result;
//    }
//



}
