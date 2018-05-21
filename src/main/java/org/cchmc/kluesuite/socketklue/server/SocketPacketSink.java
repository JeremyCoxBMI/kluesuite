package org.cchmc.kluesuite.socketklue.server;

import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/3/17.
 */
public interface SocketPacketSink {
    void sink(KlueSocketPacket packet);
}
