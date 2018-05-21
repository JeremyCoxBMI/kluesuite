package org.cchmc.kluesuite.socketklue.server.handler;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.KlueCallback;
import org.cchmc.kluesuite.socketklue.server.KlueProtoCodexHandler;
import org.cchmc.kluesuite.socketklue.server.SocketPacketSink;
import org.cchmc.kluesuite.socketklue.server.response.ExceptionResponse;
import org.cchmc.kluesuite.socketklue.server.response.OKResponse;

import java.util.ArrayList;

/**
 * Created by joe on 3/7/17.
 */
@AllArgsConstructor
public class PutHandler extends KlueProtoCodexHandler {

    private final AsyncKLUE klue;

    @Override
    protected void call(ByteBuf req, final SocketPacketSink sink) {
        long key = req.readLong();
        int posSize = req.readInt();
        ArrayList<Long> positions = new ArrayList<>(posSize);
        for (int i = 0; i < posSize; i++)
            positions.add(req.readLong());

        klue.put(key, positions, new KlueCallback<Void>() {
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
