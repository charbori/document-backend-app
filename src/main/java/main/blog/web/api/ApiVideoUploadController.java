package main.blog.web.api;

import lombok.extern.slf4j.Slf4j;
import main.blog.domain.service.VideoService;
import main.blog.util.YoutubeChannelDownload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class ApiVideoUploadController {

    @Autowired
    private VideoService videoService;

    @RequestMapping("/video/{id}")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<?> uploadVideo(@PathVariable("id") Long id) throws GeneralSecurityException, IOException {
        YoutubeChannelDownload youtubeChannelDownload = new YoutubeChannelDownload();
        youtubeChannelDownload.createVideo();
        return ResponseEntity.ok("done");
    }
}
