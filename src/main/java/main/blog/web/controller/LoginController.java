package main.blog.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "pages/login";
    }

    @GetMapping("/loginAction")
    public String loginMypage() {
        return "redirect:/pages/mypage";
    }

}
