package org.cchmc.kluesuite.socketklue.server.handler;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.klue.KlueCallback;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.socketklue.server.KlueProtoCodexHandler;
import org.cchmc.kluesuite.socketklue.server.SocketPacketSink;
import org.cchmc.kluesuite.socketklue.server.response.ExceptionResponse;
import org.cchmc.kluesuite.socketklue.server.response.PosListListResponse;
import org.cchmc.kluesuite.socketklue.server.response.PosListResponse;

/**
 * Created by joe on 3/7/17.
 */
@AllArgsConstructor
public class GetShortKmerHandler extends KlueProtoCodexHandler {

    private final AsyncKLUE klue;

    @Override
    protected void call(ByteBuf req, final SocketPacketSink sink) {
        long shorty = req.readLong();
        int prefix = req.readInt();

        klue.getShortKmerMatches(shorty, prefix, new KlueCallback<PositionList>() {
            @Override
            public void callback(PositionList value) {
                sink.sink(new PosListResponse(value));
            }

            @Override
            public void exception(Exception e) {
                sink.sink(new ExceptionResponse(e.getMessage()));
            }
        });
    }
}
