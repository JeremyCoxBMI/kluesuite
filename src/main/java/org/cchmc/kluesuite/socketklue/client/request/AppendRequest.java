package org.cchmc.kluesuite.socketklue.client.request;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/3/17.
 */
@Value
public class AppendRequest implements KlueSocketPacket {
    long key;
    long position;

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x01);
        byteBuf.writeLong(key);
        byteBuf.writeLong(position);
    }
}
