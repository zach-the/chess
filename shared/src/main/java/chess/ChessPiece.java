package chess;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    
    private PieceType type;
    private ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.color = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
       return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        
        // Get the piece at the position
        ChessPiece piece = board.getPiece(myPosition);
        
        if (piece == null) {    // No piece at this position, return an empty list
            return moves;
        }
        
        // Determine possible moves based on piece type
        switch (piece.getPieceType()) {
            case PAWN:      // Implement pawn move logic
                addPawnMoves(board, myPosition, moves); 
                break;
            case ROOK:      // Implement rook move logic
                addRookMoves(board, myPosition, moves);
                break;
            case BISHOP:    // Implement bishop move logic
                addBishopMoves(board, myPosition, moves);
                break;
            case KNIGHT:    // Implement knight move logic
                addKnightMoves(board, myPosition, moves);
                break;
            case QUEEN:     // Implement queen move logic
                addQueenMoves(board, myPosition, moves);
                break;
            case KING:      // Implement king move logic
                addKingMoves(board, myPosition, moves);
                break;
            default:
                throw new IllegalStateException("Unexpected piece type: " + piece.getPieceType());
        }
        
        return moves;
    }
    
    // Methods for adding specific piece moves

    private boolean validateMoveAndStop(ChessBoard board, ChessPosition position, ChessPosition endPosition, List<ChessMove> moves) {
        if (board.getPiece(endPosition) == null){                                           // empty space
            moves.add(new ChessMove(position, endPosition, null));
            return false;
        } else if (board.getPiece(endPosition).getTeamColor() != this.getTeamColor()) {     // enemy piece
            moves.add(new ChessMove(position, endPosition, null));
            return true;
        } else {                                                                            // friendly piece
            return true;
        }
    }

    private void checkPawnPromotionMove(ChessBoard board, ChessPosition position, ChessPosition endPosition, List<ChessMove> moves) {
        // promotion cases below
        if ((endPosition.getRow() == 8) && (this.color == ChessGame.TeamColor.WHITE)) {
            moves.add(new ChessMove(position, endPosition, PieceType.KNIGHT));
            moves.add(new ChessMove(position, endPosition, PieceType.QUEEN));
            moves.add(new ChessMove(position, endPosition, PieceType.ROOK));
            moves.add(new ChessMove(position, endPosition, PieceType.BISHOP));
        } else if ((endPosition.getRow() == 1) && (this.color == ChessGame.TeamColor.BLACK)) {
            moves.add(new ChessMove(position, endPosition, PieceType.KNIGHT));
            moves.add(new ChessMove(position, endPosition, PieceType.QUEEN));
            moves.add(new ChessMove(position, endPosition, PieceType.ROOK));
            moves.add(new ChessMove(position, endPosition, PieceType.BISHOP));
        } else {
            moves.add(new ChessMove(position, endPosition, null)); // if not promotion case, then leave pawn as is
        }
    }

    private void addPawnMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();
        // Logic will be different for white vs black
        if (this.color == ChessGame.TeamColor.WHITE) {
            // normal move
            ChessPosition oneSpace = new ChessPosition(row + 1, col);
            if (board.getPiece(oneSpace) == null){
                checkPawnPromotionMove(board, position, oneSpace, moves);
                // first move condition
                if (row == 2) {
                    ChessPosition twoSpace = new ChessPosition(row + 2, col);
                    if (board.getPiece(twoSpace) == null) {
                        moves.add(new ChessMove(position, twoSpace, null));
                    }
                }
            }
            // diagonal attacks
            ChessPosition diagonalRight = new ChessPosition(row + 1, col + 1);
            ChessPosition diagonalLeft = new ChessPosition(row + 1, col - 1);
            if ((board.getPiece(diagonalRight) != null) && (board.getPiece(diagonalRight).getTeamColor() == ChessGame.TeamColor.BLACK)) {
                checkPawnPromotionMove(board, position, diagonalRight, moves);
            }
            if ((board.getPiece(diagonalLeft) != null) && (board.getPiece(diagonalLeft).getTeamColor() == ChessGame.TeamColor.BLACK)) {
                checkPawnPromotionMove(board, position, diagonalLeft, moves);
            }
        } else /* this.color == ChessGame.TeamColor.BLACK */ {
            // normal move
            ChessPosition oneSpace = new ChessPosition(row - 1, col);
            if (board.getPiece(oneSpace) == null){
                checkPawnPromotionMove(board, position, oneSpace, moves);
                // first move condition
                if (row == 7) {
                    ChessPosition twoSpace = new ChessPosition(row - 2, col);
                    if (board.getPiece(twoSpace) == null) {
                        moves.add(new ChessMove(position, twoSpace, null));
                    }
                }
            }
            // diagonal attacks
            ChessPosition diagonalRight = new ChessPosition(row - 1, col + 1);
            ChessPosition diagonalLeft = new ChessPosition(row - 1, col - 1);
            if ((board.getPiece(diagonalRight) != null) && (board.getPiece(diagonalRight).getTeamColor() == ChessGame.TeamColor.WHITE)) {
                checkPawnPromotionMove(board, position, diagonalRight, moves);
            }
            if ((board.getPiece(diagonalLeft) != null) && (board.getPiece(diagonalLeft).getTeamColor() == ChessGame.TeamColor.WHITE)) {
                checkPawnPromotionMove(board, position, diagonalLeft, moves);
            }
        }
    }

    private void addRookMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();

        // looking up
        if (row < 8) {
            for (int j = row + 1; j <= 8; j++) {
                ChessPosition endPosition = new ChessPosition(j, col);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking down
        if (row > 1) {
            for (int j = row - 1; j >= 1; j--) {
                ChessPosition endPosition = new ChessPosition(j, col);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking left
        if (col > 1) {
            for (int i = col - 1; i >= 1; i--) {
                ChessPosition endPosition = new ChessPosition(row, i);
                if (validateMoveAndStop(board, position, endPosition, moves)) {
                    break;
                }
            }
        }
        // looking right
        if (col < 8) {
            for (int i = col + 1; i <= 8; i++) {
                ChessPosition endPosition = new ChessPosition(row, i);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
    }
    
    private void addBishopMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();

        // looking up and right
        if (row < 8 && col < 8) {
            for (int k = 1; Math.max(row, col) + k <= 8; k++) {
                ChessPosition endPosition = new ChessPosition(row + k, col + k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking down and right
        // not sure if this for loop logic is right
        if (row > 1 && col < 8) {
            for (int k = 1; (row - k >= 1) && (col + k <= 8); k++) {
                ChessPosition endPosition = new ChessPosition(row - k, col + k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking up and left
        if (row < 8 && col > 1) {
            for (int k = 1; (row + k <= 8) && (col - k >= 1); k++) {
                ChessPosition endPosition = new ChessPosition(row + k, col - k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking down and left
        if (row > 1 && col > 1) {
            for (int k = 1; Math.min(row, col) - k >= 1; k++) {
                ChessPosition endPosition = new ChessPosition(row - k, col - k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
    }
    
    private void addKnightMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();
        int tmprow = -1;
        int tmpcol = -1;

        // up right
        tmprow = row + 2;
        tmpcol = col + 1;
        if (tmprow <= 8 && tmpcol <= 8) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // right up
        tmprow = row + 1;
        tmpcol = col + 2;
        if (tmprow <= 8 && tmpcol <= 8) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // down right
        tmprow = row - 2;
        tmpcol = col + 1;
        if (tmprow >= 1 && tmpcol <= 8) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // right down
        tmprow = row - 1;
        tmpcol = col + 2;
        if (tmprow >= 1 && tmpcol <= 8) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // up left
        tmprow = row + 2;
        tmpcol = col - 1;
        if (tmprow <= 8 && tmpcol >= 1) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // left up
        tmprow = row + 1;
        tmpcol = col - 2;
        if (tmprow <= 8 && tmpcol >= 1) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // down left
        tmprow = row - 2;
        tmpcol = col - 1;
        if (tmprow >= 1 && tmpcol >= 1) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
        // left down
        tmprow = row - 1;
        tmpcol = col - 2;
        if (tmprow >= 1 && tmpcol >= 1) {
            ChessPosition endPosition = new ChessPosition(tmprow, tmpcol);
            validateMoveAndStop(board, position, endPosition, moves);
        }
    }
    
    private void addQueenMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();

        // looking up
        if (row < 8) {
            for (int j = row + 1; j <= 8; j++) {
                ChessPosition endPosition = new ChessPosition(j, col);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking down
        if (row > 1) {
            for (int j = row - 1; j >= 1; j--) {
                ChessPosition endPosition = new ChessPosition(j, col);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking left
        if (col > 1) {
            for (int i = col - 1; i >= 1; i--) {
                ChessPosition endPosition = new ChessPosition(row, i);
                if (validateMoveAndStop(board, position, endPosition, moves)) {
                    break;
                }
            }
        }
        // looking right
        if (col < 8) {
            for (int i = col + 1; i <= 8; i++) {
                ChessPosition endPosition = new ChessPosition(row, i);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking up and right
        if (row < 8 && col < 8) {
            for (int k = 1; Math.max(row, col) + k <= 8; k++) {
                ChessPosition endPosition = new ChessPosition(row + k, col + k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking down and right
        // not sure if this for loop logic is right
        if (row > 1 && col < 8) {
            for (int k = 1; (row - k >= 1) && (col + k <= 8); k++) {
                ChessPosition endPosition = new ChessPosition(row - k, col + k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking up and left
        if (row < 8 && col > 1) {
            for (int k = 1; (row + k <= 8) && (col - k >= 1); k++) {
                ChessPosition endPosition = new ChessPosition(row + k, col - k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
        // looking down and left
        if (row > 1 && col > 1) {
            for (int k = 1; Math.min(row, col) - k >= 1; k++) {
                ChessPosition endPosition = new ChessPosition(row - k, col - k);
                if (validateMoveAndStop(board, position, endPosition, moves)) { break; }
            }
        }
    }
    
    private void addKingMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement king movement logic here
    }
    
}
