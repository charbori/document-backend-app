package main.blog.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import main.blog.domain.entity.DocumentEntity;
import main.blog.domain.entity.UserEntity;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    
    Optional<DocumentEntity> findByIdAndUser(Long id, UserEntity user);
    
    List<DocumentEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    
    Page<DocumentEntity> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
    
    @Query("SELECT d FROM DocumentEntity d WHERE d.user = :user " +
           "AND (:search IS NULL OR d.title LIKE %:search% OR d.content LIKE %:search%) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND (:fileType IS NULL OR d.fileType = :fileType)")
    Page<DocumentEntity> findDocumentsWithFilters(
            @Param("user") UserEntity user,
            @Param("search") String search,
            @Param("status") DocumentEntity.DocumentStatus status,
            @Param("fileType") String fileType,
            Pageable pageable
    );
    
    Optional<DocumentEntity> findByTitleAndUser(String title, UserEntity user);
    
    List<DocumentEntity> findByStatusAndUser(DocumentEntity.DocumentStatus status, UserEntity user);
} 