package main.blog.domain.service;

import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.UserInfoDTO;
import main.blog.domain.dto.video.VideoDTO;
import main.blog.domain.dto.video.VideoUploadDTO;
import main.blog.domain.entity.UserEntity;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.repository.UserRepository;
import main.blog.domain.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
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

    public void deleteVideo(Long videoId) throws MinioException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        Optional<VideoEntity> findVideo = videoRepository.findById(videoId);
        VideoEntity videoEntity = findVideo.orElseThrow(()->
                new EntityNotFoundException("업데이트할 비디오 메타데이터가 없습니다."));

        minioService.deleteTusFile(videoEntity.getName());
        videoRepository.deleteById(videoId);
    }

    public VideoEntity createVideoMetaData(VideoDTO videoDTO) {
        VideoEntity videoEntity = videoDTO.toVideoEntity();
        videoEntity.setCreatedAt(LocalDateTime.now());
        videoEntity.setUpdatedAt(LocalDateTime.now());
        return videoRepository.save(videoEntity);
    }

    public VideoEntity updateVideoMetaData(VideoDTO videoDTO) {
        List<VideoEntity> findVideo = videoRepository.findByNameAndUser(videoDTO.getName(), videoDTO.getUser().toUserEntity());
        if (findVideo.size() == 0) {
            throw new EntityNotFoundException("업데이트할 비디오 메타데이터가 없습니다.");
        } else if (findVideo.size() > 1) {
            log.error("updateContentUpload() 중복된 비디오 메타데이터 ERROR {}", videoDTO);
        }

        VideoEntity videoEntity = videoDTO.toVideoEntity();
        videoEntity.setId(findVideo.get(0).getId());
        videoEntity.setName(findVideo.get(0).getName());
        videoEntity.setUpdatedAt(LocalDateTime.now());

        return videoRepository.save(videoEntity);
    }

    public VideoEntity updateVideoMetaDataStatus(VideoUploadDTO videoUploadDTO) {
        List<VideoEntity> findVideo = videoRepository.findByNameAndUser(videoUploadDTO.getName(), videoUploadDTO.getUser().toUserEntity());
        if (findVideo.size() == 0) {
            throw new EntityNotFoundException("업데이트할 비디오 메타데이터가 없습니다.");
        } else if (findVideo.size() > 1) {
            log.error("updateContentUpload() 중복된 비디오 메타데이터 ERROR {}", videoUploadDTO);
        }

        VideoEntity videoEntity = findVideo.get(0);
        videoEntity.setStatus(videoEntity.getStatus());
        videoEntity.setUpdatedAt(LocalDateTime.now());

        return videoRepository.save(videoEntity);
    }

    public VideoEntity updateVideoUploadStatus(@NotBlank String videoName, UserInfoDTO userInfoDTO, String status) {
        List<VideoEntity> findVideo = videoRepository.findByNameAndUser(videoName, userInfoDTO.toUserEntity());
        if (findVideo.size() == 0) {
            throw new EntityNotFoundException("업데이트할 비디오 메타데이터가 없습니다.");
        } else if (findVideo.size() > 1) {
            log.error("updateContentUpload() 중복된 비디오 메타데이터 ERROR {}", videoName);
        }

        VideoEntity videoEntity = findVideo.get(0);
        videoEntity.setStatus(status);
        videoEntity.setUpdatedAt(LocalDateTime.now());

        return videoRepository.save(videoEntity);
    }

    public void deleteVideoMetaData(String videoName, UserInfoDTO userInfoDTO) throws MinioException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        List<VideoEntity> findVideo = videoRepository.findByNameAndUser(videoName, userInfoDTO.toUserEntity());
        long videoId = 0L;

        if (findVideo.size() == 0) {
            throw new EntityNotFoundException("업데이트할 비디오 메타데이터가 없습니다.");
        } else if (findVideo.size() > 1) {
            log.error("updateContentUpload() 중복된 비디오 메타데이터 ERROR {}", videoName);
        }
        for (VideoEntity video: findVideo) {
            videoId = video.getId();
        }
        minioService.deleteTusFile(videoName);
        videoRepository.deleteById(videoId);
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

}
