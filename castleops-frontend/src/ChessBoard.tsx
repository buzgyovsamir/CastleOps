import './ChessBoard.css'

export type PieceColor = 'WHITE' | 'BLACK'

type PieceType = 'KING' | 'QUEEN' | 'ROOK' | 'BISHOP' | 'KNIGHT' | 'PAWN'

export type OccupiedSquare = {
  square: string
  type: PieceType
  color: PieceColor
}

type ChessBoardProps = {
  pieces: OccupiedSquare[]
  orientation: PieceColor
  checkedKingColor: PieceColor | null
  selectedSquare: string | null
  disabled: boolean
  onSquareClick: (square: string) => void
  legalTargets: ReadonlySet<string>
}

const symbols: Record<PieceColor, Record<PieceType, string>> = {
  WHITE: { KING: '♔', QUEEN: '♕', ROOK: '♖', BISHOP: '♗', KNIGHT: '♘', PAWN: '♙' },
  BLACK: { KING: '♚', QUEEN: '♛', ROOK: '♜', BISHOP: '♝', KNIGHT: '♞', PAWN: '♟' },
}

export default function ChessBoard({
  pieces,
  orientation,
  checkedKingColor,
  selectedSquare,
  disabled,
  onSquareClick,
  legalTargets,
}: ChessBoardProps) {
  const whiteAtBottom = orientation === 'WHITE'
  const files = whiteAtBottom ? 'abcdefgh' : 'hgfedcba'
  const ranks = whiteAtBottom ? [8, 7, 6, 5, 4, 3, 2, 1] : [1, 2, 3, 4, 5, 6, 7, 8]
  const piecesBySquare = new Map(pieces.map(piece => [piece.square, piece]))

  return (
    <div className="chess-board" role="group" aria-label="Chessboard">
      {ranks.map((rank, row) =>
        Array.from(files, (file, column) => {
          const square = `${file}${rank}`
          const piece = piecesBySquare.get(square)
          const isLegalTarget = legalTargets.has(square)
          const isKingInCheck = piece?.type === 'KING' && piece.color === checkedKingColor
          const lightSquare = (row + column) % 2 === 0

          const description = piece
            ? `${square}: ${piece.color.toLowerCase()} ${piece.type.toLowerCase()}`
            : `${square}: empty`

          const squareLabel = isKingInCheck ? `${description}, in check` : description

          return (
            <button
              key={square}
              type="button"
              className={`chess-square ${lightSquare ? 'light' : 'dark'}`}
              aria-label={isLegalTarget ? `${squareLabel}, legal destination` : squareLabel}
              title={squareLabel}
              data-in-check={isKingInCheck}
              aria-pressed={selectedSquare === square}
              disabled={disabled}
              onClick={() => onSquareClick(square)}
            >
              {isLegalTarget && (
                <span
                  aria-hidden="true"
                  className={piece ? 'legal-capture-marker' : 'legal-move-marker'}
                />
              )}

              {column === 0 && <span className="rank-label">{rank}</span>}
              {row === 7 && <span className="file-label">{file}</span>}

              {piece && (
                <span className="chess-piece" aria-hidden="true">
                  {symbols[piece.color][piece.type]}{'\uFE0E'}
                </span>
              )}
            </button>
          )
        })
      )}
    </div>
  )
}