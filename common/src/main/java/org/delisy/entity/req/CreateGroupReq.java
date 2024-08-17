package org.delisy.entity.req;

import lombok.Data;

/**
 * @Author LvWei
 * @Date 2024/8/9 15:47
 */
@Data
public class CreateGroupReq {

    private String groupName;

    private String groupDesc;

    private String creatorId;
}
