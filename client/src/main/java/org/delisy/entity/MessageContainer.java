package org.delisy.entity;

import lombok.Data;
import org.delisy.entity.dto.UnReadMsgDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author LvWei
 * @Date 2024/8/8 17:27
 */
@Data
public class MessageContainer {

    private Map<String, List<UnReadMsgDTO>> messages;


    public MessageContainer(){

    }

}
