package main.blog.domain.dto.diff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.DiffResultEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffResponseDTO {
    
    private Long id;
    
    private UserInfoDTO user;
    
    private DocumentDTO originalDocument;
    
    private DocumentDTO compareDocument;
    
    private String diffResult;
    
    private String htmlDiff;
    
    private String diffTitle;
    
    private String diffType;
    
    private DiffResultEntity.DiffStatus status;
    
    private int addedLines;
    
    private int deletedLines;
    
    private int modifiedLines;
    
    private LocalDateTime createdAt;
    
    private List<DiffLineDTO> diffLines;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiffLineDTO {
        private int lineNumber;
        private String operation; // ADD, DELETE, MODIFY, EQUAL
        private String originalContent;
        private String compareContent;
        private String cssClass;
    }
} 