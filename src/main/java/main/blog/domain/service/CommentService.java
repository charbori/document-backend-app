package main.blog.domain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CommentDTO;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.entity.CommentEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.CommentRepository;
import main.blog.domain.repository.UserRepository;
import main.blog.exception.UserNotMatchErrorException;
import main.blog.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Slf4j
@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    public CommentEntity registComment(CommentDTO commentDTO) throws AccessDeniedException {
        // 유저의 권한 검증
        if (commentDTO.getUserEntity().getRole().isEmpty() || !commentDTO.getUserEntity().getRole().equals("ROLE_ADMIN")) {
            log.info("comment role={}", commentDTO.getUserEntity().getRole());
            log.info("comment name={}", commentDTO.getUserEntity().getUsername());
            throw new AccessDeniedException("User does not have permission to write a post");
        }
        return commentRepository.save(commentDTO.toUpdateEntity());
    }

    public void deleteComment(long commentId) {
        Optional<CommentEntity> commentEntity = commentRepository.findById(commentId);
        commentEntity.orElseThrow(EntityNotFoundException::new);
        commentRepository.deleteById(commentId);
    }

    public Optional<CommentEntity> getComment(long comment_id) {
        return commentRepository.findById(comment_id);
    }
}
