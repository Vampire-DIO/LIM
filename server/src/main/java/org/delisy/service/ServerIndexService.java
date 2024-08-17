package org.delisy.service;

import org.delisy.entity.dto.UserInfo;
import org.delisy.entity.res.ResultBase;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:16
 */
public interface ServerIndexService {
    ResultBase<Boolean> login(UserInfo userInfo);
}
