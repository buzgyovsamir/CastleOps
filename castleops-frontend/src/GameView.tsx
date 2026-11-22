import { useEffect, useState } from 'react'
import JoinGame from './JoinGame'
import { Client } from '@stomp/stompjs'
import ChessBoard, { type OccupiedSquare, type PieceColor } from './ChessBoard'
import PlayableBoard from './PlayableBoard'

const endReasonLabels = {
  CHECKMATE: 'checkmate',
  STALEMATE: 'stalemate',
  INSUFFICIENT_MATERIAL: 'insufficient material',
  THREEFOLD_REPETITION: 'threefold repetition',
  FIVEFOLD_REPETITION: 'fivefold repetition',
  FIFTY_MOVE_RULE: 'the fifty-move rule',
  SEVENTY_FIVE_MOVE_RULE: 'the seventy-five-move rule',
  DRAW_AGREEMENT: 'agreement',
  RESIGNATION: 'resignation',
  RESIGNATION_WITHOUT_MATING_MATERIAL: 'resignation without mating material',
} as const

type GameResult = {
  reason: keyof typeof endReasonLabels
  winner: PieceColor | null
}

function formatGameResult(result: GameResult): string {
  const reason = endReasonLabels[result.reason]

  if (result.winner === null) {
    return `Draw by ${reason}.`
  }

  const winner = result.winner === 'WHITE' ? 'White' : 'Black'
  return `${winner} wins by ${reason}.`
}

export type Game = {
  gameId: string
  whitePlayerId: string
  blackPlayerId: string | null
  status: 'WAITING_FOR_OPPONENT' | 'IN_PROGRESS' | 'FINISHED'
  version: number
  currentTurn: PieceColor
  pieces: OccupiedSquare[]
  result: GameResult | null
}
type GameViewProps = {
  gameId: string
  userId: string
  onBack: () => void
  getCsrfHeaders: () => Promise<Record<string, string>>
}



type GameViewState =
  | { status: 'loading' }
  | { status: 'ready'; game: Game }
  | { status: 'error'; message: string }

function applyGameUpdate(previous: GameViewState, game: Game): GameViewState {
  if (previous.status === 'ready' && previous.game.version >= game.version) {
    return previous
  }

  return { status: 'ready', game }
}

export default function GameView({
  gameId,
  userId,
  onBack,
  getCsrfHeaders,
}: GameViewProps) {
  const [state, setState] = useState<GameViewState>({ status: 'loading' })

  useEffect(() => {
    const controller = new AbortController()

    function receiveGame(game: Game) {
      if (controller.signal.aborted || game.gameId !== gameId) return

      setState(previous => applyGameUpdate(previous, game))
    }

    async function loadGame() {
      try {
        const response = await fetch(`/api/games/${encodeURIComponent(gameId)}`, {
          signal: controller.signal,
        })

        if (!response.ok) {
          throw new Error(
            response.status === 404 ? 'Game not found.' : 'Unable to load the game.'
          )
        }

        const game: Game = await response.json()
        receiveGame(game)
      } catch (error) {
        if (controller.signal.aborted) return

        const message = error instanceof Error ? error.message : 'Unable to load the game.'
        setState(previous =>
          previous.status === 'ready' ? previous : { status: 'error', message }
        )
      }
    }

    const socketUrl = new URL('/ws', window.location.href)
    socketUrl.protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'

    const client = new Client({
      brokerURL: socketUrl.href,
      reconnectDelay: 3000,
      onConnect: () => {
        if (controller.signal.aborted) return

        client.subscribe(`/topic/games/${gameId}`, message => {
          const game: Game = JSON.parse(message.body)
          receiveGame(game)
        })

        void loadGame()
      },
    })

    void loadGame()
    client.activate()

    return () => {
      controller.abort()
      void client.deactivate()
    }
  }, [gameId])

  let gameDetails

  if (state.status === 'ready') {
    const { game } = state
    const isWhite = userId === game.whitePlayerId
    const isBlack = userId === game.blackPlayerId

    if (isWhite || isBlack) {
      const playerColor = isWhite ? 'WHITE' : 'BLACK'
      const isMyTurn = game.currentTurn === playerColor

      gameDetails = (
        <>
          <p>Playing as {isWhite ? 'White' : 'Black'}.</p>
          <PlayableBoard
            key={game.version}
            game={game}
            playerColor={playerColor}
            getCsrfHeaders={getCsrfHeaders}
            onGameUpdate={updatedGame =>
              setState(previous => applyGameUpdate(previous, updatedGame))
            }
          />

          {game.status === 'WAITING_FOR_OPPONENT' && <p>Waiting for an opponent.</p>}

          {game.status === 'IN_PROGRESS' && (
            <p>{isMyTurn ? 'Your turn.' : "Opponent's turn."}</p>
          )}

          {game.status === 'FINISHED' && (
            <p role="status">
              {game.result ? formatGameResult(game.result) : 'Game finished.'}
            </p>
          )}
        </>
      )
    } else if (game.status === 'WAITING_FOR_OPPONENT' && game.blackPlayerId === null) {
      gameDetails = (
        <>
          <p>Join this game to play as Black.</p>
          <JoinGame
            inviteGameId={game.gameId}
            getCsrfHeaders={getCsrfHeaders}
            onOpenGame={(_gameId, joinedGame) => {
              setState(previous => applyGameUpdate(previous, joinedGame))
            }}
          />
        </>
      )
    } else {
      gameDetails = <p>You are not a player in this game.</p>
    }
  }

  return (
    <section>
      <h2>Game</h2>
      <p>Game ID: <code>{gameId}</code></p>

      {state.status === 'loading' && <p>Loading game…</p>}
      {state.status === 'error' && <p role="alert">{state.message}</p>}
      {gameDetails}

      <button type="button" onClick={onBack}>Back to lobby</button>
    </section>
  )
}