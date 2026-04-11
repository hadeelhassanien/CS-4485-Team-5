package com.example.demo.Claims;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    @Query("SELECT c FROM Claim c ORDER BY c.claimDate DESC LIMIT 1")
    Optional<Claim> findMostRecentClaim();

    @Query("SELECT COALESCE(SUM(c.amountClaimed), 0) FROM Claim c")
    double sumAllClaimedAmounts();

    @Query("SELECT COALESCE(SUM(c.amountClaimed), 0) FROM Claim c WHERE c.genre = :genre")
    double sumClaimedAmountsByGenre(String genre);
}