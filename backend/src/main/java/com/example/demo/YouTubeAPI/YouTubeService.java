package com.example.demo.YouTubeAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class YouTubeService {
    
    @Value("${youtube.api.key}")
    private String apiKey;

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3";

    //Gaming category ID on YouTube API
    private static final String GAMING_CATEGORY_ID = "20";

    private final RestTemplate restTemplate;

    public YouTubeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    
    /* 
     * Fetches the most popular videos under the gaming category using the YouTube Data API v3.

     * @param maxResults Number of results to return (max 50)
     * @param regionCode country code (e.g., "US", "IN").
     * @return Map containing the raw API response
    */
    public Map<String, Object> getMostPopularGamingVideos(int maxResults, String regionCode){
        String url = UriComponentsBuilder
                .fromHttpUrl(YOUTUBE_API_BASE_URL + "/videos")
                .queryParam("part", "snippet,contentDetails,statistics")
                .queryParam("chart", "mostPopular")
                .queryParam("videoCategoryId", GAMING_CATEGORY_ID)
                .queryParam("maxResults", Math.min(maxResults, 50)) //API max is 50
                .queryParam("regionCode", regionCode)
                .queryParam("key", apiKey)
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    
    /*
     * Fetches popular gaming videos and maps them to a list of VideoDTO objects.

     * @param maxResults Number of results to return (max 50)
     * @param regionCode country code (e.g., "US", "IN").
     * @return List of VideoDTO with key video details
     */
    public List<VideoDTO> getPopularGamingVideoDetails(int maxResults, String regionCode) {
        Map<String, Object> response = getMostPopularGamingVideos(maxResults, regionCode);
        
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        return items.stream()
            .filter(item -> {
                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                //only include videos in the Gaming category (ID: 20)
                return "20".equals(snippet.get("categoryId"));
            })
            .map(item -> {
                String videoId = (String) item.get("id");
                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");

                return new VideoDTO(
                    videoId,
                    (String) snippet.get("title"),
                    (String) snippet.get("channelTitle"),
                    (String) snippet.get("publishedAt"),
                    (String) snippet.get("description"),
                    statistics.get("commentCount") != null ? Long.parseLong((String) statistics.get("commentCount")) : 0L,
                    statistics.get("viewCount") != null ? Long.parseLong((String) statistics.get("viewCount")) : 0L,
                    statistics.get("likeCount") != null ? Long.parseLong((String) statistics.get("likeCount")) : 0L,
                    (String) snippet.get("topicCategories"),
                    "https://www.youtube.com/watch?v=" + videoId
                );
            }).toList();
    }

}
