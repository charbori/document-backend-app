package main.blog.domain.repository;

import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository  extends JpaRepository<VideoEntity, Long> {
    boolean existsById(long id);

    List<VideoEntity> findAllById(long id);

    List<VideoEntity> findAllByUser(UserEntity user);

    List<VideoEntity> findAll();

    Optional<VideoEntity> findById(long id);
}
