package org.cchmc.kluesuite.socketklue.client;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.klue.KlueCallback;
import org.cchmc.kluesuite.socketklue.client.decoder.KlueResponseDecoder;

import java.nio.charset.StandardCharsets;

/**
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/19/17
 */
public final class KlueResponseHandler<T extends Object> {

    private final KlueCallback<T> actor;
    private final KlueResponseDecoder<T> decoder;

    public KlueResponseHandler(KlueCallback<T> actor, KlueResponseDecoder<T> decoder) {
        this.actor = actor;
        this.decoder = decoder;
    }

    public final void handle(ByteBuf result) {
        int responseOpcode = result.readByte();
        if (responseOpcode == 0x80)
            actor.exception(new IllegalAccessException("Unsupported operation!"));
        else if (responseOpcode == 0x81)
            actor.exception(new RuntimeException(result.readCharSequence(result.readableBytes(), StandardCharsets.UTF_8).toString()));
        else {
            if (!decoder.accept(responseOpcode)) {
                actor.exception(new IllegalStateException("Wrong opcode!"));
            } else {
                T d = decoder.decode(result);
                actor.callback(d);
            }
        }
    }
}
