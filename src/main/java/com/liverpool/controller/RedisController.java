package com.liverpool.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RedisController {

    @GetMapping("/hello")
    public String hello(){
        return "Hello Redis";
    }
}
