package main.blog.domain.dto.video;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.VideoEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadDTO {
    private long id;
    @NotBlank
    private String name;
    private UserInfoDTO user;
    @NotBlank
    private String status;
}
