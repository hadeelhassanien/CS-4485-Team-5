package com.example.demo.Narratives;
import java.util.List;

public class NarrativesPageResponse {
    private List<String> recentNarratives;
    private List<NarrativeSectionDTO> aiGameplayOverview;

    public NarrativesPageResponse() {}

    public NarrativesPageResponse(List<String> recentNarratives, List<NarrativeSectionDTO> aiGameplayOverview) {
        this.recentNarratives = recentNarratives;
        this.aiGameplayOverview = aiGameplayOverview;
    }

    public List<String> getRecentNarratives() {
        return recentNarratives;
    }

    public List<NarrativeSectionDTO> getAiGameplayOverview() {
        return aiGameplayOverview;
    }

    public void setRecentNarratives(List<String> recentNarratives) {
        this.recentNarratives = recentNarratives;
    }

    public void setAiGameplayOverview(List<NarrativeSectionDTO> aiGameplayOverview) {
        this.aiGameplayOverview = aiGameplayOverview;
    }
}