package main.blog.web.api;

import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.VideoDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.VideoService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/content")
public class ApiVideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/video")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> uploadVideo(@RequestParam("video") MultipartFile file,
                                         @RequestParam("videoThumbnail") MultipartFile fileImage) {
        log.info("fileinfo filename={}", file.getOriginalFilename());
        log.info("fileinfo name={}", file.getName());
        log.info("fileinfo contenttype={}", file.getContentType());
        log.info("fileinfo size={}", file.getSize());
        log.info("fileinfo size={}", file.getSize());
        log.info("fileImage filename={}", fileImage.getOriginalFilename());
        log.info("fileImage name={}", fileImage.getName());
        log.info("fileImage contenttype={}", fileImage.getContentType());
        log.info("fileImage size={}", fileImage.getSize());
        log.info("fileImage size={}", fileImage.getSize());

        String username = "tester";
        UserEntity user = new UserEntity();
        user.setId(3L);

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        String videoPath =  username + "/" + uuidString;
        String thumbnailPath = username + "/thumbnail" + uuidString;
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

    @GetMapping("/video")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> getVideo() {
        String username = "tester";

        // todo video dto로 변경하자
        List<VideoEntity> videoList = videoService.getVideoList(username);

        return ApiResponse.success(videoList);
    }

    @ExceptionHandler({ RuntimeException.class, AccessDeniedException.class, EntityNotFoundException.class })
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
