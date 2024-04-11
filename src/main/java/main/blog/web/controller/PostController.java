package main.blog.web.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.PostDTO;
import main.blog.domain.dto.PostFormDTO;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.service.CommentService;
import main.blog.domain.service.FileStorageService;
import main.blog.domain.service.PostService;
import main.blog.exception.UserNotMatchErrorException;
import main.blog.util.SecurityUtil;
import main.blog.web.validation.PostValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    CommentService commentService;

    @Value("${file.filepath}")
    private String filePath;

    @GetMapping("/")
    public String home(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                       @RequestParam(value = "criteria", required = false, defaultValue = "createAt") String criteria,
                       Model model) {
        Page<PostEntity> postPage = postService.getPostList(page, criteria);
        List<PostEntity> postList = postPage.getContent();

        model.addAttribute("posts", postList);
        model.addAttribute("imagePath", filePath);
        if (postPage.getTotalPages() > page + 1)
            model.addAttribute("nextPage", page + 1);
        return "default";
    }

    @GetMapping("/post/regist")
    public String postRegistView(@ModelAttribute("post_entity") PostDTO postDTO, Model model) {
        return "pages/post/postRegist";
    }

    @GetMapping("/post/{id}/edit")
    public String postEditView(@PathVariable(value="id") long postId, Model model) {
        PostDTO postDTO = postService.getPost(postId);
        model.addAttribute("post_id", postId);
        model.addAttribute("post_entity", postDTO);
        model.addAttribute("imagePath", filePath);
        return "pages/post/postEdit";
    }

    @GetMapping("/post/{id}")
    public String postMain(@PathVariable(value="id") long postId,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        PostDTO postDTO = postService.getPost(postId);

        model.addAttribute("post_entity", postDTO);
        model.addAttribute("commentList", postDTO.getCommentDTOS());
        model.addAttribute("imagePath", filePath);

        String user_id = "";
        if (SecurityUtil.isAuthenticated()) {

        }

        model.addAttribute("user_id", user_id);

        return "pages/post";
    }

    @PostMapping(path = "/post", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String addPost(@RequestPart("imageName") MultipartFile imageName,
                          @Validated @ModelAttribute("post_entity") PostFormDTO postFormDTO,
                          BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "pages/post/postRegist";
        }

        String imageFilename = fileStorageService.storeFile(imageName);
        postFormDTO.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        postFormDTO.setCreatedAt(LocalDateTime.now());
        PostDTO postDTO = postFormDTO.getPostDTO();
        postDTO.setImageName(imageFilename);

        try {
            PostEntity postEntity = postService.registPost(postDTO);
            return "redirect:/post/" + postEntity.getId();
        } catch (AccessDeniedException e) {
            bindingResult.reject("userRoleError");
            return "pages/post/postRegist";
        }
    }

    @PostMapping("/post/{id}/edit")
    public String editPost(@PathVariable(value="id") long postId,
                           @Validated @ModelAttribute("post_entity") PostDTO postDTO,
                           Model model,
                           BindingResult bindingResult) {

        postDTO.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", postId);
            return "pages/post/postEdit";
        }

        try {
            postService.updatePost(postId, postDTO);
            return "redirect:/post/" + postId;
        } catch (AccessDeniedException e) {
            model.addAttribute("postId", postId);
            bindingResult.reject("userRoleError");
            return "pages/post/postEdit";
        }
    }

    //@GetMapping("/post/{id}/comment/{comment_id}/delete")
    public String deleteComment(@PathVariable(value="id") long postId,
                                @PathVariable(value="comment_id") long commentId,
                                Model model) {
        boolean resDelete = false;
        Map<String, String> errors = new HashMap<>();
        try {
            commentService.deleteComment(commentId);
        } catch (EntityNotFoundException e) {
            errors.put("commentAlreadyDeleteError", "이미 삭제된 댓글입니다.");
        }

        if (resDelete == false) {
            errors.put("commentDeleteError", "댓글의 삭제에 실패하였습니다.");
            model.addAttribute("globalErrors", errors);
        }

        PostDTO postDTO = postService.getPost(postId);
        model.addAttribute("post_entity", postDTO);
        model.addAttribute("commentList", postDTO.getCommentDTOS());

        String user_id = "";
        if (SecurityUtil.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity userEntity = customUserDetails.getUserInfoDTO();
            user_id = userEntity.getUsername();
        }

        model.addAttribute("user_id", user_id);
        return "pages/post";
    }

    @GetMapping("/post/{id}/comment/{comment_id}/delete")
    public String deleteCommentV2(@PathVariable(value="id") long postId,
                                @PathVariable(value="comment_id") long commentId,
                                Model model) {
        Map<String, String> errors = new HashMap<>();
        try {
            commentService.deleteComment(commentId);
        } catch (UserNotMatchErrorException exception) {
            errors.put("commentDeleteError", "댓글의 삭제에 실패하였습니다.");
            model.addAttribute("globalErrors", errors);
        }

        String user_id = "";
        if (SecurityUtil.isAuthenticated()) {
            CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity userEntity = customUserDetails.getUserInfoDTO();
            user_id = userEntity.getUsername();
        }

        model.addAttribute("user_id", user_id);
        return "pages/post";
    }
}
