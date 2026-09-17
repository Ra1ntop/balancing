package com.ra1n.top.api.player.dto;

import com.ra1n.top.api.player.PlayerStatistics;
import java.math.BigDecimal;

public record PlayerStatisticsResponse(
        int matches,
        int wins,
        int losses,
        int draws,
        BigDecimal winRate,
        int currentWinStreak,
        int currentLossStreak,
        long totalDamage,
        BigDecimal averageDamage) {

    public static PlayerStatisticsResponse from(PlayerStatistics statistics) {
        return new PlayerStatisticsResponse(
                statistics.getMatches(),
                statistics.getWins(),
                statistics.getLosses(),
                statistics.getDraws(),
                statistics.getWinRate(),
                statistics.getCurrentWinStreak(),
                statistics.getCurrentLossStreak(),
                statistics.getTotalDamage(),
                statistics.getAverageDamage());
    }
}
