package org.cchmc.kluesuite.socketklue.client.request;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/3/17.
 */
@Value
public class GetAllRequest implements KlueSocketPacket {
    long[] keys;

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x03);
        byteBuf.writeInt(keys.length);
        for (int i = 0; i < keys.length; i++)
            byteBuf.writeLong(keys[i]);
    }
}
