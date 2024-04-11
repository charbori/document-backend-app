package main.blog.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.List;

@Entity
@Data
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    private String username;
    private String password;
    private String role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<PostEntity> postList;
}
