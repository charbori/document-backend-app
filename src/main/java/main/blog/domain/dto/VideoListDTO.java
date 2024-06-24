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
    private String _sort;
    private String _order;
    private String name_like;
    private String description_like;
    private String id;
    private String name;
    private String description;
    private String createdAt;
//    public VideoListDTO(int limit_start, int limit_end) {
//        this.limit_start = limit_start;
//        this.limit_end = limit_end;
//        this.sort = "id";
//        this.order = "desc";
//    }
}
