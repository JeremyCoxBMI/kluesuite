package org.cchmc.kluesuite.socketklue.server;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketStackHandler;

/**
 * Created by joe on 3/8/17.
 */
@AllArgsConstructor
public class KlueServerSocketStackHandler extends KlueSocketStackHandler {

    private final AsyncKLUE klue;

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        channelHandlerContext.pipeline().addLast("protoCodex", new KlueServerProtoCodex(klue));
        super.handlerAdded(channelHandlerContext);
    }
}
