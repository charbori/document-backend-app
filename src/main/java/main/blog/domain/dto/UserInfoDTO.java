package main.blog.domain.dto;

import lombok.*;
import main.blog.domain.entity.UserEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserInfoDTO extends UserEntity {
    private String username;
    private String role;
}
