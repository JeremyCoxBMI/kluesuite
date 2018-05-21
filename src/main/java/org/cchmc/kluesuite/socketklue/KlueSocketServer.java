package org.cchmc.kluesuite.socketklue;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KLUE;
import org.cchmc.kluesuite.socketklue.proto.KlueSocketStackHandler;
import org.cchmc.kluesuite.socketklue.server.KlueServerSocketStackHandler;

import java.net.SocketAddress;

public class KlueSocketServer implements AutoCloseable {

    private final Channel channel;
    private static final EventLoopGroup eventLoop = new EpollEventLoopGroup();

    private KlueSocketServer(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void close() throws Exception {
        channel.close();
    }

    public static KlueSocketServer bind(AsyncKLUE klue, SocketAddress bindAddress) {
        ChannelFuture f = new ServerBootstrap()
                .group(eventLoop)
                .channel(EpollServerSocketChannel.class)
                .childHandler(new KlueServerSocketStackHandler(klue))
                .bind(bindAddress);
        f.awaitUninterruptibly();
        return new KlueSocketServer(f.channel());
    }
}
