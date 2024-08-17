package org.delisy.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.delisy.constant.Constants;
import org.delisy.constant.MsgTypeEnums;
import org.delisy.entity.dto.MessageDTO;
import org.delisy.entity.dto.UnReadMsgDTO;
import org.delisy.service.HeartBeatService;
import org.delisy.service.impl.ServerHeartBeatServiceImpl;
import org.delisy.utils.LocalDataCache;
import org.delisy.util.UserChannelMap;
import org.delisy.utils.HeartBeatUtil;
import org.delisy.utils.HttpUtils;
import org.delisy.utils.RedisUtils;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:26
 */
@Slf4j
@ChannelHandler.Sharable
public class LIMHandler extends SimpleChannelInboundHandler<String> {

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("服务端收到消息:{}",msg);
        MessageDTO messageDTO = JSONUtil.toBean(msg, MessageDTO.class);
        RedisUtils redisUtils = SpringUtil.getBean(RedisUtils.class);

        if (messageDTO.getType().equals(MsgTypeEnums.LOGIN)){
            UserChannelMap.saveChannel(messageDTO.getFromUserId(), (NioSocketChannel) ctx.channel());
            String serverUrlConfig = LocalDataCache.get(Constants.SERVER_URL);
            String[] split = serverUrlConfig.split("/");
            redisUtils.set(String.format(Constants.USER_ID_BIND_SERVER, messageDTO.getFromUserId()), split[2]);
            log.info("用户{}登录成功",messageDTO.getFromUserId());
        }
        if (messageDTO.getType().equals(MsgTypeEnums.PING)){
            // 心跳回复
            HeartBeatUtil.updateReaderTime(ctx.channel(), System.currentTimeMillis());
            MessageDTO heartBeat = new  MessageDTO()
                    .setType(MsgTypeEnums.PONG);
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(JSONUtil.toJsonStr(heartBeat), CharsetUtil.UTF_8)).addListeners((ChannelFutureListener) future -> {
                if (future.isSuccess()){
                    log.info("用户{}心跳成功",messageDTO.getFromUserId());
                }else {
                    log.info("用户{}心跳失败",messageDTO.getFromUserId());
                    ctx.channel().close();
                }
            });
        }
        if (messageDTO.getType().equals(MsgTypeEnums.CHAT)){
            // 单聊消息
            String toUserId = messageDTO.getToUserId();
            NioSocketChannel channel = UserChannelMap.getChannel(toUserId);
            if (channel == null){
                // 用户下线 存入消息队列
                String uniqueKey = UUID.randomUUID().toString();
                String unReadMsgKey = String.format(Constants.UN_READ_MSG, toUserId);
                try {
                    redisUtils.tryLock(unReadMsgKey, 10, 1000, uniqueKey,3);
                    Object hget = redisUtils.hget(unReadMsgKey, messageDTO.getFromUserId());
                    if (hget != null){
                        String msgJson = (String) hget;
                        List<UnReadMsgDTO> list = JSONUtil.toList(msgJson, UnReadMsgDTO.class);
                        UnReadMsgDTO unReadMsgDTO = new UnReadMsgDTO(messageDTO);
                        list.add(unReadMsgDTO);
                        redisUtils.hset(unReadMsgKey, messageDTO.getFromUserId(), JSONUtil.toJsonStr(list));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    redisUtils.unLock(unReadMsgKey, uniqueKey);
                }
            }else {
                channel.writeAndFlush(Unpooled.copiedBuffer(JSONUtil.toJsonStr(messageDTO), CharsetUtil.UTF_8));
                log.info("用户{}在线，发送消息成功", toUserId);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE){
                log.info("定时检测客户端存活...");
                HeartBeatService heartBeatService = SpringUtil.getBean(ServerHeartBeatServiceImpl.class);
                heartBeatService.process(ctx);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String userId = UserChannelMap.getUserId(ctx.channel());
        if (userId != null){
            log.warn("用户{}离线", userId);
            UserChannelMap.removeChannel(userId);
            RedisUtils redisUtils = SpringUtil.getBean(RedisUtils.class);
            redisUtils.del(String.format(Constants.USER_ID_BIND_SERVER, userId));
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端异常", cause);
    }
}
