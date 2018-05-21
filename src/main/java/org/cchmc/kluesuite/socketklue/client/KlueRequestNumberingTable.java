package org.cchmc.kluesuite.socketklue.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.cchmc.kluesuite.socketklue.proto.NumberedInPacket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/19/17
 */
public class KlueRequestNumberingTable extends ChannelDuplexHandler {

    private final AtomicLong idx = new AtomicLong();
    private final ConcurrentHashMap<Long, KlueResponseHandler> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        RequestWrapper wrapper = ((RequestWrapper) msg);
        Long index;
        do {
            index = idx.getAndIncrement();
        } while (handlerMap.putIfAbsent(index, wrapper.getHandler()) != null);
        ctx.write(wrapper.getPacket(), promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NumberedInPacket in = ((NumberedInPacket) msg);
        long requestNumber = in.getRequestNumber();
        KlueResponseHandler handler = handlerMap.remove(requestNumber);
        if (handler == null)
            throw new Exception("No handler for request (" + requestNumber + ")!");

        handler.handle(in.getData());
    }
}
