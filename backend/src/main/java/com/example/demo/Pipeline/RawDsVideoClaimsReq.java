package com.example.demo.Pipeline;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawDsVideoClaimsReq {
    @JsonAlias({"video_id", "videoId"})
    private String videoId;

    @JsonAlias({"title", "video_title"})
    private String title;

    @JsonAlias({"genre", "genre_name"})
    private String genre;

    private List<String> claims;

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public List<String> getClaims() {
        return claims;
    }
}