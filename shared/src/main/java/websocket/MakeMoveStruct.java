package websocket;

import chess.ChessMove;

public record MakeMoveStruct(String commandType, String authToken, int gameID, ChessMove move) { }
