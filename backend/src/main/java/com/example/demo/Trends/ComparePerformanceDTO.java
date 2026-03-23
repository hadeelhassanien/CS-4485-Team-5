package com.example.demo.Trends;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ComparePerformanceDTO {

    private String fromGenre;
    private String toGenre;
    private long   beforeViews;
    private double beforeEngagementChangePct; 
    private long   afterViews;
    private double afterEngagementChangePct;   
    private long   avgViewsBefore;
    private long   avgViewsAfter;
    private double likeRatioBefore;
    private double likeRatioAfter;
    private long   commentsBefore;
    private long   commentsAfter;
    private double estimatedRevenueUpliftPct;
}

