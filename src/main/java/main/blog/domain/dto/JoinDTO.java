package main.blog.domain.dto;

import lombok.Data;

@Data
public class JoinDTO {
    private String username;
    private String password;
    private String verificationCode;
}
