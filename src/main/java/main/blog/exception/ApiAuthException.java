package main.blog.exception;

public class ApiAuthException extends RuntimeException {
    public ApiAuthException() {
        super();
    }

    public ApiAuthException(String message) {
        super(message);
    }

    public ApiAuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiAuthException(Throwable cause) {
        super(cause);
    }

    protected ApiAuthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
