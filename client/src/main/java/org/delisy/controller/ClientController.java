package org.delisy.controller;

import org.delisy.client.LIMClient;
import org.delisy.entity.req.SendMsgReq;
import org.delisy.entity.res.ResultBase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author LvWei
 * @Date 2024/7/30 15:25
 */
@RestController
@RequestMapping("/client")
public class ClientController {


    @Resource
    private LIMClient limClient;

    @PostMapping("/sendMsg")
    public ResultBase<Boolean> sendMsg(@RequestBody SendMsgReq req){
        return ResultBase.success(limClient.sendMessage(req.getMsg(), req.getToUserId()));
    }



}
