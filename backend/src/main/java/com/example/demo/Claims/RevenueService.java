package com.example.demo.Claims;
import com.example.demo.Trends.GamingGenre;
import com.example.demo.Trends.GamingGenreRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class RevenueService {

    private final GamingGenreRepository genreRepository;
    private final ClaimRepository claimRepository;

    public RevenueService(GamingGenreRepository genreRepository, ClaimRepository claimRepository) {
        this.genreRepository = genreRepository;
        this.claimRepository = claimRepository;
    }

    private static final double BASE_CPM  = 2.0;  
    private static final double TREND_CPM = 3.5;  

    public RevenueEstimateDTO getRevenueEstimate(String genre) {
        LocalDate today = LocalDate.now();

        GamingGenre snapshot = getLatestSnapshot(genre, today);
        if (snapshot == null) {
            throw new IllegalArgumentException("Genre not found: " + genre);
        }

        YearMonth month =YearMonth.from(today);
        String periodStart =month.atDay(1).format(DateTimeFormatter.ofPattern("MMM d"));
        String periodEnd =month.atEndOfMonth().format(DateTimeFormatter.ofPattern("MMM d"));

        double baseRevenue =(snapshot.getViews() / 1000.0) * BASE_CPM;
        double trendBoost =(snapshot.getViews() / 1000.0) * (TREND_CPM - BASE_CPM);
        double totalRevenue =baseRevenue + trendBoost;
        double alreadyClaimed = claimRepository.sumClaimedAmountsByGenre(genre);
        double unclaimedBalance= Math.max(0, totalRevenue - alreadyClaimed);

        String lastClaimDate   = "N/A";
        double lastClaimAmount = 0.0;
        var lastClaim = claimRepository.findMostRecentClaim();
        if (lastClaim.isPresent()) {
            lastClaimDate   = lastClaim.get().getClaimDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            lastClaimAmount = lastClaim.get().getAmountClaimed();
        }

        return new RevenueEstimateDTO(
            genre,
            periodStart,
            periodEnd,
            round(baseRevenue),          // baseRevenue
            round(trendBoost),           // trendBoost
            round(totalRevenue),         // totalEstimatedRevenue
            round(unclaimedBalance),
            lastClaimDate,
            lastClaimAmount
        );
    }

    public RevenueEstimateDTO claimEarnings(String genre) {
        RevenueEstimateDTO estimate = getRevenueEstimate(genre);

        if (estimate.getUnclaimedBalance() <= 0) {
            throw new IllegalStateException("No unclaimed balance available.");
        }

        Claim claim = new Claim(
                genre,
                estimate.getUnclaimedBalance(),
                estimate.getTrendBoost(),
                LocalDate.now(),
                LocalDateTime.now()
        );
        claimRepository.save(claim);
        return getRevenueEstimate(genre);
    }

    private GamingGenre getLatestSnapshot(String name, LocalDate today) {
        GamingGenre g = genreRepository.findByNameAndSnapshotDate(name, today);
        if (g != null) return g;
        LocalDate prevDate = genreRepository.findPreviousSnapshotDate(today.plusDays(1));
        return prevDate != null ? genreRepository.findByNameAndSnapshotDate(name, prevDate) : null;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}