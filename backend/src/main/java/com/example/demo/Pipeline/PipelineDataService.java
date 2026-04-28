package com.example.demo.Pipeline;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

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
        if (request.getBaseCpm() < 0) {
            throw new IllegalArgumentException("baseCpm must be non-negative.");
        }
        if (request.getTrendMultiplier() < 0) {
            throw new IllegalArgumentException("trendMultiplier must be non-negative.");
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
            String genreName = video.resolvedGenreName();
            
            if (genreName.equalsIgnoreCase("General")) {
                genreName = inferGenreFromClaims(video.getClaims());
            }

            extractedVideoClaimRepo.deleteByVideoId(videoId);

            List<String> normalizedClaims = normalizeRawClaims(video.getClaims());
            for (String claimText : normalizedClaims) {
                ExtractedVideoClaim claim = new ExtractedVideoClaim();
                claim.setVideoId(videoId);
                claim.setSourceTitle(null);
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

    private String inferGenreFromClaims(List<String> claims) {
    if (claims == null || claims.isEmpty()) {
        return "General";
    }

    String text = String.join(" ", claims).toLowerCase();

    if (text.contains("minecraft") || text.contains("craft") || text.contains("fishing")
            || text.contains("resources") || text.contains("survival")) {
        return "Survival Craft";
    }

    if (text.contains("battle royale") || text.contains("zone") || text.contains("last alive")) {
        return "Battle Royale";
    }

    if (text.contains("gun") || text.contains("weapon") || text.contains("shoot")
            || text.contains("sniper") || text.contains("fps")) {
        return "Shooter";
    }

    if (text.contains("party") || text.contains("friend") || text.contains("hide-and-seek")
            || text.contains("tagged") || text.contains("dog")) {
        return "Party / Casual";
    }

    if (text.contains("fight") || text.contains("combat") || text.contains("boss")
            || text.contains("damage") || text.contains("hero")) {
        return "Action";
    }

    if (text.contains("scary") || text.contains("monster") || text.contains("horror")
            || text.contains("escape")) {
        return "Horror";
    }

    if (text.contains("soccer") || text.contains("football") || text.contains("nba")
            || text.contains("sports")) {
        return "Sports Sim";
    }

    if (text.contains("race") || text.contains("car") || text.contains("driving")
            || text.contains("speed")) {
        return "Racing";
    }

    if (text.contains("simulator") || text.contains("tycoon")
            || text.contains("management")) {
        return "Simulation";
    }

    if (text.contains("puzzle") || text.contains("logic") || text.contains("match")) {
        return "Puzzle";
    }

    return "General";
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

            String normalizedBlock = raw
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            String[] pieces = SENTENCE_SPLIT.split(normalizedBlock);
            for (String piece : pieces) {
                String cleaned = cleanClaimText(piece);
                if (isBlank(cleaned) || cleaned.length() < 20) {
                    continue;
                }

                String dedupeKey = cleaned.toLowerCase(Locale.ROOT);
                if (seen.add(dedupeKey)) {
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

    private String inferNarrativeCategory(String claimText) {
        String lower = claimText.toLowerCase(Locale.ROOT);

        if (containsAny(lower, "world", "map", "environment", "exploration", "explore", "path", "hidden", "area", "location")) {
            return "World Interaction";
        }

        if (containsAny(lower, "upgrade", "progression", "challenge", "boss", "defeat", "inventory", "resource", "money", "damage", "dps", "hero", "obstacle", "capture", "victory", "score")) {
            return "Challenge & Progression";
        }

        if (containsAny(lower, "feel", "experience", "visual", "lag", "customization", "user interface", "technical", "casual", "competitive")) {
            return "Player Experience";
        }

        return "Gameplay Flow";
    }

    private boolean containsAny(String text, String... keywords) {
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