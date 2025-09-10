package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.AdMessage;
import com.isa.onlybuns_back.model.Post; // Pretpostavka da postoji model za objave
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Service
public class AdService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String FANOUT_EXCHANGE_NAME = "ad-broadcast-exchange";

    public void sendAdMessage(Post post) {
        AdMessage adMessage = new AdMessage();
        adMessage.setDescription(post.getDescription());
        adMessage.setTimestamp(new Date());
        adMessage.setUsername(post.getUser().getUsername());

        // URL brokera za fanout komunikaciju
        String url = "http://localhost:8083/queue/send/fanout/" + FANOUT_EXCHANGE_NAME;

        try {
            // slanje poruke na broker
            restTemplate.postForLocation(url, adMessage);
            System.out.println("Ad message successfully sent to Fanout Exchange: "  + FANOUT_EXCHANGE_NAME);
        } catch (Exception e) {
            System.err.println("Error while sending ad message: " + e.getMessage());
        }
    }
}
