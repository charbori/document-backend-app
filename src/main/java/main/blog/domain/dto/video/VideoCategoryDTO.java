package main.blog.domain.dto.video;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoCategoryDTO implements Serializable {
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String role;
}
