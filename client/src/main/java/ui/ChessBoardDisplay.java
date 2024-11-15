package ui;

import chess.*;
import model.GameData;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class ChessBoardDisplay {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int LINE_WIDTH_IN_CHARS = 1;
    private static final String BORDER_COLOR = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_WHITE;
    private static final String WHITE_TILE = SET_BG_COLOR_WHITE;
    private static final String BLACK_TILE = SET_BG_COLOR_BLACK;
    private static final String WHITE_TEAM = SET_TEXT_COLOR_RED;
    private static final String BLACK_TEAM = SET_TEXT_COLOR_BLUE;

    public static void displayGame(GameData gameData, ChessGame.TeamColor perspective) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.print(ERASE_SCREEN);
        ChessBoard board = gameData.game().getBoard();

        if (perspective == ChessGame.TeamColor.BLACK) {
            drawHeaders(out);
            for (int i = 1; i <= 8; i++) {
                drawRow(out, i, board);
            }
            drawHeaders(out);
        } else {
            drawHeaders(out);
            for (int i = 8; i >=1; i--) {
                drawRow(out, i, board);
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

    private static String getPieceString(ChessBoard board, int row, int col) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        String background = ((row + col) % 2 == 0) ? WHITE_TILE : BLACK_TILE;
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

    private static void drawRow(PrintStream out, int row, ChessBoard board) {
        out.print(borderFormat(row));
        for (int i = 1; i <= 8; i++) {
            out.print(getPieceString(board, row, i));
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
