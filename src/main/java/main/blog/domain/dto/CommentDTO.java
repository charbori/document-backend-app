package main.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.entity.CommentEntity;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO implements Serializable {
    private Long id;
    @NotBlank
    private String content;
    @NotBlank
    private UserEntity userEntity;
    @NotBlank
    private PostEntity postEntity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentEntity toEntity() {
        return CommentEntity.builder()
                .post(postEntity)
                .user(userEntity)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public CommentEntity toUpdateEntity() {
        return CommentEntity.builder()
                .id(id)
                .post(postEntity)
                .user(userEntity)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static CommentDTO toDto(CommentEntity commentEntity) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());
        commentDTO.setContent(commentEntity.getContent());
        commentDTO.setUserEntity(commentEntity.getUser());
        commentDTO.setPostEntity(commentEntity.getPost());
        commentDTO.setUpdatedAt(commentEntity.getUpdatedAt());
        commentDTO.setCreatedAt(commentEntity.getCreatedAt());
        return commentDTO;
    }
}
