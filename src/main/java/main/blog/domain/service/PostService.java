package main.blog.domain.service;

import io.micrometer.core.annotation.Counted;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CommentDTO;
import main.blog.domain.dto.PostDTO;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.PostRepository;
import main.blog.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.cache.annotation.Cacheable;

import java.io.File;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;

    static final int PAGE_SIZE = 5;

    @Cacheable(cacheNames = "post")
    public PostDTO getPost(long id) {
        log.info(" cache 되었습니다. 더이상 노출되지 않습니다. ");
        Optional<PostEntity> postEntityOptional = postRepository.findById(id);
        PostEntity postEntity = postEntityOptional.orElseThrow(() -> new EntityNotFoundException("Post not found with id " + id));
        PostDTO postDTO = PostDTO.builder()
                .id(postEntity.getId())
                .title(postEntity.getTitle())
                .createdAt(postEntity.getCreateAt())
                .imageName(postEntity.getImageName())
                .username("#GUEST")
            .build();
        if (postEntity.getUser() != null) {
            postDTO.setUsername(postEntity.getUser().getUsername());
        }

        ArrayList<CommentDTO> commentDTOS = new ArrayList<>();
        if (postEntity.getCommentEntity().size() > 0) {
            postEntity.getCommentEntity().stream().
                    map(commentEntity -> {
                        log.info(commentEntity.toString());
                        return true;

                    });
        }
        return postDTO;
    }

    public List<PostEntity> postList() {
        List<PostEntity> postsList = postRepository.findAll();
        if (postsList == null)
            throw new EntityNotFoundException("PostEntity postList() not found");
        return postsList;
    }

    public Page<PostEntity> getPostList(int pageNo, String criteria) {
        Pageable pageable = PageRequest.of(pageNo, PAGE_SIZE, Sort.by(Sort.Direction.DESC, criteria));
        Page<PostEntity> postsList = postRepository.findAll(pageable);
        if (postsList == null)
            throw new EntityNotFoundException("PostEntity getPostList() not found");
        return postsList;
    }

    public PostEntity registPost(PostDTO postDTO) throws AccessDeniedException {
        PostEntity postEntity = new PostEntity();

        postEntity.setTitle(postDTO.getTitle());
        postEntity.setContent(postDTO.getContent());
        postEntity.setCreateAt(postDTO.getCreatedAt());
        postEntity.setImageName(postDTO.getImageName());
        postEntity.setUser(userRepository.findByUsername(postDTO.getUsername()));

        // 유저의 권한 검증
        UserEntity userEntity = userRepository.findByUsername(postDTO.getUsername());
        if (userEntity.getRole().isEmpty() || !userEntity.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("User does not have permission to write a post");
        }
        return postRepository.save(postEntity);
    }

    public PostEntity updatePost(long id, @Validated PostDTO postDTO) throws AccessDeniedException {
        PostEntity postEntity = new PostEntity();

        postEntity.setId(id);
        postEntity.setTitle(postDTO.getTitle());
        postEntity.setContent(postDTO.getContent());
        postEntity.setCreateAt(LocalDateTime.now());

        postEntity.setUser(userRepository.findByUsername(postDTO.getUsername()));

        // 유저의 권한 검증
        UserEntity userEntity = userRepository.findByUsername(postDTO.getUsername());
        if (userEntity.getRole().isEmpty() || !userEntity.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("User does not have permission to write a post");
        }
        return postRepository.save(postEntity);
    }

    public void deletePost(long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        }
    }
}
