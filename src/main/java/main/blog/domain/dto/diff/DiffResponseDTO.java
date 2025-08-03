package main.blog.domain.dto.diff;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.DiffResultEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffResponseDTO {
    
    private Long id;
    
    private UserInfoDTO user;
    
    private DocumentDTO originalDocument;
    
    private DocumentDTO compareDocument;
    
    // 문서 ID들을 별도로 제공
    private Long originalDocumentId;
    
    private Long compareDocumentId;
    
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