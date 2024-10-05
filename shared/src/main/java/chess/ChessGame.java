package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() { return this.currentTurn; }

    public void setTeamTurn(TeamColor team) { this.currentTurn = team; }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) { return null; }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> movesToRemove = new ArrayList<>();
        for (ChessMove move : moves) {
            System.out.print("\n\nHere's the move: " + move.toString() + "Here's the piece: " + piece);
            ChessPosition endPosition = move.getEndPosition();
            TeamColor color = piece.getTeamColor();
//            if (board.getPiece(endPosition) != null && board.getPiece(endPosition).getTeamColor() == color)  { movesToRemove.add(move); }
            ChessPiece capturedPiece = board.getPiece(endPosition);
            board.addPiece(endPosition, piece);
            board.addPiece(startPosition, null);
            if (isInCheck(color)) { movesToRemove.add(move); System.out.println("We're in check"); }
            board.addPiece(endPosition, capturedPiece);
            board.addPiece(startPosition, piece);
        }
        for (ChessMove move : movesToRemove) {
            moves.remove(move);
        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    private boolean inBounds(ChessPosition position) {
        return (    position.getRow() <= 8 && position.getColumn() <= 8
                &&  position.getRow() >= 1 && position.getColumn() >= 1     );
    }
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        if (board.getPiece(start) == null) throw(new InvalidMoveException("Invalid Move: No piece at start position"));
        ChessPosition end = move.getEndPosition();
        ChessPiece originalPiece = board.getPiece(start);
        TeamColor color = originalPiece.getTeamColor();
        if (board.getPiece(end) != null && board.getPiece(end).getTeamColor() == color) throw(new InvalidMoveException("Invalid Move: Cannot capture own team's piece"));
        if (!originalPiece.pieceMoves(board, start).contains(move)) throw(new InvalidMoveException("Invalid Move: This piece can't move there"));
        if (color != getTeamTurn()) throw(new InvalidMoveException("Invalid Move: Cannot move out of turn"));
        if (!inBounds(end)) throw(new InvalidMoveException("Invalid Move: Move is out of bounds"));

        ChessPiece piece = (move.getPromotionPiece() == null) ? board.getPiece(move.getStartPosition()) : new ChessPiece(color, move.getPromotionPiece());
        board.addPiece(end, piece);
        board.addPiece(start, null);

        if (!isInCheck(color))  {
            if (this.currentTurn == TeamColor.BLACK) this.currentTurn = TeamColor.WHITE;
            else this.currentTurn = TeamColor.BLACK;
            return;
        }
        else {
            board.addPiece(start, originalPiece);
            board.addPiece(end, null);
            throw(new InvalidMoveException("Invalid Move: Move puts your king in check"));
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private ChessPosition findKing(TeamColor color) {
        for (int i = 1; i <= 8; i++)
            for (int j = 1; j <= 8; j++)
                if (       (board.getPiece(new ChessPosition(i,j)) != null)
                        && (board.getPiece(new ChessPosition(i,j)).getPieceType() == ChessPiece.PieceType.KING)
                        && (board.getPiece(new ChessPosition(i,j)).getTeamColor() == color) )
                    return new ChessPosition(i,j);
        return null;
    }
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition potentialPosition = new ChessPosition(i,j);
                ChessPiece potentialPiece = board.getPiece(potentialPosition);
                if ((potentialPiece != null) && (potentialPiece.getTeamColor() != teamColor)) { // piece is on opposite team
                    System.out.print("potentialPiece: " + potentialPiece + "potentialPosition: " + potentialPosition + "kingPosition: " + kingPosition);
                    Collection<ChessMove> moves = potentialPiece.pieceMoves(board, potentialPosition);
                    if (moves.contains(new ChessMove(potentialPosition, kingPosition, null))) {
                        System.out.println("case 1"); return true;
                    }
                    if (potentialPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                        if (moves.contains(new ChessMove(potentialPosition, kingPosition, ChessPiece.PieceType.QUEEN))) { System.out.println("case 2"); return true; }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;

        for (int i = 1; i <= 8; i++)
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                if (board.getPiece(position) != null && board.getPiece(position).getTeamColor() == teamColor) {
                    if (!validMoves(position).isEmpty()) return false;
                }
            }
        return true;

    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;

        for (int i = 1; i <= 8; i++)
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                if (board.getPiece(position) != null && board.getPiece(position).getTeamColor() == teamColor) {
                    if (!validMoves(position).isEmpty()) return false;
                }
            }
        return true;
    }

    public void setBoard(ChessBoard board) { this.board = board; }

    public ChessBoard getBoard() { return board; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", currentTurn=" + currentTurn +
                "}\n";
    }
}
