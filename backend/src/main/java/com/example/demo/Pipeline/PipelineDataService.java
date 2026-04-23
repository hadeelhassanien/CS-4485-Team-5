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

    private final VideoClassificationRepo classificationRepository;
    private final RevenueModelRepo revenueModelRepo;
    private final ExtractedVideoClaimRepo extractedVideoClaimRepo;

    public PipelineDataService(
            VideoClassificationRepo classificationRepository,
            RevenueModelRepo revenueModelRepo,
            ExtractedVideoClaimRepo extractedVideoClaimRepo
    ) {
        this.classificationRepository = classificationRepository;
        this.revenueModelRepo = revenueModelRepo;
        this.extractedVideoClaimRepo = extractedVideoClaimRepo;
    }

    public int upsertGenreClassifications(List<VideoClassificationInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return 0;
        }

        int updated = 0;

        for (VideoClassificationInput input : inputs) {
            if (input == null || isBlank(input.getVideoId()) || isBlank(input.getGenreName())) {
                continue;
            }

            VideoGenreClassification classification = classificationRepository
                    .findByVideoId(input.getVideoId())
                    .orElseGet(VideoGenreClassification::new);

            classification.setVideoId(input.getVideoId().trim());
            classification.setGenreName(input.getGenreName().trim());
            classification.setConfidence(input.getConfidence());
            classification.setModelVersion(blankToNull(input.getModelVersion()));
            classification.setClassifiedAt(LocalDateTime.now());

            classificationRepository.save(classification);
            updated++;
        }

        return updated;
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

    public int replaceExtractedClaims(List<ExtractedVideoClaimInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return 0;
        }

        int saved = 0;
        Set<String> clearedVideoIds = new LinkedHashSet<>();

        for (ExtractedVideoClaimInput input : inputs) {
            if (input == null || isBlank(input.getVideoId()) || isBlank(input.getClaimText())) {
                continue;
            }

            String videoId = input.getVideoId().trim();
            if (clearedVideoIds.add(videoId)) {
                extractedVideoClaimRepo.deleteByVideoId(videoId);
            }

            ExtractedVideoClaim claim = new ExtractedVideoClaim();
            claim.setVideoId(videoId);
            claim.setSourceTitle(blankToNull(input.getSourceTitle()));
            claim.setClaimText(cleanClaimText(input.getClaimText()));
            claim.setClaimCategory(blankToNull(input.getClaimCategory()));
            claim.setConfidence(input.getConfidence());
            claim.setModelVersion(blankToNull(input.getModelVersion()));
            claim.setCreatedAt(LocalDateTime.now());

            extractedVideoClaimRepo.save(claim);
            saved++;
        }

        return saved;
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