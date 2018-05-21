package org.cchmc.kluesuite.socketklue.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketPacket;
import org.cchmc.kluesuite.socketklue.proto.NumberedInPacket;
import org.cchmc.kluesuite.socketklue.proto.NumberedOutPacket;
import org.cchmc.kluesuite.socketklue.server.response.ExceptionResponse;

/**
 * Created by joe on 3/3/17.
 */
public abstract class KlueProtoCodexHandler {

    public void handle(final NumberedInPacket request, final ChannelHandlerContext ctx) {
        try {
            call(request.getData(), new SocketPacketSink() {
                @Override
                public void sink(KlueSocketPacket packet) {
                    ctx.writeAndFlush(new NumberedOutPacket(request.getRequestNumber(), packet));
                }
            });
        } catch (RuntimeException e) {
            ctx.writeAndFlush(new NumberedOutPacket(request.getRequestNumber(), new ExceptionResponse(e.getMessage())));
            e.printStackTrace();
        }
    }

    protected abstract void call(ByteBuf req, SocketPacketSink sink);
}
