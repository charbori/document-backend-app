package main.blog.domain.repository;

import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface VideoRepository  extends JpaRepository<VideoEntity, Long> {
    boolean existsById(long id);

    List<VideoEntity> findAllById(long id);

    List<VideoEntity> findAllByUser(UserEntity user);

    List<VideoEntity> findAllByUser(UserEntity user, Pageable pageable);

    List<VideoEntity> findAllByUserAndDescription(UserEntity user, String description, Pageable pageable);

    List<VideoEntity> findAllByUserAndId(UserEntity user, long id, Pageable pageable);

    List<VideoEntity> findAllByUserAndName(UserEntity user, String name, Pageable pageable);

    List<VideoEntity> findAllByUserAndNameContaining(UserEntity user, String name_like, Pageable pageable);

    List<VideoEntity> findAllByUserAndDescriptionContaining(UserEntity user, String name_like, Pageable pageable);

    List<VideoEntity> findAllByUserAndCreatedAtBetween(UserEntity user, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<VideoEntity> findAll();

    List<VideoEntity> findByName(String name);

    List<VideoEntity> findByName(String name, Pageable pageable);

    List<VideoEntity> findByNameAndUser(String name, UserEntity user);

    Optional<VideoEntity> findById(long id);
}
