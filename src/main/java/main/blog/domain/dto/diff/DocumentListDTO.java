package main.blog.domain.dto.diff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentListDTO {
    
    private int _start = 0;
    private int _end = 100;
    private String _sort = "createdAt";
    private String _order = "desc";
    
    private String search;
    private String status;
    private String fileType;
    private String userId;
} 