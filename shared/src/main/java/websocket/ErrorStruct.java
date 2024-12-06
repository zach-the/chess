package websocket;

import websocket.messages.ServerMessage;

public record ErrorStruct(ServerMessage.ServerMessageType serverMessageType, String errorMessage) {
}
