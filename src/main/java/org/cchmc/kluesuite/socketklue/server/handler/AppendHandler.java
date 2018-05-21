package org.cchmc.kluesuite.socketklue.server.handler;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.KlueCallback;
import org.cchmc.kluesuite.socketklue.server.KlueProtoCodexHandler;
import org.cchmc.kluesuite.socketklue.server.SocketPacketSink;
import org.cchmc.kluesuite.socketklue.server.response.ExceptionResponse;
import org.cchmc.kluesuite.socketklue.server.response.OKResponse;

/**
 * Created by joe on 3/3/17.
 */
@AllArgsConstructor
public class AppendHandler extends KlueProtoCodexHandler {

    private final AsyncKLUE klue;

    @Override
    protected void call(ByteBuf req, final SocketPacketSink sink) {
        long key = req.readLong();
        long position = req.readLong();
        klue.append(key, position, new KlueCallback<Void>() {
            @Override
            public void callback(Void value) {
                sink.sink(new OKResponse());
            }

            @Override
            public void exception(Exception e) {
                sink.sink(new ExceptionResponse(e.getMessage()));
            }
        });
    }
}
