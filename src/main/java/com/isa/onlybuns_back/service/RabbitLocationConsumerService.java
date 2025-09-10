package com.isa.onlybuns_back.service;

import com.isa.onlybuns_back.model.RabbitCareLocation;
import com.isa.onlybuns_back.model.RabbitLocationMessage;
import com.isa.onlybuns_back.repository.RabbitCareLocationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitLocationConsumerService {

    private final RabbitCareLocationRepository locationRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public RabbitLocationConsumerService(RabbitCareLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @PostConstruct
    public void startListening() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::pollForLocations, 0, 5, TimeUnit.SECONDS);
    }

    private void pollForLocations() {
        try {
            ResponseEntity<RabbitLocationMessage> response = restTemplate.getForEntity("http://localhost:8083/queue/receive/rabbit-care-queue", RabbitLocationMessage.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                RabbitLocationMessage locationDto = response.getBody();
                System.out.printf("PRIMLJENO (Direct): Lokacija '%s' (%.4f, %.4f) primljena.\n", locationDto.getName(), locationDto.getLatitude(), locationDto.getLongitude());

                // Čuvanje poruke u bazu podataka
                RabbitCareLocation locationEntity = new RabbitCareLocation();
                locationEntity.setName(locationDto.getName());
                locationEntity.setLatitude(locationDto.getLatitude());
                locationEntity.setLongitude(locationDto.getLongitude());
                locationRepository.save(locationEntity);
                System.out.println("Lokacija uspešno sačuvana u bazi.");
            }
        } catch (Exception e) {
            // Ignoriše greške ako je red prazan ili servis nedostupan
        }
    }

    public List<RabbitCareLocation> getAllLocations() {
        return locationRepository.findAll();
    }
}