package org.delisy.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.delisy.constant.Constants;
import org.delisy.constant.MsgTypeEnums;
import org.delisy.entity.MessageContainer;
import org.delisy.entity.dto.MessageDTO;
import org.delisy.entity.dto.UnReadMsgDTO;
import org.delisy.entity.dto.UserInfo;
import org.delisy.entity.res.ResultBase;
import org.delisy.service.ClientService;
import org.delisy.utils.HttpUtils;
import org.delisy.utils.LocalDataCache;
import org.delisy.utils.ZkUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


/**
 * @Author LvWei
 * @Date 2024/7/26 14:19
 */

@Component
@Slf4j
public class LIMClient {
    private SocketChannel channel;

    private static final String userId  = "a";

    @Resource
    private ZkUtils zkUtils;

    @Value("${app.route.url}")
    private String routeUrl;

    @Resource
    private ClientService clientService;

    @PostConstruct
    private void start(){
        ExecutorService executorService = ThreadUtil.newSingleExecutor();
        executorService.submit(()->{
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("clm-work"));
            try {
                LocalDataCache.set(Constants.USER_ID, userId);
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ClientInitializer());
                log.info("客户端启动成功");
                List<String> serverNodeList = zkUtils.getAllNode();
                if (serverNodeList == null || serverNodeList.isEmpty()){
                    log.error("没有可用的服务节点");
                    return;
                }
                String server = serverNodeList.get((int) (Math.random() % serverNodeList.size()));
                String[] serverSplit = server.split(":");
                String ip = serverSplit[0];
                int imPort = Integer.parseInt(serverSplit[1]);
                int httpPort = Integer.parseInt(serverSplit[2]);

                LocalDataCache.set(Constants.SERVER_IP, ip);
                LocalDataCache.set(Constants.SERVER_IM_PORT, String.valueOf(imPort));
                LocalDataCache.set(Constants.SERVER_WEB_PORT, String.valueOf(httpPort));

                ChannelFuture channelFuture = bootstrap.connect(ip, imPort).sync();
                channel = (SocketChannel) channelFuture.channel();
                loginInServer();
                channel.closeFuture().sync();
            }catch (Exception e){
                log.error("客户端启动失败",e);
            }finally {
                eventLoopGroup.shutdownGracefully();
            }
        });
    }

    private void loginInServer(){

        MessageDTO messageDTO = new MessageDTO()
                .setFromUserId(userId)
                .setType(MsgTypeEnums.LOGIN);
        ChannelFuture future = channel.writeAndFlush(Unpooled.copiedBuffer(JSONUtil.toJsonStr(messageDTO), CharsetUtil.UTF_8));
        future.addListener((ChannelFutureListener) channelFuture -> log.info("注册成功"));

        getAllUnreadMsg();
    }

    private void getAllUnreadMsg(){
        Map<String, List<UnReadMsgDTO>> unReadMsg = clientService.getUnReadMsg(userId);

        unReadMsg.forEach((k,v) -> {
            System.out.println("用户发来消息: ");
            v.forEach(m->{
                System.out.println(m.getFromUserId() + " 说: ");
                System.out.print(m.getMsg() + " " + DateUtil.format(m.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                System.out.println();
            });
            clientService.clearUnReadMsg(userId,k);
        });
    }

    public boolean sendMessage(String msg, String toUserId){
        MessageDTO messageDTOBuilder = new MessageDTO()
                .setToUserId(toUserId)
                .setFromUserId(userId)
                .setMsg(msg)
                .setType(MsgTypeEnums.CHAT);
        try {
            String post = HttpUtils.post(routeUrl + "/route/sendMsg", JSONUtil.toJsonStr(messageDTOBuilder));
            log.info("客户端发送消息成功:{}",post);
            ResultBase bean = JSONUtil.toBean(post, ResultBase.class);
            Assert.isTrue(bean.getCode().equals(200), bean.getMsg());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean sendMessageGroup(String msg, String groupId){

        return true;
    }


}
