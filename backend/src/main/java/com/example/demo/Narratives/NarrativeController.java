package com.example.demo.Narratives;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/narratives")
public class NarrativeController {
    private final NarrativeService narrativeService;

    public NarrativeController(NarrativeService narrativeService) {
        this.narrativeService = narrativeService;
    }

    @GetMapping
    public ResponseEntity<List<Narrative>> getNarratives() {
        return ResponseEntity.ok(narrativeService.getTopNarratives());
    }
}
