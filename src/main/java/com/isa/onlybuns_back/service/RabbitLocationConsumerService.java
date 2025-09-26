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
        try {
            // probaj da vidiš da li je servis dostupan
            restTemplate.getForEntity("http://localhost:8083/actuator/health", String.class);
        } catch (Exception e) {
            System.out.println("Rabbit side-app not active, skipping polling.");
            return; // ovde prekidamo, scheduler se ne pokreće
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::pollForLocations, 0, 30, TimeUnit.MINUTES);
    }


    private void pollForLocations() {
        try {
            ResponseEntity<RabbitLocationMessage> response = restTemplate.getForEntity("http://localhost:8083/queue/receive/rabbit-care-queue", RabbitLocationMessage.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                RabbitLocationMessage locationDto = response.getBody();
                System.out.printf("PRIMLJENO (Direct): Lokacija '%s' (%.4f, %.4f) primljena.\n", locationDto.getName(), locationDto.getLatitude(), locationDto.getLongitude());

                RabbitCareLocation locationEntity = new RabbitCareLocation();
                locationEntity.setName(locationDto.getName());
                locationEntity.setLatitude(locationDto.getLatitude());
                locationEntity.setLongitude(locationDto.getLongitude());
                locationRepository.save(locationEntity);
                System.out.println("Lokacija uspešno sačuvana u bazi.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<RabbitCareLocation> getAllLocations() {
        return locationRepository.findAll();
    }
}