package org.cchmc.kluesuite.socketklue.client.request;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/3/17.
 */
@Value
public class GetShortKmerRequest implements KlueSocketPacket {
    long shorty;
    int prefix;

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x05);
        byteBuf.writeLong(shorty);
        byteBuf.writeInt(prefix);
    }
}
