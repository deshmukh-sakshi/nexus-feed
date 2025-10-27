package com.nexus.feed.backend.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Nexus Feed Backend API is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}