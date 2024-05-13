package main.blog.web.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.dto.VideoDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.VideoCategoryService;
import main.blog.domain.service.VideoService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/content")
public class ApiVideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoCategoryService videoCategoryService;

    @PostMapping("/video")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> uploadVideo(@RequestParam("video") MultipartFile file,
                                         @RequestParam("videoThumbnail") MultipartFile fileImage) {
        log.info("fileinfo filename={}", file.getOriginalFilename());
        log.info("fileinfo name={}", file.getName());
        log.info("fileinfo contenttype={}", file.getContentType());
        log.info("fileinfo size={}", file.getSize());
        log.info("fileImage filename={}", fileImage.getOriginalFilename());
        log.info("fileImage name={}", fileImage.getName());
        log.info("fileImage contenttype={}", fileImage.getContentType());
        log.info("fileImage size={}", fileImage.getSize());
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        UserInfoDTO user = new UserInfoDTO(customUserDetails.getUserInfoDTO().getId(),customUserDetails.getUserInfoDTO().getUsername(), customUserDetails.getUserInfoDTO().getRole());

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String videoPath =  user.getUsername() + "/" + uuidString;
        String thumbnailPath = user.getUsername() + "/thumbnail" + uuidString;
        String tag = "1.0";

        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setName(file.getOriginalFilename());
        videoDTO.setDescription(file.getOriginalFilename());
        videoDTO.setStatus("WAIT");
        videoDTO.setVideoPath(videoPath);
        videoDTO.setThumbnailPath(thumbnailPath);
        videoDTO.setTag(tag);
        videoDTO.setUser(user);
        String response = "";
        response = videoService.registVideo(videoDTO, file, fileImage);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("videoPath", "video-manager/" + videoPath);
        responseMap.put("thumbnailPath", "thumbmail-manager/" + videoPath);
        return ApiResponse.success(responseMap);
    }

    @GetMapping("/video/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getVideo(@PathVariable(value="id") Long video_id) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        VideoEntity video = videoService.getVideo(customUserDetails.getUserInfoDTO().getId());
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
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            log.info("get user authentication fail");
            throw new BadCredentialsException("로그인을 해주세요.");
        }
        log.info("get user credential :{} {}", authentication.getPrincipal(), authentication.getPrincipal().equals("anonymousUser"));
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails;
    }

    @GetMapping("/video")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getVideoList(@RequestParam(value="_start") int limit_start,
                                          @RequestParam(value="_end") int limit_end) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();

        int offset = limit_start > 0 ? limit_start : 0;
        int pageSize = 100;
        if (limit_end > limit_start) {
            offset = (limit_start) / pageSize;
            pageSize = limit_end - limit_start;
        }
        Pageable paging = PageRequest.of(offset, pageSize);
        log.info("video list limit_start limit_end {} {}", limit_start, limit_end);
        log.info("video list size {} {}", offset, pageSize);

        List<VideoEntity> videoList = videoService.getVideoList(customUserDetails.getUsername(), paging);
        List<VideoDTO> videoDTOList = videoList.stream()
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
        return ApiResponse.success(videoDTOList);
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
