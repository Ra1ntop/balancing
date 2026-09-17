package com.ra1n.top.api.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayerStatisticsTest {

    private final PlayerStatistics statistics = new Player("tester", "Tester").getStatistics();

    @Test
    @DisplayName("a fresh player has zeroed counters and no division by zero")
    void startsEmpty() {
        assertThat(statistics.getMatches()).isZero();
        assertThat(statistics.getWinRate()).isEqualByComparingTo("0.00");
        assertThat(statistics.getAverageDamage()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("wins extend the win streak and clear the loss streak")
    void winsBuildAStreak() {
        statistics.record(MatchResult.LOSS, 10);
        statistics.record(MatchResult.WIN, 10);
        statistics.record(MatchResult.WIN, 10);

        assertThat(statistics.getCurrentWinStreak()).isEqualTo(2);
        assertThat(statistics.getCurrentLossStreak()).isZero();
    }

    @Test
    @DisplayName("losses extend the loss streak and clear the win streak")
    void lossesBuildAStreak() {
        statistics.record(MatchResult.WIN, 10);
        statistics.record(MatchResult.LOSS, 10);
        statistics.record(MatchResult.LOSS, 10);

        assertThat(statistics.getCurrentLossStreak()).isEqualTo(2);
        assertThat(statistics.getCurrentWinStreak()).isZero();
    }

    @Test
    @DisplayName("a draw ends both streaks")
    void drawBreaksBothStreaks() {
        statistics.record(MatchResult.WIN, 10);
        statistics.record(MatchResult.WIN, 10);
        statistics.record(MatchResult.DRAW, 10);

        assertThat(statistics.getCurrentWinStreak()).isZero();
        assertThat(statistics.getCurrentLossStreak()).isZero();
        assertThat(statistics.getDraws()).isEqualTo(1);
    }

    @Test
    @DisplayName("derived values follow the counters")
    void derivesRateAndAverageDamage() {
        statistics.record(MatchResult.WIN, 100);
        statistics.record(MatchResult.WIN, 200);
        statistics.record(MatchResult.LOSS, 60);
        statistics.record(MatchResult.DRAW, 40);

        assertThat(statistics.getMatches()).isEqualTo(4);
        assertThat(statistics.getWins()).isEqualTo(2);
        assertThat(statistics.getLosses()).isEqualTo(1);
        assertThat(statistics.getDraws()).isEqualTo(1);
        assertThat(statistics.getTotalDamage()).isEqualTo(400);
        assertThat(statistics.getWinRate()).isEqualByComparingTo("50.00");
        assertThat(statistics.getAverageDamage()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("win rate is rounded to two decimals")
    void roundsWinRate() {
        statistics.record(MatchResult.WIN, 0);
        statistics.record(MatchResult.LOSS, 0);
        statistics.record(MatchResult.LOSS, 0);

        assertThat(statistics.getWinRate()).isEqualByComparingTo(new BigDecimal("33.33"));
    }

    @Test
    @DisplayName("negative damage is rejected")
    void rejectsNegativeDamage() {
        assertThatThrownBy(() -> statistics.record(MatchResult.WIN, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
