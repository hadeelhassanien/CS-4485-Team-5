package com.example.demo.Pipeline;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_model")
public class RevenueModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "genre_name", nullable = false)
    private String genreName;

    @Column(name = "base_cpm", nullable = false)
    private double baseCpm;

    @Column(name = "trend_multiplier", nullable = false)
    private double trendMultiplier;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RevenueModel() {}

    public Long getId() { return id; }
    public String getGenreName() { return genreName; }
    public double getBaseCpm() { return baseCpm; }
    public double getTrendMultiplier() { return trendMultiplier; }
    public Double getConfidence() { return confidence; }
    public String getModelVersion() { return modelVersion; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setGenreName(String genreName) { this.genreName = genreName; }
    public void setBaseCpm(double baseCpm) { this.baseCpm = baseCpm; }
    public void setTrendMultiplier(double trendMultiplier) { this.trendMultiplier = trendMultiplier; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}