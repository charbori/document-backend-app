package main.blog.domain.service;

import main.blog.domain.dto.JoinDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.repository.UserRepository;
import net.bytebuddy.asm.MemberSubstitution;
import org.h2.engine.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class JoinServiceTest {

    @Autowired
    private JoinService joinService;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockBean
    private UserRepository userRepository;


    @Test
    public void testJoinProcess() {
        JoinDTO joinDTO = new JoinDTO();
        joinDTO.setUsername("test");
        joinDTO.setPassword("test");

        joinService.joinProcess(joinDTO);

        ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userEntityArgumentCaptor.capture());
        UserEntity userJoined = userEntityArgumentCaptor.getValue();

        //UserEntity userJoined = userRepository.findByUsername(joinDTO.getUsername());

        assertThat(userJoined.getUsername()).isEqualTo(joinDTO.getUsername());
        assertThat(userJoined.getPassword()).isEqualTo(bCryptPasswordEncoder.encode("test"));
        assertThat(userJoined.getRole()).isEqualTo("ROLE_SUPER_ADMIN");

    }
}