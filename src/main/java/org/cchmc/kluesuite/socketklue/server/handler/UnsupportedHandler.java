package org.cchmc.kluesuite.socketklue.server.handler;

import io.netty.buffer.ByteBuf;
import org.cchmc.kluesuite.socketklue.server.KlueProtoCodexHandler;
import org.cchmc.kluesuite.socketklue.server.SocketPacketSink;
import org.cchmc.kluesuite.socketklue.server.response.UnsupportedResponse;

/**
 * Created by joe on 3/8/17.
 */
public class UnsupportedHandler extends KlueProtoCodexHandler {
    @Override
    protected void call(ByteBuf req, SocketPacketSink sink) {
        sink.sink(new UnsupportedResponse());
    }
}
