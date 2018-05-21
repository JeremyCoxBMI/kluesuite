package org.cchmc.kluesuite.socketklue.server.response;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public class LongListListResponse implements KlueSocketPacket {

    private ArrayList<ArrayList<Long>> a;

    public LongListListResponse(ArrayList<ArrayList<Long>> a) {
        this.a = a;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x84);
        byteBuf.writeInt(a.size());
        for (ArrayList<Long> b : a) {
            byteBuf.writeInt(b.size());
            for (Long c : b) {
                byteBuf.writeLong(c);
            }
        }
    }
}
