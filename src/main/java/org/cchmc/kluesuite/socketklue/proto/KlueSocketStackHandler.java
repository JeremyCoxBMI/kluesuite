package org.cchmc.kluesuite.socketklue.proto;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * @author Joseph Hirschfeld (Joseph.Hirschfeld@cchmc.org
 * @since 1/19/17
 */
public class KlueSocketStackHandler implements ChannelHandler {

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        channelHandlerContext.pipeline().addLast("requestEncoder", new KlueRequestNumberer());
        channelHandlerContext.pipeline().addLast("requestDecoder", new KlueRequestNumberDecoder());
        channelHandlerContext.pipeline().addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        channelHandlerContext.pipeline().addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {

    }
}
