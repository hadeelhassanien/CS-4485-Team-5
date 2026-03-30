package com.example.demo.Trends;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GamingGenreRepository extends JpaRepository<GamingGenre, Long> {
    
    GamingGenre findByNameAndSnapshotDate(String name, LocalDate snapshotDate);

    List<GamingGenre> findBySnapshotDate(LocalDate snapshotDate);

    @Query("SELECT MAX(g.snapshotDate) FROM GamingGenre g WHERE g.snapshotDate < :date")
    LocalDate findPreviousSnapshotDate(@Param("date") LocalDate date);
}
