package org.cchmc.kluesuite.rocksDBklue;

import org.cchmc.kluesuite.masterklue.Settings_OLD;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import static java.lang.System.exit;

/**
 * Created by osboxes on 25/09/16.
 *
 * Complementary class to RocksKidDatabase, for storing DnaBitStrings.  Original class built-in to RocksKidDatabase.
 * For debugging, building, etc, need to be separated.
 *
 * DEPRECATED
 *
 */
public class RocksDnaBitString {


    protected String databasePath;
    protected Options options;
    protected boolean readonly;
    public RocksDB db;

    static {
        RocksDB.loadLibrary();
    }

    public RocksDnaBitString(String path, boolean readonly) {
        this.readonly = readonly;
        databasePath = path;
        options = new Options().setCreateIfMissing(true).setMaxOpenFiles(Settings_OLD.MAX_FILES);
        try {
            if (readonly) {
                db = RocksDB.openReadOnly(options, databasePath);
            } else {
                db = RocksDB.open(options, databasePath);
            }
        } catch( RocksDBException e) {
            System.out.println("RocksDnaBitString Constructor failed.");
            e.printStackTrace();
            exit(1);
        }
    }

    public RocksDnaBitString(String path, boolean readonly, int maxfiles) {
        this.readonly = readonly;
        databasePath = path;
        options = new Options().setCreateIfMissing(true).setMaxOpenFiles(maxfiles);
        try {
            if (readonly) {
                db = RocksDB.openReadOnly(options, databasePath);
            } else {
                db = RocksDB.open(options, databasePath);
            }
        } catch( RocksDBException e) {
            System.out.println("RocksDnaBitString Constructor failed.");
            e.printStackTrace();
            exit(1);
        }
    }

    public void put(int kidKey, DnaBitString seq){
        if (readonly){
            System.err.println("WARNING :: called storeSequence() on RocksKidDatabase that is set read only.  No action.");
        } else {
            byte[] key = RocksDbKlue.longToBytes((long) kidKey);
            byte[] value = seq.toByteArray();
            try {
                db.put(key, value);
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
    }

    public DnaBitString get(int myKID){
        byte[] key = RocksDbKlue.longToBytes( (long) myKID );
        byte[] value;
        try {
            value = db.get(key);
        } catch (RocksDBException e) {
            value = null;
        }
        return new DnaBitString(value);
    }

    public RocksIterator newIterator() {
        return db.newIterator();
    }

    public void put(long kidKey, byte[] value) {
        if (readonly){
            System.err.println("WARNING :: called storeSequence() on RocksKidDatabase that is set read only.  No action.");
        } else {
            byte[] key = RocksDbKlue.longToBytes((long) kidKey);
            try {
                db.put(key, value);
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
    }
}
