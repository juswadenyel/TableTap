package edu.cit.dingding.tabletap.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// This controller reports whether the TableTap application is running.
@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "TableTap is running!";
    }
}