package main.blog.web.api;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.dto.VideoListDTO;
import main.blog.domain.dto.video.VideoDTO;
import main.blog.domain.dto.video.VideoUploadDTO;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.VideoCategoryService;
import main.blog.domain.service.VideoService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;

@Slf4j
@RestController
@RequestMapping("/api/content")
public class ApiVideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoCategoryService videoCategoryService;

    @GetMapping("/video/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getVideo(@PathVariable(value="id") Long videoId) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        VideoEntity video = videoService.getVideo(videoId);
        VideoDTO videoDTO = new VideoDTO(video.getId(),
                new UserInfoDTO(customUserDetails.getUserInfoDTO().getId(), customUserDetails.getUserInfoDTO().getUsername(), customUserDetails.getUserInfoDTO().getRole()),
                video.getName(),
                video.getDescription(),
                video.getStatus(),
                video.getThumbnailPath(),
                video.getVideoPath(),
                video.getTag(),
                video.getVideoType(),
                video.getRole(),
                video.getCreatedAt(),
                video.getUpdatedAt());
        return ApiResponse.success(videoDTO);
    }

    private static CustomUserDetails getAuthenticatedUserDetail() {
        log.info("start authenticate");
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadCredentialsException("로그인을 해주세요.");
        }
        log.info("get user credential :{} {}", authentication.getPrincipal(), authentication.getPrincipal().equals("anonymousUser"));
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails;
    }

    @GetMapping("/video")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getVideoList(VideoListDTO videoListDTO) {
        log.info("start getlist");
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        int offset = videoListDTO.get_start() > 0 ? videoListDTO.get_start() : 0;
        int pageSize = 100;
        if (videoListDTO.get_end() > videoListDTO.get_start()) {
            offset = (videoListDTO.get_start()) / pageSize;
            pageSize = videoListDTO.get_end() - videoListDTO.get_start();
        }
        log.info("contoller getVideoList {}", videoListDTO);
        Sort sortData = Sort.by(videoListDTO.get_order().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, videoListDTO.get_sort());
        Pageable paging = PageRequest.of(offset, pageSize, sortData);
        List<VideoEntity> videoList = videoService.getVideoList(customUserDetails.getUsername(), videoListDTO, paging);
        List<VideoDTO> videoListData = videoList.stream()
                .map(m-> new VideoDTO(m.getId(), new UserInfoDTO(m.getUser().getId(),
                        m.getUser().getUsername(), m.getUser().getRole()),
                        m.getName(),
                        m.getDescription(),
                        m.getStatus(),
                        m.getThumbnailPath(),
                        m.getVideoPath(),
                        m.getTag(),
                        m.getVideoType(),
                        m.getRole(),
                        m.getCreatedAt(),
                        m.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(videoListData);
    }

    @GetMapping("/video/validation/{videoname}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> isExistVideo(@PathVariable(value="videoname") String videoName) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        try {
            VideoEntity video = videoService.getVideoByVideoname(videoName, customUserDetails.getUserInfoDTO().toUserEntity());
            videoName = video.getName();
        } catch (EntityNotFoundException e) {
            return ApiResponse.success(new ApiResponseMessage("", ""));
        }
        return ApiResponse.success(new ApiResponseMessage(videoName, ""));
    }

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST})
    public ResponseEntity<?> contentUpload(@RequestBody @Valid VideoDTO videoDTO, final HttpServletResponse servletResponse) throws IOException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoDTO.setUser(customUserDetails.getUserInfoDTO());
        return ApiResponse.success(new ApiResponseMessage(videoService.createVideoMetaData(videoDTO), ""));
    }

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.PATCH})
    public ResponseEntity<?> updateVideo(@RequestBody @Valid VideoDTO videoDTO, final HttpServletResponse servletResponse) throws IOException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoDTO.setUser(customUserDetails.getUserInfoDTO());

        return ApiResponse.success(new ApiResponseMessage(videoService.updateVideoMetaData(videoDTO), ""));
    }

    @DeleteMapping("/video/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> deleteVideo(@PathVariable(value="id") Long videoId) throws MinioException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoService.deleteVideo(videoId);
        return ApiResponse.success(videoId);
    }

    @RequestMapping(value = {"/status"}, method = {RequestMethod.PATCH})
    public ResponseEntity<?> updateVideoUploadStatus(@RequestBody @Valid VideoUploadDTO videoUploadDTO, final HttpServletResponse servletResponse) throws IOException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoUploadDTO.setUser(customUserDetails.getUserInfoDTO());

        return ApiResponse.success(new ApiResponseMessage(videoService.updateVideoMetaDataStatus(videoUploadDTO), ""));
    }

    @GetMapping("/video/category")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getVideoList() {
        return ApiResponse.success(videoCategoryService.getVideoCategoryList());
    }

    @ExceptionHandler({ RuntimeException.class, AccessDeniedException.class, EntityNotFoundException.class })
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
