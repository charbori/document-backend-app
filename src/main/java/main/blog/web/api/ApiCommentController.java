package main.blog.web.api;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CommentApiDTO;
import main.blog.domain.dto.CommentDTO;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.entity.CommentEntity;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.PostRepository;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.service.CommentService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/comment")
public class ApiCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<CommentApiDTO> getComment(@PathVariable("id") long comment_id) {
        Optional<CommentEntity> commentEntity = commentService.getComment(comment_id);
        CommentEntity commentEntity1 = commentEntity.orElseThrow(() ->
                new EntityNotFoundException("Comment not found with id " + comment_id));
        CommentApiDTO commentApiDTO = CommentApiDTO.toApiDTO(commentEntity1);
        return ApiResponse.success(commentApiDTO);
    }

    @PostMapping
    public ResponseEntity<CommentApiDTO> registComment(@RequestBody CommentApiDTO commentApiDTO) throws AccessDeniedException {
        PostEntity postEntity = new PostEntity();
        postEntity.setId(commentApiDTO.getPost_id());
        UserEntity userEntity = new UserEntity();
        userEntity.setId(2);
        userEntity.setRole("ROLE_ADMIN");

        commentApiDTO.setUpdatedAt(LocalDateTime.now());
        commentApiDTO.setCreatedAt(LocalDateTime.now());
        CommentDTO commentDTO = commentApiDTO.commentDTO(userEntity, postEntity);
        CommentEntity commentEntity = commentService.registComment(commentDTO);
        commentApiDTO.setId(commentEntity.getId());
        return ApiResponse.success(commentApiDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentApiDTO> commentEdit(@PathVariable("id") long comment_id,
                                                     @RequestBody CommentApiDTO commentApiDTO) throws AccessDeniedException {
        Optional<CommentEntity> commentEntity = commentService.getComment(comment_id);
        CommentEntity comment = commentEntity.orElseThrow(() ->
                new EntityNotFoundException("Comment not found with id " + comment_id));

        comment.setContent(commentApiDTO.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        commentApiDTO = CommentApiDTO.toApiDTO(commentService.registComment(CommentDTO.toDto(comment)));

        return ApiResponse.success(commentApiDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> commentDelete(@PathVariable("id") long comment_id) throws AccessDeniedException {
        Optional<CommentEntity> commentEntity = commentService.getComment(comment_id);
        CommentEntity comment = commentEntity.orElseThrow(() ->
                new EntityNotFoundException("Comment not found with id " + comment_id));
        commentService.deleteComment(comment_id);
        return ApiResponse.success(new ApiResponseMessage(comment_id, ""));
    }

    @ExceptionHandler({ EntityNotFoundException.class, AccessDeniedException.class })
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
