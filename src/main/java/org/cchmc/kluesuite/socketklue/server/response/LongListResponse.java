package org.cchmc.kluesuite.socketklue.server.response;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public class LongListResponse implements KlueSocketPacket {

    private ArrayList<Long> a;

    public LongListResponse(ArrayList<Long> a) {
        this.a = a;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x83);
        byteBuf.writeInt(a.size());
        for (Long z : a) {
            byteBuf.writeLong(z);
        }
    }
}
