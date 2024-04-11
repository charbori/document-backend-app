package main.blog.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.blog.BlogApplication;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.PostDTO;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.entity.PostEntity;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.service.PostService;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.AccessDeniedException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostEntity testPostEntity;

    @BeforeEach
    void initData() {
        testPostEntity = new PostEntity();
        testPostEntity.setId(1);
        UserEntity user = new UserEntity();
        user.setUsername("test");
        user.setId(1L);
        user.setRole("ROLE_ADMIN");
        testPostEntity.setUser(user);
        testPostEntity.setTitle("title");
        testPostEntity.setContent("content");
        testPostEntity.setCreateAt(LocalDateTime.now());


        UserDetails user2 = createUserDetails();

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user2, user.getPassword(), user2.getAuthorities()));

    }

    private UserDetails createUserDetails() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setRole("ROLE_BANNED");
        userEntity.setUsername("test1");
        userEntity.setPassword("test");

        UserInfoDTO userInfoDTO = new UserInfoDTO(userEntity.getUsername(), userEntity.getRole());
        return new CustomUserDetails(userInfoDTO);
    }

    @Test
    void home() throws Exception {
        // given
        int page = 0;
        String criteria = "createAt";

        List<PostEntity> postList = new ArrayList<>();
        Page<PostEntity> postPage = new PageImpl<>(postList);

        when(postService.getPostList(page, criteria)).thenReturn(postPage);

        // when
        mockMvc.perform(get("/").param("page", String.valueOf(page))
                        .param("criteria", criteria))
                .andExpect(status().isOk())
                .andExpect(view().name("default"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("posts", postList));
    }

    @Test
    void postRegistView() throws Exception {
        mockMvc.perform(get("/post/regist"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/post/postRegist"));
    }

    @Test
    void postEditView() throws Exception {
        // given
        long postId = 1L;

        Optional<PostEntity> postEntityOptional = Optional.of(testPostEntity);
        /*
        when(postService.getPost(postId)).thenReturn(postEntityOptional);

        mockMvc.perform(get("/post/1/edit").param("id", String.valueOf(postId)))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/post/postEdit"))
                .andExpect(model().attribute("post_id", 1L))
                .andExpect(model().attribute("post_entity", testPostEntity));

         */
    }

    @Test
    void postMain() throws Exception {
        long postId = 1L;

        Optional<PostEntity> postEntityOptional = Optional.of(testPostEntity);
        /*
        when(postService.getPost(postId)).thenReturn(postEntityOptional);

        mockMvc.perform(get("/post/1").param("id", String.valueOf(postId)))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/post"))
                .andExpect(model().attribute("post_entity", testPostEntity));

         */
    }

    @Test
    @DisplayName("post 등록")
    void addPost() throws Exception {
        long postId = 1L;
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1);
        postDTO.setUsername("test");
        postDTO.setTitle("title");
        postDTO.setContent("content");

        objectMapper = new ObjectMapper();

        when(postService.registPost(any(PostDTO.class))).thenReturn(testPostEntity);

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", "1");
        formParams.add("username", postDTO.getUsername());
        formParams.add("title", postDTO.getTitle());
        formParams.add("content", postDTO.getContent());

        mockMvc.perform(post("/post").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .params(formParams))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));
    }

    @Test
    @DisplayName("post 수정")
    void editPost() throws Exception {
        long postId = 1L;
        PostDTO postDTO = new PostDTO();
        postDTO.setId(1);
        postDTO.setUsername("test");
        postDTO.setTitle("title");
        postDTO.setContent("content");

        when(postService.updatePost(any(Long.class), any(PostDTO.class))).thenReturn(testPostEntity);

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("id", "1");
        formParams.add("username", postDTO.getUsername());
        formParams.add("title", postDTO.getTitle());
        formParams.add("content", postDTO.getContent());

        mockMvc.perform(post("/post/1/edit").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formParams))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/post/1"));
    }

    @Test
    void deletePost() throws Exception {
        long postId = 1L;
        doNothing().when(postService).deletePost(postId);
        mockMvc.perform(post("/post/" + postId + "/delete"))
                .andExpect(status().is3xxRedirection()) // 리다이렉션 발생 검증
                .andExpect(redirectedUrl("/home")); // 리다이렉션 URL 검증
    }
}