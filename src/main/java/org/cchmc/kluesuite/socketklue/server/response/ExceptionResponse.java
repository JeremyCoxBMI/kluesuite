package org.cchmc.kluesuite.socketklue.server.response;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;

import java.nio.charset.StandardCharsets;

/**
 * Created by joe on 3/7/17.
 */
public class ExceptionResponse implements KlueSocketPacket {

   private String message;

    public ExceptionResponse(String message) {
        this.message = message;
    }

    @Override
    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeByte(0x81);
        byteBuf.writeCharSequence(message, StandardCharsets.UTF_8);
    }
}
