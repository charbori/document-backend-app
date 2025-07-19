package main.blog.domain.dto.diff;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.dto.UserInfoDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffRequestDTO {
    
    @NotNull(message = "원본 문서 ID는 필수입니다")
    private Long originalDocumentId;
    
    @NotNull(message = "비교 문서 ID는 필수입니다")
    private Long compareDocumentId;
    
    private String diffTitle;
    
    private String diffType = "text"; // text, word, line
    
    private UserInfoDTO user;
    
    // 직접 텍스트로 비교하는 경우
    private String originalText;
    private String compareText;
} 