package org.cchmc.kluesuite.socketklue.server.response;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public class PosListListResponse implements KlueSocketPacket {

    private ArrayList<PositionList> a;

    public PosListListResponse(ArrayList<PositionList> a) {
        this.a = a;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x85);
        byteBuf.writeInt(a.size());
        for (PositionList b : a) {
            long[] ll = b.toLongArray();
            byteBuf.writeInt(ll.length);
            for (long l : ll)
                byteBuf.writeLong(l);
        }
    }
}
