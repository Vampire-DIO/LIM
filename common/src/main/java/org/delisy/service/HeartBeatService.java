package org.delisy.service;


import io.netty.channel.ChannelHandlerContext;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:42
 */
public interface HeartBeatService {

    void process(ChannelHandlerContext ctx) throws Exception;

}
