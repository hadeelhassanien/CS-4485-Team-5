package com.example.demo.YouTubeAPI;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoDTO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    String videoId;
    String title;
    String channelTitle;
    String publishedAt;
    String description;
    long viewCount;
    long likeCount;
    long commentCount;
    String videoUrl;
    String topicCategories;
}
