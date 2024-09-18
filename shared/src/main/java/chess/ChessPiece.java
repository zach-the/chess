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
    
    // Example methods for adding specific piece moves

    private boolean validateMove(ChessBoard board, ChessPosition position, ChessPosition endPosition, List<ChessMove> moves) {
        if (board.getPiece(endPosition) != null) { // if there's a piece where we want to go
            if (this.color != board.getPiece(endPosition).getTeamColor()){
                moves.add(new ChessMove(position, endPosition, this.type));  // opposite team's pieces can be captured
                System.out.println("Enemy Piece");
                return true;
            }
            System.out.println("Friendly Piece");
            return false; // if it's the same team then we can't go there
        } else {
            moves.add(new ChessMove(position, endPosition, this.type));
            System.out.println("Empty Space");
            return true;
        }
    }

    private void addRookMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        int row = position.getRow();
        int col = position.getColumn();
        System.out.println("\nTHE BOARD:\n" + board.toString());
        // looking up
        System.out.println("\nLOOKING UP");
        if (row < 7) {
            for (int j = row + 1; j < 8; j++) {
                if (validateMove(board, position, new ChessPosition(col, j), moves)) {
                    System.out.println("column (x): " + col);
                    System.out.println("row (y): " + j);
                }
            }
        }
        // looking down
        System.out.println("\nLOOKING DOWN");
        if (row > 0) {
            for (int j = row - 1; j >= 0; j--) {
                if (validateMove(board, position, new ChessPosition(col, j), moves)) {
                    System.out.println("column (x): " + col);
                    System.out.println("row (y): " + j);
                }
            }
        }
        // looking left
        System.out.println("\nLOOKING LEFT");
        if (col > 0) {
            for (int i = col - 1; i >= 0; i--) {
                if (validateMove(board, position, new ChessPosition(i, row), moves)) {
                    System.out.println("column (x): " + i);
                    System.out.println("row (y): " + row);
                }
            }
        }
        // looking right
        System.out.println("\nLOOKING RIGHT");
        if (col < 7) {
            for (int i = col + 1; i < 8; i++) {
                if (validateMove(board, position, new ChessPosition(i, row), moves)) {
                    System.out.println("column (x): " + i);
                    System.out.println("row (y): " + row);
                }
            }
        }

    }

    private void addPawnMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement pawn movement logic here
    }
    
    private void addBishopMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement bishop movement logic here
    }
    
    private void addKnightMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement knight movement logic here
    }
    
    private void addQueenMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement queen movement logic here
    }
    
    private void addKingMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement king movement logic here
    }
    
}
