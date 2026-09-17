package com.ra1n.top.api.player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Only raw counters are persisted. Matches played, win rate and average damage are derived
 * on read, so the stored rows can never disagree with the values the API reports.
 */
@Entity
@Table(name = "player_statistics")
public class PlayerStatistics {

    @Id
    @Column(name = "player_id")
    private UUID playerId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int losses;

    @Column(nullable = false)
    private int draws;

    @Column(name = "total_damage", nullable = false)
    private long totalDamage;

    @Column(name = "current_win_streak", nullable = false)
    private int currentWinStreak;

    @Column(name = "current_loss_streak", nullable = false)
    private int currentLossStreak;

    protected PlayerStatistics() {
        // for JPA
    }

    PlayerStatistics(Player player) {
        this.player = player;
    }

    /**
     * Applies one match outcome. A draw ends both streaks: it is a match that neither
     * continues a winning run nor a losing one.
     */
    public void record(MatchResult result, int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must not be negative");
        }
        switch (result) {
            case WIN -> {
                wins++;
                currentWinStreak++;
                currentLossStreak = 0;
            }
            case LOSS -> {
                losses++;
                currentLossStreak++;
                currentWinStreak = 0;
            }
            case DRAW -> {
                draws++;
                currentWinStreak = 0;
                currentLossStreak = 0;
            }
        }
        totalDamage += damage;
    }

    public int getMatches() {
        return wins + losses + draws;
    }

    /** Share of wins among all matches, in percent, rounded to two decimals. */
    public BigDecimal getWinRate() {
        int matches = getMatches();
        if (matches == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(wins)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(matches), 2, RoundingMode.HALF_UP);
    }

    /** Damage dealt per match, rounded to two decimals. */
    public BigDecimal getAverageDamage() {
        int matches = getMatches();
        if (matches == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(totalDamage)
                .divide(BigDecimal.valueOf(matches), 2, RoundingMode.HALF_UP);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public long getTotalDamage() {
        return totalDamage;
    }

    public int getCurrentWinStreak() {
        return currentWinStreak;
    }

    public int getCurrentLossStreak() {
        return currentLossStreak;
    }
}
