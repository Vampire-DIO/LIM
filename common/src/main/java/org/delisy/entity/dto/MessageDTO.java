package org.delisy.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.delisy.constant.MsgTypeEnums;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:20
 */
@Data
@Accessors(chain = true)
public class MessageDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fromUserId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String toUserId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String msg;

    private MsgTypeEnums type;


}
