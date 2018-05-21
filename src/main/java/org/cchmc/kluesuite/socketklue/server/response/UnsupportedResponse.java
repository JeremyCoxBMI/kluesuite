package org.cchmc.kluesuite.socketklue.server.response;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/8/17.
 */
public class UnsupportedResponse implements KlueSocketPacket {
    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x80);
    }
}
