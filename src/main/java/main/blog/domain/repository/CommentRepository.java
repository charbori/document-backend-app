package main.blog.domain.repository;

import main.blog.domain.entity.CommentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @Override
    <S extends CommentEntity> S save(S entity);

    @EntityGraph(attributePaths = {"post"})
    Optional<CommentEntity> findById(Long aLong);

    @Override
    boolean existsById(Long aLong);

    @Override
    long count();

    @Override
    void deleteById(Long aLong);
}
