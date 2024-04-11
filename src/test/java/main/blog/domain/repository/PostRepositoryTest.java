package main.blog.domain.repository;

import main.blog.domain.entity.PostEntity;
import org.assertj.core.api.Assertions;
import org.h2.security.AES;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.parameters.P;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private PostRepository postRepository;


    @Test
    @DisplayName("test existsById")
    void existsById() {
        PostEntity savedEntity = entityManager.persistFlushFind(new PostEntity());
        long postId = savedEntity.getId();

        boolean exists = postRepository.existsById(postId);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("test findAllById")
    void findAllById() {
        PostEntity post1 = entityManager.persistFlushFind(new PostEntity());
        PostEntity post2 = entityManager.persistFlushFind(new PostEntity());

        long post1Id = post1.getId();

        List<PostEntity> posts = postRepository.findAllById(Collections.singletonList(post1Id));

        Assertions.assertThat(posts).hasSize(1).contains(post1);
    }

    @Test
    @DisplayName("test findAll")
    void findAll() {
        entityManager.persistFlushFind(new PostEntity());
        entityManager.persistFlushFind(new PostEntity());

        List<PostEntity> posts = postRepository.findAll();

        Assertions.assertThat(posts).hasSize(2);
    }

    @Test
    void findById() {
        PostEntity savedPost = entityManager.persistFlushFind(new PostEntity());
        long id = savedPost.getId();

        Optional<PostEntity> post1 = postRepository.findById(id);

        Assertions.assertThat(post1).isPresent().contains(savedPost);
    }
}