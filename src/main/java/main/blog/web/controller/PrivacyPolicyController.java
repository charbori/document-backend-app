package main.blog.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Slf4j
@Controller
public class PrivacyPolicyController {
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/info/privacy")
    public String getMethodName(Locale locale, Model model) {
        log.info("locale={}",locale.getLanguage());

        if (!locale.getLanguage().equals("ko")) {
            locale = new Locale("en");
        }
        log.info("locale={}",locale);

        model.addAttribute("title", messageSource.getMessage("info.privacy.title", null, locale));
        model.addAttribute("content", messageSource.getMessage("info.privacy.content", null, locale));
        model.addAttribute("detail1", messageSource.getMessage("info.privacy.detail1", null, locale));
        model.addAttribute("detail2", messageSource.getMessage("info.privacy.detail2", null, locale));
        model.addAttribute("detail3", messageSource.getMessage("info.privacy.detail3", null, locale));
        model.addAttribute("detail4", messageSource.getMessage("info.privacy.detail4", null, locale));
        model.addAttribute("detail5", messageSource.getMessage("info.privacy.detail5", null, locale));
        model.addAttribute("detail6", messageSource.getMessage("info.privacy.detail6", null, locale));
        model.addAttribute("detail7", messageSource.getMessage("info.privacy.detail7", null, locale));
        model.addAttribute("detail8", messageSource.getMessage("info.privacy.detail8", null, locale));

        return "pages/info/privacy";
    }
}
