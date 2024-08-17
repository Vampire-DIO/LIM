package org.delisy.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.delisy.handler.LIMHandler;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:52
 */
public class LIMServerInitializer extends ChannelInitializer<SocketChannel> {

    private LIMHandler limHandler = new LIMHandler();


    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new IdleStateHandler(11,0,0))
                .addLast(new StringDecoder())
                .addLast(new StringEncoder())
                .addLast(limHandler);
    }
}
