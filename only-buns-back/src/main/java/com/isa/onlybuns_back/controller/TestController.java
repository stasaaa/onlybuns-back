package com.isa.onlybuns_back.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// Provera da li authentication radi
@RestController
@RequestMapping(path = "test")
public class TestController {
    @GetMapping
    public String test() {
        return "test";
    }
}
