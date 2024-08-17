package org.delisy.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.delisy.constant.Constants;
import org.delisy.entity.MessageContainer;
import org.delisy.entity.dto.UnReadMsgDTO;
import org.delisy.utils.RedisUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author LvWei
 * @Date 2024/8/2 16:51
 */
@Service
@Slf4j
public class ClientService {


    @Resource
    private RedisUtils redisUtils;


    public Map<String, List<UnReadMsgDTO>> getUnReadMsg(String userId){
        Object o = redisUtils.hmget(String.format(Constants.UN_READ_MSG, userId));
        if (o != null){
            Map<String, String> map1 = JSONUtil.toBean(JSONUtil.toJsonStr(o), new TypeReference<Map<String, String>>(){},true);

            Map<String, List<UnReadMsgDTO>> map = new HashMap<>();
            map1.forEach((key, value) -> {
                List<UnReadMsgDTO> list = JSONUtil.toList(value,UnReadMsgDTO.class);
                map.put(key, list);
            });

            return map;
        }
        return new HashMap<>();
    }

    public boolean clearUnReadMsg(String userId, String fromUserId){
        redisUtils.hdel(String.format(Constants.UN_READ_MSG, userId), fromUserId);
        return true;
    }

}
