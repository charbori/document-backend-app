package main.blog.web.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CommentDTO;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.PostDTO;
import main.blog.domain.entity.CommentEntity;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.service.CommentService;
import main.blog.domain.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.xml.stream.events.Comment;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Controller
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    PostService postService;

    @GetMapping("/comment/{id}")
    public String getComment(@PathVariable("id") long comment_id,
                        Model model) {
        Optional<CommentEntity> commentEntity = commentService.getComment(comment_id);
        commentEntity.orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + comment_id));
        commentEntity.ifPresent((commentEntity1 -> {
            model.addAttribute("comment", commentEntity1);
        }));
        return "pages/comment";
    }

    @PostMapping("/comment")
    public String registComment(@Validated CommentDTO commentDTO,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/post/" + commentDTO.getPostEntity().getId();
        }

        try {
            CommentEntity commentEntity = commentService.registComment(commentDTO);
            return "redirect:/post/" + commentDTO.getPostEntity().getId();
        } catch (AccessDeniedException e) {
            bindingResult.reject("userRoleError");
            return "redirect:/post/" + commentDTO.getPostEntity().getId();
        }
    }

    @GetMapping("/commentEdit/{id}")
    public String commentEdit(@PathVariable("id") long comment_id,
                              Model model) {
        model.addAttribute("id", 1);
        model.addAttribute("comment_id", 1);
        Optional<CommentEntity> commentEntity = commentService.getComment(comment_id);
        commentEntity.orElseThrow(() -> new EntityNotFoundException("Comment not found with id " + comment_id));
        commentEntity.ifPresent((commentEntity1 -> {
            model.addAttribute("comment", commentEntity1);
        }));
        return "pages/comment/commentEdit";
    }

    @PostMapping("/post/{id}/comments")
    public String registComment(@PathVariable("id") long id,
                                @ModelAttribute("commentContent") String commentContent,
                                Model model) {
        CustomUserDetails customUserDetails =
                (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity userEntity = customUserDetails.getUserInfoDTO();
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setContent(commentContent);
        commentDTO.setCreatedAt(LocalDateTime.now());
        commentDTO.setUpdatedAt(LocalDateTime.now());
        commentDTO.setUserEntity(userEntity);

        try {
            commentService.registComment(commentDTO);
            return "redirect:/post/" + commentDTO.getPostEntity().getId();
        } catch (AccessDeniedException e) {
            return "redirect:/post/" + commentDTO.getPostEntity().getId();
        }
    }

}
