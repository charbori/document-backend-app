package main.blog.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "diff_results")
public class DiffResultEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diff_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_document_id")
    private DocumentEntity originalDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compare_document_id")
    private DocumentEntity compareDocument;

    @Column(columnDefinition = "TEXT")
    private String diffResult;

    @Column(columnDefinition = "TEXT")
    private String htmlDiff;

    private String diffTitle;
    private String diffType;
    
    @Enumerated(EnumType.STRING)
    private DiffStatus status;

    private int addedLines;
    private int deletedLines;
    private int modifiedLines;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum DiffStatus {
        PROCESSING, COMPLETED, FAILED
    }
} 