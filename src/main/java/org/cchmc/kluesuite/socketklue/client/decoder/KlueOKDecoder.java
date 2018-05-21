package org.cchmc.kluesuite.socketklue.client.decoder;

import io.netty.buffer.ByteBuf;

/**
 * Created by joe on 3/8/17.
 */
public class KlueOKDecoder implements KlueResponseDecoder<Void> {

    public static final KlueOKDecoder i = new KlueOKDecoder();

    @Override
    public boolean accept(int typeOpcode) {
        return typeOpcode == 0x82;
    }

    @Override
    public Void decode(ByteBuf b) {
        return null;
    }
}
