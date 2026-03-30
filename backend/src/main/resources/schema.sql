--  CreatorXP  –  Database Schema

CREATE DATABASE IF NOT EXISTS creatorxp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE creatorxp;

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