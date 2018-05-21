package org.cchmc.kluesuite.zDevelopmentPrograms;

import org.cchmc.kluesuite.klat.AlignmentKLAT1;
import org.cchmc.kluesuite.klue.KidDatabaseDisk;
import org.cchmc.kluesuite.builddb.DnaBitStringToDb;
import org.cchmc.kluesuite.klue.DnaBitString;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;

/**
 * Created by osboxes on 24/09/16.
 */
public class VeridyDnaBitStringToDb{

    public static void main(String[] args) {


//        RocksKidDatabase rkd = RocksKidDatabase.loadFromFile(Settings_OLD.RocksDbKidDbLocation,true);
        RocksDbKlue klue = new RocksDbKlue("/mnt/vmdk/temptest",false);



        KidDatabaseDisk rkd = KidDatabaseDisk.loadFromFileUnsafe(args[0]);

        DnaBitString dns;


        int kid = 5171;
        dns = rkd .getSequence(kid);
        String out = dns.toString();

        DnaBitStringToDb td = new DnaBitStringToDb(dns,klue,kid);
        td.writeAllPositions();

        String out2 = out.substring(0,100)+"X"+out.substring(101);
        kid += 10000;
        dns = new DnaBitString(out2);
        td = new DnaBitStringToDb(dns,klue,kid);
        td.writeAllPositions();

        AlignmentKLAT1 al = new AlignmentKLAT1(out.substring(50,150), "buttmunch", klue);
        al.testAll(rkd);

    }
}
