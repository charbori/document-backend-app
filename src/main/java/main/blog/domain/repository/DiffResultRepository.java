package main.blog.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import main.blog.domain.entity.DiffResultEntity;
import main.blog.domain.entity.DocumentEntity;
import main.blog.domain.entity.UserEntity;

@Repository
public interface DiffResultRepository extends JpaRepository<DiffResultEntity, Long> {
    
    @Query("SELECT d FROM DiffResultEntity d " +
           "LEFT JOIN FETCH d.user " +
           "LEFT JOIN FETCH d.originalDocument od " +
           "LEFT JOIN FETCH od.user " +
           "LEFT JOIN FETCH d.compareDocument cd " +
           "LEFT JOIN FETCH cd.user " +
           "WHERE d.id = :id AND d.user = :user")
    Optional<DiffResultEntity> findByIdAndUserWithFetch(@Param("id") Long id, @Param("user") UserEntity user);
    
    Optional<DiffResultEntity> findByIdAndUser(Long id, UserEntity user);
    
    List<DiffResultEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    
    Page<DiffResultEntity> findByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);
    
    List<DiffResultEntity> findByOriginalDocumentOrCompareDocument(
            DocumentEntity originalDocument, 
            DocumentEntity compareDocument
    );
    
    @Query("SELECT d FROM DiffResultEntity d WHERE d.user = :user " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND (:diffType IS NULL OR d.diffType = :diffType)")
    Page<DiffResultEntity> findDiffResultsWithFilters(
            @Param("user") UserEntity user,
            @Param("status") DiffResultEntity.DiffStatus status,
            @Param("diffType") String diffType,
            Pageable pageable
    );
} 