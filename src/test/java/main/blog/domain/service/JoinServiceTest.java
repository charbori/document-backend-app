package main.blog.domain.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import main.blog.domain.dto.JoinDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class JoinServiceTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JoinService joinService;

    @Test
    public void testJoinProcess() {
        // Mock BCryptPasswordEncoder behavior
        when(bCryptPasswordEncoder.encode("test")).thenReturn("encodedPassword");
        when(userRepository.existsByUsername("test")).thenReturn(false);
        
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUsername("test");
        joinDTO.setPassword("test");

        joinService.joinProcess(joinDTO);

        ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userEntityArgumentCaptor.capture());
        UserEntity userJoined = userEntityArgumentCaptor.getValue();

        assertThat(userJoined.getUsername()).isEqualTo(joinDTO.getUsername());
        assertThat(userJoined.getPassword()).isEqualTo("encodedPassword");
        assertThat(userJoined.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(userJoined.getVerification()).isEqualTo("Y");
        assertThat(userJoined.getVerificationCode()).isEqualTo("");
    }

    @Test
    public void testJoinProcessWithVerificationCode() {
        // Mock BCryptPasswordEncoder behavior
        when(bCryptPasswordEncoder.encode("test")).thenReturn("encodedPassword");
        when(userRepository.existsByUsername("test")).thenReturn(false);
        
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUsername("test");
        joinDTO.setPassword("test");
        joinDTO.setVerificationCode("20250913abc123");

        joinService.joinProcess(joinDTO);

        ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userEntityArgumentCaptor.capture());
        UserEntity userJoined = userEntityArgumentCaptor.getValue();

        assertThat(userJoined.getUsername()).isEqualTo(joinDTO.getUsername());
        assertThat(userJoined.getPassword()).isEqualTo("encodedPassword");
        assertThat(userJoined.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(userJoined.getVerification()).isEqualTo("N");
        assertThat(userJoined.getVerificationCode()).isEqualTo("20250913abc123");
    }

    @Test
    public void testJoinProcessWithExistingUser() {
        // Mock behavior for existing user
        when(userRepository.existsByUsername("test")).thenReturn(true);
        
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUsername("test");
        joinDTO.setPassword("test");

        // Should throw IllegalArgumentException
        assertThatThrownBy(() -> joinService.joinProcess(joinDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 사용자입니다: test");

        // Verify save was never called
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}