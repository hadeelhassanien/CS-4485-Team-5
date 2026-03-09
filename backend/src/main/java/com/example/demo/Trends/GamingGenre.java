package com.example.demo.Trends;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gaming_genres")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GamingGenre {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "views")
    private long views;

    @Column(name = "likes")
    private long likes;

    @Column(name = "comments")
    private long comments;

    @Column(name = "change_percent")
    private double changePercent;

    @Column(name = "snapshot_date")
    private LocalDate snapshotDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
