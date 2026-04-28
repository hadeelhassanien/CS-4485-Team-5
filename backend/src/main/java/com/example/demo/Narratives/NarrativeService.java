package com.example.demo.Narratives;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NarrativeService {

    private final NarrativeClient narrativeClient;

    public NarrativeService(NarrativeClient narrativeClient) {
        this.narrativeClient = narrativeClient;
    }

    public List<Narrative> getTopNarratives() {
        List<Narrative> all = narrativeClient.fetchNarratives();

        if (all == null || all.isEmpty()) {
            System.out.println("Falling back to local JSON");
            all = loadNarratives();
        }

        return all;
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

            NarrativeResponse response = mapper.readValue(is, NarrativeResponse.class);
            if (response != null && response.getResults() != null) {
                narratives.addAll(response.getResults());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return narratives;
    }
}