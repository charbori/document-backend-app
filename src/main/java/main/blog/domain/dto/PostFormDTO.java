package main.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class PostFormDTO {

    private long id;

    @NotBlank
    private String title;

    @NotBlank
    private String content;
    private LocalDateTime createdAt;

    private String username;

    private MultipartFile imageName;

    public PostDTO getPostDTO() {
        PostDTO postDTO = new PostDTO();
        postDTO.setContent(content);
        postDTO.setTitle(title);
        postDTO.setUsername(username);
        postDTO.setCreatedAt(createdAt);
        return postDTO;
    }
}
