package main.blog.exception;


public class AuthEncryptException extends RuntimeException {
    public AuthEncryptException() {
        super();
    }

    public AuthEncryptException(String message) {
        super(message);
    }

    public AuthEncryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthEncryptException(Throwable cause) {
        super(cause);
    }

    protected AuthEncryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
