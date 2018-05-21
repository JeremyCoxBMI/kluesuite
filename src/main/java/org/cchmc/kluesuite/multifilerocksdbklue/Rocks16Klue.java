package org.cchmc.kluesuite.multifilerocksdbklue;

import org.cchmc.kluesuite.TimeTotals;
import org.cchmc.kluesuite.klue.KeyValuePair;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.rocksdb.RocksIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by osboxes on 22/09/16.
 *
 * For reading 16 separate databases as one database.
 * Note that this should not be used to build the database, although it can be.
 *
 *
 * WHY I AM NOT PURSUING ASYNCHRONOUS AT THIS TIME
 * There is no compelling need.  As discussed in depth below, this would only help with network lag, which I am not concerned with ATM.
 * Moreover, I don't want to solve that problem, as this is AGAINST PRIMARY DESIGN principle: use a committed machine.
 *
 *
 * Many ideas for multiple asynchronous requests are valid:
 *
 * You could answer all get() asynchronously, and the overlord object would keep a sorted list of requests, and
 * then go through them in sorted order as chunks to minimize seek time.  (FOR HD only)
 *
 * However, multithreading each of the 16 smaller databases seems ill-advised.  The bottleneck is access time to disk.
 * Doing them asynchronously doesn't help that.
 * UNLESS you are accessing via network file system, then this would minimize network lag.
 *
 */


public class Rocks16Klue extends RocksDbKlue {

    protected final String databasePath;
    //protected final String databases[] = new String[16];
    protected final RocksDbKlue rocks[] = new RocksDbKlue[16];
    protected boolean readonly;
    protected Iterator<byte[]> it;

    /**
     *
     * @param dbPath    path to database.  Note that the db files individually are dbPath+"."+number
     * @param readonly
     */
    public Rocks16Klue(String dbPath, boolean readonly){
        super();
        this.readonly = readonly;
        databasePath = dbPath;
        if (!readonly)  System.err.println("WARNING : Opening Rocks16Klue in writable mode.  This is ill-advised.");
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("\tRocks16Klue opening\t\t"+tt.toHMS());
        for (int k=0; k<16; k++){
            System.out.println("\t\topening part "+k+"\t\t"+tt.toHMS());
            rocks[k] = new RocksDbKlue(databasePath+"."+k, readonly);  //read only!
        }
        // possibly will addWithTrim some sort of multi-threading support
        // design decision: if we need (probably won't), will addWithTrim this as a child class.  For now, basic functionality for testing.
    }


    public Rocks16Klue(String textFile, String[] dbPaths, boolean readonly){
        super();
        this.readonly = readonly;
        databasePath = textFile;
        if (!readonly)  System.err.println("WARNING : Opening Rocks16Klue in writable mode.  This is ill-advised.");
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("\tRocks16Klue opening\t\t"+tt.toHMS());
        for (int k=0; k<16; k++){
            System.out.println("\t\topening part "+k+"\t\t"+tt.toHMS());
            rocks[k] = new RocksDbKlue(dbPaths[k], readonly);  //read only!
        }
        // possibly will addWithTrim some sort of multi-threading support
        // design decision: if we need (probably won't), will addWithTrim this as a child class.  For now, basic functionality for testing.
    }

    public Rocks16Klue(boolean b, String klue16file) {
        super();
        String[] dbPaths = new String[16];

        try(BufferedReader br = new BufferedReader(new FileReader(klue16file))) {
            int k=0;
            for(String line; (line = br.readLine()) != null; ) {
                line = line.trim();
                dbPaths[k] = line;
                k++;
                if (k==16) break;   //prevents white space on lines past first 16 from crashing program
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.readonly = readonly;
        databasePath = klue16file;
        if (!readonly)  System.err.println("WARNING : Opening Rocks16Klue in writable mode.  This is ill-advised.");
        TimeTotals tt = new TimeTotals();
        tt.start();
        System.out.println("\tRocks16Klue opening\t\t"+tt.toHMS());
        for (int k=0; k<16; k++){
            System.out.println("\t\topening part "+k+"\t\t"+tt.toHMS());
            rocks[k] = new RocksDbKlue(dbPaths[k], readonly);  //read only!
        }

    }


    // Converts a key into a 4 bit number, representing the first 2 letters.
    // So, this is the index representing which database to access, from 0 to 15
    static public int split(long key){
        //return (int) key >> 58;
        return Prefix16.longToInt( key );
    }

    static public ArrayList<ArrayList<Long>> makeChunks(long[] keys){
        int split;
        //split into 16 chunks
        ArrayList<ArrayList<Long>> chunks = new ArrayList<ArrayList<Long>>();
        for (int k=0; k<16; k++){
            //chunks.set(k, new ArrayList<Long>());
            chunks.add(new ArrayList<Long>());
        }

        for (long key : keys){
            chunks.get(split(key)).add(key);
        }

        for (int k=0; k<16; k++){
            Collections.sort(chunks.get(k));
        }
        return chunks;
    }

    @Override
    public void put(long key, ArrayList<Long> positions) {
        if (readonly) System.err.println("Warning :: Rocks16Klue.put() called in READONLY mode. Cannot write to Rocks16Klue.");
        else {
            rocks[split(key)].put(key, positions);
        }
    }

    @Override
    public void append(long key, long pos) {
        if(readonly) System.err.println("Warning :: Rocks16Klue.append() called called in READONLY mode. Cannot write to Rocks16Klue.");
        else{
            int split = split(key);
            rocks[split].append(key,pos);
        }
    }

    @Override
    public ArrayList<Long> get(long key) {
        return rocks[split(key)].get(key);
    }

    @Override
    public ArrayList<ArrayList<Long>> getAll(long[] keys) {

        //split into 16 chunks, query 16 databases
        ArrayList<ArrayList<Long>> chunks = makeChunks(keys);
        ArrayList<ArrayList<ArrayList<Long>>> temp = new ArrayList<ArrayList<ArrayList<Long>>>();
        for(int k=0; k < 16; k++){
            temp.add( rocks[k].getAll(chunks.get(k)) );
        }

        //Preserve Output Order
        //This is slightly confusing.  Chunks are sorted to ease rapid lookup difficulties.
        //Only original keys is UNMODIFIED ORDER

        ArrayList<ArrayList<Long>> result = new ArrayList<ArrayList<Long>>();

        //for each key IN ORDER, write to output
        for ( long key : keys ){

            //for each key, search through the corresponding chunk output for result
            int x = 0;
            for( long chunkKey : chunks.get(split(key))){
                if (chunkKey == key){
                    result.add(temp.get(split(key)).get(x));
                    //nextOffset++;
                    break;
                }
                x++;
            }
        }
        return result;
    }

    @Override
    public ArrayList<PositionList> getAllPL(long[] keys) {

        //split into 16 chunks
        ArrayList<ArrayList<Long>> chunks = makeChunks(keys);

        ArrayList<ArrayList<PositionList>> temp = new ArrayList<ArrayList<PositionList>>();
        for(int k=0; k < 16; k++){
            temp.add( rocks[k].getAllPL(chunks.get(k)) );
        }

        //Preserve Output Order
        //This is slightly confusing.  Chunks are sorted to ease rapid lookup difficulties.
        //Only original keys is UNMODIFIED ORDER

        ArrayList<PositionList> result = new ArrayList<PositionList>();

        //for each key IN ORDER, write to output
        for ( long key : keys ){

            //for each key, search through the corresponding chunk output for result
            int x = 0;
            for( long chunkKey : chunks.get(split(key))){
                if (chunkKey == key){
                    result.add(temp.get(split(key)).get(x));
                    //nextOffset++;
                    break;
                }
                x++;
            }
        }
        return result;
    }

    @Override
    public void shutDown() {
        for (RocksDbKlue rock : rocks){
            rock.shutDown();
        }
    }

    @Override
    public PositionList getShortKmerMatches(long shorty, int prefixLength) {
        //This will access a single member of rocks
        int split = split(shorty);
        return rocks[split].getShortKmerMatches(shorty, prefixLength);
    }

    @Override
    public RocksIterator newIterator() {
        System.err.println("WARNING\tRocks16Klue :: newIterator does nothing");
        return null;
    }

    @Override
    public Iterator<KeyValuePair> iterator(){
        return new Rocks16Iterator(rocks);
    }

    public Iterator<KeyValuePair> iterator(long key){
        return new Rocks16Iterator(rocks, key);
    }

}
