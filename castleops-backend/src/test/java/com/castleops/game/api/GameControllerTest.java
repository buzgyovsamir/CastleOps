package com.castleops.game.api;

import com.castleops.account.CurrentAccountService;
import com.castleops.account.RegisteredUser;
import com.castleops.account.RegistrationService;
import com.castleops.game.GameService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationService registrationService;

    private GameService gameService;
    private CurrentAccountService currentAccountService;

    @Test
    void shouldRejectSecondWhiteMoveAndAllowBlackToReply() throws Exception {
        String password = "castleops game test";
        RegisteredUser white = registrationService.register("GameFlowWhite", password);
        RegisteredUser black = registrationService.register("GameFlowBlack", password);

        MockHttpSession whiteSession = login(white.username(), password);
        MockHttpSession blackSession = login(black.username(), password);

        MvcResult created = mockMvc.perform(post("/api/games")
                        .session(whiteSession)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.whitePlayerId").value(white.id().toString()))
                .andReturn();

        String location = created.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location).isNotNull().startsWith("/api/games/");
        UUID gameId = UUID.fromString(location.substring("/api/games/".length()));

        mockMvc.perform(post("/api/games/{gameId}/join", gameId)
                        .session(blackSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whitePlayerId").value(white.id().toString()))
                .andExpect(jsonPath("$.blackPlayerId").value(black.id().toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentTurn").value("WHITE"));

        mockMvc.perform(get("/api/auth/me").session(whiteSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(white.username()));

        move(gameId, whiteSession, "e2", "e4")
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("BLACK"));

        move(gameId, whiteSession, "d2", "d4")
                .andExpect(status().isConflict());

        move(gameId, blackSession, "e7", "e5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("WHITE"))
                .andExpect(jsonPath("$.pieces[?(@.square == 'd2' && @.type == 'PAWN' && @.color == 'WHITE')]").isNotEmpty())
                .andExpect(jsonPath("$.pieces[?(@.square == 'd4')]").isEmpty());
    }

    private ResultActions move(UUID gameId, MockHttpSession session,
                               String from, String to) throws Exception {
        return mockMvc.perform(post("/api/games/{gameId}/moves", gameId)
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"from":"%s","to":"%s"}
                    """.formatted(from, to)));
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    @Test
    void shouldReturnProblemDetailForInvalidSquare() throws Exception {
        String password = "castleops game test";
        var white = registrationService.register("InvalidSquareWhite", password);
        var black = registrationService.register("InvalidSquareBlack", password);

        MockHttpSession whiteSession = login(white.username(), password);
        MockHttpSession blackSession = login(black.username(), password);

        String gameUrl = createGame(whiteSession);
        UUID gameId = UUID.fromString(gameUrl.substring(gameUrl.lastIndexOf('/') + 1));

        mockMvc.perform(post(gameUrl + "/join")
                        .session(blackSession)
                        .with(csrf()))
                .andExpect(status().isOk());

        move(gameId, whiteSession, "e9", "e4")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        move(gameId, whiteSession, "e2", "e4")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentTurn").value("BLACK"));
    }
    @Test
    void shouldReturnNotFoundForUnknownGame() throws Exception {
        mockMvc.perform(get("/api/games/{gameId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String createGame(MockHttpSession whiteSession) throws Exception {
        var response = mockMvc.perform(post("/api/games")
                        .session(whiteSession)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        String gameUrl = response.getHeader(HttpHeaders.LOCATION);
        assertThat(gameUrl).isNotNull();
        return gameUrl;
    }

    @Test
    void shouldAllowAnonymousSpectatorToReadGameButRejectGameCommands() throws Exception {
        String password = "castleops spectator test";
        var white = registrationService.register("PublicGameWhite", password);
        var black = registrationService.register("PublicGameBlack", password);

        MockHttpSession whiteSession = login(white.username(), password);
        MockHttpSession blackSession = login(black.username(), password);

        mockMvc.perform(post("/api/games").with(csrf()))
                .andExpect(status().isUnauthorized());

        String gameUrl = createGame(whiteSession);
        UUID gameId = UUID.fromString(gameUrl.substring("/api/games/".length()));

        // Черното място още е свободно, когато анонимният зрител опитва да влезе.
        mockMvc.perform(post(gameUrl + "/join").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post(gameUrl + "/join")
                        .session(blackSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(post(gameUrl + "/moves")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"from":"e2","to":"e4"}
                            """))
                .andExpect(status().isUnauthorized());

        // Същият ход е легален и белите все още могат да го изиграят.
        move(gameId, whiteSession, "e2", "e4")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2));

        // GET без логната сесия връща актуалната дъска на зрителя.
        mockMvc.perform(get(gameUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.currentTurn").value("BLACK"))
                .andExpect(jsonPath(
                        "$.pieces[?(@.square == 'e4' && @.type == 'PAWN' && @.color == 'WHITE')]"
                ).isNotEmpty());
    }

    @Test
    void shouldAllowSignedInSpectatorToWatchButRejectMoveOnBehalfOfBlack() throws Exception {
        String password = "castleops spectator test";
        var white = registrationService.register("SpectatorGameWhite", password);
        var black = registrationService.register("SpectatorGameBlack", password);
        var spectator = registrationService.register("SpectatorGameViewer", password);

        MockHttpSession whiteSession = login(white.username(), password);
        MockHttpSession blackSession = login(black.username(), password);
        MockHttpSession spectatorSession = login(spectator.username(), password);

        String gameUrl = createGame(whiteSession);
        UUID gameId = UUID.fromString(gameUrl.substring("/api/games/".length()));

        mockMvc.perform(post(gameUrl + "/join")
                        .session(blackSession)
                        .with(csrf()))
                .andExpect(status().isOk());

        move(gameId, whiteSession, "e2", "e4")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.currentTurn").value("BLACK"));

        move(gameId, spectatorSession, "e7", "e5")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("The player is not a participant in this game"));

        mockMvc.perform(get(gameUrl).session(spectatorSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.currentTurn").value("BLACK"))
                .andExpect(jsonPath(
                        "$.pieces[?(@.square == 'e7' && @.type == 'PAWN' && @.color == 'BLACK')]"
                ).isNotEmpty())
                .andExpect(jsonPath("$.pieces[?(@.square == 'e5')]").isEmpty());

        move(gameId, blackSession, "e7", "e5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.currentTurn").value("WHITE"));
    }

    private void joinGame(String gameUrl, UUID black) throws Exception {
        postJson(gameUrl + "/join", playerBody(black))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blackPlayerId").value(black.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentTurn").value("WHITE"))
                .andExpect(jsonPath("$.version").value(1));

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
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}