package main.blog.web.api;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.CustomUserDetailsService;
import main.blog.domain.service.VideoCategoryService;
import main.blog.domain.service.VideoService;
import main.blog.filter.JwtAuthFilter;
import main.blog.util.ApiResponse;
import main.blog.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class ApiVideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private Authentication authentication;

    @MockBean
    private VideoCategoryService videoCategoryService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Mock
    private VideoService videoService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetVideo_Success() throws Exception {
        // Setup mock user details
        CustomUserDetails customUserDetails = mock(CustomUserDetails.class);
        UserInfoDTO userInfoDTO = new UserInfoDTO(1L, "user", "ROLE_USER");

        // Setup mock video entity
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setId(1L);
        videoEntity.setName("Test Video");
        videoEntity.setDescription("Test Description");

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(customUserDetails.getUserInfoDTO()).thenReturn(userInfoDTO);

        // Setup video service mock
        //when(this.videoService.getVideo(anyLong())).thenReturn(videoEntity);
        given(videoService.getVideo(anyLong()))
                .willReturn(videoEntity); // stub을 해주지 않아도 원시 타입이라 false를 반환하지만 명시적으로 적어주었다.
        log.info("getvideo {} ", this.videoService.getVideo(1L));

        // Execute and verify
        mockMvc.perform(get("/api/content/video/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Video"))
                .andExpect(jsonPath("$.data.description").value("Test Description"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void testGetVideo_NotFound() throws Exception {
        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mock(CustomUserDetails.class));
        when(userDetailsService.loadUserByUsername("user")).thenReturn(mock(CustomUserDetails.class));
        // Setup video service mock to throw EntityNotFoundException
        when(videoService.getVideo(anyLong())).thenThrow(new EntityNotFoundException("VideoEntity video() not found"));

        // Execute and verify
        mockMvc.perform(get("/api/content/video/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("auth","eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImJpbnN0YXIxM0BnbWFpbC5jb20iLCJyb2xlIjoiUk9MRV9BRE1JTiIsImlhdCI6MTcxOTc1NTQyMywiZXhwIjoxODA2MTU1NDIzfQ.Lv3NKoa1wYP593BO4kvYUOzHr3vXMGL5qmT2U6BKVa8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VideoEntity video() not found"));
    }

    @Test
    public void testGetVideo_Unauthenticated() throws Exception {
        // Execute and verify
        mockMvc.perform(get("/api/content/video/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

}
