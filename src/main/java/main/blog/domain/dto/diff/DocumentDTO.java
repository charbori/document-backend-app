package main.blog.domain.dto.diff;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.DocumentEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    
    private Long id;
    
    private UserInfoDTO user;
    
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    
    @NotNull(message = "내용은 필수입니다")
    private String content;
    
    private String description;
    
    private DocumentEntity.DocumentStatus status;
    
    private String fileName;
    
    private String fileType;
    
    private String version;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 