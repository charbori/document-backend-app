package main.blog.resolver;

import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class SseEmitters {
    private static final AtomicLong counter = new AtomicLong();
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter add(CustomUserDetails customUserDetails, String lastEventId) {
        String emitterId = customUserDetails.getUsername() + "_" + System.currentTimeMillis();
        log.info(customUserDetails.getUsername() + " / " + customUserDetails.getAuthorities());
        SseEmitter emitter = new SseEmitter();

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data(emitterId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        emitters.put(emitterId, emitter);
        emitter.onCompletion(() -> {
            emitters.remove(emitterId);
        });
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitterId);
        });
        emitter.onError((e) -> {
            emitters.remove(emitterId);
        });
        //todo
        /* client가 미수신한 event 목록이 존재하는 경우 */

        return emitter;
    }

    public void count(String emitterId) {
        long count = counter.incrementAndGet();
        SseEmitter emitter = emitters.get(emitterId);
        try {
            emitter.send(SseEmitter.event()
                    .name("count")
                    .data(count));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendJsonResponse(String emitterId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(emitterId);
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
