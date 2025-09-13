package main.blog.domain.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import main.blog.domain.entity.UserEntity;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.main.web-application-type=none",
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("test existsByUsername")
    void existsByUsername() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser");
        userEntity.setPassword("testpassword");
        userEntity.setRole("ROLE_USER");
        userEntity.setVerification("Y");
        userEntity = entityManager.persistFlushFind(userEntity);

        boolean userEntityFound = userRepository.existsByUsername(userEntity.getUsername());

        Assertions.assertThat(userEntityFound).isTrue();
    }

    @Test
    @DisplayName("test findByUsername")
    void findByUsername() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testuser2");
        userEntity.setPassword("testpassword2");
        userEntity.setRole("ROLE_USER");
        userEntity.setVerification("Y");
        userEntity = entityManager.persistFlushFind(userEntity);

        UserEntity userEntityFound = userRepository.findByUsername(userEntity.getUsername());

        Assertions.assertThat(userEntityFound).isEqualTo(userEntity);
    }
}