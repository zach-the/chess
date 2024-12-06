package ui;

import chess.*;
import model.GameData;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import static ui.EscapeSequences.*;

public class ChessBoardDisplay {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String BORDER_COLOR = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE;
    private static final String WHITE_TILE = SET_BG_COLOR_WHITE;
    private static final String BLACK_TILE = SET_BG_COLOR_BLACK;
    private static final String WHITE_TEAM = SET_TEXT_COLOR_RED;
    private static final String BLACK_TEAM = SET_TEXT_COLOR_BLUE;
    private static final String HIGHLIGHT = SET_BG_COLOR_YELLOW;

    public static void displayGame(ChessGame game, ChessGame.TeamColor perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        ChessBoard board = game.getBoard();

        if (perspective == ChessGame.TeamColor.WHITE || perspective == null){
            drawHeaders(out);
            for (int i = 8; i >=1; i--) {
                drawRow(out, i, board, null);
            }
            drawHeaders(out);
        } else {
            drawHeaders(out);
            for (int i = 1; i <= 8; i++) {
                drawRow(out, i, board, null);
            }
            drawHeaders(out);
        }
    }

    public static void highlightGame(ChessGame game, ChessGame.TeamColor perspective, ChessPosition highlightThis) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        ChessBoard board = game.getBoard();
        Collection<ChessPosition> highlightThese = new ArrayList<>();
        if (highlightThis != null) {
            Collection<ChessMove> validMoves = game.validMoves(highlightThis);
            for(ChessMove move : validMoves) {
                if (move.getStartPosition().equals(highlightThis)) highlightThese.add(move.getEndPosition());
            }
        } else {
            highlightThese = null;
        }

        if (perspective == ChessGame.TeamColor.WHITE || perspective == null){
            drawHeaders(out);
            for (int i = 8; i >=1; i--) {
                drawRow(out, i, board, highlightThese);
            }
            drawHeaders(out);
        } else {
            drawHeaders(out);
            for (int i = 1; i <= 8; i++) {
                drawRow(out, i, board, highlightThese);
            }
            drawHeaders(out);
        }
    }

    private static void drawHeaders(PrintStream out) {
        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        out.print(borderFormat(" "));
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            out.print(borderFormat(headers[boardCol]));
        }
        out.println("   " + EscapeSequences.RESET);
    }

    private static String getPieceString(ChessBoard board, int row, int col, boolean highlight) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        String background = (highlight) ? HIGHLIGHT : ( ((row + col) % 2 == 0) ? BLACK_TILE : WHITE_TILE );
        if (piece!=null) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                return WHITE_TEAM + background + " " + getPieceSymbol(piece) + " ";
            } else {
                return BLACK_TEAM + background + " " + getPieceSymbol(piece) + " ";
            }
        } else {
            return background + "   ";
        }
    }

    private static String borderFormat(Object character) {
        return BORDER_COLOR + " " + character + " ";
    }

    private static void drawRow(PrintStream out, int row, ChessBoard board, Collection<ChessPosition> highlight) {
        out.print(borderFormat(row));
        for (int i = 1; i <= 8; i++) {
            if (highlight != null && highlight.contains(new ChessPosition(row, i))) out.print(getPieceString(board, row, i, true));
            else out.print(getPieceString(board, row, i, false));
        }
        out.print(borderFormat(row) + EscapeSequences.RESET + "\n");
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
            default -> " ";
        };
    }
}
