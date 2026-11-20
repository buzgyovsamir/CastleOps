package com.castleops.game.api;

import com.castleops.chess.model.Move;
import com.castleops.chess.model.Position;
import com.castleops.game.GameService;
import com.castleops.game.GameSnapshot;
import com.castleops.game.api.dto.CreateGameRequest;
import com.castleops.game.api.dto.GameResponse;
import com.castleops.game.api.dto.JoinGameRequest;
import com.castleops.game.api.dto.MakeMoveRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(
            GameService gameService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<GameResponse> createGame(
            @Valid @RequestBody CreateGameRequest request
    ) {
        GameSnapshot snapshot = gameService.createGame(
                request.playerId()
        );

        URI location = URI.create("/api/games/" + snapshot.gameId());

        return ResponseEntity.created(location)
                .body(GameResponse.from(snapshot));
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(
            @PathVariable("gameId") UUID gameId
    ) {
        return GameResponse.from(gameService.getGame(gameId));
    }

    @PostMapping("/{gameId}/join")
    public GameResponse joinGame(
            @PathVariable("gameId") UUID gameId,
            @Valid @RequestBody JoinGameRequest request
    ) {
        GameSnapshot snapshot = gameService.joinGame(
                gameId,
                request.playerId()
        );

        return publishGameUpdate(snapshot);
    }

    @PostMapping("/{gameId}/moves")
    public GameResponse makeMove(
            @PathVariable("gameId") UUID gameId,
            @Valid @RequestBody MakeMoveRequest request
    ) {
        Move move = new Move(
                Position.fromAlgebraic(request.from()),
                Position.fromAlgebraic(request.to()),
                request.promotionType()
        );

        GameSnapshot snapshot = gameService.makeMove(
                gameId,
                request.playerId(),
                move
        );

        return publishGameUpdate(snapshot);
    }

    private GameResponse publishGameUpdate(GameSnapshot snapshot) {
        GameResponse response = GameResponse.from(snapshot);
        String destination = "/topic/games/" + response.gameId();

        messagingTemplate.convertAndSend(destination, response);

        return response;
    }
}