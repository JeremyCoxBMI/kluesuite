package org.cchmc.kluesuite.socketklue.client.decoder;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.klue.PositionList;

/**
 * Created by joe on 3/8/17.
 */
public class KluePosListDecoder implements KlueResponseDecoder<PositionList> {

    public static final KluePosListDecoder i = new KluePosListDecoder();

    private KluePosListDecoder(){}

    @Override
    public boolean accept(int typeOpcode) {
        return typeOpcode == 0x86;
    }

    @Override
    public PositionList decode(ByteBuf b) {
        int len = b.readInt();
        long[] a = new long[len];
        for (int i = 0; i < len; i++)
            a[i] = b.readLong();
        return new PositionList(a);
    }
}
