package main.blog.web.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.JoinDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.service.AuthServiceImpl;
import main.blog.domain.service.JoinService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiUserController {

    @Autowired
    private JoinService joinService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private final AuthServiceImpl authService;

    @PostMapping("/user")
    public ResponseEntity<?> joinUser(@Valid @RequestBody JoinDTO joinDTO) {
        log.info("join dto = {}", joinDTO);
        UserEntity userEntity = (UserEntity) joinService.joinProcess(joinDTO);
        JoinDTO returnDTO = new JoinDTO();
        returnDTO.setUsername(joinDTO.getUsername());
        return ApiResponse.success(new ApiResponseMessage(returnDTO, ""));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> getUser(@Valid @RequestBody JoinDTO joinDTO) {
        String token = authService.login(joinDTO);
        log.info("login dto = {} token={}", joinDTO, token);
        return ApiResponse.success(new ApiResponseMessage(token, ""));
    }

    @ExceptionHandler({ AccessDeniedException.class, EntityNotFoundException.class, UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
