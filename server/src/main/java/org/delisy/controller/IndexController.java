package org.delisy.controller;

import cn.hutool.json.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.delisy.entity.dto.MessageDTO;
import org.delisy.entity.dto.UserInfo;
import org.delisy.entity.res.ResultBase;
import org.delisy.service.ServerIndexService;
import org.delisy.util.UserChannelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:15
 */
@Slf4j
@RestController
@RequestMapping("/")
public class IndexController {


    @Resource
    private ServerIndexService service;


    @PostMapping("/login")
    public ResultBase<Boolean> login(UserInfo userInfo){
        return service.login(userInfo);
    }

    @PostMapping("/sendMsg")
    public ResultBase<Boolean> sendMsg(@RequestBody MessageDTO msgReq){
        log.info("sendMsg:{}",msgReq);
        NioSocketChannel channel = UserChannelMap.getChannel(msgReq.getToUserId());
        if (channel == null){
            // 下线或者其他原因
            log.warn("用户{}离线", msgReq.getToUserId());
            return ResultBase.fail("用户离线", 4111);
        }
        channel.writeAndFlush(Unpooled.copiedBuffer(JSONUtil.toJsonStr(msgReq).getBytes(CharsetUtil.UTF_8)));
        return ResultBase.success(true);
    }

}
