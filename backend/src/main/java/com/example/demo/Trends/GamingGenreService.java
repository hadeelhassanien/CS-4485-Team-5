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

    private static final Map<String, String> TOPIC_TO_GENRE = new LinkedHashMap<>(Map.of(
        "Battle_royal_game",   "Battle Royale",
        "Survival_game",       "Survival Craft",
        "Sports_video_game",   "Sports Sim",
        "Horror_video_game",   "Horror",
        "shooter_game",        "Shooter",
        "Action_game",         "Action",
        "Racing_video_game",   "Racing",
        "Casual_game",         "Party / Casual",
        "Simulation_video_game", "Simulation",
        "Puzzle_video_game",   "Puzzle"
    ));

    public void updateGenreStats() {
        List<VideoDTO> videos = youTubeService.getPopularGamingVideoDetails(50, "US");
        LocalDate today = LocalDate.now();
        LocalDate previousDate = genreRepository.findPreviousSnapshotDate(today);

        // Aggregate views, likes, comments per genre
        Map<String, Long> genreViews    = new HashMap<>();
        Map<String, Long> genreLikes    = new HashMap<>();
        Map<String, Long> genreComments = new HashMap<>();

        for (String genre : TOPIC_TO_GENRE.values()) {
            genreViews.put(genre, 0L);
            genreLikes.put(genre, 0L);
            genreComments.put(genre, 0L);
        }

        for (VideoDTO video : videos) {
            String topics = video.getTopicCategories();
            if (topics == null || topics.isEmpty()) continue;

            for (Map.Entry<String, String> entry : TOPIC_TO_GENRE.entrySet()) {
                if (topics.contains(entry.getKey())) {
                    String genre = entry.getValue();
                    genreViews.merge(genre, video.getViewCount(), Long::sum);
                    genreLikes.merge(genre, video.getLikeCount(), Long::sum);
                    genreComments.merge(genre, video.getCommentCount(), Long::sum);
                }
            }
        }

        for (String genre : TOPIC_TO_GENRE.values()) {
            long todayViews = genreViews.getOrDefault(genre, 0L);

            // Calculate real changePercent vs previous snapshot
            double changePercent = 0.0;
            if (previousDate != null) {
                GamingGenre previous = genreRepository.findByNameAndSnapshotDate(genre, previousDate);
                if (previous != null && previous.getViews() > 0) {
                    changePercent = (double)(todayViews - previous.getViews()) / previous.getViews();
                    // Round to 2 decimal places e.g. 0.48
                    changePercent = Math.round(changePercent * 100.0) / 100.0;
                }
            }

            // Save or update today's snapshot
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
