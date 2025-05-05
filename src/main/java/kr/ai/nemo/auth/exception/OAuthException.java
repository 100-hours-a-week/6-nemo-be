package kr.ai.nemo.auth.exception;

import lombok.Getter;

@Getter
public class OAuthException extends RuntimeException {
    private final String code;
    private final String message;

    public OAuthException(OAuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public OAuthException(OAuthErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }
}
