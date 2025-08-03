package main.blog.domain.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.diff.DiffRequestDTO;
import main.blog.domain.dto.diff.DiffResponseDTO;
import main.blog.domain.entity.DiffResultEntity;
import main.blog.domain.entity.DocumentEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.DiffResultRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiffService {

    private final DiffResultRepository diffResultRepository;
    private final DocumentService documentService;

    @Transactional
    public DiffResponseDTO performDiff(DiffRequestDTO diffRequest) {
        log.info("Starting diff operation for documents: {} vs {}", 
                diffRequest.getOriginalDocumentId(), diffRequest.getCompareDocumentId());

        UserEntity user = diffRequest.getUser().toUserEntity();
        
        // 문서 가져오기
        DocumentEntity originalDoc = null;
        DocumentEntity compareDoc = null;
        String originalText = diffRequest.getOriginalText();
        String compareText = diffRequest.getCompareText();
        
        if (diffRequest.getOriginalDocumentId() != null) {
            originalDoc = documentService.getDocumentByUser(diffRequest.getOriginalDocumentId(), user);
            originalText = originalDoc.getContent();
        }
        
        if (diffRequest.getCompareDocumentId() != null) {
            compareDoc = documentService.getDocumentByUser(diffRequest.getCompareDocumentId(), user);
            compareText = compareDoc.getContent();
        }

        // diff 실행
        DiffResult diffResult = calculateDiff(originalText, compareText, diffRequest.getDiffType());
        
        // 결과 저장
        DiffResultEntity diffEntity = DiffResultEntity.builder()
                .user(user)
                .originalDocument(originalDoc)
                .compareDocument(compareDoc)
                .diffResult(diffResult.getDiffText())
                .htmlDiff(diffResult.getHtmlDiff())
                .diffTitle(diffRequest.getDiffTitle())
                .diffType(diffRequest.getDiffType())
                .status(DiffResultEntity.DiffStatus.COMPLETED)
                .addedLines(diffResult.getAddedLines())
                .deletedLines(diffResult.getDeletedLines())
                .modifiedLines(diffResult.getModifiedLines())
                .createdAt(LocalDateTime.now())
                .build();
        
        DiffResultEntity savedResult = diffResultRepository.save(diffEntity);
        
        // 저장된 결과를 fetch join으로 다시 조회하여 lazy loading 문제 해결
        DiffResultEntity fetchedResult = diffResultRepository.findByIdAndUserWithFetch(savedResult.getId(), user)
                .orElseThrow(() -> new EntityNotFoundException("저장된 diff 결과를 찾을 수 없습니다: " + savedResult.getId()));
        
        return convertToResponseDTO(fetchedResult, diffResult.getDiffLines());
    }

    @Transactional(readOnly = true)
    public DiffResponseDTO getDiffResult(Long diffId, UserEntity user) {
        DiffResultEntity diffResult = diffResultRepository.findByIdAndUserWithFetch(diffId, user)
                .orElseThrow(() -> new EntityNotFoundException("diff 결과를 찾을 수 없습니다: " + diffId));
        
        return convertToResponseDTO(diffResult, null);
    }

    @Transactional(readOnly = true)
    public List<DiffResultEntity> getDiffResultList(UserEntity user, Pageable pageable) {
        Page<DiffResultEntity> results = diffResultRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return results.getContent();
    }

    @Transactional
    public void deleteDiffResult(Long diffId, UserEntity user) {
        DiffResultEntity diffResult = diffResultRepository.findByIdAndUser(diffId, user)
                .orElseThrow(() -> new EntityNotFoundException("diff 결과를 찾을 수 없습니다: " + diffId));
        
        diffResultRepository.delete(diffResult);
    }

    private DiffResult calculateDiff(String originalText, String compareText, String diffType) {
        // 간단한 라인별 diff 구현
        String[] originalLines = originalText != null ? originalText.split("\n") : new String[0];
        String[] compareLines = compareText != null ? compareText.split("\n") : new String[0];
        
        List<DiffResponseDTO.DiffLineDTO> diffLines = new ArrayList<>();
        StringBuilder diffText = new StringBuilder();
        StringBuilder htmlDiff = new StringBuilder();
        
        int addedLines = 0;
        int deletedLines = 0;
        int modifiedLines = 0;
        
        int maxLines = Math.max(originalLines.length, compareLines.length);
        
        for (int i = 0; i < maxLines; i++) {
            String originalLine = i < originalLines.length ? originalLines[i] : null;
            String compareLine = i < compareLines.length ? compareLines[i] : null;
            
            DiffResponseDTO.DiffLineDTO diffLine = new DiffResponseDTO.DiffLineDTO();
            diffLine.setLineNumber(i + 1);
            
            if (originalLine == null && compareLine != null) {
                // 추가된 라인
                diffLine.setOperation("ADD");
                diffLine.setCompareContent(compareLine);
                diffLine.setCssClass("diff-added");
                diffText.append("+ ").append(compareLine).append("\n");
                htmlDiff.append("<div class='diff-added'>+ ").append(escapeHtml(compareLine)).append("</div>");
                addedLines++;
            } else if (originalLine != null && compareLine == null) {
                // 삭제된 라인
                diffLine.setOperation("DELETE");
                diffLine.setOriginalContent(originalLine);
                diffLine.setCssClass("diff-deleted");
                diffText.append("- ").append(originalLine).append("\n");
                htmlDiff.append("<div class='diff-deleted'>- ").append(escapeHtml(originalLine)).append("</div>");
                deletedLines++;
            } else if (originalLine != null && compareLine != null) {
                if (originalLine.equals(compareLine)) {
                    // 동일한 라인
                    diffLine.setOperation("EQUAL");
                    diffLine.setOriginalContent(originalLine);
                    diffLine.setCompareContent(compareLine);
                    diffLine.setCssClass("diff-equal");
                    diffText.append("  ").append(originalLine).append("\n");
                    htmlDiff.append("<div class='diff-equal'>  ").append(escapeHtml(originalLine)).append("</div>");
                } else {
                    // 수정된 라인
                    diffLine.setOperation("MODIFY");
                    diffLine.setOriginalContent(originalLine);
                    diffLine.setCompareContent(compareLine);
                    diffLine.setCssClass("diff-modified");
                    diffText.append("- ").append(originalLine).append("\n");
                    diffText.append("+ ").append(compareLine).append("\n");
                    htmlDiff.append("<div class='diff-deleted'>- ").append(escapeHtml(originalLine)).append("</div>");
                    htmlDiff.append("<div class='diff-added'>+ ").append(escapeHtml(compareLine)).append("</div>");
                    modifiedLines++;
                }
            }
            
            diffLines.add(diffLine);
        }
        
        return new DiffResult(diffText.toString(), htmlDiff.toString(), diffLines, addedLines, deletedLines, modifiedLines);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private DiffResponseDTO convertToResponseDTO(DiffResultEntity entity, List<DiffResponseDTO.DiffLineDTO> diffLines) {
        DiffResponseDTO response = new DiffResponseDTO();
        response.setId(entity.getId());
        
        try {
            response.setUser(entity.getUser() != null ? entity.getUser().toUserInfoDTO() : null);
        } catch (Exception e) {
            log.warn("User 정보 로딩 실패: {}", e.getMessage());
            response.setUser(null);
        }
        
        try {
            response.setOriginalDocument(entity.getOriginalDocument() != null ? convertToDocumentDTO(entity.getOriginalDocument()) : null);
        } catch (Exception e) {
            log.warn("원본 문서 정보 로딩 실패: {}", e.getMessage());
            response.setOriginalDocument(null);
        }
        
        try {
            response.setCompareDocument(entity.getCompareDocument() != null ? convertToDocumentDTO(entity.getCompareDocument()) : null);
        } catch (Exception e) {
            log.warn("비교 문서 정보 로딩 실패: {}", e.getMessage());
            response.setCompareDocument(null);
        }
        
        response.setDiffResult(entity.getDiffResult());
        response.setHtmlDiff(entity.getHtmlDiff());
        response.setDiffTitle(entity.getDiffTitle());
        response.setDiffType(entity.getDiffType());
        response.setStatus(entity.getStatus());
        response.setAddedLines(entity.getAddedLines());
        response.setDeletedLines(entity.getDeletedLines());
        response.setModifiedLines(entity.getModifiedLines());
        response.setCreatedAt(entity.getCreatedAt());
        response.setDiffLines(diffLines);
        
        return response;
    }

    private main.blog.domain.dto.diff.DocumentDTO convertToDocumentDTO(DocumentEntity entity) {
        main.blog.domain.dto.diff.DocumentDTO dto = new main.blog.domain.dto.diff.DocumentDTO();
        dto.setId(entity.getId());
        
        try {
            dto.setUser(entity.getUser() != null ? entity.getUser().toUserInfoDTO() : null);
        } catch (Exception e) {
            log.warn("문서의 User 정보 로딩 실패: {}", e.getMessage());
            dto.setUser(null);
        }
        
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setFileName(entity.getFileName());
        dto.setFileType(entity.getFileType());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }

    // Inner class for diff calculation result
    private static class DiffResult {
        private final String diffText;
        private final String htmlDiff;
        private final List<DiffResponseDTO.DiffLineDTO> diffLines;
        private final int addedLines;
        private final int deletedLines;
        private final int modifiedLines;

        public DiffResult(String diffText, String htmlDiff, List<DiffResponseDTO.DiffLineDTO> diffLines, 
                         int addedLines, int deletedLines, int modifiedLines) {
            this.diffText = diffText;
            this.htmlDiff = htmlDiff;
            this.diffLines = diffLines;
            this.addedLines = addedLines;
            this.deletedLines = deletedLines;
            this.modifiedLines = modifiedLines;
        }

        public String getDiffText() { return diffText; }
        public String getHtmlDiff() { return htmlDiff; }
        public List<DiffResponseDTO.DiffLineDTO> getDiffLines() { return diffLines; }
        public int getAddedLines() { return addedLines; }
        public int getDeletedLines() { return deletedLines; }
        public int getModifiedLines() { return modifiedLines; }
    }
} 