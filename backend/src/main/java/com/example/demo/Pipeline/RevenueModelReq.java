package com.example.demo.Pipeline;

import java.time.LocalDate;

public class RevenueModelReq {
    private String genreName;
    private double baseCpm;
    private double trendMultiplier;
    private Double confidence;
    private String modelVersion;
    private LocalDate effectiveDate;

    public String getGenreName() { return genreName; }
    public double getBaseCpm() { return baseCpm; }
    public double getTrendMultiplier() { return trendMultiplier; }
    public Double getConfidence() { return confidence; }
    public String getModelVersion() { return modelVersion; }
    public LocalDate getEffectiveDate() { return effectiveDate; }

    public void setGenreName(String genreName) { this.genreName = genreName; }
    public void setBaseCpm(double baseCpm) { this.baseCpm = baseCpm; }
    public void setTrendMultiplier(double trendMultiplier) { this.trendMultiplier = trendMultiplier; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}