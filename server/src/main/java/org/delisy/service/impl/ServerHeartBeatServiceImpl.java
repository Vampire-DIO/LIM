package org.delisy.service.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.delisy.service.HeartBeatService;
import org.delisy.util.UserChannelMap;
import org.delisy.utils.HeartBeatUtil;
import org.springframework.stereotype.Service;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:41
 */
@Slf4j
@Service
public class ServerHeartBeatServiceImpl implements HeartBeatService {

    private int heartBeatTimeConfig = 10;

    public void process(ChannelHandlerContext ctx) throws Exception {
        long heartBeatTime = heartBeatTimeConfig * 1000;

        Channel channel = ctx.channel();
        Long lastReadTime = HeartBeatUtil.getReaderTime(channel);
        if (lastReadTime != null){
            long diff = System.currentTimeMillis() - lastReadTime;
            if (diff > heartBeatTime){
                String userId =  UserChannelMap.getUserId(channel);
                if (userId != null){
                    log.info("用户{} 心跳超时【{}】, 链接断开关闭",userId, diff);
                    UserChannelMap.removeChannel(userId);
                    ctx.channel().close();
                }

            }
        }

    }

}
