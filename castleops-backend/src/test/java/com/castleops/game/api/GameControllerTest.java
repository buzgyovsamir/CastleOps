package com.castleops.game.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldRejectSecondWhiteMoveAndAllowBlackToReply() throws Exception {
        UUID white = UUID.randomUUID();
        UUID black = UUID.randomUUID();

        String gameUrl = createGame(white);
        joinGame(gameUrl, black);

        move(gameUrl, white, "e2", "e4")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("BLACK"))
                .andExpect(jsonPath(
                        "$.pieces[?(@.square == 'e4' && @.color == 'WHITE' && @.type == 'PAWN')]"
                ).value(hasSize(1)));

        // d2 → d4 е легален ход от тази позиция, ако белите са на ход.
        // Така проверяваме конкретно отказа за грешен ред.
        move(gameUrl, white, "d2", "d4")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mvc.perform(get(gameUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("BLACK"))
                .andExpect(jsonPath("$.pieces[?(@.square == 'd2')]").value(hasSize(1)))
                .andExpect(jsonPath("$.pieces[?(@.square == 'd4')]").isEmpty());

        move(gameUrl, black, "e7", "e5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("WHITE"))
                .andExpect(jsonPath(
                        "$.pieces[?(@.square == 'e5' && @.color == 'BLACK' && @.type == 'PAWN')]"
                ).value(hasSize(1)));
    }

    @Test
    void shouldReturnProblemDetailForInvalidSquare() throws Exception {
        UUID white = UUID.randomUUID();
        String gameUrl = createGame(white);
        joinGame(gameUrl, UUID.randomUUID());

        move(gameUrl, white, "e9", "e4")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        // След отказа белите все още могат да изиграят първия си ход.
        move(gameUrl, white, "e2", "e4")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("BLACK"));
    }

    @Test
    void shouldReturnNotFoundForUnknownGame() throws Exception {
        mvc.perform(get("/api/games/{gameId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String createGame(UUID white) throws Exception {
        var response = postJson("/api/games", playerBody(white))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, is(notNullValue(String.class))))
                .andExpect(jsonPath("$.whitePlayerId").value(white.toString()))
                .andExpect(jsonPath("$.status").value("WAITING_FOR_OPPONENT"))
                .andExpect(jsonPath("$.pieces").value(hasSize(32)))
                .andReturn()
                .getResponse();

        String gameUrl = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(gameUrl);
        return gameUrl;
    }

    private void joinGame(String gameUrl, UUID black) throws Exception {
        postJson(gameUrl + "/join", playerBody(black))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blackPlayerId").value(black.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentTurn").value("WHITE"));
    }

    private ResultActions move(String gameUrl, UUID playerId, String from, String to)
            throws Exception {
        String body = """
                {"playerId":"%s","from":"%s","to":"%s"}
                """.formatted(playerId, from, to);

        return postJson(gameUrl + "/moves", body);
    }

    private String playerBody(UUID playerId) {
        return """
                {"playerId":"%s"}
                """.formatted(playerId);
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}