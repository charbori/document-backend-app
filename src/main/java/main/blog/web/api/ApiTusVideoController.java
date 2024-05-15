package main.blog.web.api;

import com.google.api.services.youtube.model.Video;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.FileStorageService;
import main.blog.domain.service.VideoService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/api/content/tus")
//access Cros
@Slf4j
@CrossOrigin(origins = "*")
public class ApiTusVideoController {

    @Autowired
    private TusFileUploadService tusFileUploadService;

    private final Path uploadDirectory;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private VideoService videoService;

    private ConcurrentHashMap<String, Integer> hashMap;
    private final Path tusUploadDirectory;

    public ApiTusVideoController(@Value("${app.server.data.directory}") String uploadDirectoryPath,
                                 @Value("${tus.server.data.directory}") String tusUploadDirectoryPath) {
        uploadDirectory = Paths.get(uploadDirectoryPath);
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            log.info("create upload directory " + e.getMessage());
        }

        hashMap = new ConcurrentHashMap<>();
        tusUploadDirectory = Paths.get(tusUploadDirectoryPath);
    }

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS})
    public void processUpload(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException {
        tusFileUploadService.process(servletRequest, servletResponse);
        servletResponse.addHeader("Access-Control-Expose-Headers", "Location,Upload-Offset,Upload-Length");
        String uploadURI = servletRequest.getRequestURI();

        if (!hashMap.containsKey(uploadURI)) {
            hashMap.put(uploadURI, 1);
        } else {
            if (hashMap.get(uploadURI) == 1) {
                hashMap.put(uploadURI, 1);
                //videoService.updateVideoMetaData(uploadURI, "UPLOADING");
                log.info("upload status UPLOADING");
            }
            hashMap.put(uploadURI, hashMap.get(uploadURI) + 1);
        }

        log.info("log url ={}",uploadURI);
        UploadInfo uploadInfo = null;
        try {
            uploadInfo = tusFileUploadService.getUploadInfo(uploadURI);
        } catch (IOException | TusException e) {
            throw new IOException("GET_UPLOAD_FILE_SERVICE FAIL:" + uploadURI);
        }

        if (uploadInfo != null && !uploadInfo.isUploadInProgress()) {
            String contentType = handleContentType(uploadInfo.getFileMimeType());

            try (InputStream is = tusFileUploadService.getUploadedBytes(uploadURI)) {
                Path output = uploadDirectory.resolve(uploadInfo.getFileName());
                Files.copy(is, output, StandardCopyOption.REPLACE_EXISTING);
                long fileSize = Files.size(output.toAbsolutePath());
                InputStream is2 = tusFileUploadService.getUploadedBytes(uploadURI);
                //videoService.updateVideoMetaData(uploadURI, "COMPLETE");
                log.info("upload status COMPLETE");
                videoService.registTusVideo(is2, uploadInfo.getFileName(), fileSize, contentType);
                hashMap.remove(uploadURI);
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

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.DELETE})
    public ResponseEntity<?> deleteFilePreview(@RequestBody String videoName, final HttpServletRequest servletRequest) throws IOException {
        String uploadURI = servletRequest.getRequestURI();
        try {
            //videoService.deleteVideoMetaData(videoName);
            tusFileUploadService.deleteUpload(uploadURI);
            tusFileUploadService.cleanup();
        } catch (IOException | TusException e) {
            throw new IOException("DELETE FAIL:" + uploadURI);
        }
        return ApiResponse.success(new ApiResponseMessage(uploadURI, ""));
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

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.GET})
    public ResponseEntity<Resource> getFilePreview(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws IOException {
        String filePath = URLDecoder.decode(servletRequest.getRequestURI().substring(17), StandardCharsets.UTF_8);
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        // 파일의 컨텐트 타입을 결정
        //String contentType = MediaType.IMAGE_PNG_VALUE;
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }

    @ExceptionHandler({ InvalidContentTypeException.class })
    public ResponseEntity<?> handleException(Exception exception) {
        return ApiResponse.fail(new ApiResponseMessage("", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
