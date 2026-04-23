package com.example.demo.Narratives;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NarrativeClient {
    private static final String FASTAPI_URL = "https://cyclist-frustrate-enlighten.ngrok-free.dev/";

    private final RestTemplate restTemplate;

    public NarrativeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Narrative> fetchNarratives() {
        try {
            ResponseEntity<List<Narrative>> response = restTemplate.exchange(
                FASTAPI_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Narrative>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            System.out.println("FastAPI unavailable: " + e.getMessage());
            return List.of();
        }
    }
}
