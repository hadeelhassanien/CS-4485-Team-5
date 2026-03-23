package com.example.demo.Trends;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/genres")
public class GamingGenreController {
    
    private final GamingGenreService genreService;

    public GamingGenreController(GamingGenreService genreService) {
        this.genreService = genreService;
    }

    //Get all genres from DB
    @GetMapping
    public ResponseEntity<List<GamingGenre>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    // Manually trigger a refresh from YouTube
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshGenres() {
        genreService.updateGenreStats();
        return ResponseEntity.ok("Genre stats updated successfully");
    }

    @GetMapping("/predicted")
    public ResponseEntity<List<Map<String, Object>>> getPredictedTrends() {
        return ResponseEntity.ok(genreService.getPredictedTrends());
    }

    @GetMapping("/recommendation")
    public ResponseEntity<Map<String, Object>> getRecommendation() {
        return ResponseEntity.ok(genreService.getGeneralRecommendation());
    }
}
