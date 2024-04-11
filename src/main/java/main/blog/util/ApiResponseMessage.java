package main.blog.util;

import lombok.Data;

@Data
public class ApiResponseMessage<T> {
    private T data;
    private String message;

    public ApiResponseMessage(T data, java.lang.String message) {
        this.data = data;
        this.message = message;
    }
}
