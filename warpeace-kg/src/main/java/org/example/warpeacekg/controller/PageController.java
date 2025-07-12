package org.example.warpeacekg.controller;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/search")
    public String searchPage() {
        return "searchpage"; // 会自动查找 templates/searchpage.html
    }

    @GetMapping("/graph")
    public String graphPage() {
        return "graphpage";
    }

    @GetMapping("/")
    public String indexPage() {
        return "indexpage";
    }
    @GetMapping("/answer")
    public String KGQAPage() {
        return "KGQA"; // 会自动查找 templates/searchpage.html
    }
}
