package main.blog.domain.dto;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoDTO implements Serializable {

    private long id;

    // todo userdto로 변경하자
    private UserEntity user;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String status;
    @NotBlank
    private String thumbnailPath;
    @NotBlank
    private String videoPath;
    private String tag;
    private String videoType;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
