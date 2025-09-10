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

    /**
     * Šalje oglasnu poruku u Fanout Exchange kada administrator odobri objavu.
     *
     * @param post Objava koja je odobrena za reklamiranje.
     */
    public void sendAdMessage(Post post) {
        // Kreiranje DTO objekta (Data Transfer Object)
        AdMessage adMessage = new AdMessage();
        adMessage.setDescription(post.getDescription());
        adMessage.setTimestamp(new Date()); // Koristimo trenutno vreme
        adMessage.setUsername(post.getUser().getUsername()); // Pretpostavka da Post ima referencu na autora

        // URL brokera za fanout komunikaciju
        String url = "http://localhost:8083/queue/send/fanout/" + FANOUT_EXCHANGE_NAME;

        try {
            // Slanje poruke na broker
            restTemplate.postForLocation(url, adMessage);
            System.out.println("Oglasna poruka uspešno poslata u Fanout Exchange: " + FANOUT_EXCHANGE_NAME);
        } catch (Exception e) {
            System.err.println("Greška pri slanju oglasne poruke: " + e.getMessage());
        }
    }
}
