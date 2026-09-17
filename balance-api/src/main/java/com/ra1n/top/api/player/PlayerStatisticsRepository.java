package com.ra1n.top.api.player;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerStatisticsRepository extends JpaRepository<PlayerStatistics, UUID> {

    /**
     * Locks the statistics row for the duration of the transaction. Two matches reported for
     * the same player at the same time would otherwise read the same counters and one update
     * would be lost.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PlayerStatistics s where s.playerId = :playerId")
    Optional<PlayerStatistics> findByPlayerIdForUpdate(@Param("playerId") UUID playerId);
}
