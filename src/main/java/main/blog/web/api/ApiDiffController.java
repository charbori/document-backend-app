package main.blog.web.api;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.dto.diff.DiffRequestDTO;
import main.blog.domain.dto.diff.DiffResponseDTO;
import main.blog.domain.dto.diff.DocumentDTO;
import main.blog.domain.dto.diff.DocumentListDTO;
import main.blog.domain.entity.DiffResultEntity;
import main.blog.domain.entity.DocumentEntity;
import main.blog.domain.service.DiffService;
import main.blog.domain.service.DocumentService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;

@Slf4j
@RestController
@RequestMapping("/api/diff")
public class ApiDiffController {

    @Autowired
    private DiffService diffService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/compare")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> performDiff(@RequestBody @Valid DiffRequestDTO diffRequest) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        diffRequest.setUser(customUserDetails.getUserInfoDTO());

        DiffResponseDTO result = diffService.performDiff(diffRequest);
        return ApiResponse.success(result);
    }

    @GetMapping("/result/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getDiffResult(@PathVariable(value="id") Long diffId) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        DiffResponseDTO result = diffService.getDiffResult(diffId, customUserDetails.getUserInfoDTO().toUserEntity());
        return ApiResponse.success(result);
    }

    @GetMapping("/results")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getDiffResultList(DocumentListDTO documentListDTO) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        
        int offset = documentListDTO.get_start() > 0 ? documentListDTO.get_start() : 0;
        int pageSize = 100;
        if (documentListDTO.get_end() > documentListDTO.get_start()) {
            offset = (documentListDTO.get_start()) / pageSize;
            pageSize = documentListDTO.get_end() - documentListDTO.get_start();
        }
        
        Sort sortData = Sort.by(documentListDTO.get_order().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, documentListDTO.get_sort());
        Pageable paging = PageRequest.of(offset, pageSize, sortData);
        
        List<DiffResultEntity> diffResults = diffService.getDiffResultList(customUserDetails.getUserInfoDTO().toUserEntity(), paging);
        
        List<DiffResponseDTO> diffResponseList = diffResults.stream()
                .map(entity -> {
                    DiffResponseDTO dto = new DiffResponseDTO();
                    dto.setId(entity.getId());
                    dto.setUser(new UserInfoDTO(entity.getUser().getId(), entity.getUser().getUsername(), entity.getUser().getRole()));
                    dto.setDiffTitle(entity.getDiffTitle());
                    dto.setDiffType(entity.getDiffType());
                    dto.setStatus(entity.getStatus());
                    dto.setAddedLines(entity.getAddedLines());
                    dto.setDeletedLines(entity.getDeletedLines());
                    dto.setModifiedLines(entity.getModifiedLines());
                    dto.setCreatedAt(entity.getCreatedAt());
                    
                    // 문서 ID들 설정
                    dto.setOriginalDocumentId(entity.getOriginalDocument() != null ? entity.getOriginalDocument().getId() : null);
                    dto.setCompareDocumentId(entity.getCompareDocument() != null ? entity.getCompareDocument().getId() : null);
                    
                    return dto;
                })
                .collect(Collectors.toList());

        return ApiResponse.success(diffResponseList);
    }

    @DeleteMapping("/result/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> deleteDiffResult(@PathVariable(value="id") Long diffId) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        
        diffService.deleteDiffResult(diffId, customUserDetails.getUserInfoDTO().toUserEntity());
        return ApiResponse.success(diffId);
    }

    // Document management endpoints
    @PostMapping("/document")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> createDocument(@RequestBody @Valid DocumentDTO documentDTO, final HttpServletResponse servletResponse) throws IOException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        documentDTO.setUser(customUserDetails.getUserInfoDTO());

        return ApiResponse.success(new ApiResponseMessage(documentService.createDocument(documentDTO), ""));
    }

    @GetMapping("/document/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getDocument(@PathVariable(value="id") Long documentId) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        DocumentEntity document = documentService.getDocumentByUser(documentId, customUserDetails.getUserInfoDTO().toUserEntity());
        DocumentDTO documentDTO = new DocumentDTO(
                document.getId(),
                new UserInfoDTO(document.getUser().getId(), document.getUser().getUsername(), document.getUser().getRole()),
                document.getTitle(),
                document.getContent(),
                document.getDescription(),
                document.getStatus(),
                document.getFileName(),
                document.getFileType(),
                document.getVersion(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
        
        return ApiResponse.success(documentDTO);
    }

    @GetMapping("/documents")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getDocumentList(DocumentListDTO documentListDTO) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        
        int offset = documentListDTO.get_start() > 0 ? documentListDTO.get_start() : 0;
        int pageSize = 100;
        if (documentListDTO.get_end() > documentListDTO.get_start()) {
            offset = (documentListDTO.get_start()) / pageSize;
            pageSize = documentListDTO.get_end() - documentListDTO.get_start();
        }
        
        Sort sortData = Sort.by(documentListDTO.get_order().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, documentListDTO.get_sort());
        Pageable paging = PageRequest.of(offset, pageSize, sortData);
        
        List<DocumentEntity> documentList = documentService.getDocumentList(customUserDetails.getUserInfoDTO().toUserEntity(), documentListDTO, paging);
        List<DocumentDTO> documentListData = documentList.stream()
                .map(m -> new DocumentDTO(
                        m.getId(),
                        new UserInfoDTO(m.getUser().getId(), m.getUser().getUsername(), m.getUser().getRole()),
                        m.getTitle(),
                        m.getContent(),
                        m.getDescription(),
                        m.getStatus(),
                        m.getFileName(),
                        m.getFileType(),
                        m.getVersion(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(documentListData);
    }

    @PatchMapping("/document")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> updateDocument(@RequestBody @Valid DocumentDTO documentDTO, final HttpServletResponse servletResponse) throws IOException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        documentDTO.setUser(customUserDetails.getUserInfoDTO());

        return ApiResponse.success(new ApiResponseMessage(documentService.updateDocument(documentDTO), ""));
    }

    @DeleteMapping("/document/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> deleteDocument(@PathVariable(value="id") Long documentId) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        
        documentService.deleteDocument(documentId, customUserDetails.getUserInfoDTO().toUserEntity());
        return ApiResponse.success(documentId);
    }

    @GetMapping("/document/validation/{title}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> isExistDocument(@PathVariable(value="title") String title) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        try {
            DocumentEntity document = documentService.getDocumentByTitle(title, customUserDetails.getUserInfoDTO().toUserEntity());
            title = document.getTitle();
        } catch (EntityNotFoundException e) {
            return ApiResponse.success(new ApiResponseMessage("", ""));
        }
        return ApiResponse.success(new ApiResponseMessage(title, ""));
    }

    private static CustomUserDetails getAuthenticatedUserDetail() {
        log.info("start authenticate");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadCredentialsException("로그인을 해주세요.");
        }
        log.info("get user credential :{} {}", authentication.getPrincipal(), authentication.getPrincipal().equals("anonymousUser"));
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails;
    }

    @ExceptionHandler({ RuntimeException.class, AccessDeniedException.class, EntityNotFoundException.class })
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}