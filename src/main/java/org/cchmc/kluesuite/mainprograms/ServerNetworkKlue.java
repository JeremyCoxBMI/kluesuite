package org.cchmc.kluesuite.mainprograms;

import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.PooledAsyncKLUE;
import org.cchmc.kluesuite.rocksDBklue.RocksDbKlue;
import org.cchmc.kluesuite.socketklue.KlueSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by joe on 4/19/17.
 */
public class ServerNetworkKlue {

    public static void main(String[] args) throws Exception {
        String file = args[0];

        RocksDbKlue klue = new RocksDbKlue(file, true);
        AsyncKLUE asyncKlue = new PooledAsyncKLUE(klue, 4, 4, 1, TimeUnit.SECONDS);
        KlueSocketServer s = KlueSocketServer.bind(asyncKlue, new InetSocketAddress(45219));
        try {
            while (true)
                Thread.sleep(1000000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        s.close();
    }
}
