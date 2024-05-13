package main.blog.domain.repository;

import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoCategoryEntity;
import main.blog.domain.entity.VideoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VideoCategoryRepository  extends JpaRepository<VideoCategoryEntity, Long> {
    boolean existsById(long id);
    List<VideoCategoryEntity> findAll();
    Optional<VideoCategoryEntity> findById(long id);
}
