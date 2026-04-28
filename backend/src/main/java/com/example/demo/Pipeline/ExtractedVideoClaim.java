package com.example.demo.Pipeline;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "extracted_video_claim")
public class ExtractedVideoClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private String videoId;

    @Column(name = "source_title")
    private String sourceTitle;

    @Column(name = "claim_text", nullable = false, length = 2000)
    private String claimText;

    @Column(name = "claim_category")
    private String claimCategory;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "genre_name")
    private String genreName;

    public String getGenreName() {
        return genreName;
    }
    
    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public ExtractedVideoClaim() {}

    public Long getId() {
        return id;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getSourceTitle() {
        return sourceTitle;
    }

    public String getClaimText() {
        return claimText;
    }

    public String getClaimCategory() {
        return claimCategory;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }

    public void setClaimText(String claimText) {
        this.claimText = claimText;
    }

    public void setClaimCategory(String claimCategory) {
        this.claimCategory = claimCategory;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
