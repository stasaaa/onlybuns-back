package com.isa.onlybuns_back.controller;

import com.isa.onlybuns_back.model.Post;
import com.isa.onlybuns_back.repository.PostRepository; // Pretpostavka da postoji repozitorijum za objave
import com.isa.onlybuns_back.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ad")
public class AdController {

    private final PostRepository postRepository;
    private final AdService adService;

    @Autowired
    public AdController(PostRepository postRepository, AdService adService) {
        this.postRepository = postRepository;
        this.adService = adService;
    }

    @PostMapping("/approve-ad/{postId}")
    public ResponseEntity<String> approvePostForAd(@PathVariable Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        adService.sendAdMessage(post);
        return ResponseEntity.ok("Objava #" + postId + " odobrena za reklamiranje.");
    }
}