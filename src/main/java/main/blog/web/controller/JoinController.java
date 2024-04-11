package main.blog.web.controller;

import lombok.extern.slf4j.Slf4j;
import main.blog.domain.dto.JoinDTO;
import main.blog.domain.service.JoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class JoinController {

    @Autowired
    private JoinService joinService;

    @GetMapping("/join")
    public String joinPage() {
        return "pages/join";
    }

    @PostMapping("/joinAction")
    public String joinAction(JoinDTO joinDTO) {
        log.debug("Join dto={}", joinDTO);

        joinService.joinProcess(joinDTO);

        return "redirect:/login";
    }
}
