package org.delisy.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author LvWei
 * @Date 2024/8/2 16:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UnReadMsgDTO extends MessageDTO{
    private LocalDateTime createTime;

    public UnReadMsgDTO(MessageDTO messageDTO){
        this.setFromUserId(messageDTO.getFromUserId());
        this.setMsg(messageDTO.getMsg());
        this.setToUserId(messageDTO.getToUserId());
        this.setType(messageDTO.getType());
        this.createTime = LocalDateTime.now();
    }
}
