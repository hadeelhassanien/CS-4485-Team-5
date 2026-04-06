package com.example.demo.Trends;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    public ComparePerformanceDTO compare(String fromGenre, String toGenre) {
        LocalDate today = LocalDate.now();

        //get the latest snapshot for each genre
        GamingGenre before = getLatestSnapshot(fromGenre, today);
        GamingGenre after  = getLatestSnapshot(toGenre,   today);

        if (before == null) throw new IllegalArgumentException("Genre not found: " + fromGenre);
        if (after  == null) throw new IllegalArgumentException("Genre not found: " + toGenre);

        //get prev snapshots for 30 day avg
        LocalDate prevDate = genreRepository.findPreviousSnapshotDate(today);
        GamingGenre beforePrev = prevDate != null ? genreRepository.findByNameAndSnapshotDate(fromGenre, prevDate) : null;
        GamingGenre afterPrev  = prevDate != null ? genreRepository.findByNameAndSnapshotDate(toGenre,   prevDate) : null;

        long avgViewsBefore = average(before.getViews(), beforePrev != null ? beforePrev.getViews() : before.getViews());
        long avgViewsAfter  = average(after.getViews(),  afterPrev  != null ? afterPrev.getViews()  : after.getViews());

        double likeRatioBefore = likeRatio(before.getLikes(), before.getViews());
        double likeRatioAfter  = likeRatio(after.getLikes(),  after.getViews());
        double revenueUplift   = pctChange(before.getViews(), after.getViews());

        return new ComparePerformanceDTO(
                fromGenre,
                toGenre,
                before.getViews(),
                before.getChangePercent(),
                after.getViews(),
                after.getChangePercent(),
                avgViewsBefore,
                avgViewsAfter,
                likeRatioBefore,
                likeRatioAfter,
                before.getComments(),
                after.getComments(),
                revenueUplift
        );
    }

    private GamingGenre getLatestSnapshot(String name, LocalDate today) {
        GamingGenre g = genreRepository.findByNameAndSnapshotDate(name, today);
        if (g != null) return g;
        LocalDate prevDate = genreRepository.findPreviousSnapshotDate(today.plusDays(1));
        return prevDate != null ? genreRepository.findByNameAndSnapshotDate(name, prevDate) : null;
    }

    private long average(long a, long b) { return (a + b) / 2; }

    private double likeRatio(long likes, long views) {
        if (views == 0) return 0.0;
        return Math.round((likes * 100.0 / views) * 10.0) / 10.0;
    }

    private double pctChange(long from, long to) {
        if (from == 0) return 0.0;
        return Math.round(((to - from) * 100.0 / from) * 10.0) / 10.0;
    }

    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void scheduledUpdate() {
        updateGenreStats();
    }

    // Get top 5 predicted trends based on today's views and % change
    public List<Map<String, Object>> getPredictedTrends() {
        List<GamingGenre> latestGenres = getAllGenres();

        return latestGenres.stream()
                .map(genre -> {
                    // predictedViews = todayViews * (1 + changePercent)
                    long predictedViews = (long) (genre.getViews() * (1 + genre.getChangePercent()));

                    Map<String, Object> result = new HashMap<>();
                    result.put("name", genre.getName());
                    result.put("currentViews", genre.getViews());
                    result.put("predictedViews", predictedViews);
                    result.put("predictedGrowthPercent", Math.round(genre.getChangePercent() * 10000.0) / 100.0); //round to 1 decimal
                    //result.put("predictedGrowthPercent", genre.getChangePercent() * 100);
                    return result;
                })
                // Sort by predicted growth % descending
                .sorted((a, b) -> Double.compare(
                    (double) b.get("predictedGrowthPercent"),
                    (double) a.get("predictedGrowthPercent")
                ))
                .limit(5)
                .toList();
    }

    // Provide a general recommendation based on the top predicted trend
    public Map<String, Object> getGeneralRecommendation() {
        List<Map<String, Object>> predicted = getPredictedTrends();

        if (predicted.isEmpty()) {
            return Map.of(
                "nextMonthPeak", "No data available",
                "recommendedGenre", "No data available",
                "predictedGrowth", 0,
                "message", "Not enough data to make a recommendation yet."
            );
        }

        // Top genre by predicted growth
        Map<String, Object> topGenre = predicted.get(0);
        String recommendedGenre = (String) topGenre.get("name");
        double predictedGrowth = (double) topGenre.get("predictedGrowthPercent");

        // Next month peak — top 2 genres
        String secondGenre = predicted.size() > 1 ? (String) predicted.get(1).get("name") : "";
        String nextMonthPeak = recommendedGenre + (secondGenre.isEmpty() ? "" : " / " + secondGenre);

        String message = String.format(
            "%s is predicted to peak next month with %.0f%% growth — now is a good time to create content in this genre.",
            recommendedGenre, predictedGrowth
        );

        Map<String, Object> result = new HashMap<>();
        result.put("nextMonthPeak", nextMonthPeak);
        result.put("recommendedGenre", recommendedGenre);
        result.put("predictedGrowth", Math.round(predictedGrowth));
        result.put("message", message);
        return result;
    }

    /*
    --------------------------------------------
    Claims page - Breakdown: Why Trend Works
    ---------------------------------------------
    */

    // Industry average CPM per genre (USD)
    private static final Map<String, Double> CPM_BY_GENRE = Map.of(
        "Battle Royale",  4.26,
        "Survival Craft", 2.80,
        "Sports Sim",     5.50,
        "Horror",         3.00,
        "Shooter",        4.00,
        "Action",         2.50,
        "Racing",         4.50,
        "Party / Casual", 2.00,
        "Simulation",     3.50,
        "Puzzle",         2.20
    );

    public BreakdownDTO getBreakdown(String fromGenre, String toGenre) {
        List<GamingGenre> allGenres = getAllGenres();

        // Find the from and to genre data from DB
        GamingGenre fromData = allGenres.stream()
                .filter(g -> g.getName().equalsIgnoreCase(fromGenre))
                .findFirst().orElse(null);

        GamingGenre toData = allGenres.stream()
                .filter(g -> g.getName().equalsIgnoreCase(toGenre))
                .findFirst().orElse(null);

        if (fromData == null || toData == null) {
            return new BreakdownDTO(fromGenre, toGenre, 0, 0, 0, 1.0, toGenre, "Genre data not found");
        }

        if (toData.getViews() == 0) {
            return new BreakdownDTO(
                fromGenre, toGenre,
                0, 0, 0, 1.0,
                fromGenre,
                "Not enough data for " + toGenre + " — no trending videos found"
            );
        }

        // CPM increase %
        double fromCPM = CPM_BY_GENRE.getOrDefault(fromGenre, 3.0);
        double toCPM   = CPM_BY_GENRE.getOrDefault(toGenre, 3.0);

        if (toCPM <= fromCPM) {
            return new BreakdownDTO(
                fromGenre, toGenre,
                (int) Math.round(((toCPM - fromCPM) / fromCPM) * 100),
                0,
                toData.getViews(),
                (double) Math.round((toCPM / fromCPM) * 10.0) / 10.0,
                fromGenre,  // recommendedGenre stays as fromGenre
                "No better genre found — " + fromGenre + " already has a high CPM"
            );
        }

        int cpmIncrease = (int) Math.round(((toCPM - fromCPM) / fromCPM) * 100);

        // Engagement rate = (likes + comments) / views * 100
        double fromEng = fromData.getViews() > 0
                ? ((double)(fromData.getLikes() + fromData.getComments()) / fromData.getViews()) * 100
                : 0;
        double toEng = toData.getViews() > 0
                ? ((double)(toData.getLikes() + toData.getComments()) / toData.getViews()) * 100
                : 0;
        int engagementRateDiff = (int) Math.round(fromEng > 0 ? ((toEng - fromEng) / fromEng) * 100 : 0);

        // Avg monthly views of trending genre
        long avgMonthlyViews = toData.getViews();

        // Performance multiplier
        double fromRevenue = (100000.0 / 1000) * fromCPM * 0.45;
        double toRevenue   = (100000.0 / 1000) * toCPM   * 0.45;
        double multiplier  = fromRevenue > 0
                ? Math.round((toRevenue / fromRevenue) * 10.0) / 10.0
                : 1.0;

        // Recommended genre = highest predicted growth
        String recommendedGenre = allGenres.stream()
                .max(Comparator.comparingDouble(GamingGenre::getChangePercent))
                .map(GamingGenre::getName)
                .orElse(toGenre);

        return new BreakdownDTO(
            fromGenre,
            toGenre,
            cpmIncrease,
            engagementRateDiff,
            avgMonthlyViews,
            multiplier,
            recommendedGenre,
            toGenre + " is a better genre for revenue growth"  // normal message
        );
    }
}
