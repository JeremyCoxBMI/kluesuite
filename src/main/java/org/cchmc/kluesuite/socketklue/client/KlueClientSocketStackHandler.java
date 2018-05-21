package org.cchmc.kluesuite.socketklue.client;

import io.netty.channel.ChannelHandlerContext;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketStackHandler;

/**
 * Created by joe on 3/8/17.
 */
public class KlueClientSocketStackHandler extends KlueSocketStackHandler {
    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        channelHandlerContext.pipeline().addLast("reqNumberingTable", new KlueRequestNumberingTable());
        super.handlerAdded(channelHandlerContext);
    }
}
