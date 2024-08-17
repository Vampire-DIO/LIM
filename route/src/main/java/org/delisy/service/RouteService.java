package org.delisy.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.delisy.constant.Constants;
import org.delisy.entity.dto.MessageDTO;
import org.delisy.entity.dto.UnReadMsgDTO;
import org.delisy.entity.req.CreateGroupReq;
import org.delisy.entity.req.JoinGroupReq;
import org.delisy.entity.res.GroupUnReadMsgRes;
import org.delisy.exception.LIMException;
import org.delisy.utils.HttpUtils;
import org.delisy.utils.LocalDataCache;
import org.delisy.utils.RedisUtils;
import org.delisy.utils.ZkUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author LvWei
 * @Date 2024/8/2 15:36
 */
@Service
@Slf4j
public class RouteService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ZkUtils zkUtils;


    public void sendMsg(MessageDTO messageDTO) {
        Assert.notBlank(messageDTO.getFromUserId(), "fromUserId 不能为空");
        Assert.notBlank(messageDTO.getToUserId(), "toUserId 不能为空");
        Assert.notBlank(messageDTO.getMsg(), "msg 不能为空");
        Assert.notNull(messageDTO.getType(), "type 不能为空");

        String targetServerUrl = LocalDataCache.get(String.format(Constants.USER_ID_BIND_SERVER, messageDTO.getToUserId()));
        if (StrUtil.isEmpty(targetServerUrl)) {
            Object o = redisUtils.get(String.format(Constants.USER_ID_BIND_SERVER, messageDTO.getToUserId()));
            if (o != null) {
                String serverUrl = o.toString();
                String[] split = serverUrl.split(":");
                targetServerUrl = "http://" + split[0] + ":" + split[2] + "/sendMsg";
                try {
                    HttpUtils.post(targetServerUrl, JSONUtil.toJsonStr(messageDTO));
                } catch (IOException e) {
                    log.warn("route 转发消息失败", e);
                    checkServerAlive(targetServerUrl, String.format(Constants.USER_ID_BIND_SERVER, messageDTO.getToUserId()));
                    throw new RuntimeException(e);
                }

                LocalDataCache.set(String.format(Constants.USER_ID_BIND_SERVER, messageDTO.getToUserId()), targetServerUrl);
            } else {
                log.info("用户{}离线", messageDTO.getToUserId());
                String uniqueKey = UUID.randomUUID().toString();
                String unReadMsgLockKey = String.format(Constants.UN_READ_MSG_LOCK, messageDTO.getToUserId());
                String unReadMsgKey = String.format(Constants.UN_READ_MSG, messageDTO.getToUserId());
                try {
                    redisUtils.tryLock(unReadMsgLockKey, 10, 1000, uniqueKey, 3);
                    String msgJson;
                    List<UnReadMsgDTO> list;
                    if (redisUtils.hHasKey(unReadMsgKey, messageDTO.getFromUserId())) {
                        msgJson = (String) redisUtils.hget(unReadMsgKey, messageDTO.getFromUserId());
                        list = JSONArray.parseArray(msgJson, UnReadMsgDTO.class);
                    } else {
                        list = new ArrayList<>();
                    }
                    UnReadMsgDTO unReadMsgDTO = new UnReadMsgDTO(messageDTO);
                    list.add(unReadMsgDTO);
                    redisUtils.hset(unReadMsgKey, messageDTO.getFromUserId(), JSONUtil.toJsonStr(list));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    redisUtils.unLock(unReadMsgLockKey, uniqueKey);
                }
            }
        }
    }


    private void checkServerAlive(String targetServerUrl, String redisKey) {
        List<String> serverNodeList = zkUtils.getAllNode();
        if (serverNodeList == null || serverNodeList.isEmpty()) {
            log.error("没有可用的服务节点");
            redisUtils.del(redisKey);
            return;
        }
        List<String> httpServerUrlList = serverNodeList.stream().map(s -> {
            String[] split = s.split(":");
            return "http://" + split[0] + ":" + split[2] + "/sendMsg";
        }).collect(Collectors.toList());

        if (!httpServerUrlList.contains(targetServerUrl)) {
            log.error("该服务器不可用，请检查服务节点是否可用, {}", targetServerUrl);
            redisUtils.del(redisKey);
        }
    }


    public void createGroup(CreateGroupReq req) {
        String groupId = System.currentTimeMillis() + UUID.randomUUID().toString().replaceAll("-", "");
        redisUtils.hset(String.format(Constants.GROUP, groupId), Constants.GROUP_NAME, req.getGroupName());
        redisUtils.hset(String.format(Constants.GROUP, groupId), Constants.GROUP_OWNER, req.getCreatorId());
        redisUtils.hset(String.format(Constants.GROUP, groupId), Constants.GROUP_DESC, req.getGroupDesc());
        redisUtils.hset(String.format(Constants.GROUP_MEMBER, groupId), req.getCreatorId(), "owner");
        redisUtils.hset(Constants.ALL_GROUP_INFO, groupId, JSONUtil.toJsonStr(req));
    }

    public void joinGroup(JoinGroupReq req) {
        Assert.isTrue(StrUtil.isNotBlank(req.getGroupId()), "groupId 不能为空");
        Assert.isTrue(StrUtil.isNotBlank(req.getJoinUserId()), "joinUserId 不能为空");
        String groupKey = String.format(Constants.GROUP, req.getGroupId());
        String groupMemberKey = String.format(Constants.GROUP_MEMBER, req.getGroupId());
        if (!redisUtils.hasKey(groupKey)) {
            throw new LIMException("群组不存在");
        }
        if (!redisUtils.hasKey(groupMemberKey)) {
            throw new LIMException("群组不存在");
        }
        redisUtils.hset(String.format(Constants.GROUP_MEMBER, req.getGroupId()), req.getJoinUserId(), "member");
    }

    public List<GroupUnReadMsgRes> getGroupUnReadMsg(String userId) {
        List<GroupUnReadMsgRes> res = new ArrayList<>();
        String msgKey = String.format(Constants.UN_READ_GROUP_MSG, userId);
        Map<Object, Object> msgMap = redisUtils.hmget(msgKey);
        Map<Object, Object> groupInfoMap = redisUtils.hmget(Constants.ALL_GROUP_INFO);
        if (!CollectionUtils.isEmpty(msgMap)) {
            msgMap.forEach((k, v) -> {
                GroupUnReadMsgRes dto = new GroupUnReadMsgRes();
                List<UnReadMsgDTO> msg = JSONUtil.toList(JSONUtil.toJsonStr(v), UnReadMsgDTO.class);
                dto.setUnReadMsgList(msg);
                dto.setGroupId((String) k);
                Object o1 = groupInfoMap.get(k);
                if (o1 == null){
                    throw new LIMException("群组信息不存在");
                }
                CreateGroupReq groupInfo = JSON.parseObject((String) o1, CreateGroupReq.class);
                dto.setGroupName(groupInfo.getGroupName());
                res.add(dto);
            });
        }
        return res;
    }

    public boolean sendGroupMsg(MessageDTO messageDTO) {
        Object groupInfo = redisUtils.hget(Constants.ALL_GROUP_INFO, messageDTO.getToUserId());
        if (groupInfo == null){
            throw new LIMException("群组不存在");
        }


        return true;
    }
}
