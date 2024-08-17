package org.delisy.entity.req;

import lombok.Data;
import org.delisy.constant.MsgTypeEnums;

/**
 * @Author LvWei
 * @Date 2024/7/30 15:26
 */
@Data
public class SendMsgReq {

    private String msg;

    private String toUserId;

}
