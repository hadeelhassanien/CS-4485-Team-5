package com.example.demo.Claims;

public class RevenueEstimateDTO {

    private String genre;
    private String periodStart;
    private String periodEnd;
    private double trendBoost;
    private double baseRevenue;
    private double unclaimedBalance;
    private double totalEstimatedRevenue;
    private String lastClaimDate;
    private double lastClaimAmount;

    public RevenueEstimateDTO() {}

    public RevenueEstimateDTO(String genre, String periodStart, String periodEnd, double baseRevenue, double trendBoost, double totalEstimatedRevenue, double unclaimedBalance, String lastClaimDate, double lastClaimAmount) {
        this.genre =genre;
        this.periodStart =periodStart;
        this.periodEnd =periodEnd;
        this.totalEstimatedRevenue= totalEstimatedRevenue;
        this.baseRevenue =baseRevenue;
        this.unclaimedBalance =unclaimedBalance;
        this.trendBoost =trendBoost;
        this.lastClaimDate =lastClaimDate;
        this.lastClaimAmount =lastClaimAmount;
    }

    public String getGenre() { return genre; }
    public String getPeriodStart() { return periodStart; }
    public String getPeriodEnd() { return periodEnd; }
    public double getTotalEstimatedRevenue() { return totalEstimatedRevenue; }
    public double getUnclaimedBalance() { return unclaimedBalance; }
    public double getBaseRevenue() { return baseRevenue; }
    public double getTrendBoost() { return trendBoost; }
    public String getLastClaimDate() { return lastClaimDate; }
    public double getLastClaimAmount() { return lastClaimAmount; }

    public void setGenre(String genre) { this.genre = genre; }
    public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }
    public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }
    public void setTotalEstimatedRevenue(double totalEstimatedRevenue) { this.totalEstimatedRevenue = totalEstimatedRevenue; }
    public void setBaseRevenue(double baseRevenue) { this.baseRevenue = baseRevenue; }
    public void setTrendBoost(double trendBoost) { this.trendBoost = trendBoost; }
    public void setUnclaimedBalance(double unclaimedBalance) { this.unclaimedBalance = unclaimedBalance; }
    public void setLastClaimDate(String lastClaimDate) { this.lastClaimDate = lastClaimDate; }
    public void setLastClaimAmount(double lastClaimAmount) { this.lastClaimAmount = lastClaimAmount; }
}