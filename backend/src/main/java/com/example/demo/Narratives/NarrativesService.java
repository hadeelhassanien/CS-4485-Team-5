package com.example.demo.Narratives;
import org.springframework.stereotype.Service;
import com.example.demo.Pipeline.ExtractedVideoClaimRepo;
import com.example.demo.YouTubeAPI.VideoDTO;
import com.example.demo.YouTubeAPI.YouTubeService;
import com.example.demo.Pipeline.ExtractedVideoClaim;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NarrativesService {

    private final YouTubeService youTubeService;
    private final ExtractedVideoClaimRepo extractedVideoClaimRepo;

    public NarrativesService(
            YouTubeService youTubeService,
            ExtractedVideoClaimRepo extractedVideoClaimRepo
    ) {
        this.youTubeService = youTubeService;
        this.extractedVideoClaimRepo = extractedVideoClaimRepo;
    }

    public NarrativesPageResponse buildPageData(String regionCode, int maxResults) {
        List<ExtractedVideoClaim> claimsToUse = loadClaimsForNarratives(regionCode, maxResults);

        if (claimsToUse.isEmpty()) {
            return new NarrativesPageResponse(defaultRecentNarratives(), defaultOverviewSections());
        }

        Map<String, List<ExtractedVideoClaim>> byCategory = new LinkedHashMap<>();
        byCategory.put("Gameplay Flow", new ArrayList<>());
        byCategory.put("World Interaction", new ArrayList<>());
        byCategory.put("Challenge & Progression", new ArrayList<>());
        byCategory.put("Player Experience", new ArrayList<>());

        for (ExtractedVideoClaim claim : claimsToUse) {
            String category = normalizeCategory(claim.getClaimCategory());
            byCategory.computeIfAbsent(category, ignored -> new ArrayList<>()).add(claim);
        }

        List<String> recentNarratives = claimsToUse.stream()
                .map(ExtractedVideoClaim::getClaimText)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(5)
                .toList();

        List<NarrativeSectionDTO> sections = new ArrayList<>();
        for (Map.Entry<String, List<ExtractedVideoClaim>> entry : byCategory.entrySet()) {
            List<String> items = entry.getValue().stream()
                    .sorted(Comparator.comparing(
                            c -> c.getConfidence() == null ? 0.0 : c.getConfidence(),
                            Comparator.reverseOrder()
                    ))
                    .map(ExtractedVideoClaim::getClaimText)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .limit(3)
                    .collect(Collectors.toList());

            if (items.isEmpty()) {
                items = fallbackItemsFor(entry.getKey());
            }

            sections.add(new NarrativeSectionDTO(entry.getKey(), items));
        }

        if (recentNarratives.isEmpty()) {
            recentNarratives = defaultRecentNarratives();
        }

        return new NarrativesPageResponse(recentNarratives, sections);
    }

    private List<ExtractedVideoClaim> loadClaimsForNarratives(String regionCode, int maxResults) {
        List<VideoDTO> videos = youTubeService.getPopularGamingVideoDetails(Math.min(maxResults, 20), regionCode);

        List<String> currentVideoIds = videos.stream()
                .map(VideoDTO::getVideoId)
                .filter(Objects::nonNull)
                .toList();

        List<ExtractedVideoClaim> matchingClaims = currentVideoIds.isEmpty()
                ? List.of()
                : extractedVideoClaimRepo.findByVideoIdInOrderByConfidenceDescCreatedAtDesc(currentVideoIds);

        if (!matchingClaims.isEmpty()) {
            return matchingClaims;
        }

        // Demo-safe fallback: use any recently imported claims in DB if live video IDs don't match.
        List<ExtractedVideoClaim> allClaims = extractedVideoClaimRepo.findAll();
        if (allClaims.isEmpty()) {
            return List.of();
        }

        return allClaims.stream()
                .sorted(Comparator.comparing(ExtractedVideoClaim::getCreatedAt).reversed())
                .limit(30)
                .toList();
    }

    private String normalizeCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Gameplay Flow";
        }

        String value = raw.trim();
        if (value.equalsIgnoreCase("World Interaction")) return "World Interaction";
        if (value.equalsIgnoreCase("Challenge & Progression")) return "Challenge & Progression";
        if (value.equalsIgnoreCase("Player Experience")) return "Player Experience";
        return "Gameplay Flow";
    }

    private List<String> defaultRecentNarratives() {
        return List.of(
                "Highlights how gameplay pacing encourages constant movement and reactive decision-making.",
                "Explores how environmental structure supports exploration through alternate routes and hidden spaces.",
                "Describes how progression systems gradually introduce more demanding challenges.",
                "Highlights how encounter design creates tension through shifting enemy pressure.",
                "Explores how the overall gameplay loop stays engaging through steady feedback and reward."
        );
    }

    private List<NarrativeSectionDTO> defaultOverviewSections() {
        return List.of(
                new NarrativeSectionDTO("Gameplay Flow", List.of(
                        "Highlights how gameplay systems encourage quick decisions and fluid transitions between encounters.",
                        "Describes how core mechanics support a responsive and engaging gameplay loop."
                )),
                new NarrativeSectionDTO("World Interaction", List.of(
                        "Explores how environments encourage exploration through alternate paths and subtle visual guidance.",
                        "Highlights how level structure supports player choice without over-directing movement."
                )),
                new NarrativeSectionDTO("Challenge & Progression", List.of(
                        "Describes how progression systems create a steady sense of growth over time.",
                        "Highlights how escalating encounters help maintain difficulty and long-term engagement."
                )),
                new NarrativeSectionDTO("Player Experience", List.of(
                        "Highlights how pacing and feedback shape the overall feel of moment-to-moment play.",
                        "Explores how satisfying interactions support immersion and player investment."
                ))
        );
    }

    private List<String> fallbackItemsFor(String title) {
        return switch (title) {
            case "World Interaction" -> List.of(
                    "Explores how environments encourage exploration through alternate paths and subtle visual guidance.",
                    "Highlights how level structure supports player choice without over-directing movement."
            );
            case "Challenge & Progression" -> List.of(
                    "Describes how progression systems create a steady sense of growth over time.",
                    "Highlights how escalating encounters help maintain difficulty and long-term engagement."
            );
            case "Player Experience" -> List.of(
                    "Highlights how pacing and feedback shape the overall feel of moment-to-moment play.",
                    "Explores how satisfying interactions support immersion and player investment."
            );
            default -> List.of(
                    "Highlights how gameplay systems encourage quick decisions and fluid transitions between encounters.",
                    "Describes how core mechanics support a responsive and engaging gameplay loop."
            );
        };
    }
}