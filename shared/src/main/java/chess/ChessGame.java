package chess;

import java.util.*;

public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    // returns the team whose turn it currently is
    public TeamColor getTeamTurn() { return currentTurn; }

    // gives the current turn to the specified team
    public void setTeamTurn(TeamColor team) { currentTurn = team; }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    // returns a collection of all valid moves (accounting for check) of a given position
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) { return null; }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> movesToRemove = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessPosition endPosition = move.getEndPosition();
            TeamColor color = piece.getTeamColor();
            ChessPiece capturedPiece = board.getPiece(endPosition);
            board.addPiece(endPosition, piece);
            board.addPiece(startPosition, null);
            if (isInCheck(color)) { movesToRemove.add(move); }
            board.addPiece(endPosition, capturedPiece);
            board.addPiece(startPosition, piece);
        }
        for (ChessMove move : movesToRemove) { moves.remove(move); }
        return moves;
    }

    // returns true if a given move is in bounds
    private boolean inBounds(ChessPosition position) {
        return (    position.getRow() <= 8 && position.getColumn() <= 8
                &&  position.getRow() >= 1 && position.getColumn() >= 1     );
    }

    // makes the move given, has lots of error checking
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        if (board.getPiece(start) == null) { throw(new InvalidMoveException("Invalid Move: No piece at start position")); }
        ChessPosition end = move.getEndPosition();
        ChessPiece originalPiece = board.getPiece(start);
        TeamColor color = originalPiece.getTeamColor();
        if (board.getPiece(end) != null && board.getPiece(end).getTeamColor() == color) {
            throw(new InvalidMoveException("Invalid Move: Cannot capture own team's piece"));
        }
        if (!originalPiece.pieceMoves(board, start).contains(move))  { throw(new InvalidMoveException("Invalid Move: This piece can't move there")); }
        if (color != getTeamTurn()) { throw(new InvalidMoveException("Invalid Move: Cannot move out of turn")); }
        if (!inBounds(end)) { throw(new InvalidMoveException("Invalid Move: Move is out of bounds")); }

        // here's where the actual move happens
        ChessPiece piece = (move.getPromotionPiece() == null) ?
                board.getPiece(move.getStartPosition()) :
                new ChessPiece(color, move.getPromotionPiece());
        board.addPiece(end, piece);
        board.addPiece(start, null);

        if (!isInCheck(color))  {
            if (this.currentTurn == TeamColor.BLACK) { this.currentTurn = TeamColor.WHITE; }
            else { this.currentTurn = TeamColor.BLACK; }
            return;
        } else { // error check for if the move puts king in danger
            board.addPiece(start, originalPiece);
            board.addPiece(end, null);
            throw(new InvalidMoveException("Invalid Move: Move puts your king in check"));
        }
    }

    // uses a for loop to find the king for a given team
    private ChessPosition findKing(TeamColor color) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if ((board.getPiece(new ChessPosition(i, j)) != null)
                        && (board.getPiece(new ChessPosition(i, j)).getPieceType() == ChessPiece.PieceType.KING)
                        && (board.getPiece(new ChessPosition(i, j)).getTeamColor() == color)) {
                    return new ChessPosition(i, j);
                }
            }
        }
        return null;
    }

    // returns true if the team's king is in check
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition potentialPosition = new ChessPosition(i,j);
                ChessPiece potentialPiece = board.getPiece(potentialPosition);
                if ((potentialPiece != null) && (potentialPiece.getTeamColor() != teamColor)) { // piece is on opposite team
                    Collection<ChessMove> moves = potentialPiece.pieceMoves(board, potentialPosition);
                    if (moves.contains(new ChessMove(potentialPosition, kingPosition, null))) { return true; }
                    if (    potentialPiece.getPieceType() == ChessPiece.PieceType.PAWN
                            && moves.contains(new ChessMove(potentialPosition, kingPosition, ChessPiece.PieceType.QUEEN))) { return true; }
                }
            }
        }
        return false;
    }

    private boolean checkHelper(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                if (board.getPiece(position) != null && board.getPiece(position).getTeamColor() == teamColor && !validMoves(position).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    // returns true if the team's king is in checkmate
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) { return false; }
        return checkHelper(teamColor);
    }

    // returns true if the team's king is in stalemate
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) { return false; }
        return checkHelper(teamColor);
    }

    // sets the game's board equal to the board that has been passed in
    public void setBoard(ChessBoard board) { this.board = board; }

    // returns the game's current board
    public ChessBoard getBoard() { return board; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
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
