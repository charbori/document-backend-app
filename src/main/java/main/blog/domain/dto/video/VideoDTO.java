package main.blog.domain.dto.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.VideoEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoDTO implements Serializable {
    private long id;
    private UserInfoDTO user;
    @NotBlank
    private String name;
    private String description;
    @NotBlank
    private String status;
    private String thumbnailPath;
    private String videoPath;
    private String tag;
    private String videoType;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public VideoEntity toVideoEntity() {
        return VideoEntity.builder()
                .id(id)
                .name(name)
                .user(user.toUserEntity())
                .description(description)
                .status(status)
                .thumbnailPath(thumbnailPath)
                .videoPath(videoPath)
                .tag(tag)
                .videoType(videoType)
                .role(role)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
