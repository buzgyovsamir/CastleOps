package com.castleops.game.websocket;

import com.castleops.account.RegistrationService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.SimpleMessageConverter;
import java.nio.charset.StandardCharsets;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GameWebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SimpMessagingTemplate broker;

    @Autowired
    RegistrationService registrationService;

    @Test
    void shouldBroadcastAcceptedUpdatesToBothSubscribers() throws Exception {
        String password = "castleops websocket test";
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var white = registrationService.register("WsWhite" + suffix, password);
        var black = registrationService.register("WsBlack" + suffix, password);

        MockHttpSession whiteSession = login(white.username(), password);
        MockHttpSession blackSession = login(black.username(), password);

        String gameUrl = mockMvc.perform(post("/api/games")
                        .session(whiteSession)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(gameUrl);

        String gameId = gameUrl.substring(gameUrl.lastIndexOf('/') + 1);
        String topic = "/topic/games/" + gameId;

        var client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new SimpleMessageConverter());

        try {
            var first = subscribe(client, topic);
            var second = subscribe(client, topic);
            awaitSubscriptions(topic, first, second);

            String joined = mockMvc.perform(post(gameUrl + "/join")
                            .session(blackSession)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertUpdate(joined, 1, first, second);

            String whiteMoved = move(gameUrl, whiteSession, "e2", "e4")
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertUpdate(whiteMoved, 2, first, second);

            move(gameUrl, whiteSession, "d2", "d4")
                    .andExpect(status().isConflict());

            String blackMoved = move(gameUrl, blackSession, "e7", "e5")
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertUpdate(blackMoved, 3, first, second);
        } finally {
            client.stop();
        }
    }

    private ResultActions move(String gameUrl, MockHttpSession session,
                               String from, String to) throws Exception {
        return mockMvc.perform(post(gameUrl + "/moves")
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

    private BlockingQueue<String> subscribe(WebSocketStompClient client, String topic)
            throws Exception {
        var messages = new LinkedBlockingQueue<String>();
        var session = client.connectAsync(
                "ws://localhost:" + port + "/ws",
                new StompSessionHandlerAdapter() {}
        ).get(5, SECONDS);

        session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                byte[] bytes = (byte[]) payload;
                messages.add(new String(bytes, StandardCharsets.UTF_8));
            }
        });

        return messages;
    }

    private void awaitSubscriptions(
            String topic, BlockingQueue<String> first, BlockingQueue<String> second
    ) throws InterruptedException {
        String probe = "subscription-ready";
        boolean firstReady = false;
        boolean secondReady = false;
        long deadline = System.nanoTime() + SECONDS.toNanos(5);

        while ((!firstReady || !secondReady) && System.nanoTime() < deadline) {
            broker.convertAndSend(topic, probe);
            if (!firstReady) firstReady = probe.equals(first.poll(100, MILLISECONDS));
            if (!secondReady) secondReady = probe.equals(second.poll(100, MILLISECONDS));
        }

        assertTrue(firstReady && secondReady, "Both clients must receive the probe");
    }

    private void assertUpdate(
            String restJson, int expectedVersion,
            BlockingQueue<String> first, BlockingQueue<String> second
    ) throws InterruptedException {
        int version = JsonPath.read(restJson, "$.version");
        assertEquals(expectedVersion, version);

        Object expectedGame = JsonPath.parse(restJson).json();

        for (var inbox : java.util.List.of(first, second)) {
            String message;
            do {
                message = inbox.poll(5, SECONDS);
                assertNotNull(message, "Missing WebSocket update for version " + version);
            } while ("subscription-ready".equals(message));

            Object receivedGame = JsonPath.parse(message).json();
            assertEquals(expectedGame, receivedGame);
        }
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String playerBody(UUID playerId) {
        return """
                {"playerId":"%s"}
                """.formatted(playerId);
    }

    private String moveBody(UUID playerId, String from, String to) {
        return """
                {"playerId":"%s","from":"%s","to":"%s"}
                """.formatted(playerId, from, to);
    }
}