package org.delisy.entity.res;

import lombok.Data;

/**
 * @Author LvWei
 * @Date 2024/7/29 14:17
 */
@Data
public class ResultBase<T> {

    private T data;

    private String msg;

    private Integer code;

    private String traceId;

    public static <T> ResultBase<T> success(T data){
        ResultBase<T> resultBase = new ResultBase<>();
        resultBase.setData(data);
        resultBase.setCode(200);
        resultBase.setMsg("success");
        return resultBase;
    }

    public static <T> ResultBase<T> fail(String msg, int code){
        ResultBase<T> resultBase = new ResultBase<>();
        resultBase.setCode(code);
        resultBase.setMsg(msg);
        return resultBase;
    }
}
