package com.netty.rpc.server.netty;

import com.netty.rpc.codec.*;
import com.netty.rpc.serializer.Serializer;
import com.netty.rpc.serializer.kryo.KryoSerializer;
import com.netty.rpc.server.netty.handler.BusinessHandler;
import com.netty.rpc.server.netty.handler.HeartBeatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> serviceKey2BeanMap;
    private ThreadPoolExecutor businessTaskThreadPool;

    public RpcServerInitializer(Map<String, Object> serviceKey2BeanMap, ThreadPoolExecutor businessTaskThreadPool) {
        this.serviceKey2BeanMap = serviceKey2BeanMap;
        this.businessTaskThreadPool = businessTaskThreadPool;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        cp.addLast(new RpcDecoder(RpcRequest.class, serializer));
        cp.addLast(new RpcEncoder(RpcResponse.class, serializer));
        cp.addLast(new HeartBeatHandler());
        cp.addLast(new BusinessHandler(serviceKey2BeanMap, businessTaskThreadPool));
    }
}
