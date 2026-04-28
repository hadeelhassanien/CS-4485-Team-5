package com.example.demo.Claims;
import com.example.demo.Trends.GamingGenre;
import com.example.demo.Trends.GamingGenreRepository;
import com.example.demo.Pipeline.RevenueModel;
import com.example.demo.Pipeline.RevenueModelRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class RevenueService {

    private final GamingGenreRepository genreRepository;
    private final ClaimRepository claimRepository;
    private final RevenueModelRepo revenueModelRepo;

    public RevenueService(
            GamingGenreRepository genreRepository,
            ClaimRepository claimRepository,
            RevenueModelRepo revenueModelRepo
    ) {
        this.genreRepository = genreRepository;
        this.claimRepository = claimRepository;
        this.revenueModelRepo = revenueModelRepo;
    }

    public RevenueEstimateDTO getRevenueEstimate(String genre) {
        LocalDate today = LocalDate.now();

        GamingGenre snapshot = getLatestSnapshot(genre, today);
        if (snapshot == null) {
            throw new IllegalArgumentException("Genre not found: " + genre);
        }

        RevenueModel profile = revenueModelRepo
                .findFirstByGenreNameAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(genre, today)
                .or(() -> revenueModelRepo
                        .findFirstByGenreNameAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc("__default__", today))
                .orElseGet(() -> {
                    RevenueModel fallback = new RevenueModel();
                    fallback.setGenreName(genre);
                    fallback.setBaseCpm(2.8);
                    fallback.setTrendMultiplier(1.15);
                    fallback.setConfidence(0.75);
                    fallback.setModelVersion("fallback-default");
                    fallback.setEffectiveDate(today);
                    fallback.setCreatedAt(LocalDateTime.now());
                    return fallback;
                });

        YearMonth month = YearMonth.from(today);
        String periodStart = month.atDay(1).format(DateTimeFormatter.ofPattern("MMM d"));
        String periodEnd = month.atEndOfMonth().format(DateTimeFormatter.ofPattern("MMM d"));

        /*
         * Revenue Model
         * views / 1000 = CPM units
         * baseRevenue = CPM units * CPM
         * trendMultiplier example:
         * 1.15 = +15%
         * 1.30 = +30%
         */
        double views = snapshot.getViews();
        if (views <= 0) {
            views = 500000; //fallback
        }
        
        double monetizedViews = views / 1000.0;

        double baseRevenue = monetizedViews * profile.getBaseCpm();

        double multiplier = profile.getTrendMultiplier();
        if (multiplier < 1.0) {
            multiplier = 1.0;
        }

        double trendBoost = baseRevenue * (multiplier - 1.0);

        double totalEstimatedRevenue = baseRevenue + trendBoost;

        double alreadyClaimed = claimRepository.sumAllClaimedAmounts();

        double unclaimedBalance = Math.max(0, totalEstimatedRevenue - alreadyClaimed);

        String lastClaimDate = "N/A";
        double lastClaimAmount = 0.0;

        var lastClaim = claimRepository.findMostRecentClaim();
        if (lastClaim.isPresent()) {
            lastClaimDate = lastClaim.get()
                    .getClaimDate()
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

            lastClaimAmount = lastClaim.get().getAmountClaimed();
        }

        return new RevenueEstimateDTO(
                genre,
                periodStart,
                periodEnd,
                round(baseRevenue),
                round(trendBoost),
                round(totalEstimatedRevenue),
                round(unclaimedBalance),
                lastClaimDate,
                round(lastClaimAmount)
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

        if (g != null) {
            return g;
        }

        LocalDate prevDate = genreRepository.findPreviousSnapshotDate(today.plusDays(1));

        return prevDate != null
                ? genreRepository.findByNameAndSnapshotDate(name, prevDate)
                : null;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}