package org.delisy.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.delisy.handler.LIMClientHandler;

/**
 * @Author LvWei
 * @Date 2024/7/29 15:48
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new StringDecoder())
                .addLast(new StringEncoder())
                .addLast(new IdleStateHandler(0,10,0))
                .addLast(new LIMClientHandler());
    }
}
