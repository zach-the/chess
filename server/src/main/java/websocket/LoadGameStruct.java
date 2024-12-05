package websocket;

import chess.ChessGame;
import websocket.messages.ServerMessage;

public record LoadGameStruct(ServerMessage.ServerMessageType serverMessageType, ChessGame game) {}
