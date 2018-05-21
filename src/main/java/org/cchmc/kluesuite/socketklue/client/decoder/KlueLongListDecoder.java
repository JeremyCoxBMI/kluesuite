package org.cchmc.kluesuite.socketklue.client.decoder;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public final class KlueLongListDecoder implements KlueResponseDecoder<ArrayList<Long>> {

    public static final KlueLongListDecoder i = new KlueLongListDecoder();

    private KlueLongListDecoder(){}

    @Override
    public boolean accept(int typeOpcode) {
        return typeOpcode == 0x83;
    }

    @Override
    public ArrayList<Long> decode(ByteBuf b) {
        int len = b.readInt();
        ArrayList<Long> l = new ArrayList<>(len);
        for (int i = 0; i < len; i++)
            l.add(b.readLong());
        return l;
    }
}
