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

import java.util.ArrayList;

/**
 * Created by joe on 3/7/17.
 */
@AllArgsConstructor
public class GetAllPlHandler extends KlueProtoCodexHandler {

    private final AsyncKLUE klue;

    @Override
    protected void call(ByteBuf req, final SocketPacketSink sink) {
        int length = req.readInt();
        long[] a = new long[length];
        for (int i = 0; i < length; i++)
            a[i] = req.readLong();

        klue.getAllPL(a, new KlueCallback<ArrayList<PositionList>>() {
            @Override
            public void callback(ArrayList<PositionList> value) {
                sink.sink(new PosListListResponse(value));
            }

            @Override
            public void exception(Exception e) {
                sink.sink(new ExceptionResponse(e.getMessage()));
            }
        });
    }
}
