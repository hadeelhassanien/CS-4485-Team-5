package com.example.demo.Pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RawDsVideoClaimsReq {
    @JsonProperty("video_id")
    private String videoId;

    @JsonProperty("genre")
    private String genre;

    @JsonProperty("genre_name")
    private String genreName;

    private List<String> claims;

    public String getVideoId() {
        return videoId;
    }

    public String getGenre() {
        return genre;
    }

    public String getGenreName() {
        return genreName;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }

    public String resolvedGenreName() {
        if (genreName != null && !genreName.isBlank()) return genreName.trim();
        if (genre != null && !genre.isBlank()) return genre.trim();
        return "General";
    }
}