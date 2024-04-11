package main.blog.web.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ApiResponseAuth {

    //@Value("${auth.path.callback}")
    private String SERVER_ENDPOINT_REDIRECT = "https://dev.couhensoft.com/oauth2/callback";

    //@Value("${auth.key.client.tiktok}")
    private String AUTH_CLIENT_KEY = "awi26jvyuznaicno";

    @RequestMapping("/auth/authorize")
    public ResponseEntity<?> getTikTokAuthorize() {
        String defineCallbackMethod = "";
        String csrfState = RandomStringUtils.random(34, false, true);
        String tiktokUrl = "https://www.tiktok.com/v2/auth/authorize/";
        tiktokUrl += "?client_key=" + AUTH_CLIENT_KEY;
        tiktokUrl += "&scope=user.info.basic";
        tiktokUrl += "&response_type=code";
        tiktokUrl += "&redirect_uri=" + SERVER_ENDPOINT_REDIRECT;
        tiktokUrl += "&state=" + csrfState;

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("url", tiktokUrl);

        return ResponseEntity.ok(responseMap);
    }

    @RequestMapping("/oauth2/callback")
    public ResponseEntity<?> authCallback() {

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("code", "test");
        responseMap.put("state", "test");

        return ResponseEntity.ok(responseMap);
    }

    @RequestMapping("/Callback")
    public ResponseEntity<?> authCallback2() {

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("code", "test");
        responseMap.put("state", "test");

        return ResponseEntity.ok(responseMap);
    }
}
