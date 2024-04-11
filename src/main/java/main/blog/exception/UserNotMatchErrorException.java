package main.blog.exception;

public class UserNotMatchErrorException extends RuntimeException {
    public UserNotMatchErrorException() {
        super();
    }

    public UserNotMatchErrorException(String message) {
        super(message);
    }

    public UserNotMatchErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotMatchErrorException(Throwable cause) {
        super(cause);
    }

    protected UserNotMatchErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
