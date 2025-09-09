package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.model.RabbitCareLocation;
import com.isa.onlybuns_back.service.RabbitLocationConsumerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rabbit-care")
@AllArgsConstructor
public class RabbitCareLocationController {
    private RabbitLocationConsumerService service;

    @GetMapping("")
    public ResponseEntity<List<RabbitCareLocation>> getAllLocations() {
        return ResponseEntity.ok().body(service.getAllLocations());
    }
}
