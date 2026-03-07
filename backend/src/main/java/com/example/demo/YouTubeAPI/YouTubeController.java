package com.example.demo.YouTubeAPI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;

    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }
    
    @GetMapping("/gaming/popular")
    public ResponseEntity<List<VideoDTO>> getPopularGamingVideos(
            @RequestParam(defaultValue = "50") int maxResults,
            @RequestParam(defaultValue = "US") String regionCode) {
        List<VideoDTO> vidoes = youTubeService.getPopularGamingVideoDetails(maxResults, regionCode);
        return ResponseEntity.ok(vidoes);
    }

}
