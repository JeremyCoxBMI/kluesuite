package org.cchmc.kluesuite.socketklue.proto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/19/17
 */
public class KlueRequestNumberer extends MessageToByteEncoder<NumberedOutPacket> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, NumberedOutPacket in, ByteBuf out) throws Exception {
        ByteBuf buf = channelHandlerContext.alloc().buffer();
        in.getPacket().serialize(buf);
        int bodyLen = buf.readableBytes();
        out.ensureWritable(8 + bodyLen);
        out.writeLong(in.getRequestNumber());
        out.writeBytes(buf, buf.readerIndex(), bodyLen);
    }
}
