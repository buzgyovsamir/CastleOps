import { useEffect, useState } from 'react'
import JoinGame from './JoinGame'

export type Game = {
  gameId: string
  whitePlayerId: string
  blackPlayerId: string | null
  status: 'WAITING_FOR_OPPONENT' | 'IN_PROGRESS' | 'FINISHED'
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

export default function GameView({
  gameId,
  userId,
  onBack,
  getCsrfHeaders,
}: GameViewProps) {
  const [state, setState] = useState<GameViewState>({ status: 'loading' })

  useEffect(() => {
    const controller = new AbortController()

    async function loadGame() {
      try {
        const response = await fetch(`/api/games/${encodeURIComponent(gameId)}`, {
          signal: controller.signal,
        })

        if (!response.ok) {
          const message = response.status === 404
            ? 'Game not found.'
            : 'Unable to load the game.'
          throw new Error(message)
        }

        const game: Game = await response.json()

        if (!controller.signal.aborted) {
          setState({ status: 'ready', game })
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          const message = error instanceof Error ? error.message : 'Unable to load the game.'
          setState({ status: 'error', message })
        }
      }
    }

    void loadGame()
    return () => controller.abort()
  }, [gameId])

  let gameDetails

  if (state.status === 'ready') {
    const { game } = state

    if (userId === game.whitePlayerId) {
      gameDetails = (
        <>
          <p>Playing as White.</p>
          {!game.blackPlayerId && <p>Waiting for an opponent.</p>}
        </>
      )
    } else if (userId === game.blackPlayerId) {
      gameDetails = <p>Playing as Black.</p>
    } else if (game.status === 'WAITING_FOR_OPPONENT' && game.blackPlayerId === null) {
      gameDetails = (
        <>
          <p>Join this game to play as Black.</p>
          <JoinGame
            inviteGameId={game.gameId}
            getCsrfHeaders={getCsrfHeaders}
            onOpenGame={(_gameId, joinedGame) => {
              setState({ status: 'ready', game: joinedGame })
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