package com.ra1n.top.api.player.dto;

import com.ra1n.top.api.player.Player;
import java.time.Instant;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String nickname,
        String displayName,
        Instant createdAt,
        Instant updatedAt,
        PlayerStatisticsResponse statistics) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getNickname(),
                player.getDisplayName(),
                player.getCreatedAt(),
                player.getUpdatedAt(),
                PlayerStatisticsResponse.from(player.getStatistics()));
    }
}
