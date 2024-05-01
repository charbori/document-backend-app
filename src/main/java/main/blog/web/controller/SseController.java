package main.blog.web.controller;


import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import main.blog.domain.dto.VideoDTO;
import main.blog.domain.entity.VideoEntity;
import main.blog.domain.service.VideoService;
import main.blog.resolver.SseEmitters;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/sse/content")
@Slf4j
public class SseController {
    @Autowired
    private final SseEmitters sseEmitters;
    @Autowired
    private VideoService videoService;

    public SseController(SseEmitters sseEmitters) {
        this.sseEmitters = sseEmitters;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        SseEmitter emitter = sseEmitters.add(userDetails, lastEventId);
        return ResponseEntity.ok(emitter);
    }

    @PostMapping("/count")
    public ResponseEntity<Void> count(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam(value="emitterId") String emitterId) {
        sseEmitters.count(emitterId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/videos")
    public ResponseEntity<Void> videos(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam(value="emitterId") String emitterId) {
        List<VideoEntity> videoDTO = videoService.getVideoList("");
        sseEmitters.sendJsonResponse(emitterId, "videoList", videoDTO);
        return ResponseEntity.ok().build();
    }
}
