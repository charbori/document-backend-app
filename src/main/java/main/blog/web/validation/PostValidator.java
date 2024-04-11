package main.blog.web.validation;

import main.blog.domain.dto.PostDTO;
import main.blog.domain.entity.PostEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Component
public class PostValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PostEntity.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PostDTO post = (PostDTO) target;
        if (!StringUtils.hasText(post.getTitle())) {
            errors.rejectValue("title", "required");
        }
        if (!StringUtils.hasText(post.getContent())) {
            errors.rejectValue("content", "required");
        }
        if (!StringUtils.hasText(SecurityContextHolder.getContext().getAuthentication().getName())) {
            errors.rejectValue("username", "required");
        }
    }
}
