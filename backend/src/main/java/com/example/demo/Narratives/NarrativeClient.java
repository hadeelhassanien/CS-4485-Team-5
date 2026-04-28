package com.example.demo.Narratives;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class NarrativeClient {

    private static final String FASTAPI_URL = "https://prayer-diffs-worldwide-ever.trycloudflare.com/";

    private final RestTemplate restTemplate;

    public NarrativeClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Narrative> fetchNarratives() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("Calling FastAPI: " + FASTAPI_URL);

            ResponseEntity<NarrativeResponse> response = restTemplate.exchange(
                FASTAPI_URL,
                HttpMethod.GET,
                entity,
                NarrativeResponse.class
            );

            System.out.println("FastAPI response status: " + response.getStatusCode());
            System.out.println("FastAPI response body: " + response.getBody());

            if (response.getBody() != null && response.getBody().getResults() != null) {
                System.out.println("Narratives fetched from FastAPI: " + response.getBody().getResults().size());
                return response.getBody().getResults();
            }

            return List.of();

        } catch (Exception e) {
            System.out.println("FastAPI unavailable: " + e.getMessage());
            return List.of();
        }
    }
}