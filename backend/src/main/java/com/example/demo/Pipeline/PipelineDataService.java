package com.example.demo.Pipeline;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class PipelineDataService {

    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    private final RevenueModelRepo revenueModelRepo;
    private final ExtractedVideoClaimRepo extractedVideoClaimRepo;

    public PipelineDataService(
            RevenueModelRepo revenueModelRepo,
            ExtractedVideoClaimRepo extractedVideoClaimRepo
    ) {
        this.revenueModelRepo = revenueModelRepo;
        this.extractedVideoClaimRepo = extractedVideoClaimRepo;
    }

    public RevenueModel saveRevenueProfile(RevenueModelReq request) {
        if (request == null || isBlank(request.getGenreName())) {
            throw new IllegalArgumentException("genreName is required.");
        }

        RevenueModel model = new RevenueModel();
        model.setGenreName(request.getGenreName().trim());
        model.setBaseCpm(request.getBaseCpm());
        model.setTrendMultiplier(request.getTrendMultiplier());
        model.setConfidence(request.getConfidence());
        model.setModelVersion(blankToNull(request.getModelVersion()));
        model.setEffectiveDate(
                request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now()
        );
        model.setCreatedAt(LocalDateTime.now());

        return revenueModelRepo.save(model);
    }

    public int importRawDsClaims(List<RawDsVideoClaimsReq> videos) {
        if (videos == null || videos.isEmpty()) {
            return 0;
        }

        int saved = 0;

        for (RawDsVideoClaimsReq video : videos) {
            if (video == null || isBlank(video.getVideoId())) {
                continue;
            }

            String videoId = video.getVideoId().trim();
            String title = blankToNull(video.getTitle());

            String genreName = blankToNull(video.getGenre());
            if (isBlank(genreName)) {
                genreName = inferGenreFromClaims(title, video.getClaims());
            }

            extractedVideoClaimRepo.deleteByVideoId(videoId);

            List<String> normalizedClaims = normalizeRawClaims(video.getClaims());

            for (String claimText : normalizedClaims) {
                ExtractedVideoClaim claim = new ExtractedVideoClaim();
                claim.setVideoId(videoId);
                claim.setSourceTitle(title != null ? title : videoId);
                claim.setClaimText(claimText);
                claim.setClaimCategory(inferNarrativeCategory(claimText));
                claim.setConfidence(null);
                claim.setModelVersion("ds-json-demo");
                claim.setCreatedAt(LocalDateTime.now());
                claim.setGenreName(genreName);
                extractedVideoClaimRepo.save(claim);
                saved++;
            }
        }

        return saved;
    }

    private List<String> normalizeRawClaims(List<String> rawClaims) {
        if (rawClaims == null || rawClaims.isEmpty()) {
            return List.of();
        }

        List<String> output = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String raw : rawClaims) {
            if (isBlank(raw)) {
                continue;
            }

            String block = raw
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            String[] pieces = SENTENCE_SPLIT.split(block);
            for (String piece : pieces) {
                String cleaned = cleanClaimText(piece);
                if (isBlank(cleaned) || cleaned.length() < 20) {
                    continue;
                }

                String key = cleaned.toLowerCase(Locale.ROOT);
                if (seen.add(key)) {
                    output.add(cleaned);
                }
            }
        }

        return output;
    }

    private String cleanClaimText(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        cleaned = cleaned.replaceAll("^[\\-•*]+\\s*", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        if (!cleaned.endsWith(".") && !cleaned.endsWith("!") && !cleaned.endsWith("?")) {
            cleaned = cleaned + ".";
        }

        return cleaned;
    }

    private String inferGenreFromClaims(String title, List<String> claims) {
        String text = "";

        if (!isBlank(title)) {
            text += title + " ";
        }

        if (claims != null && !claims.isEmpty()) {
            text += String.join(" ", claims);
        }

        text = text.toLowerCase(Locale.ROOT);

        if (containsAny(text, "minecraft", "craft", "survival", "fishing", "resources", "orbs")) {
            return "Survival Craft";
        }

        if (containsAny(text, "battle royale", "fortnite", "last alive", "elimination", "zone")) {
            return "Battle Royale";
        }

        if (containsAny(text, "overwatch", "cod", "call of duty", "gun", "weapon", "shoot", "sniper", "fps")) {
            return "Shooter";
        }

        if (containsAny(text, "roblox", "party", "casual", "dog", "friend", "hide-and-seek", "tagged", "troll tower")) {
            return "Party / Casual";
        }

        if (containsAny(text, "boss", "combat", "hero", "fight", "damage", "warrior", "archer", "upgrade")) {
            return "Action";
        }

        if (containsAny(text, "football", "madden", "nba", "nfc", "championship", "sports")) {
            return "Sports Sim";
        }

        if (containsAny(text, "race", "racing", "car", "driving", "speed")) {
            return "Racing";
        }

        if (containsAny(text, "horror", "scary", "monster", "haunted")) {
            return "Horror";
        }

        if (containsAny(text, "simulator", "simulation", "tycoon", "management")) {
            return "Simulation";
        }

        if (containsAny(text, "puzzle", "logic", "match")) {
            return "Puzzle";
        }

        return "General";
    }

    private String inferNarrativeCategory(String claimText) {
        String lower = claimText == null ? "" : claimText.toLowerCase(Locale.ROOT);

        if (containsAny(lower, "world", "map", "environment", "exploration", "explore", "path", "hidden", "area", "location", "room", "theater", "store", "tower")) {
            return "World Interaction";
        }

        if (containsAny(lower, "upgrade", "progression", "challenge", "boss", "defeat", "inventory", "resource", "money", "damage", "dps", "hero", "obstacle", "capture", "victory", "score", "quest")) {
            return "Challenge & Progression";
        }

        if (containsAny(lower, "feel", "experience", "visual", "lag", "customization", "user interface", "technical", "casual", "competitive")) {
            return "Player Experience";
        }

        return "Gameplay Flow";
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}