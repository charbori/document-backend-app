package main.blog.domain.repository;

import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface VideoRepository  extends JpaRepository<VideoEntity, Long> {
    boolean existsById(long id);

    List<VideoEntity> findAllById(long id);

    List<VideoEntity> findAllByUser(UserEntity user);

    List<VideoEntity> findAllByUser(UserEntity user, Pageable pageable);

    List<VideoEntity> findAll();

    List<VideoEntity> findByName(String name);

    List<VideoEntity> findByName(String name, Pageable pageable);

    Optional<VideoEntity> findById(long id);
}
