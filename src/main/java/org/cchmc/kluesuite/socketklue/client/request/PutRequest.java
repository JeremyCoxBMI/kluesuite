package org.cchmc.kluesuite.socketklue.client.request;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

import java.util.ArrayList;

@Value
public class PutRequest implements KlueSocketPacket {
    long key;
    ArrayList<Long> positions;

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x00);
        byteBuf.writeLong(key);
        byteBuf.writeInt(positions.size());
        for (Long position : positions)
            byteBuf.writeLong(position);
    }
}
