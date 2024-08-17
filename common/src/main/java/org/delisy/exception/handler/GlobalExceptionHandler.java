package org.delisy.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.delisy.entity.res.ResultBase;
import org.delisy.exception.LIMException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author LvWei
 * @Date 2024/8/9 14:43
 */
@ResponseBody
@ControllerAdvice
@Slf4j
@Order(1)
public class GlobalExceptionHandler {


    @ExceptionHandler(LIMException.class)
    public ResultBase<String> handlerLIMException(LIMException e){
        log.error(e.getMessage());
        return ResultBase.fail(e.getErrorMessage(), e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResultBase<String> handlerException(Exception e){
        log.error(e.getMessage());
        return ResultBase.fail("服务器异常",5000);
    }


}
