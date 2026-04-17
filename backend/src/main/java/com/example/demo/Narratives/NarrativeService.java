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

    public List<Narrative> getTopNarratives() {
        List<Narrative> all = loadNarratives();

        // Separate into two lists
        List<Narrative> trends = all.stream()
                .filter(n -> "trend".equalsIgnoreCase(n.getType()))
                .collect(Collectors.toList());

        List<Narrative> recommendations = all.stream()
                .filter(n -> "recommendation".equalsIgnoreCase(n.getType()))
                .collect(Collectors.toList());

        // Shuffle both lists randomly each time
        Collections.shuffle(trends);
        Collections.shuffle(recommendations);

        // Take top N after shuffle
        List<Narrative> result = new ArrayList<>();
        result.addAll(trends.stream().limit(MAX_TRENDS).toList());
        result.addAll(recommendations.stream().limit(MAX_RECOMMENDATIONS).toList());

        // Shuffle the final result so trends and recommendations are mixed
        Collections.shuffle(result);

        return result;
    }

    private List<Narrative> loadNarratives() {
        List<Narrative> narratives = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            InputStream is = getClass().getResourceAsStream("/final_narratives.json");

            if (is == null) {
                System.out.println("FILE NOT FOUND");
                return narratives;
            }

            System.out.println("FILE FOUND");

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Narrative narrative = mapper.readValue(line, Narrative.class);
                    narratives.add(narrative);
                }
            }

            System.out.println("Total narratives loaded: " + narratives.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return narratives;
    }
}
