package main.blog.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import main.blog.domain.dto.UserInfoDTO;

@Entity
@Data
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    private String username;
    private String password;
    private String role;
    private String verification;
    private String verificationCode;

    @CreatedDate
    private LocalDateTime verificationAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public UserInfoDTO toUserInfoDTO() {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setId(this.id);
        userInfoDTO.setUsername(this.username);
        userInfoDTO.setRole(this.role);
        return userInfoDTO;
    }
}
