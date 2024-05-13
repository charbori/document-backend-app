package main.blog.domain.repository;

import main.blog.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

    UserEntity findByVerificationCode(String verificationCode);
}
