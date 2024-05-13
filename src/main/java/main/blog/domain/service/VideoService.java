package main.blog.domain.service;

import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.VideoDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MinioService minioService;

    public List<VideoEntity> videoList() {
        List<VideoEntity> videosList = videoRepository.findAll();
        if (videosList == null)
            throw new EntityNotFoundException("VideoEntity videosList() not found");
        return videosList;
    }

    public VideoEntity getVideo(Long videoId) {
        Optional<VideoEntity> video = videoRepository.findById(videoId);
        return video.orElseThrow(() -> {
            throw new EntityNotFoundException("VideoEntity video() not found");
        });
    }

    public String registVideo(VideoDTO videoDTO, MultipartFile file, MultipartFile fileImage) {
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setName(videoDTO.getName());
        videoEntity.setDescription(videoDTO.getDescription());
        videoEntity.setTag(videoDTO.getTag());
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setVideoPath(videoDTO.getVideoPath());
        videoEntity.setThumbnailPath(videoDTO.getThumbnailPath());
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setCreatedAt(LocalDateTime.now());
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setUser(videoDTO.getUser());

        this.createVideoMetaData(videoEntity);

        String response = null;
        try {
            response = minioService.uploadFile(file, videoDTO.getVideoPath());
            response = minioService.uploadFile(fileImage, videoDTO.getThumbnailPath());
        } catch (MinioException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    public VideoEntity createVideoMetaData(VideoEntity videoEntity) {
        return videoRepository.save(videoEntity);
    }

    public String registTusVideo(InputStream io, String filename, long filesize, String contentTypeName) {
        String response = null;
        try {
            response = minioService.uploadTusFile(io, filename, filesize, contentTypeName);
        } catch (MinioException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public VideoEntity updateVideo(long id, @Validated VideoDTO videoDTO) throws AccessDeniedException {
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setName(videoDTO.getName());
        videoEntity.setDescription(videoDTO.getDescription());
        videoEntity.setTag(videoDTO.getTag());
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setThumbnailPath(videoDTO.getThumbnailPath());
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setStatus(videoDTO.getStatus());
        videoEntity.setUpdatedAt(LocalDateTime.now());
        videoEntity.setUser(videoDTO.getUser());
        return videoRepository.save(videoEntity);
    }

    public ResponseEntity<?> downloadVideo(Long id) {
        Optional<VideoEntity> videoEntityOptional = videoRepository.findById(id);
        VideoEntity videoEntity = videoEntityOptional.orElseThrow(()
                        -> new EntityNotFoundException("Post not found with id " + id));
        String videoPath = videoEntity.getVideoPath();
        ResponseEntity response = minioService.downloadFile(videoPath);
        return response;
    }

    public List<VideoEntity> getVideoList(String username) {
        UserEntity user = userRepository.findByUsername(username);
        return videoRepository.findAllByUser(user);
    }

    public List<VideoEntity> getVideoList(String username, Pageable pageable) {
        UserEntity user = userRepository.findByUsername(username);
        return videoRepository.findAllByUser(user, pageable);
    }

    public void updateStatusVideo(String uploadURI, String statusType) {
        List<VideoEntity> findVideo = videoRepository.findByName(uploadURI);
        for (VideoEntity videoEntity: findVideo) {
            videoEntity.setStatus(statusType);
            createVideoMetaData(videoEntity);
        }
    }

}
