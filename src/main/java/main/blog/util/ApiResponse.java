package main.blog.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {
    private HttpHeaders header;
    private T data;

    private ApiResponse(HttpHeaders header, T data) {
        this.header = header;
        this.data = data;
    }

    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok()
                .body(data);
    }

    public static <T> ResponseEntity<T> fail(T data, HttpStatus httpStatus) {
        HttpHeaders headers= new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        return new ResponseEntity<T>(data, headers, httpStatus);
    }

    public static <T> ResponseEntity<T> fail(HttpHeaders apiHeader, T data, HttpStatus httpStatus) {
        return new ResponseEntity<T>(data, apiHeader, httpStatus);
    }
}
