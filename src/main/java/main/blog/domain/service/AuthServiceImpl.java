package main.blog.domain.service;

import lombok.RequiredArgsConstructor;
import main.blog.domain.dto.JoinDTO;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public String login(JoinDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        UserEntity userEntity = userRepository.findByUsername(username);

        if (userEntity == null) {
            throw new UsernameNotFoundException("회원이 존재하지 않습니다.");
        }
        if (!bCryptPasswordEncoder.matches(password, userEntity.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO(username, userEntity.getRole());
        return jwtTokenUtil.createAccessToken(userInfoDTO);
    }
}
