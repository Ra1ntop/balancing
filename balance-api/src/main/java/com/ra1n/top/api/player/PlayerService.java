package com.ra1n.top.api.player;

import com.ra1n.top.api.error.NicknameAlreadyUsedException;
import com.ra1n.top.api.error.PlayerNotFoundException;
import com.ra1n.top.api.player.dto.CreatePlayerRequest;
import com.ra1n.top.api.player.dto.PlayerResponse;
import com.ra1n.top.api.player.dto.PlayerStatisticsResponse;
import com.ra1n.top.api.player.dto.RecordMatchRequest;
import com.ra1n.top.api.player.dto.UpdatePlayerRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository players;
    private final PlayerStatisticsRepository statistics;

    public PlayerService(PlayerRepository players, PlayerStatisticsRepository statistics) {
        this.players = players;
        this.statistics = statistics;
    }

    @Transactional
    public PlayerResponse create(CreatePlayerRequest request) {
        if (players.existsByNicknameIgnoreCase(request.nickname())) {
            throw new NicknameAlreadyUsedException(request.nickname());
        }
        Player player = new Player(request.nickname(), request.displayName());
        return PlayerResponse.from(players.save(player));
    }

    public Page<PlayerResponse> findAll(Pageable pageable) {
        return players.findAll(pageable).map(PlayerResponse::from);
    }

    public PlayerResponse findById(UUID id) {
        return PlayerResponse.from(requirePlayer(id));
    }

    @Transactional
    public PlayerResponse update(UUID id, UpdatePlayerRequest request) {
        Player player = requirePlayer(id);
        boolean nicknameChanged = !player.getNickname().equalsIgnoreCase(request.nickname());
        if (nicknameChanged && players.existsByNicknameIgnoreCase(request.nickname())) {
            throw new NicknameAlreadyUsedException(request.nickname());
        }
        player.rename(request.nickname(), request.displayName());
        return PlayerResponse.from(player);
    }

    @Transactional
    public void delete(UUID id) {
        players.delete(requirePlayer(id));
    }

    public PlayerStatisticsResponse findStatistics(UUID id) {
        return PlayerStatisticsResponse.from(requirePlayer(id).getStatistics());
    }

    /** Folds a finished match into the player's counters and returns the fresh statistics. */
    @Transactional
    public PlayerStatisticsResponse recordMatch(UUID id, RecordMatchRequest request) {
        PlayerStatistics playerStatistics = statistics.findByPlayerIdForUpdate(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
        playerStatistics.record(request.result(), request.damage());
        return PlayerStatisticsResponse.from(playerStatistics);
    }

    private Player requirePlayer(UUID id) {
        return players.findById(id).orElseThrow(() -> new PlayerNotFoundException(id));
    }
}
