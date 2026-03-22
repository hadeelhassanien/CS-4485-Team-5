package com.example.demo.YouTubeAPI;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoDTO {
    
    @Id
    String videoId;

    @Column(name = "title")
    String title;

    @Column(name = "channel_title") 
    String channelTitle;

    @Column(name = "published_at")
    String publishedAt;

    @Column(name = "description", length = 5000)
    String description;

    @Column(name = "view_count")
    long viewCount;

    @Column(name = "like_count")
    long likeCount;

    @Column(name = "comment_count")
    long commentCount;

    @Column(name = "video_url")
    String videoUrl;

    @Column(name = "topic_categories", length = 1000)
    String topicCategories;
}
