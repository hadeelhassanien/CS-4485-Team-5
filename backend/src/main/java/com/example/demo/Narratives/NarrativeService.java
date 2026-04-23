package com.example.demo.Narratives;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NarrativeService {
    private static final int MAX_TRENDS = 2;
    private static final int MAX_RECOMMENDATIONS = 3;

    private final NarrativeClient narrativeClient;

    public NarrativeService(NarrativeClient narrativeClient) {
        this.narrativeClient = narrativeClient;
    }

    public List<Narrative> getTopNarratives() {
        // Try FastAPI first
        List<Narrative> all = narrativeClient.fetchNarratives();

        // Fall back to local JSON if FastAPI is down
        if (all == null || all.isEmpty()) {
            System.out.println("Falling back to local JSON");
            all = loadNarratives();
        }

        List<Narrative> trends = all.stream()
                .filter(n -> "trend".equalsIgnoreCase(n.getType()))
                .collect(Collectors.toList());

        List<Narrative> recommendations = all.stream()
                .filter(n -> "recommendation".equalsIgnoreCase(n.getType()))
                .collect(Collectors.toList());

        Collections.shuffle(trends);
        Collections.shuffle(recommendations);

        List<Narrative> result = new ArrayList<>();
        result.addAll(trends.stream().limit(MAX_TRENDS).toList());
        result.addAll(recommendations.stream().limit(MAX_RECOMMENDATIONS).toList());

        Collections.shuffle(result);
        return result;
    }

    private List<Narrative> loadNarratives() {
        List<Narrative> narratives = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            InputStream is = getClass().getResourceAsStream("/final_narratives.json");
            if (is == null) {
                System.out.println("Local JSON not found");
                return narratives;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    narratives.add(mapper.readValue(line, Narrative.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return narratives;
    }
}
