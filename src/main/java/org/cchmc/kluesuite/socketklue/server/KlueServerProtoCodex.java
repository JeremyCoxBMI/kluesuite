package org.cchmc.kluesuite.socketklue.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.socketklue.proto.NumberedInPacket;
import org.cchmc.kluesuite.socketklue.server.handler.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joe on 3/3/17.
 */
public class KlueServerProtoCodex extends ChannelInboundHandlerAdapter {

    private final Map<Integer, KlueProtoCodexHandler> handlers;
    private final KlueProtoCodexHandler defHndlr;

    public KlueServerProtoCodex(AsyncKLUE klue) {
        handlers = new HashMap<>();
        handlers.put(0x00, new PutHandler(klue));
        handlers.put(0x01, new AppendHandler(klue));
        handlers.put(0x02, new GetHandler(klue));
        handlers.put(0x03, new GetAllHandler(klue));
        handlers.put(0x04, new GetAllPlHandler(klue));
        handlers.put(0x05, new GetShortKmerHandler(klue));
        defHndlr = new UnsupportedHandler();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NumberedInPacket nip = ((NumberedInPacket) msg);
        Integer reqOp = (int) nip.getData().readByte();
        KlueProtoCodexHandler hndlr = handlers.get(reqOp);
        if (hndlr == null)
            hndlr = defHndlr;
        hndlr.handle(nip, ctx);
    }
}
