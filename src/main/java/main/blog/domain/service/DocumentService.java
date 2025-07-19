package main.blog.domain.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.diff.DocumentDTO;
import main.blog.domain.dto.diff.DocumentListDTO;
import main.blog.domain.entity.DocumentEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.DocumentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional
    public String createDocument(DocumentDTO documentDTO) {
        log.info("Creating new document: {}", documentDTO.getTitle());
        
        DocumentEntity document = DocumentEntity.builder()
                .user(documentDTO.getUser().toUserEntity())
                .title(documentDTO.getTitle())
                .content(documentDTO.getContent())
                .description(documentDTO.getDescription())
                .status(documentDTO.getStatus() != null ? documentDTO.getStatus() : DocumentEntity.DocumentStatus.DRAFT)
                .fileName(documentDTO.getFileName())
                .fileType(documentDTO.getFileType())
                .version(documentDTO.getVersion() != null ? documentDTO.getVersion() : "1.0")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        DocumentEntity savedDocument = documentRepository.save(document);
        return savedDocument.getId().toString();
    }

    @Transactional(readOnly = true)
    public DocumentEntity getDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + documentId));
    }

    @Transactional(readOnly = true)
    public DocumentEntity getDocumentByUser(Long documentId, UserEntity user) {
        return documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + documentId));
    }

    @Transactional(readOnly = true)
    public List<DocumentEntity> getDocumentList(UserEntity user, DocumentListDTO documentListDTO, Pageable pageable) {
        log.info("Getting document list for user: {}", user.getUsername());
        
        DocumentEntity.DocumentStatus status = null;
        if (documentListDTO.getStatus() != null) {
            try {
                status = DocumentEntity.DocumentStatus.valueOf(documentListDTO.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", documentListDTO.getStatus());
            }
        }
        
        Page<DocumentEntity> documents = documentRepository.findDocumentsWithFilters(
                user,
                documentListDTO.getSearch(),
                status,
                documentListDTO.getFileType(),
                pageable
        );
        
        return documents.getContent();
    }

    @Transactional
    public String updateDocument(DocumentDTO documentDTO) {
        log.info("Updating document: {}", documentDTO.getId());
        
        DocumentEntity existingDocument = getDocumentByUser(documentDTO.getId(), documentDTO.getUser().toUserEntity());
        
        existingDocument.setTitle(documentDTO.getTitle());
        existingDocument.setContent(documentDTO.getContent());
        existingDocument.setDescription(documentDTO.getDescription());
        existingDocument.setStatus(documentDTO.getStatus());
        existingDocument.setFileName(documentDTO.getFileName());
        existingDocument.setFileType(documentDTO.getFileType());
        existingDocument.setVersion(documentDTO.getVersion());
        existingDocument.setUpdatedAt(LocalDateTime.now());
        
        DocumentEntity savedDocument = documentRepository.save(existingDocument);
        return savedDocument.getId().toString();
    }

    @Transactional
    public void deleteDocument(Long documentId, UserEntity user) {
        log.info("Deleting document: {}", documentId);
        
        DocumentEntity document = getDocumentByUser(documentId, user);
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public DocumentEntity getDocumentByTitle(String title, UserEntity user) {
        return documentRepository.findByTitleAndUser(title, user)
                .orElseThrow(() -> new EntityNotFoundException("문서를 찾을 수 없습니다: " + title));
    }
} 