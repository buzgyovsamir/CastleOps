import { useState } from 'react'

type CreateGameProps = {
  getCsrfHeaders: () => Promise<Record<string, string>>
  onOpenGame: (gameId: string) => void
}

export default function CreateGame({ getCsrfHeaders, onOpenGame }: CreateGameProps) {
  const [error, setError] = useState('')
  const [isCreating, setIsCreating] = useState(false)

  async function handleCreateGame() {
    if (isCreating) return

    setIsCreating(true)
    setError('')

    try {
      const headers = await getCsrfHeaders()
      const response = await fetch('/api/games', {
        method: 'POST',
        headers,
      })

      if (response.status === 401) {
        setError('Your session has expired. Refresh the page to sign in again.')
        return
      }

      if (response.status !== 201) {
        setError('Unable to create the game. Please try again.')
        return
      }

      const game = await response.json()
      onOpenGame(game.gameId)
    } catch {
      setError('Unable to confirm whether the game was created.')
    } finally {
      setIsCreating(false)
    }
  }

  return (
    <section>
      <h2>Play chess</h2>

      <button type="button" onClick={handleCreateGame} disabled={isCreating}>
                {isCreating ? 'Creating game…' : 'Create game'}
              </button>

      {error && <p role="alert">{error}</p>}
    </section>
  )
}