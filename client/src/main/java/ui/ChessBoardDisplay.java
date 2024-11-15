package ui;

import chess.*;
import model.GameData;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class ChessBoardDisplay {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_CHARS = 3;
    private static final int LINE_WIDTH_IN_CHARS = 1;

    private static final String EMPTY = "   ";

    public static void displayGame(GameData gameData) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);

        ChessBoard board = gameData.game().getBoard();

        drawHeaders(out);
        drawChessBoard(out, board);
        drawHeaders(out);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void drawHeaders(PrintStream out) {
        setBlack(out);

        String[] headers = {"a", "b", "c", "d", "e", "f", "g", "h"};
        out.print("  ");
        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            drawHeader(out, headers[boardCol]);
            if (boardCol < BOARD_SIZE_IN_SQUARES - 1) {
                out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
            }
        }
        out.println();
    }

    private static void drawHeader(PrintStream out, String headerText) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_GREEN);
        out.print(" " + headerText + " ");
        setBlack(out);
    }

    private static void drawChessBoard(PrintStream out, ChessBoard board) {
        for (int boardRow = BOARD_SIZE_IN_SQUARES; boardRow > 0; --boardRow) {
            drawRowNumber(out, boardRow);
            drawRowOfSquares(out, board, boardRow);
            drawRowNumber(out, boardRow);
            out.println();
        }
    }

    private static void drawRowNumber(PrintStream out, int rowNumber) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_GREEN);
        out.print(rowNumber + " ");
        setBlack(out);
    }

    private static void drawRowOfSquares(PrintStream out, ChessBoard board, int boardRow) {
        for (int boardCol = 1; boardCol <= BOARD_SIZE_IN_SQUARES; ++boardCol) {
            ChessPosition position = new ChessPosition(boardRow, boardCol);
            ChessPiece piece = board.getPiece(position);

            if ((boardRow + boardCol) % 2 == 0) {
                setWhite(out);
            } else {
                setBlack(out);
            }

            if (piece == null) {
                out.print(EMPTY);
            } else {
                printPiece(out, piece);
            }

            if (boardCol < BOARD_SIZE_IN_SQUARES) {
                out.print(EMPTY.repeat(LINE_WIDTH_IN_CHARS));
            }
        }
    }

    private static void setWhite(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void printPiece(PrintStream out, ChessPiece piece) {
        String pieceSymbol = getPieceSymbol(piece);
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            out.print(SET_TEXT_COLOR_RED);
        } else {
            out.print(SET_TEXT_COLOR_BLUE);
        }
        out.print(" " + pieceSymbol + " ");
    }

    private static String getPieceSymbol(ChessPiece piece) {
        switch (piece.getPieceType()) {
            case KING: return "K";
            case QUEEN: return "Q";
            case BISHOP: return "B";
            case KNIGHT: return "N";
            case ROOK: return "R";
            case PAWN: return "P";
            default: return " ";
        }
    }
}
