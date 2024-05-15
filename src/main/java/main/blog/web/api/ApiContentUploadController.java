package main.blog.web.api;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.video.VideoDTO;
import main.blog.domain.dto.video.VideoUploadDTO;
import main.blog.domain.repository.VideoRepository;
import main.blog.domain.service.VideoService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/content/metadata")
@Slf4j
@CrossOrigin(origins = "*")
public class ApiContentUploadController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST})
    public ResponseEntity<?> contentUpload(@RequestBody @Valid VideoDTO videoDTO, final HttpServletResponse servletResponse) throws IOException {
        log.info("content create metadata={}",videoDTO);
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoDTO.setUser(customUserDetails.getUserInfoDTO());
        return ApiResponse.success(new ApiResponseMessage(videoService.createVideoMetaData(videoDTO), ""));
    }

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.PATCH})
    public ResponseEntity<?> updateVideo(@RequestBody @Valid VideoDTO videoDTO, final HttpServletResponse servletResponse) throws IOException {
        log.info("content update metadata={}",videoDTO);
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoDTO.setUser(customUserDetails.getUserInfoDTO());

        return ApiResponse.success(new ApiResponseMessage(videoService.updateVideoMetaData(videoDTO), ""));
    }

    @RequestMapping(value = {"/status"}, method = {RequestMethod.PATCH})
    public ResponseEntity<?> updateVideoUploadStatus(@RequestBody @Valid VideoUploadDTO videoUploadDTO, final HttpServletResponse servletResponse) throws IOException {
        log.info("content update videoUploadDTO data={}",videoUploadDTO);
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoUploadDTO.setUser(customUserDetails.getUserInfoDTO());

        return ApiResponse.success(new ApiResponseMessage(videoService.updateVideoMetaDataStatus(videoUploadDTO), ""));
    }

    private static CustomUserDetails getAuthenticatedUserDetail() {
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.info("get user authentication fail {} ", authentication);
            throw new BadCredentialsException("로그인을 해주세요.");
        }
        log.info("get user credential :{} {}", authentication.getPrincipal(), authentication.getPrincipal().equals("anonymousUser"));
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails;
    }

}
