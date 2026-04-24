--  CreatorXP  –  Database Schema

CREATE DATABASE IF NOT EXISTS creatorxp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE creatorxp;


-- gaming_genre
CREATE TABLE IF NOT EXISTS gaming_genre (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255),
    views           BIGINT       DEFAULT 0,
    likes           BIGINT       DEFAULT 0,
    comments        BIGINT       DEFAULT 0,
    change_percent  DOUBLE       DEFAULT 0.0,
    snapshot_date   DATE,
    last_updated    DATETIME,
    PRIMARY KEY (id),
    INDEX idx_genre_name          (name),
    INDEX idx_genre_snapshot_date (snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- claim 
CREATE TABLE IF NOT EXISTS claim (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    genre           VARCHAR(255),
    trend_boost     DOUBLE       DEFAULT 0.0,
    amount_claimed  DOUBLE       DEFAULT 0.0,
    claim_date      DATE,
    created_at      DATETIME,
    PRIMARY KEY (id),
    INDEX idx_claim_date  (claim_date),
    INDEX idx_claim_genre (genre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- data science pipeline: per-video genre classifications
CREATE TABLE IF NOT EXISTS video_genre_classification (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    video_id        VARCHAR(255) NOT NULL,
    genre_name      VARCHAR(255) NOT NULL,
    confidence      DOUBLE       NULL,
    model_version   VARCHAR(255) NULL,
    classified_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_video_genre_classification_video_id (video_id),
    INDEX idx_video_genre_classification_genre_name (genre_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- data science pipeline: monetization model profiles
CREATE TABLE IF NOT EXISTS revenue_model_profile (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    genre_name       VARCHAR(255) NOT NULL,
    base_cpm         DOUBLE       NOT NULL,
    trend_multiplier DOUBLE       NOT NULL,
    confidence       DOUBLE       NULL,
    model_version    VARCHAR(255) NULL,
    effective_date   DATE         NOT NULL,
    created_at       DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_revenue_model_profile_genre_name (genre_name),
    INDEX idx_revenue_model_profile_effective_date (effective_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS extracted_video_claim (
    id BIGINT NOT NULL AUTO_INCREMENT,
    video_id VARCHAR(255) NOT NULL,
    source_title VARCHAR(255) NULL,
    claim_text TEXT NOT NULL,
    claim_category VARCHAR(255) NULL,
    confidence DOUBLE NULL,
    model_version VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_extracted_video_claim_video_id (video_id),
    INDEX idx_extracted_video_claim_claim_category (claim_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;