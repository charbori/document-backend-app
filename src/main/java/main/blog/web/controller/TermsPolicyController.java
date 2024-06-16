package main.blog.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Slf4j
@Controller
public class TermsPolicyController {
    @Autowired
    MessageSource messageSource;

    @GetMapping("/info/terms")
    public String getMethodName(Locale locale, Model model) {

        log.info("locale info={}", locale);
        if (!locale.getLanguage().equals("ko")) {
            locale = new Locale("en");
        }

        model.addAttribute("title", messageSource.getMessage("info.terms.title", null, locale));
        model.addAttribute("purpose", messageSource.getMessage("info.terms.purpose", null, locale));
        model.addAttribute("definition", messageSource.getMessage("info.terms.definition", null, locale));
        model.addAttribute("terms3", messageSource.getMessage("info.terms.terms3", null, locale));
        model.addAttribute("terms4", messageSource.getMessage("info.terms.terms4", null, locale));
        model.addAttribute("terms5", messageSource.getMessage("info.terms.terms5", null, locale));
        model.addAttribute("terms6", messageSource.getMessage("info.terms.terms6", null, locale));
        model.addAttribute("terms7", messageSource.getMessage("info.terms.terms7", null, locale));
        model.addAttribute("terms8", messageSource.getMessage("info.terms.terms8", null, locale));
        model.addAttribute("terms9", messageSource.getMessage("info.terms.terms9", null, locale));
        model.addAttribute("terms10", messageSource.getMessage("info.terms.terms10", null, locale));

        return "pages/info/terms";
    }
}
