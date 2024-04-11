package main.blog.domain.repository;

import main.blog.domain.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    boolean existsById(long id);

    List<PostEntity> findAllById(long id);

    List<PostEntity> findAll();

    Page<PostEntity> findAll(Pageable pageable);

    Optional<PostEntity> findById(long id);
}
