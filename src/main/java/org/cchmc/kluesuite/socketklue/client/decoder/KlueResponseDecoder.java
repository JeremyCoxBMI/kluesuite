package org.cchmc.kluesuite.socketklue.client.decoder;

import io.netty.buffer.ByteBuf;

/**
 * Created by joe on 3/8/17.
 */
public interface KlueResponseDecoder<T> {
    boolean accept(int typeOpcode);
    T decode(ByteBuf b);
}
