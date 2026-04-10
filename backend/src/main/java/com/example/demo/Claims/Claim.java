package com.example.demo.Claims;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "genre")
    private String genre;

    @Column(name = "trend_boost")
    private double trendBoost;

    @Column(name = "claim_date")
    private LocalDate claimDate;

     @Column(name = "amount_claimed")
    private double amountClaimed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Claim() {}

    public Claim(String genre, double amountClaimed, double trendBoost,
                 LocalDate claimDate, LocalDateTime createdAt) {
        this.genre         = genre;
        this.trendBoost    = trendBoost;
        this.claimDate     = claimDate;
        this.amountClaimed = amountClaimed;
        this.createdAt     = createdAt;
    }

    public Long getId()                  { return id; }
    public String getGenre()             { return genre; }
    public double getTrendBoost()        { return trendBoost; }
    public double getAmountClaimed()     { return amountClaimed; }
    public LocalDate getClaimDate()      { return claimDate; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setId(Long id)                        { this.id = id; }
    public void setGenre(String genre)                { this.genre = genre; }
    public void setTrendBoost(double trendBoost)      { this.trendBoost = trendBoost; }
    public void setAmountClaimed(double amountClaimed){ this.amountClaimed = amountClaimed; }
    public void setClaimDate(LocalDate claimDate)     { this.claimDate = claimDate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
