package org.delisy.exception;

import lombok.Getter;

/**
 * @Author LvWei
 * @Date 2024/8/9 14:40
 */
@Getter
public class LIMException extends RuntimeException {

    private String errorMessage;

    private int errorCode;

    public LIMException(String message, int errorCode) {
        this.errorMessage = message;
        this.errorCode = errorCode;
    }

    public LIMException(String message) {
        super(message);
    }

    public LIMException(String message, Throwable cause) {
        super(message, cause);
    }

}
