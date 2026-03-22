package com.example.demo.Trends;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

//import org.hibernate.mapping.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.YouTubeAPI.VideoDTO;
import com.example.demo.YouTubeAPI.YouTubeService;

@Service
public class GamingGenreService {
    
    private final GamingGenreRepository genreRepository;
    private final YouTubeService youTubeService;

    public GamingGenreService(GamingGenreRepository genreRepository, YouTubeService youTubeService) {
        this.genreRepository = genreRepository;
        this.youTubeService = youTubeService;
    }

    private static final Map<String, List<String>> GENRE_KEYWORDS = new LinkedHashMap<>();

    static {
        GENRE_KEYWORDS.put("Battle Royale",   List.of("fortnite", "warzone", "pubg", "apex", "battle royale", "free fire", "squad", "br"));
        GENRE_KEYWORDS.put("Survival Craft",  List.of("minecraft", "survival", "rust", "valheim", "terraria", "craft"));
        GENRE_KEYWORDS.put("Sports Sim",      List.of("fifa", "nba 2k", "madden", "mlb", "nhl", "fc 25", "sports"));
        GENRE_KEYWORDS.put("Horror",          List.of("horror", "scary", "five nights", "fnaf", "resident evil", "outlast", "fear"));
        GENRE_KEYWORDS.put("Shooter",         List.of("valorant", "cod", "call of duty", "overwatch", "counter strike", "cs2", "fps", "shooter"));
        GENRE_KEYWORDS.put("Action",          List.of("gta", "action", "elden ring", "dark souls", "hack", "slash", "brawl"));
        GENRE_KEYWORDS.put("Racing",          List.of("racing", "mario kart", "need for speed", "forza", "f1", "drift", "nascar", "trackmania", "gran turismo"));
        GENRE_KEYWORDS.put("Party / Casual",  List.of("among us", "fall guys", "party", "casual", "roblox", "gang beasts"));
        GENRE_KEYWORDS.put("Simulation",      List.of("simulation", "sims", "stardew", "farming", "city", "tycoon", "simulator", "building", "zoo", "planet"));
        GENRE_KEYWORDS.put("Puzzle",          List.of("puzzle", "tetris", "portal", "escape", "brain", "wordle", "among us"));
    }

    private String detectGenre(String title, String description) {
    String text = (title + " " + description).toLowerCase();
    for (Map.Entry<String, List<String>> entry : GENRE_KEYWORDS.entrySet()) {
        for (String keyword : entry.getValue()) {
            if (text.contains(keyword)) {
                return entry.getKey();
            }
        }
    }
    return null; // no genre matched
}


    public void updateGenreStats() {
        List<VideoDTO> videos = youTubeService.getPopularGamingVideoDetails(50, "US");
        LocalDate today = LocalDate.now();
        LocalDate previousDate = genreRepository.findPreviousSnapshotDate(today);

        Map<String, Long> genreViews    = new HashMap<>();
        Map<String, Long> genreLikes    = new HashMap<>();
        Map<String, Long> genreComments = new HashMap<>();

        // Initialize all genres to 0
        for (String genre : GENRE_KEYWORDS.keySet()) {
            genreViews.put(genre, 0L);
            genreLikes.put(genre, 0L);
            genreComments.put(genre, 0L);
        }

        // Map each video to a genre using title/description keywords
        for (VideoDTO video : videos) {
            String genre = detectGenre(video.getTitle(), video.getDescription());
            if (genre == null) continue;

            genreViews.merge(genre, video.getViewCount(), Long::sum);
            genreLikes.merge(genre, video.getLikeCount(), Long::sum);
            genreComments.merge(genre, video.getCommentCount(), Long::sum);
        }

        // Save or update each genre in DB
        for (String genre : GENRE_KEYWORDS.keySet()) {
            long todayViews = genreViews.getOrDefault(genre, 0L);

            double changePercent = 0.0;
            if (previousDate != null) {
                GamingGenre previous = genreRepository.findByNameAndSnapshotDate(genre, previousDate);
                if (previous != null && previous.getViews() > 0) {
                    changePercent = (double)(todayViews - previous.getViews()) / previous.getViews();
                    changePercent = Math.round(changePercent * 100.0) / 100.0;
                }
            }

            GamingGenre existing = genreRepository.findByNameAndSnapshotDate(genre, today);
            if (existing != null) {
                existing.setViews(todayViews);
                existing.setLikes(genreLikes.getOrDefault(genre, 0L));
                existing.setComments(genreComments.getOrDefault(genre, 0L));
                existing.setChangePercent(changePercent);
                existing.setLastUpdated(LocalDateTime.now());
                genreRepository.save(existing);
            } else {
                GamingGenre newGenre = new GamingGenre();
                newGenre.setName(genre);
                newGenre.setViews(todayViews);
                newGenre.setLikes(genreLikes.getOrDefault(genre, 0L));
                newGenre.setComments(genreComments.getOrDefault(genre, 0L));
                newGenre.setChangePercent(changePercent);
                newGenre.setSnapshotDate(today);
                newGenre.setLastUpdated(LocalDateTime.now());
                genreRepository.save(newGenre);
            }
        }
    }

    public List<GamingGenre> getAllGenres() {
        LocalDate today = LocalDate.now();
        List<GamingGenre> todaySnapshot = genreRepository.findBySnapshotDate(today);

        if (todaySnapshot.isEmpty()) {
            LocalDate previousDate = genreRepository.findPreviousSnapshotDate(today.plusDays(1));
            if (previousDate != null) {
                return genreRepository.findBySnapshotDate(previousDate);
            }
        }

        return todaySnapshot;
    }

    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void scheduledUpdate() {
        updateGenreStats();
    }
}
