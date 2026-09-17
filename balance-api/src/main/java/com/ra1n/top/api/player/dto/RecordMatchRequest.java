package com.ra1n.top.api.player.dto;

import com.ra1n.top.api.player.MatchResult;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** One finished match to fold into a player's statistics. */
public record RecordMatchRequest(

        @NotNull(message = "must be one of WIN, LOSS, DRAW")
        MatchResult result,

        @NotNull(message = "must be provided")
        @PositiveOrZero(message = "must be greater than or equal to 0")
        Integer damage) {
}
