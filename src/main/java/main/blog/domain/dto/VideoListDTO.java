package main.blog.domain.dto;

import lombok.*;
import main.blog.domain.entity.UserEntity;

@Data
@AllArgsConstructor
@Getter
@Setter
public class VideoListDTO {
    private int _start;
    private int _end;
    private String _sort = "id";
    private String _order = "desc";
    private String name_like;
    private String description_like;
    private String id;
    private String name;
    private String description;
    private String createdAt;
}
