package com.ra1n.top.api.player;

import com.ra1n.top.api.player.dto.CreatePlayerRequest;
import com.ra1n.top.api.player.dto.PlayerResponse;
import com.ra1n.top.api.player.dto.PlayerStatisticsResponse;
import com.ra1n.top.api.player.dto.RecordMatchRequest;
import com.ra1n.top.api.player.dto.UpdatePlayerRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService players;

    public PlayerController(PlayerService players) {
        this.players = players;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> create(@Valid @RequestBody CreatePlayerRequest request) {
        PlayerResponse created = players.create(request);
        return ResponseEntity.created(URI.create("/api/players/" + created.id())).body(created);
    }

    @GetMapping
    public PagedModel<PlayerResponse> findAll(
            @PageableDefault(size = 20, sort = "nickname", direction = Sort.Direction.ASC) Pageable pageable) {
        return new PagedModel<>(players.findAll(pageable));
    }

    @GetMapping("/{id}")
    public PlayerResponse findById(@PathVariable UUID id) {
        return players.findById(id);
    }

    @PutMapping("/{id}")
    public PlayerResponse update(@PathVariable UUID id, @Valid @RequestBody UpdatePlayerRequest request) {
        return players.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        players.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/statistics")
    public PlayerStatisticsResponse findStatistics(@PathVariable UUID id) {
        return players.findStatistics(id);
    }

    /** Reports a finished match; the server recalculates counters, streaks and averages. */
    @PostMapping("/{id}/matches")
    public PlayerStatisticsResponse recordMatch(@PathVariable UUID id,
            @Valid @RequestBody RecordMatchRequest request) {
        return players.recordMatch(id, request);
    }
}
