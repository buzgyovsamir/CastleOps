package com.castleops.game.websocket;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GameWebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SimpMessagingTemplate broker;

    @Test
    void shouldBroadcastAcceptedUpdatesToBothSubscribers() throws Exception {
        UUID white = UUID.randomUUID();
        UUID black = UUID.randomUUID();

        String gameUrl = postJson("/api/games", playerBody(white))
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

            String joined = postJson(gameUrl + "/join", playerBody(black))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertUpdate(joined, 1, first, second);

            String whiteMoved = postJson(gameUrl + "/moves", moveBody(white, "e2", "e4"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertUpdate(whiteMoved, 2, first, second);

            postJson(gameUrl + "/moves", moveBody(white, "d2", "d4"))
                    .andExpect(status().isConflict());

            String blackMoved = postJson(gameUrl + "/moves", moveBody(black, "e7", "e5"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // The next update on each connection must be the accepted black move.
            assertUpdate(blackMoved, 3, first, second);
        } finally {
            client.stop();
        }
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
        return mvc.perform(post(url)
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