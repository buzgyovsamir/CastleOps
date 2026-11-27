import { useState, type FormEvent } from 'react'
import type { Game } from './GameView'

type JoinGameProps = {
  getCsrfHeaders: () => Promise<Record<string, string>>
  onOpenGame: (gameId: string, game: Game) => void
  inviteGameId?: string
}

export default function JoinGame({
  getCsrfHeaders,
  onOpenGame,
  inviteGameId,
}: JoinGameProps) {
  const [gameId, setGameId] = useState('')
  const [error, setError] = useState('')
  const [isJoining, setIsJoining] = useState(false)

  async function handleJoinGame(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (isJoining) return

    const id = (inviteGameId ?? gameId).trim()
    if (!id) {
      setError('Enter a game ID.')
      return
    }

    setError('')
    setIsJoining(true)

    try {
      const headers = await getCsrfHeaders()
      const response = await fetch(`/api/games/${encodeURIComponent(id)}/join`, {
        method: 'POST',
        headers,
      })

      if (response.status === 401) {
        setError('Your session has expired. Refresh the page to sign in again.')
        return
      }

      if (!response.ok) {
        const problem = await response.json().catch(() => null)
        setError(problem?.detail ?? 'Unable to join the game. Check the game ID and try again.')
        return
      }

      const game: Game = await response.json()
      onOpenGame(game.gameId, game)
    } catch {
      setError('Unable to confirm whether you joined the game.')
    } finally {
      setIsJoining(false)
    }
  }

  return (
    <section>
      {!inviteGameId && <h2>Join a game</h2>}

      <form onSubmit={handleJoinGame}>
        {!inviteGameId && (
          <>
            <label htmlFor="join-game-id">Game ID</label>
            <input
              id="join-game-id"
              value={gameId}
              onChange={(event) => setGameId(event.target.value)}
              disabled={isJoining}
              required
            />
          </>
        )}

        <button type="submit" disabled={isJoining}>
          {isJoining ? 'Joining…' : 'Join game'}
        </button>
      </form>

      {error && <p role="alert">{error}</p>}
    </section>
  )
}