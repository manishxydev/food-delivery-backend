package com.fooddelivery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HomeController {
    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "service", "Food Delivery API",
                "status", "running",
                "github", "https://github.com/manishxydev/food-delivery-backend"
        );
    }
}