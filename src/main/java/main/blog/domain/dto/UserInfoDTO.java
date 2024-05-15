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
    private long id;
    private String username;
    private String role;

    public UserEntity toUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(this.id);
        userEntity.setUsername(this.username);
        userEntity.setRole(this.role);
        return userEntity;
    }
}
