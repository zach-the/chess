package chess;
import javax.lang.model.type.NullType;
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
    
    private void addPawnMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement pawn movement logic here
    }
    
    private void addRookMoves(ChessBoard board, ChessPosition position, List<ChessMove> moves) {
        // Implement rook movement logic here
        
        // I think I'm just going to check
        // up, down, left, and right, using four 
        // for-loops

        int row = position.getRow();
        int col = position.getColumn();

        // looking up
        if (row < 8) {
            for (int i = row + 1; i <= 8; i++) {
                // make a function called validate_move that does all the code from here.....
                ChessPosition end_position =  new ChessPosition(i,col);
                if (board.getPiece(end_position) != null) { // if there's a piece where we want to go
                    if (this.color != board.getPiece(end_position).getTeamColor()){
                        moves.add(new ChessMove(position, end_position, this.type));  // opposite team's pieces can be captured
                    }
                    break;
                } else {
                    moves.add(new ChessMove(position, end_position, this.type));
                }
                // .......until here
            }
        }
        // looking down
        if (row > 1) {
            for (int i = row - 1; i >= 1; i--) {
                ChessPosition move_candidate = new ChessPosition(i,col);
            }
        }
        // looking left
//        if (col > 1) {
//            for (int i = col - 1; i >= 1; i--) {}
//        }
//        // looking right
//        if (col < 1) {
//            for (int i = col + 1; i <= 8; i++) {}
//        }

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
