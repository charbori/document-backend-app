package main.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.entity.CommentEntity;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentApiDTO {
    private Long id;
    @NotBlank
    private String content;
    @NotBlank
    private Long user_id;
    @NotBlank
    private Long post_id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentApiDTO toApiDTO(CommentEntity commentEntity) {
        return CommentApiDTO.builder()
                .id(commentEntity.getId())
                .post_id(commentEntity.getPost().getId())
                .user_id(commentEntity.getUser().getId())
                .content(commentEntity.getContent())
                .createdAt(commentEntity.getCreatedAt())
                .updatedAt(commentEntity.getUpdatedAt())
                .build();
    }

    public CommentDTO commentDTO(UserEntity user, PostEntity post) {
        return CommentDTO.builder()
                .postEntity(post)
                .userEntity(user)
                .content(this.content)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public CommentEntity toEntity(UserEntity user, PostEntity post) {
        return CommentEntity.builder()
                .id(this.id)
                .post(post)
                .user(user)
                .content(this.content)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
