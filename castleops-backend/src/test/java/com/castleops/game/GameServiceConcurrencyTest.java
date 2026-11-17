package com.castleops.game;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.PieceColor;
import com.castleops.chess.model.Position;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceConcurrencyTest {

    private final GameService service = new GameService();

    @Test
    void shouldAllowOnlyOneOpponentToJoinConcurrently() throws Exception {
        GameSnapshot created = service.createGame(UUID.randomUUID());
        UUID gameId = created.gameId();

        UUID firstPlayerId = UUID.randomUUID();
        UUID secondPlayerId = UUID.randomUUID();

        List<Boolean> results = runConcurrently(
                () -> tryJoin(gameId, firstPlayerId),
                () -> tryJoin(gameId, secondPlayerId)
        );

        assertExactlyOneSucceeded(results);

        UUID successfulPlayerId = results.get(0)
                ? firstPlayerId
                : secondPlayerId;

        GameSnapshot snapshot = service.getGame(gameId);

        assertAll(
                () -> assertEquals(
                        successfulPlayerId,
                        snapshot.blackPlayerId().orElseThrow()
                ),
                () -> assertEquals(
                        created.whitePlayerId(),
                        snapshot.whitePlayerId()
                ),
                () -> assertEquals(
                        GameSessionStatus.IN_PROGRESS,
                        snapshot.status()
                )
        );
    }

    @Test
    void shouldAcceptOnlyOneConcurrentMoveFromSamePlayer() throws Exception {
        UUID whitePlayerId = UUID.randomUUID();
        UUID blackPlayerId = UUID.randomUUID();

        UUID gameId = service.createGame(whitePlayerId).gameId();
        service.joinGame(gameId, blackPlayerId);

        Move firstMove = move("e2", "e4");
        Move secondMove = move("d2", "d4");

        List<Boolean> results = runConcurrently(
                () -> tryMove(gameId, whitePlayerId, firstMove),
                () -> tryMove(gameId, whitePlayerId, secondMove)
        );

        assertExactlyOneSucceeded(results);

        Move acceptedMove = results.get(0) ? firstMove : secondMove;
        Move rejectedMove = results.get(0) ? secondMove : firstMove;

        GameSnapshot snapshot = service.getGame(gameId);

        assertAll(
                () -> assertEquals(
                        PieceColor.BLACK,
                        snapshot.currentTurn()
                ),
                () -> assertFalse(
                        snapshot.pieces().containsKey(acceptedMove.from())
                ),
                () -> assertTrue(
                        snapshot.pieces().containsKey(acceptedMove.to())
                ),
                () -> assertTrue(
                        snapshot.pieces().containsKey(rejectedMove.from())
                ),
                () -> assertFalse(
                        snapshot.pieces().containsKey(rejectedMove.to())
                ),
                () -> assertEquals(32, snapshot.pieces().size())
        );

        // The game must remain usable after the competing requests.
        GameSnapshot afterBlackMove = service.makeMove(
                gameId,
                blackPlayerId,
                move("e7", "e5")
        );

        assertEquals(PieceColor.WHITE, afterBlackMove.currentTurn());
    }

    private boolean tryJoin(UUID gameId, UUID playerId) {
        try {
            service.joinGame(gameId, playerId);
            return true;
        } catch (IllegalStateException expectedRejection) {
            return false;
        }
    }

    private boolean tryMove(UUID gameId, UUID playerId, Move move) {
        try {
            service.makeMove(gameId, playerId, move);
            return true;
        } catch (IllegalStateException expectedRejection) {
            return false;
        }
    }

    private List<Boolean> runConcurrently(
            Callable<Boolean> firstAction,
            Callable<Boolean> secondAction
    ) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<Boolean> firstResult = executor.submit(
                    () -> awaitStartAndRun(ready, start, firstAction)
            );
            Future<Boolean> secondResult = executor.submit(
                    () -> awaitStartAndRun(ready, start, secondAction)
            );

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "Both workers must be ready before starting"
            );

            start.countDown();

            return List.of(
                    firstResult.get(5, TimeUnit.SECONDS),
                    secondResult.get(5, TimeUnit.SECONDS)
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private boolean awaitStartAndRun(
            CountDownLatch ready,
            CountDownLatch start,
            Callable<Boolean> action
    ) throws Exception {
        ready.countDown();

        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "Timed out waiting for the start signal"
            );
        }

        return action.call();
    }

    private void assertExactlyOneSucceeded(List<Boolean> results) {
        long successfulActions = results.stream()
                .filter(Boolean::booleanValue)
                .count();

        assertEquals(
                1L,
                successfulActions,
                "Exactly one competing action must succeed"
        );
    }

    private Move move(String from, String to) {
        return new Move(
                Position.fromAlgebraic(from),
                Position.fromAlgebraic(to)
        );
    }
}