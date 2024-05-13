package main.blog.domain.service;

import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity userData = userRepository.findByUsername(username);

        if (userData != null) {
            UserInfoDTO userInfoDTO = new UserInfoDTO(userData.getId(), userData.getUsername(), userData.getRole());
            return new CustomUserDetails(userInfoDTO);
        }
        return null;
    }
}