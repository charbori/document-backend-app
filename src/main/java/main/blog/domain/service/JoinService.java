package main.blog.domain.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import main.blog.domain.dto.JoinDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;

@Service
public class JoinService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public Object joinProcess(JoinDTO joinDTO) {
        UserEntity userEntity = new UserEntity();
        if (userRepository.existsByUsername(joinDTO.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다: " + joinDTO.getUsername());
        }
        userEntity.setUsername(joinDTO.getUsername());
        userEntity.setPassword(bCryptPasswordEncoder.encode(joinDTO.getPassword()));
        userEntity.setRole("ROLE_ADMIN");
        
        // verificationCode가 있으면 이메일 인증이 필요한 상태로, 없으면 즉시 활성화
        if (joinDTO.getVerificationCode() != null && !joinDTO.getVerificationCode().isEmpty()) {
            userEntity.setVerificationCode(joinDTO.getVerificationCode());
            userEntity.setVerification("N");
            userEntity.setVerificationAt(null);
        } else {
            userEntity.setVerificationCode("");
            userEntity.setVerification("Y");
            userEntity.setVerificationAt(LocalDateTime.now());
        }

        return userRepository.save(userEntity);
    }

    public boolean setVerification(String verificationCode) throws ParseException {
        UserEntity userEntity = userRepository.findByVerificationCode(verificationCode);
        if (userEntity == null) {
            throw new EntityNotFoundException("인증요청 회원이 존재하지 않습니다.");
        }
        String requestDate = verificationCode.substring(0, 8);

        if (validUpdate(requestDate)) {
            userEntity.setVerification("Y");
            userEntity.setVerificationAt(LocalDateTime.now());
            userEntity.setVerificationCode("");
            userRepository.save((userEntity));
            return true;
        } else {
            throw new BadCredentialsException("인증번호가 만료되었습니다. 비밀번호를 찾기로 계정을 활성화해주세요.");
        }
    }

    public boolean updatePassword(JoinDTO joinDTO) throws ParseException {
        UserEntity userEntity = userRepository.findByVerificationCode(joinDTO.getVerificationCode());
        if (userEntity == null) {
            throw new EntityNotFoundException("비밀번호 변경요청이 존재하지 않습니다.");
        }
        String requestDate = joinDTO.getVerificationCode().substring(0, 8);

        if (validUpdate(requestDate)) {
            userEntity.setVerification("Y");
            userEntity.setVerificationAt(LocalDateTime.now());
            userEntity.setPassword(bCryptPasswordEncoder.encode(joinDTO.getPassword()));
            userEntity.setVerificationCode("");
            userRepository.save((userEntity));
            return true;
        } else {
            throw new BadCredentialsException("인증번호가 만료되었습니다. 비밀번호를 찾기로 계정을 활성화해주세요.");
        }
    }

    private boolean validUpdate(String requestDate) throws ParseException {
        String todayfm = new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(dateFormat.parse(requestDate).getTime());
        Date today = new Date(dateFormat.parse(todayfm).getTime());

        long diffSec = (today.getTime() - date.getTime()) / 1000;
        long diffDays = diffSec / (24*60*60); //일자수 차이

        if (diffDays <= 30) {
            return true;
        }
        return false;
    }
}
