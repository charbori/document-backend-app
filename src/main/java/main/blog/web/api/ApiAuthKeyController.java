package main.blog.web.api;

import main.blog.util.ApiAuthUtil;
import main.blog.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ApiAuthKeyController {
    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/api/v1/auth/{key}")
    public ResponseEntity<?> getAuthKey(@PathVariable("key") String requestKey) {
        String getAesEncodedKey = ApiAuthUtil.getAuthKeyPublished(requestKey);
        return ResponseEntity.ok()
                .body(getAesEncodedKey);
    }
}
