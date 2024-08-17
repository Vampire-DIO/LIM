package org.delisy.controller;

import org.delisy.entity.dto.MessageDTO;
import org.delisy.entity.req.CreateGroupReq;
import org.delisy.entity.res.ResultBase;
import org.delisy.service.RouteService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author LvWei
 * @Date 2024/7/30 14:45
 */
@RestController
@RequestMapping("/route")
public class RouteController {

    @Resource
    private RouteService routeService;

    @PostMapping("/sendMsg")
    public ResultBase<Boolean> sendMsg(@RequestBody MessageDTO messageDTO){
        routeService.sendMsg(messageDTO);
        return ResultBase.success(true);
    }

    @PostMapping("/createGroup")
    public ResultBase<Boolean> createGroup(@RequestBody CreateGroupReq req){
        routeService.createGroup(req);
        return ResultBase.success(true);
    }


}
