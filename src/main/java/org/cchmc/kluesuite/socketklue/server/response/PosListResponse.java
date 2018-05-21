package org.cchmc.kluesuite.socketklue.server.response;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

/**
 * Created by joe on 3/8/17.
 */
public class PosListResponse implements KlueSocketPacket {

    private PositionList a;

    public PosListResponse(PositionList a) {
        this.a = a;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x86);
        long[] ll = a.toLongArray();
        byteBuf.writeInt(ll.length);
        for (long b : ll)
            byteBuf.writeLong(b);
    }
}
