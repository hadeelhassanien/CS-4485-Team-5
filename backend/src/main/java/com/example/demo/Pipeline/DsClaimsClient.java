package com.example.demo.Pipeline;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Service
public class DsClaimsClient {

    private static final String DS_CLAIMS_URL = "https://epidermal-scrooge-unbuckled.ngrok-free.dev/";
    private final RestTemplate restTemplate;

    public DsClaimsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<RawDsVideoClaimsReq> fetchClaims() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("Calling DS claims API: " + DS_CLAIMS_URL);

            ResponseEntity<List<RawDsVideoClaimsReq>> response = restTemplate.exchange(
                DS_CLAIMS_URL,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<RawDsVideoClaimsReq>>() {}
            );

            System.out.println("DS claims response status: " + response.getStatusCode());

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                System.out.println("Claims fetched from DS API: " + response.getBody().size());
                return response.getBody();
            }

            return List.of();

        } catch (Exception e) {
            System.out.println("DS claims API unavailable: " + e.getMessage());
            return List.of();
        }
    }
}