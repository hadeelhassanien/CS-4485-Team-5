package com.example.demo.Narratives;
import java.util.List;

public class GenreClaimsResponse {
    private List<String> genres;
    private String selectedGenre;
    private List<String> narratives;

    public GenreClaimsResponse() {}

    public GenreClaimsResponse(List<String> genres, String selectedGenre, List<String> narratives) {
        this.genres = genres;
        this.selectedGenre = selectedGenre;
        this.narratives = narratives;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getSelectedGenre() {
        return selectedGenre;
    }

    public List<String> getNarratives() {
        return narratives;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public void setSelectedGenre(String selectedGenre) {
        this.selectedGenre = selectedGenre;
    }

    public void setNarratives(List<String> narratives) {
        this.narratives = narratives;
    }
}