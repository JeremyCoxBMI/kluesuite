package org.cchmc.kluesuite.socketklue.client.decoder;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public final class KlueLongListListDecoder implements KlueResponseDecoder<ArrayList<ArrayList<Long>>> {

    public static final KlueLongListListDecoder i = new KlueLongListListDecoder();

    @Override
    public boolean accept(int typeOpcode) {
        return typeOpcode == 0x84;
    }

    @Override
    public ArrayList<ArrayList<Long>> decode(ByteBuf b) {
        int lena = b.readInt();
        ArrayList<ArrayList<Long>> aa = new ArrayList<>();
        for (int i = 0; i < lena; i++) {
            int lenb = b.readInt();
            ArrayList<Long> a = new ArrayList<>(lenb);
            aa.add(a);
            for (int j = 0; j < lenb; j++)
                a.add(b.readLong());
        }
        return aa;
    }
}
