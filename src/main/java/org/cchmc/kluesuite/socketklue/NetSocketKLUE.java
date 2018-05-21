package org.cchmc.kluesuite.socketklue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.cchmc.kluesuite.klue.AsyncKLUE;
import org.cchmc.kluesuite.klue.KlueCallback;
import org.cchmc.kluesuite.klue.PositionList;
import org.cchmc.kluesuite.socketklue.client.KlueClientSocketStackHandler;
import org.cchmc.kluesuite.socketklue.client.KlueResponseActor;
import org.cchmc.kluesuite.socketklue.client.KlueResponseHandler;
import org.cchmc.kluesuite.socketklue.client.RequestWrapper;
import org.cchmc.kluesuite.socketklue.client.decoder.*;
import org.cchmc.kluesuite.socketklue.client.request.*;

import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * Created by joe on 3/8/17.
 */
public class NetSocketKLUE implements AsyncKLUE {

    private final Channel c;

    private NetSocketKLUE(Channel c){
        this.c = c;
    }

    @Override
    public void put(long key, ArrayList<Long> positions, KlueCallback<Void> response) {
        c.writeAndFlush(new RequestWrapper(
                new PutRequest(key, positions),
                new KlueResponseHandler<>(response, KlueOKDecoder.i)
        ));
    }

    @Override
    public void append(long key, long pos, KlueCallback<Void> response) {
        c.writeAndFlush(new RequestWrapper(
                new AppendRequest(key, pos),
                new KlueResponseHandler<>(response, KlueOKDecoder.i)
        ));
    }

    @Override
    public void get(long key, KlueCallback<ArrayList<Long>> response) {
        c.writeAndFlush(new RequestWrapper(
                new GetRequest(key),
                new KlueResponseHandler<>(response, KlueLongListDecoder.i)
        ));
    }

    @Override
    public void getAll(long[] keys, KlueCallback<ArrayList<ArrayList<Long>>> response) {
        c.writeAndFlush(new RequestWrapper(
                new GetAllRequest(keys),
                new KlueResponseHandler<>(response, KlueLongListListDecoder.i)
        ));
    }

    @Override
    public void getAllPL(long[] keys, KlueCallback<ArrayList<PositionList>> response) {
        c.writeAndFlush(new RequestWrapper(
                new GetAllPlRequest(keys),
                new KlueResponseHandler<>(response, KluePosListListDecoder.i)
        ));
    }

    @Override
    public void getShortKmerMatches(long shorty, int prefixLength, KlueCallback<PositionList> response) {
        c.writeAndFlush(new RequestWrapper(
                new GetShortKmerRequest(shorty, prefixLength),
                new KlueResponseHandler<>(response, KluePosListDecoder.i)
        ));
    }

    @Override
    public void shutdown(KlueCallback<Void> v) {
        try {
            c.close();
            v.callback(null);
        } catch (Exception e) {
            v.exception(e);
        }
    }

    public static NetSocketKLUE connect(SocketAddress address) {
        ChannelFuture connect = new Bootstrap()
                .handler(new KlueClientSocketStackHandler()).connect(address);
        connect.awaitUninterruptibly();
        return new NetSocketKLUE(connect.channel());
    }
}
