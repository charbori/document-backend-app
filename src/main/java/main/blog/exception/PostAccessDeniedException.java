package main.blog.exception;

public class PostAccessDeniedException extends RuntimeException {
    public PostAccessDeniedException() {
        super();
    }

    public PostAccessDeniedException(String message) {
        super(message);
    }

    public PostAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PostAccessDeniedException(Throwable cause) {
        super(cause);
    }

    protected PostAccessDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
