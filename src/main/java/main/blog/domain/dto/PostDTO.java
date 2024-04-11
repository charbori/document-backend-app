package main.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.entity.CommentEntity;
import main.blog.domain.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO implements Serializable {

    private long id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
    private LocalDateTime createdAt;

    private String username;
    private String imageName;
    private ArrayList<CommentDTO> commentDTOS;
}
