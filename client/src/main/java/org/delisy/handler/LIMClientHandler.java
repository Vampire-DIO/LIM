package org.delisy.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.delisy.constant.Constants;
import org.delisy.constant.MsgTypeEnums;
import org.delisy.entity.dto.MessageDTO;
import org.delisy.utils.HeartBeatUtil;
import org.delisy.utils.LocalDataCache;

/**
 * @Author LvWei
 * @Date 2024/7/29 15:50
 */
@Slf4j
@ChannelHandler.Sharable
public class LIMClientHandler extends SimpleChannelInboundHandler<String> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        log.info("客户端：{} 收到消息：{}", LocalDataCache.get(Constants.USER_ID), msg);
        if (StrUtil.isEmpty(msg)){
            return;
        }
        MessageDTO messageDTO = JSONUtil.toBean(msg, MessageDTO.class);
        if (messageDTO.getType().equals(MsgTypeEnums.PONG)){
            // 心跳回复
            HeartBeatUtil.updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
            log.info("客户端：{} 收到服务端心跳检测回复", LocalDataCache.get(Constants.USER_ID));
        }

        if (messageDTO.getType().equals(MsgTypeEnums.PING)){
            // 心跳回复
            log.info("客户端：{} 收到心跳检测PING", LocalDataCache.get(Constants.USER_ID));
            HeartBeatUtil.updateReaderTime(channelHandlerContext.channel(), System.currentTimeMillis());
            MessageDTO heartBeat = new MessageDTO().setType(MsgTypeEnums.PONG);
            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(JSONUtil.toJsonStr(heartBeat), CharsetUtil.UTF_8)).addListeners((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    log.error("IO error,close Channel");
                    future.channel().close();
                }
            });
        }
        if (messageDTO.getType().equals(MsgTypeEnums.CHAT)){
            System.out.println("用户: " + messageDTO.getFromUserId() + " : " + messageDTO.getMsg());
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            log.info("客户端：{} 读写超时", LocalDataCache.get(Constants.USER_ID));
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                MessageDTO heartBeat = new MessageDTO()
                        .setFromUserId(LocalDataCache.get(Constants.USER_ID))
                        .setType(MsgTypeEnums.PING);
                ctx.writeAndFlush(Unpooled.copiedBuffer(JSONUtil.toJsonStr(heartBeat), CharsetUtil.UTF_8)).addListeners((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        log.error("IO error,close Channel");
                        future.channel().close();
                    }
                });
            }

        }
    }
}
