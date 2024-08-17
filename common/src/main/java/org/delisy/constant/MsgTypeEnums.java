package org.delisy.constant;

import lombok.Getter;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:21
 */
@Getter
public enum MsgTypeEnums {

    LOGIN,
    LOGOUT,
    CHAT,
    PING,
    PONG,
    GROUP_CHAT;


    public static MsgTypeEnums getMsgType(String msgType){
        for (MsgTypeEnums msgTypeEnums : MsgTypeEnums.values()) {
            if(msgTypeEnums.name().equals(msgType)){
                return msgTypeEnums;
            }
        }
        throw new IllegalArgumentException("msg type " + msgType + "not found");
    }
}
