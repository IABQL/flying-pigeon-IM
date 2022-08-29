package com.iabql.nettyprovider8002.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

// 连接初始化
public class WSServerInitialzer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        // 读是从pipeline从上至下进行处理，，写是从pipeline从下至上进行处理

        // 获取管道（pipeline）
        ChannelPipeline pipeline = channel.pipeline();

        // websocket 基于http协议，所需要的http 编解码器
        pipeline.addLast(new HttpServerCodec());

        // 分块写数据
        pipeline.addLast(new ChunkedWriteHandler());

        /** 对httpMessage 进行聚合处理
         * 一个HTTP请求最少也会在HttpRequestDecoder里分成两次往后传递，第一次是消息行和消息头，第二次是消息体，
         * 哪怕没有消息体，也会传一个空消息体。如果发送的消息体比较大的话，
         * 可能还会分成好几个消息体来处理，往后传递多次，这样使得我们后续的处理器可能要写多个逻辑判断，
         * 比较麻烦，那能不能把消息都整合成一个完整的，再往后传递呢，当然可以，用HttpObjectAggregator。
         */
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        //===========================增加心跳支持==============================

        /**
         * 针对客户端，如果在12s时间内没有向服务端发送读写心跳（ALL），
         * 则会产生IdleState.READER_IDLE、IdleState.WRITER_IDLE、IdleState.ALL_IDLE事件
         */
        pipeline.addLast(new IdleStateHandler(8,10,12));
        // 自定义的空闲状态检测的handler，刚才的产生的事件交于该handler处理
        pipeline.addLast(new HeartBeatHandler());

        /**
         * 本handler 会帮你处理一些繁重复杂的事情
         * 会帮你处理握手动作：handshaking（close、ping、pong） ping+pong = 心跳
         * 对于websocket 来讲，都是以frams 进行传输的，不同的数据类型对应的frams 也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        // 自定义的handler，处理消息，将消息转发等操作
        pipeline.addLast(new ChatHandler());

    }
}
