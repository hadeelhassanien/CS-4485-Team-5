package com.example.demo.Pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RawDsVideoClaimsReq {
    @JsonProperty("video_id")
    private String videoId;
    private List<String> claims;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(List<String> claims) {
        this.claims = claims;
    }
}