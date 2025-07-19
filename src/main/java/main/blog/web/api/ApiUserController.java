package main.blog.web.api;

import java.io.UnsupportedEncodingException;
import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.JoinDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.service.AuthServiceImpl;
import main.blog.domain.service.JoinService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import main.blog.util.SendEmail;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiUserController {

    @Autowired
    private JoinService joinService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private final AuthServiceImpl authService;

    @Value("${domain.url}")
    private String domainMainUrl;

    @Value("${domain.front.url}")
    private String domainFrontUrl;

    @Autowired
    private SendEmail sendEmailVerification;

    @PostMapping("/auth/user")
    public ResponseEntity<?> joinUser(@Valid @RequestBody JoinDTO joinDTO) throws MessagingException, UnsupportedEncodingException {
        log.info("join dto = {}", joinDTO);
        String nowDate =  LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        UUID uuid = UUID.randomUUID();
        joinDTO.setVerificationCode(nowDate + uuid.toString());
        UserEntity userEntity = (UserEntity) joinService.joinProcess(joinDTO);

        JoinDTO returnDTO = new JoinDTO();
        returnDTO.setUsername(joinDTO.getUsername());

        try {
            String verificationLink = domainMainUrl + "api/v1/auth/verification?verificationCode=" + nowDate + uuid;
            sendEmailVerification.sendEmailVerification(joinDTO.getUsername(), verificationLink);
        } catch (Exception e) {
            log.error("send email verification error={}", e.getMessage());
        }

        return ApiResponse.success(new ApiResponseMessage(returnDTO, ""));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> getUser(@Valid @RequestBody JoinDTO joinDTO) {
        String token = authService.login(joinDTO);
        log.info("login dto = {} token={}", joinDTO, token);
        return ApiResponse.success(new ApiResponseMessage(token, ""));
    }

    @GetMapping("/auth/password")
    public ResponseEntity<?> findPassword(@RequestParam(value = "username") String username) throws MessagingException, UnsupportedEncodingException {
        log.info("GET user password info = {}", username);
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new EntityNotFoundException("비밀번호 찾기 계정이 없습니다.");
        }

        String nowDate =  LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        UUID uuid = UUID.randomUUID();
        String passwordResetCode = nowDate + uuid.toString();
        String passwordResetLink = domainFrontUrl + "update-password?verificationCode=" + passwordResetCode;
        userEntity.setVerificationCode(passwordResetCode);
        userRepository.save(userEntity);
        sendEmailVerification.sendPasswordVerification(username, passwordResetLink);

        return ApiResponse.success(new ApiResponseMessage("", "비밀번호 변경 인증메일을 확인해주세요."));
    }

    @PostMapping("/auth/password")
    public ResponseEntity<?> setPassword(@Valid @RequestBody JoinDTO joinDTO) throws ParseException {
        joinService.updatePassword(joinDTO);
        log.info("password verification = {}", joinDTO);
        return ApiResponse.success(new ApiResponseMessage("", "비밀번호가 변경되었습니다."));
    }

    @GetMapping("/auth/verification")
    public ResponseEntity<?> setVerification(@RequestParam(value = "verificationCode") String verificationCode) throws ParseException {
        joinService.setVerification(verificationCode);
        log.info("verification = {}", verificationCode);
        return ApiResponse.success(new ApiResponseMessage("", "인증이 완료되었습니다. 로그인을 해주세요."));
    }

    @ExceptionHandler({ AccessDeniedException.class, EntityNotFoundException.class, UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<?> handleException(Exception exception) {
        log.info("auth error={} token={}", exception.getMessage());
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
