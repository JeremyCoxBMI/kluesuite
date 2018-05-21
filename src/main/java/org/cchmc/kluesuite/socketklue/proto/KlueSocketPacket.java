package org.cchmc.kluesuite.socketklue.proto;

import io.netty.buffer.ByteBuf;

/**
 * A request over a socket
 *
 * @author Joseph Hirschfeld (Joseph.Hirschfeld@cchmc.org)
 * @since 1/19/17
 */
public interface KlueSocketPacket {
    void serialize(ByteBuf byteBuf);
}
