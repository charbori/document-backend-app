package main.blog.util;

import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.entity.UserEntity;
import main.blog.exception.AuthEncryptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SecurityUtil {

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // 인증되지 않은 경우
        }
        return !authentication.getPrincipal().equals("anonymousUser"); // 익명 사용자가 아닌 경우 인증된 것으로 간주
    }
}