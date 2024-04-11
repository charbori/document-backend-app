package main.blog.domain.service;

import main.blog.domain.dto.JoinDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public Object joinProcess(JoinDTO joinDTO) {
        UserEntity userEntity = new UserEntity();

        userEntity.setUsername(joinDTO.getUsername());
        userEntity.setPassword(bCryptPasswordEncoder.encode(joinDTO.getPassword()));
        userEntity.setRole("ROLE_ADMIN");

        return userRepository.save(userEntity);
    }
}
