package com.ra1n.top.api.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ra1n.top.api.support.AbstractPostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class PlayerApiIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository players;

    @BeforeEach
    void clearDatabase() {
        players.deleteAll();
    }

    @Test
    @DisplayName("a new player starts with empty statistics")
    void createsPlayerWithEmptyStatistics() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "shroud", "displayName": "Michael"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nickname").value("shroud"))
                .andExpect(content().json("""
                        {"statistics": {
                            "matches": 0, "wins": 0, "losses": 0, "draws": 0,
                            "winRate": 0.00, "currentWinStreak": 0, "currentLossStreak": 0,
                            "totalDamage": 0, "averageDamage": 0.00
                        }}
                        """));
    }

    @Test
    @DisplayName("nicknames are unique regardless of case")
    void rejectsDuplicateNickname() throws Exception {
        createPlayer("shroud");

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "SHROUD"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Nickname already taken"));
    }

    @Test
    @DisplayName("an invalid nickname is reported field by field")
    void rejectsInvalidNickname() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "no spaces allowed"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nickname").value(
                        "must be 3-32 characters long and contain only letters, digits, '_' or '-'"));
    }

    @Test
    @DisplayName("an unknown player yields 404")
    void returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/players/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Player not found"));
    }

    @Test
    @DisplayName("a player can be renamed")
    void updatesPlayer() throws Exception {
        String id = createPlayer("shroud");

        mockMvc.perform(put("/api/players/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname": "shr0ud", "displayName": "Mike"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("shr0ud"))
                .andExpect(jsonPath("$.displayName").value("Mike"));
    }

    @Test
    @DisplayName("deleting a player removes the statistics with it")
    void deletesPlayer() throws Exception {
        String id = createPlayer("shroud");

        mockMvc.perform(delete("/api/players/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/players/{id}", id)).andExpect(status().isNotFound());

        assertThat(players.count()).isZero();
    }

    @Test
    @DisplayName("reported matches add up into counters, streaks and averages")
    void recordsMatches() throws Exception {
        String id = createPlayer("shroud");

        recordMatch(id, "WIN", 100);
        recordMatch(id, "WIN", 200);
        recordMatch(id, "LOSS", 60);

        mockMvc.perform(get("/api/players/{id}/statistics", id))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "matches": 3, "wins": 2, "losses": 1, "draws": 0,
                            "winRate": 66.67, "currentWinStreak": 0, "currentLossStreak": 1,
                            "totalDamage": 360, "averageDamage": 120.00
                        }
                        """));

        // A draw ends the losing run without starting a winning one
        recordMatch(id, "DRAW", 40);

        mockMvc.perform(get("/api/players/{id}/statistics", id))
                .andExpect(content().json("""
                        {
                            "matches": 4, "draws": 1, "winRate": 50.00,
                            "currentWinStreak": 0, "currentLossStreak": 0,
                            "totalDamage": 400, "averageDamage": 100.00
                        }
                        """));
    }

    @Test
    @DisplayName("negative damage is rejected before it reaches the database")
    void rejectsNegativeDamage() throws Exception {
        String id = createPlayer("shroud");

        mockMvc.perform(post("/api/players/{id}/matches", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"result": "WIN", "damage": -5}
                                """))
                .andExpect(status().isBadRequest())
                // Pinned wording: the default validator message follows the JVM locale
                .andExpect(jsonPath("$.errors.damage").value("must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("players are listed page by page")
    void listsPlayers() throws Exception {
        createPlayer("alpha");
        createPlayer("bravo");
        createPlayer("charlie");

        mockMvc.perform(get("/api/players").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nickname").value("alpha"))
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    private String createPlayer(String nickname) throws Exception {
        String body = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \"%s\"}".formatted(nickname)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return body.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    private void recordMatch(String playerId, String result, int damage) throws Exception {
        mockMvc.perform(post("/api/players/{id}/matches", playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\": \"%s\", \"damage\": %d}".formatted(result, damage)))
                .andExpect(status().isOk());
    }
}
