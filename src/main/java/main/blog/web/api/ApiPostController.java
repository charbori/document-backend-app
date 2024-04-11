package main.blog.web.api;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.PostDTO;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.service.PostService;
import main.blog.exception.PostAccessDeniedException;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/post")
public class ApiPostController {

    @Autowired
    private PostService postService;

    @Value("${file.filepath}")
    private String filePath;

    @GetMapping
    public ResponseEntity<?> getPosts() {
        List<PostEntity> posts = postService.postList();
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<?> addPost(@RequestBody PostDTO postDTO) throws AccessDeniedException {
        PostEntity postEntity = postService.registPost(postDTO);
        postDTO.setId(postEntity.getId());
        return ResponseEntity.ok()
                .body(postDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable(value="id") long id) {
        PostDTO postDTO = postService.getPost(id);

        return ResponseEntity.ok()
                .body(postDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable("id") long id, @RequestBody PostDTO postDTO) {
        try {
            PostEntity post = postService.updatePost(id, postDTO);
            postDTO.setId(post.getId());
            postDTO.setTitle(post.getTitle());
            postDTO.setContent(post.getContent());
            postDTO.setImageName(post.getImageName());
            postDTO.setCreatedAt(post.getCreateAt());
            return ResponseEntity.ok()
                    .body(postDTO);
        } catch (AccessDeniedException exception) {
            throw new PostAccessDeniedException();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") long id) {
        PostDTO postDTO = postService.getPost(id);
        postService.deletePost(id);
        return ApiResponse.success(new ApiResponseMessage(id, ""));
    }

    @ExceptionHandler({ PostAccessDeniedException.class, EntityNotFoundException.class })
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
