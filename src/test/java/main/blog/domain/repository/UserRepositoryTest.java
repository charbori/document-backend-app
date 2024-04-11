package main.blog.domain.repository;

import main.blog.domain.entity.UserEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("test existsByUsername")
    void existsByUsername() {
        UserEntity userEntity = entityManager.persistFlushFind(new UserEntity());

        boolean userEntityFound = userRepository.existsByUsername(userEntity.getUsername());

        Assertions.assertThat(userEntityFound).isTrue();
    }

    @Test
    @DisplayName("test findByUsername")
    void findByUsername() {
        UserEntity userEntity = entityManager.persistFlushFind(new UserEntity());

        UserEntity userEntityFound = userRepository.findByUsername(userEntity.getUsername());

        Assertions.assertThat(userEntityFound).isEqualTo(userEntity);
    }
}