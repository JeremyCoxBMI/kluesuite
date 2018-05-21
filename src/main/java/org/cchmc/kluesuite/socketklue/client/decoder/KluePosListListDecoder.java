package org.cchmc.kluesuite.socketklue.client.decoder;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.klue.PositionList;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public final class KluePosListListDecoder implements KlueResponseDecoder<ArrayList<PositionList>> {

    public static final KluePosListListDecoder i = new KluePosListListDecoder();

    private KluePosListListDecoder() {}

    @Override
    public boolean accept(int typeOpcode) {
        return typeOpcode == 0x85;
    }

    @Override
    public ArrayList<PositionList> decode(ByteBuf b) {
        int len = b.readInt();
        ArrayList<PositionList> aa = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            int lenb = b.readInt();
            long[] a = new long[lenb];
            for (int j = 0; j < lenb; j++)
                a[j] = b.readLong();
            aa.add(new PositionList(a));
        }
        return aa;
    }
}
