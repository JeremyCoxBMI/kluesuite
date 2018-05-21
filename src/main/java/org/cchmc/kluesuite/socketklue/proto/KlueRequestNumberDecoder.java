package org.cchmc.kluesuite.socketklue.proto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/19/17
 */
public class KlueRequestNumberDecoder extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = ((ByteBuf) msg);
        long reqNum = byteBuf.readLong();
        ByteBuf request = byteBuf.slice();
        ctx.fireChannelRead(new NumberedInPacket(reqNum, request));
    }
}
