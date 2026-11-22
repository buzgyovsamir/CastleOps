import { useState } from 'react'
import ChessBoard, { type PieceColor } from './ChessBoard'
import type { Game } from './GameView'

type PlayableBoardProps = {
  game: Game
  playerColor: PieceColor
  getCsrfHeaders: () => Promise<Record<string, string>>
  onGameUpdate: (game: Game) => void
}

export default function PlayableBoard({
  game,
  playerColor,
  getCsrfHeaders,
  onGameUpdate,
}: PlayableBoardProps) {
  const [selectedSquare, setSelectedSquare] = useState<string | null>(null)
  const [promotionType, setPromotionType] = useState('QUEEN')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')

  const canMove = game.status === 'IN_PROGRESS' && game.currentTurn === playerColor
  const selectedPiece = game.pieces.find(piece => piece.square === selectedSquare)
  const promotionRank = playerColor === 'WHITE' ? '8' : '1'
  const rankBeforePromotion = playerColor === 'WHITE' ? '7' : '2'
  const canPromote =
    selectedPiece?.type === 'PAWN' && selectedPiece.square[1] === rankBeforePromotion

  async function handleSquareClick(square: string) {
    if (!canMove || isSubmitting) return

    setError('')

    if (square === selectedSquare) {
      setSelectedSquare(null)
      return
    }

    const clickedPiece = game.pieces.find(piece => piece.square === square)

    if (clickedPiece?.color === playerColor) {
      setSelectedSquare(square)
      setPromotionType('QUEEN')
      return
    }

    if (!selectedSquare) return

    const isPromotion = canPromote && square[1] === promotionRank
    setIsSubmitting(true)

    try {
      const headers = await getCsrfHeaders()
      const response = await fetch(`/api/games/${game.gameId}/moves`, {
        method: 'POST',
        headers: { ...headers, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          from: selectedSquare,
          to: square,
          promotionType: isPromotion ? promotionType : undefined,
        }),
      })

      if (!response.ok) {
        const problem = await response.json().catch(() => null)
        setError(problem?.detail ?? 'Unable to make this move.')
        return
      }

      const updatedGame: Game = await response.json()
      setSelectedSquare(null)
      onGameUpdate(updatedGame)
    } catch {
      setError('Could not confirm the move. Refresh the game to check the position.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <>
      {canPromote && (
        <label>
          Promote to{' '}
          <select
            value={promotionType}
            disabled={isSubmitting}
            onChange={event => setPromotionType(event.target.value)}
          >
            <option value="QUEEN">Queen</option>
            <option value="ROOK">Rook</option>
            <option value="BISHOP">Bishop</option>
            <option value="KNIGHT">Knight</option>
          </select>
        </label>
      )}

      <ChessBoard
        pieces={game.pieces}
        orientation={playerColor}
        checkedKingColor={game.status === 'IN_PROGRESS' && game.inCheck ? game.currentTurn : null}
        selectedSquare={selectedSquare}
        disabled={!canMove || isSubmitting}
        onSquareClick={handleSquareClick}
      />

      {error && <p role="alert">{error}</p>}
    </>
  )
}