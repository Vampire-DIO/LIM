package org.delisy.util;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:13
 */
public class UserChannelMap {

    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<String, NioSocketChannel>(32);

    public static void saveChannel(String userId, NioSocketChannel channel){
        CHANNEL_MAP.put(userId, channel);
    }

    public static NioSocketChannel getChannel(String userId){
        return CHANNEL_MAP.get(userId);
    }

    public static void removeChannel(String userId){
        CHANNEL_MAP.remove(userId);
    }


    public static String getUserId(Channel channel) {
        for (Map.Entry<String, NioSocketChannel> entry : CHANNEL_MAP.entrySet()) {
            if (entry.getValue() == channel) {
                return entry.getKey();
            }
        }
        return null;
    }
}
