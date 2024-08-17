package org.delisy.entity.res;

import lombok.Data;
import org.delisy.entity.dto.UnReadMsgDTO;

import java.util.List;

/**
 * @Author LvWei
 * @Date 2024/8/9 16:10
 */
@Data
public class GroupUnReadMsgRes {

    private String groupId;

    private String groupName;

    private List<UnReadMsgDTO> unReadMsgList;
}
