package com.example.demo.Narratives;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/narratives")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://cs-4485-team-5.pages.dev"})
public class NarrativesController {

    private final NarrativesService narrativesService;

    public NarrativesController(NarrativesService narrativesService) {
        this.narrativesService = narrativesService;
    }

    @GetMapping("/page-data")
    public ResponseEntity<NarrativesPageResponse> getPageData(
            @RequestParam(defaultValue = "US") String regionCode,
            @RequestParam(defaultValue = "12") int maxResults
    ) {
        return ResponseEntity.ok(narrativesService.buildPageData(regionCode, maxResults));
    }

    @GetMapping("/claims-by-genre")
    public ResponseEntity<GenreClaimsResponse> getClaimsByGenre(
            @RequestParam(required = false) String genre
    ) {
        return ResponseEntity.ok(narrativesService.buildClaimsByGenre(genre));
    }
}