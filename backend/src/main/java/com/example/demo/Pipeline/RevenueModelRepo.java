package com.example.demo.Pipeline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RevenueModelRepo extends JpaRepository<RevenueModel, Long> {
    Optional<RevenueModel> findFirstByGenreNameAndEffectiveDateLessThanEqualOrderByEffectiveDateDescCreatedAtDesc(
            String genreName,
            LocalDate targetDate
    );
}
