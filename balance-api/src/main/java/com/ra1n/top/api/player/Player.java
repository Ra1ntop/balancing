package com.ra1n.top.api.player;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 32)
    private String nickname;

    @Column(name = "display_name", length = 64)
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Statistics share the player's primary key, so the pair is strictly 1:1 and the row
     * cannot outlive its player.
     */
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, optional = false,
            fetch = FetchType.LAZY)
    private PlayerStatistics statistics;

    protected Player() {
        // for JPA
    }

    public Player(String nickname, String displayName) {
        this.nickname = nickname;
        this.displayName = displayName;
        this.statistics = new PlayerStatistics(this);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void rename(String nickname, String displayName) {
        this.nickname = nickname;
        this.displayName = displayName;
    }

    public UUID getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public PlayerStatistics getStatistics() {
        return statistics;
    }
}
