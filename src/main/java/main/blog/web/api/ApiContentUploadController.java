package main.blog.web.api;


import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.VideoDTO;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.repository.VideoRepository;
import main.blog.domain.service.VideoService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/content/metadata")
//access Cros
@Slf4j
@CrossOrigin(origins = "*")
public class ApiContentUploadController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST})
    public ResponseEntity<?> contentUpload(@RequestBody VideoDTO videoDTO, final HttpServletResponse servletResponse) throws IOException {
        log.info("content create metadata={}",videoDTO);
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setName(videoDTO.getName());
        videoEntity.setDescription("");
        videoEntity.setTag("1.0");
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setVideoPath("");
        videoEntity.setVideoType(videoDTO.getVideoType());
        videoEntity.setRole(videoDTO.getRole());
        videoEntity.setThumbnailPath("");

        return ApiResponse.success(new ApiResponseMessage(videoService.createVideoMetaData(videoEntity), ""));
    }

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.PATCH})
    public ResponseEntity<?> updateContentUpload(@RequestBody VideoDTO videoDTO, final HttpServletResponse servletResponse) throws IOException {
        log.info("content update metadata={}",videoDTO);
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setName(videoDTO.getName());
        videoEntity.setDescription("");
        videoEntity.setTag("1.0");
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setVideoPath("");
        videoEntity.setVideoType(videoDTO.getVideoType());
        videoEntity.setRole(videoDTO.getRole());
        videoEntity.setThumbnailPath("");

        List<VideoEntity> findVideo = videoRepository.findByName(videoDTO.getName());

        for (VideoEntity video: findVideo) {
            videoEntity.setId(video.getId());
            if (video.getStatus().equals("COMPLETE")) {

            }
            videoService.createVideoMetaData(videoEntity);
        }

        return ApiResponse.success(new ApiResponseMessage(videoService.createVideoMetaData(videoEntity), ""));
    }

}
