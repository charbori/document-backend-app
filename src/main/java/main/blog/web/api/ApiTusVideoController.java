package main.blog.web.api;

import com.google.api.services.youtube.model.Video;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.FileStorageService;
import main.blog.domain.service.VideoService;
import main.blog.domain.service.VideoTusUploadService;
import main.blog.util.ApiResponse;
import main.blog.util.ApiResponseMessage;
import me.desair.tus.server.HttpHeader;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/api/content/tus")
//access Cros
@Slf4j
@CrossOrigin(origins = "*")
public class ApiTusVideoController {
    @Autowired
    private TusFileUploadService tusFileUploadService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private VideoTusUploadService videoTusUploadService;

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.POST, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS})
    public void processUpload(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, TusException {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        tusFileUploadService.process(servletRequest, servletResponse);
        servletResponse.addHeader("Access-Control-Expose-Headers", "Location,Upload-Offset,Upload-Length");
        String requestUri = servletRequest.getRequestURI();
        videoTusUploadService.uploadProcess(requestUri, customUserDetails.getUserInfoDTO());
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

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.DELETE})
    public ResponseEntity<?> deleteFilePreview(@RequestBody String videoName, HttpServletRequest servletRequest) throws IOException {
        String uploadURI = servletRequest.getHeader(HttpHeader.LOCATION);
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        videoTusUploadService.deleteUploadFile(videoName, uploadURI, customUserDetails.getUserInfoDTO());
        log.info("upload request url & user {} {}", uploadURI, customUserDetails);
        log.info("upload getHeaderNames {} ", servletRequest.getHeaderNames());

        return ApiResponse.success(new ApiResponseMessage(uploadURI, ""));
    }

    @RequestMapping(value = {"", "/**"}, method = {RequestMethod.GET})
    public ResponseEntity<Resource> getFilePreview(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
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
