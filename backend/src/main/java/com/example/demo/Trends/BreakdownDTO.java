package com.example.demo.Trends;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BreakdownDTO {
    
    private String fromGenre;
    private String toGenre;
    private int cpmIncrease;
    private int engagementRateDiff;
    private long avgMonthlyViews;
    private double performanceMultiplier;
    private String recommendedGenre;
    private String message;
}
