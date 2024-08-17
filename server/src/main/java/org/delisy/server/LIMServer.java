package org.delisy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @Author LvWei
 * @Date 2024/7/26 14:55
 */
@Component
@Slf4j
public class LIMServer {

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();

    @Value("${im.server.port}")
    private int imServerPort;


    @PostConstruct
    private void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap().group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(imServerPort))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new LIMServerInitializer());
        ChannelFuture sync = bootstrap.bind().sync();
        if (sync.isSuccess()){
            log.info("服务启动成功");
        }
    }

    public static void main(String[] args) {
        System.out.println(new BigDecimal(999 * 100).divide(new BigDecimal(999229), 2, RoundingMode.HALF_UP).doubleValue());
    }

}
