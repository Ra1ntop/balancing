package com.ra1n.top.api.player;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    boolean existsByNicknameIgnoreCase(String nickname);

    Optional<Player> findByNicknameIgnoreCase(String nickname);

    // Statistics are always part of the response, so fetch them in the same query
    @Override
    @EntityGraph(attributePaths = "statistics")
    Page<Player> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "statistics")
    Optional<Player> findById(UUID id);
}
