package main.blog.domain.service;

import io.minio.errors.MinioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.UserInfoDTO;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VideoTusUploadService {
    @Autowired
    private TusFileUploadService tusFileUploadService;
    private ConcurrentHashMap<String, Integer> processingPhaseMap;
    private final Path uploadDirectory;
    private final Path tusUploadDirectory;
    @Autowired
    private VideoService videoService;

    public VideoTusUploadService(@Value("${app.server.data.directory}") String uploadDirectoryPath,
                                 @Value("${tus.server.data.directory}") String tusUploadDirectoryPath) {
        processingPhaseMap = new ConcurrentHashMap<>();
        uploadDirectory = Paths.get(uploadDirectoryPath);
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            log.info("create upload directory " + e.getMessage());
        }

        tusUploadDirectory = Paths.get(tusUploadDirectoryPath);
    }

    public void uploadProcess(String uploadURI, UserInfoDTO userInfoDTO) throws IOException {
        registUploadProcessingPhase(uploadURI, userInfoDTO);

        UploadInfo uploadInfo = null;
        log.info("log url ={}",uploadURI);
        try {
            uploadInfo = tusFileUploadService.getUploadInfo(uploadURI);
        } catch (IOException | TusException e) {
            throw new IOException("GET_UPLOAD_FILE_SERVICE FAIL:" + uploadURI);
        }
        log.info("upload status COMPLETE {}", uploadURI);

        if (uploadInfo != null && !uploadInfo.isUploadInProgress()) {
            String contentType = handleContentType(uploadInfo.getFileMimeType());
            try (InputStream is = tusFileUploadService.getUploadedBytes(uploadURI)) {
                Path output = uploadDirectory.resolve(uploadInfo.getFileName());
                Files.copy(is, output, StandardCopyOption.REPLACE_EXISTING);
                long fileSize = Files.size(output.toAbsolutePath());
                InputStream is2 = tusFileUploadService.getUploadedBytes(uploadURI);
                videoService.updateVideoUploadStatus(uploadInfo.getFileName(), userInfoDTO, "COMPLETE");

                videoService.registTusVideo(is2, uploadInfo.getFileName(), fileSize, contentType);
                processingPhaseMap.remove(uploadURI);
                tusFileUploadService.cleanup();
            } catch (IOException | TusException e) {
                log.info(e.getMessage());
            }

            try {
                tusFileUploadService.deleteUpload(uploadURI);
            } catch (IOException | TusException e) {
                throw new IOException("UPLOAD COMPLETE DELETE FAIL:" + uploadURI);
            }
        }
    }

    private void registUploadProcessingPhase(String uploadURI, UserInfoDTO userInfoDTO) {
        if (!processingPhaseMap.containsKey(uploadURI)) {
            processingPhaseMap.put(uploadURI, 1);
        } else {
            if (processingPhaseMap.get(uploadURI) == 1) {
                processingPhaseMap.put(uploadURI, 1);
                log.info("upload status UPLOADING {}", uploadURI);
                try {
                    videoService.updateVideoUploadStatus(uploadURI, userInfoDTO,"UPLOADING");
                } catch (Exception e) {
                    log.info("updateVideoUploadStatus {} error={}", uploadURI, e.getMessage());
                }
            }
            processingPhaseMap.put(uploadURI, processingPhaseMap.get(uploadURI) + 1);
        }
    }

    public void deleteUploadFile(String videoName, String uploadURI, UserInfoDTO userInfoDTO) throws IOException {
        try {
            videoService.deleteVideoMetaData(videoName, userInfoDTO);
            tusFileUploadService.deleteUpload(uploadURI);
            tusFileUploadService.cleanup();
        } catch (IOException | TusException e) {
            throw new IOException("DELETE FAIL:" + uploadURI);
        } catch (MinioException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String handleContentType(String contentType) throws InvalidContentTypeException {
        if (contentType.startsWith("image/")) {
            return contentType;
        } else if (contentType.startsWith("video/")) {
            return contentType;
        } else {
            throw new InvalidContentTypeException("타입이 적절하지 않음.");
        }
    }
}
